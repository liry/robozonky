/*
 * Copyright 2016 Lukáš Petrovický
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

package com.github.triceo.robozonky.api.remote.entities;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistics implements BaseEntity {

    private static BigDecimal getOrDefault(final BigDecimal actualValue) {
        return Statistics.getOrDefault(actualValue, () -> BigDecimal.ZERO);
    }

    private static <T> Collection<T> getOrDefault(final Collection<T> actualValue) {
        return actualValue == null ? Collections.emptyList() : actualValue;
    }

    private static <T> T getOrDefault(final T actualValue, final Supplier<T> defaultValue) {
        return actualValue == null ? defaultValue.get() : actualValue;
    }

    private BigDecimal currentProfitability, expectedProfitability;
    private CurrentOverview currentOverview;
    private OverallOverview overallOverview;
    private OverallPortfolio overallPortfolio;
    private Collection<Instalment> cashFlow;
    private List<RiskPortfolio> riskPortfolio;

    @XmlElement
    public BigDecimal getCurrentProfitability() {
        return Statistics.getOrDefault(this.currentProfitability);
    }

    @XmlElement
    public BigDecimal getExpectedProfitability() {
        return Statistics.getOrDefault(this.expectedProfitability);
    }

    @XmlElement
    public CurrentOverview getCurrentOverview() {
        return Statistics.getOrDefault(this.currentOverview, CurrentOverview::new);
    }

    @XmlElement
    public OverallOverview getOverallOverview() {
        return Statistics.getOrDefault(this.overallOverview, OverallOverview::new);

    }

    @XmlElement
    public OverallPortfolio getOverallPortfolio() {
        return Statistics.getOrDefault(this.overallPortfolio, OverallPortfolio::new);
    }

    @XmlElementWrapper
    public Collection<Instalment> getCashFlow() {
        return Statistics.getOrDefault(this.cashFlow);
    }

    @XmlElementWrapper
    public Collection<RiskPortfolio> getRiskPortfolio() {
        return Statistics.getOrDefault(this.riskPortfolio);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Statistics{");
        sb.append("currentProfitability=").append(currentProfitability);
        sb.append(", expectedProfitability=").append(expectedProfitability);
        sb.append(", currentOverview=").append(currentOverview);
        sb.append(", overallOverview=").append(overallOverview);
        sb.append(", overallPortfolio=").append(overallPortfolio);
        sb.append(", riskPortfolio=").append(riskPortfolio);
        sb.append(", cashFlow=").append(cashFlow);
        sb.append('}');
        return sb.toString();
    }
}
