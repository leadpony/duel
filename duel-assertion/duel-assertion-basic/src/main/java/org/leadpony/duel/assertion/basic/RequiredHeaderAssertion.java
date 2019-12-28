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

package org.leadpony.duel.assertion.basic;

import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class RequiredHeaderAssertion extends AbstractAssertion {

    private final Set<String> expected;

    RequiredHeaderAssertion(Stream<String> expected) {
        this.expected = expected
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public void assertOn(HttpResponse<ResponseBody> response) {
        Set<String> actual = response.headers().map().keySet();
        if (!actual.containsAll(this.expected)) {
            fail(buildMessage(actual), this.expected, actual);
        }
    }

    private String buildMessage(Set<String> actual) {
        Set<String> missing = new HashSet<>(this.expected);
        missing.removeAll(actual);
        return Message.thatHeaderFieldsAreMissing(missing);
    }
}
