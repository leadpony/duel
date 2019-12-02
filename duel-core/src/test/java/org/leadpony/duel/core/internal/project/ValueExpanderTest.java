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

package org.leadpony.duel.core.internal.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * @author leadpony
 */
public class ValueExpanderTest {

    enum TestCase implements Function<String, String> {
        NO_PLACEHOLDER("Hello World", "Hello World"),

        SINGLE("Hello ${name}", "Hello John") {{
            map.put("name", "John");
        }},

        MULTIPLE("Hello ${firstName} ${lastName}", "Hello John Smith") {{
            map.put("firstName", "John");
            map.put("lastName", "Smith");
        }},

        MULTIPLE_SAME("Hello ${name}, bye ${name}", "Hello John, bye John") {{
            map.put("name", "John");
        }},

        NOT_FOUND("Hello ${name}", "Hello ${name}"),

        PARTIAL("Hello ${firstName} ${lastName}", "Hello ${firstName} Smith") {{
            map.put("lastName", "Smith");
        }};

        final String original;
        final String expected;
        final Map<String, String> map = new HashMap<>();

        TestCase(String original, String expected) {
            this.original = original;
            this.expected = expected;
        }

        @Override
        public String apply(String name) {
            return map.get(name);
        }
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    public void expandShouldExpandSingleValueAsExpected(TestCase test) {
        var expander = new ValueExpander(test);
        String actual = expander.expand(test.original);
        assertThat(actual).isEqualTo(test.expected);
    }
}
