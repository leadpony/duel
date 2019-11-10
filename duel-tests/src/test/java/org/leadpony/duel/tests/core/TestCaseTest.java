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

package org.leadpony.duel.tests.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;
import org.leadpony.duel.tests.annotation.Logging;
import org.leadpony.duel.tests.annotation.ProjectSource;
import org.leadpony.duel.tests.annotation.RunFakeServer;

/**
 * @author leadpony
 */
public class TestCaseTest {

    private static final String BASE_PATH = "src/test/projects/case";

    public enum EndpointTestCase {
        ABSOLUTE("http://example.org/bookstore/books/"),
        EXPANDED("http://example.org/bookstore/books/salinger/catcherintherye"),
        FULLPATH("http://example.org/library/books/"),
        RELATIVE("http://example.org/bookstore/books/");

        final URI expected;

        EndpointTestCase(String expected) {
            this.expected = URI.create(expected);
        }

        Path getStartPath() {
            return Paths.get(BASE_PATH, "endpoint", name().toLowerCase());
        }
    }

    private static TestCase findFirstTestCase(Path path) {
        Project project = ProjectLoader.loadFrom(path);
        TestGroup group = project.createRootGroup();
        TestCase testCase = group.testCases().findFirst().get();
        return testCase;
    }

    @ParameterizedTest
    @EnumSource(EndpointTestCase.class)
    public void getEndpointUrlShouldReturnCorrectUrl(EndpointTestCase test) {
        TestCase testCase = findFirstTestCase(test.getStartPath());
        var actual = testCase.getEndpointUrl();
        assertThat(actual).isEqualTo(test.expected);
    }

    public enum PropertyTestCase {
        SIMPLE() {{
            expected.put("firstName", "John");
            expected.put("lastName", "Smith");
            expected.put("age", "42");
        }},

        EXPANDED() {{
            expected.put("firstName", "John");
            expected.put("greeting", "Hello John Smith");
        }};

        final Map<String, String> expected = new HashMap<>();

        Path getStartPath() {
            return Paths.get(BASE_PATH, "property", name().toLowerCase());
        }
    }

    @ParameterizedTest
    @EnumSource(PropertyTestCase.class)
    public void getPropertiesShouldReturnExpectedResult(PropertyTestCase test) {
        TestCase testCase = findFirstTestCase(test.getStartPath());
        var actual = testCase.getProperties();
        assertThat(actual).isEqualTo(test.expected);
    }

    /**
     * @author leadpony
     */
    @RunFakeServer
    @Logging
    public static class RunTest {

        private static final Logger LOG = Logger.getLogger(RunTest.class.getName());

        @ParameterizedTest
        @ProjectSource({"case/run/valid", "case/run/request"})
        public void runShouldSucceedIfResponseIsValid(TestCase test) {
            test.run();
        }

        @ParameterizedTest
        @ProjectSource("case/run/invalid")
        public void runShouldFailIfResponseIsInvalid(TestCase test) {
            Throwable thrown = catchThrowable(() -> {
                test.run();
            });
            assertThat(thrown)
                .isNotNull()
                .isInstanceOf(AssertionError.class);

            LOG.info(thrown.getMessage());
        }
    }
}
