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

package com.github.robozonky.internal.remote;

import com.github.robozonky.api.remote.entities.ZonkyApiToken;

import javax.ws.rs.client.ClientRequestContext;
import java.util.function.Supplier;

class AuthenticatedFilter extends RoboZonkyFilter {

    private static final char[] EMPTY_TOKEN = new char[0];
    private final Supplier<ZonkyApiToken> token;

    public AuthenticatedFilter(final Supplier<ZonkyApiToken> token) {
        this.token = token; // null token = no token, testing purposes
    }

    @Override
    public void filter(final ClientRequestContext clientRequestContext) {
        final char[] t = token == null ? AuthenticatedFilter.EMPTY_TOKEN  : token.get().getAccessToken();
        this.setRequestHeader("Authorization", "Bearer " + String.valueOf(t));
        super.filter(clientRequestContext);
    }
}
