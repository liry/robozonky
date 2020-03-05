/*
 * Copyright 2020 The RoboZonky Project
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

package com.github.robozonky.strategy.natural;

import java.math.BigDecimal;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Reservation;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.remote.enums.Region;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.api.strategies.ReservationDescriptor;
import com.github.robozonky.test.mock.MockReservationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

class ReservationWrapperTest {

    private static final PortfolioOverview FOLIO = mock(PortfolioOverview.class);

    @Test
    void fromReservation() {
        final Reservation reservation = new MockReservationBuilder()
                .setAnnuity(BigDecimal.ONE)
                .setRating(Rating.D)
                .setAmount(100_000)
                .setRegion(Region.JIHOCESKY)
                .setRevenueRate(Ratio.ZERO)
                .setInterestRate(Ratio.ONE)
                .build();
        final Wrapper<ReservationDescriptor> w = Wrapper.wrap(new ReservationDescriptor(reservation, () -> null), FOLIO);
        assertSoftly(softly -> {
            softly.assertThat(w.getId()).isNotEqualTo(0);
            softly.assertThat(w.getStory()).isEqualTo(reservation.getStory());
            softly.assertThat(w.getRegion()).isEqualTo(reservation.getRegion());
            softly.assertThat(w.getRating()).isEqualTo(reservation.getRating());
            softly.assertThat(w.getOriginalAmount()).isEqualTo(reservation.getAmount().getValue().intValue());
            softly.assertThat(w.getInterestRate()).isEqualTo(Ratio.ONE);
            softly.assertThat(w.getRevenueRate()).isEqualTo(Ratio.ZERO);
            softly.assertThat(w.getOriginalAnnuity()).isEqualTo(reservation.getAnnuity().getValue().intValue());
            softly.assertThat(w.getRemainingTermInMonths()).isEqualTo(reservation.getTermInMonths());
            softly.assertThatThrownBy(w::getRemainingPrincipal).isInstanceOf(UnsupportedOperationException.class);
            softly.assertThat(w.getSellFee()).isEmpty();
            softly.assertThat(w.toString()).isNotNull();
        });
    }

    @Test
    void fromReservationWithoutRevenueRate() {
        final Reservation reservation = new MockReservationBuilder()
                .setRating(Rating.B)
                .build();
        final Wrapper<ReservationDescriptor> w = Wrapper.wrap(new ReservationDescriptor(reservation, () -> null),
                FOLIO);
        when(FOLIO.getInvested()).thenReturn(Money.ZERO);
        assertThat(w.getRevenueRate()).isEqualTo(Ratio.fromPercentage("9.99"));
    }

}