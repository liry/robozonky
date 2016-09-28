/*
 *
 *  Copyright 2016 Lukáš Petrovický
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.triceo.robozonky.app;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import com.github.triceo.robozonky.app.authentication.AuthenticationHandler;
import com.github.triceo.robozonky.app.version.VersionCheck;
import com.github.triceo.robozonky.app.version.VersionIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * You are required to exit this app by calling {@link #exit(ReturnCode)}.
 */
class App {

    static {
        // add process identification to log files
        MDC.put("process_id", ManagementFactory.getRuntimeMXBean().getName());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final State STATE = new State();

    /**
     * Executes a version check on the background.
     */
    private static class VersionChecker implements Supplier<Optional<Consumer<ReturnCode>>> {

        private final ExecutorService executor = Executors.newWorkStealingPool();

        @Override
        public Optional<Consumer<ReturnCode>> get() {
            final Future<VersionIdentifier> future = VersionCheck.retrieveLatestVersion(this.executor);
            return Optional.of((code) -> {
                App.newerRoboZonkyVersionExists(future);
                this.executor.shutdown();
            });
        }
    }

    /**
     * Makes sure that RoboZonky only proceeds after all other instances of RoboZonky have terminated.
     */
    private static class ExclusivityGuarantee implements Supplier<Optional<Consumer<ReturnCode>>> {

        private final Exclusivity exclusivity = Exclusivity.INSTANCE;

        @Override
        public Optional<Consumer<ReturnCode>> get() {
            try {
                this.exclusivity.ensure();
            } catch (final IOException ex) {
                App.LOGGER.error("Failed acquiring file lock, another RoboZonky process likely running.", ex);
                return Optional.empty();
            }
            return Optional.of((code) -> {
                try { // other RoboZonky instances can now start executing
                    this.exclusivity.waive();
                } catch (final IOException ex) {
                    App.LOGGER.warn("Failed releasing file lock, other RoboZonky processes may fail to launch.", ex);
                }
            });
        }
    }

    /**
     * Will terminate the application. Call this on every exit of the app to ensure proper termination. Failure to do
     * so may result in unpredictable behavior of this instance of RoboZonky or future ones.
     * @param returnCode Will be passed to {@link System#exit(int)}.
     */
    private static void exit(final ReturnCode returnCode) {
        App.STATE.shutdown(returnCode);
    }

    public static void main(final String... args) {
        // make sure other RoboZonky processes are excluded
        if (!App.STATE.register(new App.ExclusivityGuarantee())) {
            App.exit(ReturnCode.ERROR_LOCK);
        }
        // and actually start running
        App.LOGGER.info("RoboZonky v{} loading.", VersionCheck.retrieveCurrentVersion());
        App.LOGGER.debug("Running {} Java v{} on {} v{} ({}, {} CPUs, {}).", System.getProperty("java.vendor"),
                System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.version"),
                System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(), Locale.getDefault());
        // start the check for new version, making sure it is properly handled during shutdown
        App.STATE.register(new App.VersionChecker());
        // read the command line and execute the runtime
        boolean faultTolerant = false;
        try {
            final Optional<CommandLineInterface> optionalCli = CommandLineInterface.parse(args);
            if (!optionalCli.isPresent()) {
                App.exit(ReturnCode.ERROR_WRONG_PARAMETERS);
            }
            final CommandLineInterface cli = optionalCli.get();
            faultTolerant = cli.isFaultTolerant();
            if (faultTolerant) {
                App.LOGGER.info("RoboZonky is in fault-tolerant mode. Certain errors may not be reported as such.");
            }
            final Optional<AppContext> optionalCtx = cli.getCliOperatingMode().setup(cli);
            if (!optionalCtx.isPresent()) {
                App.exit(ReturnCode.ERROR_WRONG_PARAMETERS);
            }
            final AppContext ctx = optionalCtx.get();
            final Optional<AuthenticationHandler> optionalAuth = new AuthenticationHandlerProvider().apply(cli);
            if (!optionalAuth.isPresent()) {
                App.exit(ReturnCode.ERROR_SETUP);
            }
            App.LOGGER.info("===== RoboZonky at your service! =====");
            final boolean loginSucceeded = new Remote(ctx, optionalAuth.get()).call().isPresent();
            if (!loginSucceeded) {
                App.exit(ReturnCode.ERROR_SETUP);
            } else {
                App.exit(ReturnCode.OK);
            }
        } catch (final ProcessingException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof SocketException || cause instanceof UnknownHostException) {
                App.handleZonkyMaintenanceError(ex, faultTolerant);
            } else {
                App.handleUnexpectedError(ex);
            }
        } catch (final NotAllowedException ex) {
            App.handleZonkyMaintenanceError(ex, faultTolerant);
        } catch (final WebApplicationException ex) {
            App.LOGGER.error("Application encountered remote API error.", ex);
            App.exit(ReturnCode.ERROR_REMOTE);
        } catch (final RuntimeException ex) {
            App.handleUnexpectedError(ex);
        }
    }

    private static void handleUnexpectedError(final RuntimeException ex) {
        App.LOGGER.error("Unexpected error, likely RoboZonky bug." , ex);
        App.exit(ReturnCode.ERROR_UNEXPECTED);
    }

    private static void handleZonkyMaintenanceError(final RuntimeException ex, final boolean faultTolerant) {
        App.LOGGER.warn("Application not allowed to access remote API, Zonky likely down for maintenance.", ex);
        App.exit(faultTolerant ? ReturnCode.OK : ReturnCode.ERROR_DOWN);
    }

    /**
     * Check the current version against a different version. Print log message with results.
     * @param futureVersion Version to check against.
     * @return True when a more recent version was found.
     */
    static boolean newerRoboZonkyVersionExists(final Future<VersionIdentifier> futureVersion) {
        try {
            final VersionIdentifier version = futureVersion.get();
            final Optional<String> latestUnstable = version.getLatestUnstable();
            final String latestStable = version.getLatestStable();
            final boolean hasNewerStable = VersionCheck.isCurrentVersionOlderThan(latestStable);
            if (hasNewerStable) {
                App.LOGGER.info("You are using an obsolete version of RoboZonky. Please upgrade to version {}.",
                        version);
            } else {
                App.LOGGER.info("You are using the latest stable version of RoboZonky.");
            }
            final boolean hasNewerUnstable =
                    latestUnstable.isPresent() && VersionCheck.isCurrentVersionOlderThan(latestUnstable.get());
            if (hasNewerUnstable) {
                App.LOGGER.info("There is a new beta version of RoboZonky available. Give a try to version {}, " +
                        " if you feel adventurous.", version);
            }
            return hasNewerStable || hasNewerUnstable;
        } catch (final InterruptedException | ExecutionException ex) {
            App.LOGGER.trace("Version check failed.", ex);
            return false;
        }
    }

}
