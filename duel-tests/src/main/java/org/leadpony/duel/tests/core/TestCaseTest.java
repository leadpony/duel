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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.tests.helper.ProjectSource;
import org.leadpony.duel.tests.helper.TestWithServer;

/**
 * @author leadpony
 */
public class TestCaseTest {

    private static final Logger LOG = Logger.getLogger(TestCaseTest.class.getName());

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

    /**
     * @author leadpony
     */
    @TestWithServer
    public static class RunTest {

        @ParameterizedTest
        @ProjectSource("case/run/valid")
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

            LOG.severe(thrown.getMessage());
        }
    }
}
