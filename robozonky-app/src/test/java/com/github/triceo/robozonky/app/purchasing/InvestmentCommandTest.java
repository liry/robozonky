/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.app.purchasing;

import com.github.triceo.robozonky.api.strategies.PurchaseStrategy;
import com.github.triceo.robozonky.app.investing.AbstractInvestingTest;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class InvestmentCommandTest extends AbstractInvestingTest {

    @Test
    public void empty() {
        final Session sess = Mockito.mock(Session.class);
        final PurchaseStrategy s = Mockito.mock(PurchaseStrategy.class);
        final InvestmentCommand c = new InvestmentCommand(s);
        c.accept(sess); // SUT
        Mockito.verify(sess, Mockito.never()).purchase(ArgumentMatchers.any());
    }
}
