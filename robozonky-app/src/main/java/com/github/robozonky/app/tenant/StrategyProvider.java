/*
 * Copyright 2019 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.app.tenant;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.robozonky.api.strategies.InvestmentStrategy;
import com.github.robozonky.api.strategies.PurchaseStrategy;
import com.github.robozonky.api.strategies.ReservationStrategy;
import com.github.robozonky.api.strategies.SellStrategy;
import com.github.robozonky.internal.async.ReloadListener;
import com.github.robozonky.internal.async.Reloadable;
import com.github.robozonky.internal.extensions.StrategyLoader;
import com.github.robozonky.internal.util.StringUtil;
import com.github.robozonky.internal.util.UrlUtil;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class StrategyProvider implements ReloadListener<String> {

    private static final Logger LOGGER = LogManager.getLogger(StrategyProvider.class);

    private final AtomicReference<String> lastLoadedStrategy = new AtomicReference<>();
    private final AtomicReference<InvestmentStrategy> toInvest = new AtomicReference<>();
    private final AtomicReference<SellStrategy> toSell = new AtomicReference<>();
    private final AtomicReference<PurchaseStrategy> toPurchase = new AtomicReference<>();
    private final AtomicReference<ReservationStrategy> forReservations = new AtomicReference<>();
    private final Reloadable<String> reloadableStrategy;

    StrategyProvider(final String strategyLocation) {
        this.reloadableStrategy = Reloadable.with(() ->
                Try.withResources(() -> UrlUtil.open(UrlUtil.toURL(strategyLocation)))
                        .of(StringUtil::toString)
                        .getOrElseThrow(t -> {
                            LOGGER.error("Failed reading strategy source.", t);
                            throw new IllegalStateException("Failed reading strategy source.", t);
                        }))
                .addListener(this)
                .reloadAfter(Duration.ofHours(1))
                .async()
                .build();
    }

    StrategyProvider() {
        this.reloadableStrategy = null;
    }

    public static StrategyProvider createFor(final String strategyLocation) {
        return new StrategyProvider(strategyLocation);
    }

    public static StrategyProvider empty() { // for testing purposes only
        return new StrategyProvider();
    }

    private static <T> T set(final AtomicReference<T> ref, final Supplier<Optional<T>> provider, final String desc) {
        final T value = ref.updateAndGet(old -> provider.get().orElse(null));
        if (Objects.isNull(value)) {
            LOGGER.info("{} strategy inactive or missing, disabling all such operations.", desc);
        } else {
            LOGGER.debug("{} strategy correctly loaded.", desc);
        }
        return value;
    }

    @Override
    public void newValue(final String newValue) {
        var oldStrategy = lastLoadedStrategy.getAndSet(newValue);
        if (Objects.equals(oldStrategy, newValue)) {
            LOGGER.debug("No change in strategy source code detected.");
            return;
        }
        LOGGER.trace("Loading strategies.");
        var investStrategy = set(toInvest, () -> StrategyLoader.toInvest(newValue), "Investing");
        var purchaseStrategy = set(toPurchase, () -> StrategyLoader.toPurchase(newValue), "Purchasing");
        var sellingStrategy = set(toSell, () -> StrategyLoader.toSell(newValue), "Selling");
        var reserveStrategy = set(forReservations, () -> StrategyLoader.forReservations(newValue), "Reservations");
        var allStrategiesMissing = Stream.of(investStrategy, purchaseStrategy, sellingStrategy, reserveStrategy)
                .allMatch(Objects::isNull);
        if (allStrategiesMissing) {
            LOGGER.warn("No strategies are available, all operations are disabled. Check log for parser errors.");
        }
        LOGGER.trace("Finished.");
    }

    @Override
    public void valueUnset() {
        lastLoadedStrategy.set(null);
        Stream.of(toInvest, toSell, toPurchase, forReservations).forEach(ref -> ref.set(null));
        LOGGER.warn("There are no strategies, all operations are disabled.");
    }

    private void possiblyReloadStrategy() {
        if (reloadableStrategy != null) {
            reloadableStrategy.get();
        }
    }

    public Optional<InvestmentStrategy> getToInvest() {
        possiblyReloadStrategy();
        return Optional.ofNullable(toInvest.get());
    }

    public Optional<SellStrategy> getToSell() {
        possiblyReloadStrategy();
        return Optional.ofNullable(toSell.get());
    }

    public Optional<PurchaseStrategy> getToPurchase() {
        possiblyReloadStrategy();
        return Optional.ofNullable(toPurchase.get());
    }

    public Optional<ReservationStrategy> getForReservations() {
        possiblyReloadStrategy();
        return Optional.ofNullable(forReservations.get());
    }
}
