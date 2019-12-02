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

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.internal.Logging;

/**
 * @author leadpony
 */
@Logging
public class TestCaseTest {

    private static final Logger LOG = Logger.getLogger(TestCaseTest.class.getName());

    private static final Path BASE_PATH = Path.of("src/test/projects/case");

    private static CaseNode findFirstTestCase(Path path) {
        Project project = ProjectLoader.loadFrom(path);
        CaseNode testCase = project.getTestCases().iterator().next();
        return testCase;
    }

    private static final Path PROPERTIES_BASE_PATH = BASE_PATH.resolve("properties");

    public enum PropertiesTestCase {
        SIMPLE() {{
            expected.put("firstName", "John");
            expected.put("lastName", "Smith");
            expected.put("age", "42");
        }},

        EXPAND_FULL() {{
            expected.put("firstName", "John");
            expected.put("greeting", "Hello John Smith");
            expected.put("lastName", "Smith");
        }},

        EXPAND_PARTIAL() {{
            expected.put("lastName", "Smith");
            expected.put("greeting", "Hello ${firstName} Smith");
        }};

        final Map<String, String> expected = new HashMap<>();

        Path getStartPath() {
            return PROPERTIES_BASE_PATH.resolve(name().toLowerCase());
        }
    }

    @ParameterizedTest
    @EnumSource(PropertiesTestCase.class)
    public void getPropertiesShouldReturnExpectedResult(PropertiesTestCase test) {
        CaseNode testCase = findFirstTestCase(test.getStartPath());
        var actual = testCase.getProperties();
        assertThat(actual).isEqualTo(test.expected);
    }

    public enum EndpointTestCase {
        ENDPOINT_BASE_PATH("http://example.org/bookstore/books"),
        ENDPOINT_HTTPS("https://example.org/bookstore/books"),
        ENDPOINT_NO_BASE_PATH("http://example.org/books"),
        ENDPOINT_WITH_PATH_VARIABLE("http://example.org/bookstore/books/12345"),
        ENDPOINT_WITH_PORT("http://example.org:8080/books"),
        ENDPOINT_WITH_QUERY("http://example.org/bookstore/books?lang=en"),
        ENDPOINT_WITH_QUERY_MULTIPLE("http://example.org/bookstore/books?lang=en&year=1970");

        final URI expected;

        EndpointTestCase(String expected) {
            this.expected = URI.create(expected);
        }

        Path getStartPath() {
            return BASE_PATH.resolve(name().toLowerCase());
        }
    }

    @ParameterizedTest
    @EnumSource(EndpointTestCase.class)
    public void getEndpointUrlShouldReturnCorrectUrl(EndpointTestCase test) {
        CaseNode testCase = findFirstTestCase(test.getStartPath());
        var actual = testCase.getEndpointUrl();
        LOG.info(actual.toASCIIString());
        assertThat(actual).isEqualTo(test.expected);
    }
}
