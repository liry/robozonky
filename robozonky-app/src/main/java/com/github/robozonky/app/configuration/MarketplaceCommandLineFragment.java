/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.app.configuration;

import java.time.Duration;

import com.beust.jcommander.Parameter;

class MarketplaceCommandLineFragment extends AbstractCommandLineFragment {

    @Parameter(names = {"-w", "--wait-primary", "--wait"},
            description = "Number of seconds between consecutive checks of primary marketplace.")
    private int primaryMarketplaceCheckDelay = 5;

    @Parameter(names = {"-ws", "--wait-secondary"},
            description = "Number of seconds between consecutive checks of secondary marketplace.")
    private int secondaryMarketplaceCheckDelay = primaryMarketplaceCheckDelay;

    public Duration getPrimaryMarketplaceCheckDelay() {
        return Duration.ofSeconds(primaryMarketplaceCheckDelay);
    }

    public Duration getSecondaryMarketplaceCheckDelay() {
        return Duration.ofSeconds(secondaryMarketplaceCheckDelay);
    }

}
