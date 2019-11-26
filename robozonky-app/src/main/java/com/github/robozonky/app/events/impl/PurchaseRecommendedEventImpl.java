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

import com.github.robozonky.api.notifications.PurchaseRecommendedEvent;
import com.github.robozonky.api.remote.entities.Participation;
import com.github.robozonky.api.strategies.ParticipationDescriptor;
import com.github.robozonky.api.strategies.RecommendedParticipation;

final class PurchaseRecommendedEventImpl extends AbstractRecommendationBasedEventImpl<RecommendedParticipation,
        ParticipationDescriptor, Participation> implements PurchaseRecommendedEvent {

    public PurchaseRecommendedEventImpl(final RecommendedParticipation recommendation) {
        super(recommendation);
    }

    @Override
    public Participation getParticipation() {
        return super.getRecommending();
    }
}
