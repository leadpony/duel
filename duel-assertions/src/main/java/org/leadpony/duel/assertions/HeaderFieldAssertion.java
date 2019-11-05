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

package org.leadpony.duel.assertions;

import java.net.http.HttpResponse;

import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class HeaderFieldAssertion extends AbstractAssertion {

    private final String name;
    private final String expected;

    HeaderFieldAssertion(String name, String expected) {
        this.name = name;
        this.expected = expected;
    }

    @Override
    public void assertOn(HttpResponse<ResponseBody> response) {
        response.headers()
            .firstValue(this.name)
            .ifPresent(this::testValue);
    }

    private void testValue(String actual) {
        if (!this.expected.equals(actual)) {
            fail(buildMessage(actual), this.expected, actual);
        }
    }

    private String buildMessage(String actual) {
        return Message.HEADER_FIELD_NOT_EQUAL
                .format(this.name, this.expected, actual);
    }
}
