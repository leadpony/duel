/*
 * Copyright 2019 the original author or authors.
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

package org.leadpony.duel.core.internal.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.leadpony.duel.core.spi.ResponseValidator;

/**
 * @author leadpony
 */
class HeaderFieldValidator implements ResponseValidator {

    private final String name;
    private final String expected;

    HeaderFieldValidator(String name, String expected) {
        this.name = name;
        this.expected = expected;
    }

    @Override
    public void validateResponse(HttpResponse<?> response) {
        response.headers()
            .firstValue(this.name)
            .ifPresent(actual -> {
                assertThat(actual).isEqualTo(expected);
            });
    }
}
