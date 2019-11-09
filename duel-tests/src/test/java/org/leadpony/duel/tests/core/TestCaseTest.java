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


    @SuppressWarnings("serial")
    static final Map<String, String> EXPECTED_ENDPOINT_URL = new HashMap<>() {{
       put("absolute.test.json", "http://example.org/example-api/articles/");
       put("fullpath.test.json", "http://example.org/publisher-api/articles/");
       put("relative.test.json", "http://example.org/example-api/articles/");
    }};

    @ParameterizedTest
    @ProjectSource("case/endpoint")
    public void getEndpointUrlShouldReturnCorrectUrl(TestCase test) {
        URI expected = URI.create(EXPECTED_ENDPOINT_URL.get(test.getName()));
        assertThat(test.getEndpointUrl()).isEqualTo(expected);
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
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        TestGroup group = project.createRootGroup();
        TestCase testCase = group.testCases().findFirst().get();
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
