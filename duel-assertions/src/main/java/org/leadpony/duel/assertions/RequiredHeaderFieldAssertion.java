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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author leadpony
 */
class RequiredHeaderFieldAssertion extends AbstractAssertion {

    private final Set<String> expected;

    RequiredHeaderFieldAssertion(Stream<String> expected) {
        this.expected = expected
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public void doAssert(HttpResponse<byte[]> response) {
        Set<String> actual = response.headers().map().keySet();
        if (!actual.containsAll(this.expected)) {
            fail(buildMessage(actual), this.expected, actual);
        }
    }

    private String buildMessage(Set<String> actual) {
        return Message.HEADER_FIELD_MISSING.format(this.expected, actual);
    }
}
