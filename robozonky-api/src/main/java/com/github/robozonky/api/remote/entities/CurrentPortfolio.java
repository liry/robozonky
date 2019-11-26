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

package com.github.robozonky.api.remote.entities;

import java.util.StringJoiner;
import javax.xml.bind.annotation.XmlElement;

public class CurrentPortfolio extends BaseEntity {

    private InvestmentSummary running = InvestmentSummary.EMPTY;
    private InvestmentSummary onWay = InvestmentSummary.EMPTY;
    private InvestmentSummary dueInPast = InvestmentSummary.EMPTY;
    private InvestmentSummary terminated = InvestmentSummary.EMPTY;
    private DelinquentPortfolio due = new DelinquentPortfolio();

    CurrentPortfolio() {
        // fox JAXB
        super();
    }

    @XmlElement
    public InvestmentSummary getRunning() {
        return running;
    }

    @XmlElement
    public InvestmentSummary getOnWay() {
        return onWay;
    }

    @XmlElement
    public InvestmentSummary getDueInPast() {
        return dueInPast;
    }

    @XmlElement
    public InvestmentSummary getTerminated() {
        return terminated;
    }

    @XmlElement
    public DelinquentPortfolio getDue() {
        return due;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CurrentPortfolio.class.getSimpleName() + "[", "]")
                .add("due=" + due)
                .add("dueInPast=" + dueInPast)
                .add("onWay=" + onWay)
                .add("running=" + running)
                .add("terminated=" + terminated)
                .toString();
    }
}
