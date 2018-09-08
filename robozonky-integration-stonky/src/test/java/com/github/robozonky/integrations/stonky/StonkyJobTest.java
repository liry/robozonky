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

package com.github.robozonky.integrations.stonky;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

import com.github.robozonky.common.secrets.SecretProvider;
import com.github.robozonky.internal.api.Defaults;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StonkyJobTest {

    @Test
    void instantiateStonky() {
        assertThat(new StonkyJob().payload()).isNotNull();
    }

    @Test
    void refreshesDaily() {
        assertThat(new StonkyJob().repeatEvery()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    void refreshesShortlyAfterMidnight() {
        final StonkyJob instance = new StonkyJob();
        final Duration startIn = instance.startIn();
        final OffsetDateTime timeToStartOn = OffsetDateTime.now().plus(startIn);
        final OffsetDateTime timeToStartBefore = timeToStartOn.plusSeconds(1000);
        final OffsetDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay(Defaults.ZONE_ID).toOffsetDateTime();
        assertThat(timeToStartOn)
                .isAfter(tomorrow)
                .isBefore(timeToStartBefore);
    }

    @SuppressWarnings("unchecked")
    @Test
    void callsStonky() {
        final Consumer<SecretProvider> c = mock(Consumer.class);
        final StonkyJob instance = new StonkyJob(c);
        final SecretProvider s = SecretProvider.inMemory("someone@somewhere.cz");
        instance.payload().accept(s);
        verify(c).accept(eq(s));
    }
}
