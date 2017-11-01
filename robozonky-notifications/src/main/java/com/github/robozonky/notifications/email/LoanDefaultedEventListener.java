/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.notifications.email;

import com.github.robozonky.api.notifications.LoanDefaultedEvent;

class LoanDefaultedEventListener extends AbstractLoanTerminatedEmailingListener<LoanDefaultedEvent> {

    public LoanDefaultedEventListener(final ListenerSpecificNotificationProperties properties) {
        super(LoanDefaultedEvent::getLoan, LoanDefaultedEvent::getDelinquentSince, properties);
    }

    @Override
    String getSubject(final LoanDefaultedEvent event) {
        return "Půjčka č. " + event.getLoan().getId() + " byla zesplatněna";
    }

    @Override
    String getTemplateFileName() {
        return "loan-defaulted.ftl";
    }
}