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
class SimpleStatusAssertion extends AbstractAssertion {

    private final int expected;

    SimpleStatusAssertion(int expected) {
        this.expected = expected;
    }

    @Override
    public void assertOn(HttpResponse<ResponseBody> response) {
        int actual = response.statusCode();
        if (this.expected != actual) {
            fail(buildMessage(actual), this.expected, actual);
        }
    }

    private String buildMessage(int actual) {
        return Message.STATUS_CODE_NOT_EQUAL.format(this.expected, actual);
    }
}
