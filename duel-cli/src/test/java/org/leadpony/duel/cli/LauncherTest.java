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

package org.leadpony.duel.cli;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.fake.server.FakeServer;

/**
 * @author leadpony
 */
public class LauncherTest {

    private static final String BASE_PATH = "src/test/projects";

    public static class RunTest {

        private static FakeServer server;

        @BeforeAll
        public static void setUpOnce() throws Exception {
            server = new FakeServer(8080);
            server.start();
        }

        @AfterAll
        public static void tearDownOnce() throws Exception {
            server.stop();
            server = null;
        }

        public enum ProjectTestCase {
            EMPTY(0),
            HEADER_VALUE_MATCH(0),
            HEADER_VALUE_MISMATCH(1),
            JSON_BODY_MATCH(0),
            JSON_BODY_MISMATCH(1),
            JSON_BODY_TYPE_MISMATCH(1),
            REQUIRED_HEADER_EXISTS(0),
            REQUIRED_HEADER_MISSING(1),
            STATUS_CODE_EXPECTED(0),
            STATUS_CODE_UNEXPECTED(1);

            final int exitCode;

            ProjectTestCase(int exitCode) {
                this.exitCode = exitCode;
            }

            Path getProjectPath() {
                return Paths.get(BASE_PATH, name().toLowerCase());
            }
        }

        @ParameterizedTest
        @EnumSource(ProjectTestCase.class)
        public void runShouldRunTestsAsExpected(ProjectTestCase test) {
            Launcher launcher = new Launcher(test.getProjectPath());
            int exitCode = launcher.launch();
            assertThat(exitCode).isEqualTo(test.exitCode);
        }
    }
}
