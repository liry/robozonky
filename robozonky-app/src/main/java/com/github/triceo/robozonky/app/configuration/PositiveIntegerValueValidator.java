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

package com.github.triceo.robozonky.app.configuration;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class PositiveIntegerValueValidator implements IValueValidator<Integer> {

    @Override
    public void validate(final String name, final Integer value) throws ParameterException {
        if (value < 0) {
            throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
        }
    }
}
