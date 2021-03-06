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

package com.github.robozonky.app.events.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

import com.github.robozonky.api.notifications.ReservationCheckStartedEvent;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.api.strategies.ReservationDescriptor;

final class ReservationCheckStartedEventImpl extends AbstractEventImpl implements ReservationCheckStartedEvent {

    private final Collection<ReservationDescriptor> reservationDescriptors;
    private final PortfolioOverview portfolioOverview;

    public ReservationCheckStartedEventImpl(final Collection<ReservationDescriptor> reservationDescriptors,
                                            final PortfolioOverview portfolio) {
        this.reservationDescriptors = Collections.unmodifiableCollection(reservationDescriptors);
        this.portfolioOverview = portfolio;
    }

    @Override
    public PortfolioOverview getPortfolioOverview() {
        return portfolioOverview;
    }

    @Override
    public Collection<ReservationDescriptor> getReservationDescriptors() {
        return this.reservationDescriptors;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReservationCheckStartedEventImpl.class.getSimpleName() + "[", "]")
                .add("portfolioOverview=" + portfolioOverview)
                .toString();
    }
}
