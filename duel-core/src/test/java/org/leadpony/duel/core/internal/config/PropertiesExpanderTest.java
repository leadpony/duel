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

package org.leadpony.duel.core.internal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.PropertyFinder;

/**
 * @author leadpony
 */
public class PropertiesExpanderTest {

    /**
     * @author leadpony
     */
    enum TestCase implements PropertyFinder {
        EMPTY(),

        LITERAL() {{
            original.put("user", "john");
            original.put("pass", "secret");

            expected.put("user", "john");
            expected.put("pass", "secret");
        }},

        REPLACE() {{
            original.put("greeting", "Hello ${name}");
            map.put("name", "John");

            expected.put("greeting", "Hello John");
        }},

        DEPENDENT() {{
            original.put("greeting", "Hello ${name}");
            original.put("name", "John Smith");

            expected.put("greeting", "Hello John Smith");
            expected.put("name", "John Smith");
        }},

        MULTIPLE_DEPENDENT() {{
            original.put("greeting", "Hello ${name}");
            original.put("name", "${firstName} ${lastName}");
            original.put("firstName", "John");
            original.put("lastName", "Smith");

            expected.put("greeting", "Hello John Smith");
            expected.put("name", "John Smith");
            expected.put("firstName", "John");
            expected.put("lastName", "Smith");
        }};

        final Map<String, String> original = new HashMap<>();
        final Map<String, String> expected = new HashMap<>();
        final Map<String, String> map = new HashMap<>();

        @Override
        public Optional<String> findProperty(String name) {
            return Optional.ofNullable(map.get(name));
        }
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    public void expandShouldExpandPropertiesAsExpected(TestCase test) {
        var expander = new PropertiesExpander(test.original, test);
        var actual = expander.expandAll();
        assertThat(actual).isEqualTo(test.expected);
    }

    /**
     * @author leadpony
     */
    enum IllegalTestCase {
        SELF() {{
            original.put("foo", "${foo}");
        }},

        CYCLIC() {{
            original.put("foo", "${bar}");
            original.put("bar", "${baz}");
            original.put("baz", "${foo}");
        }},

        CYCLIC_PARTIAL() {{
            original.put("a", "${b}, ${c}");
            original.put("b", "123");
            original.put("c", "${d}");
            original.put("d", "${c}");
        }};

        final Map<String, String> original = new HashMap<>();
    }

    @ParameterizedTest
    @EnumSource(IllegalTestCase.class)
    public void expandShouldThrowExceptionIfLoopingExists(IllegalTestCase test) {
        var expander = new PropertiesExpander(test.original, PropertyFinder.empty());
        Throwable thrown = catchThrowable(() -> {
           expander.expandAll();
        });
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }
}
