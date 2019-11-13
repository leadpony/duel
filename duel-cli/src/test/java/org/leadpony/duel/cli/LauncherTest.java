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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.duel.fake.server.FakeServer;

/**
 * @author leadpony
 */
public class LauncherTest {

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

        @ParameterizedTest
        @ProjectSource("src/test/projects")
        public void launchShouldReturnExpectedCode(Path dir) throws IOException {
            Path projectDir = Path.of("src/test/projects").resolve(dir);
            Launcher launcher = new Launcher(projectDir);
            int actual = launcher.launch();
            int expected = getExpectetExitCode(projectDir);
            assertThat(actual).isEqualTo(expected);
        }

        private static int getExpectetExitCode(Path dir) throws IOException {
            Properties properties = new Properties();
            Path path = dir.resolve("result.properties");
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            }
            return Integer.valueOf(properties.getProperty("exit"));
        }

        @Test
        public void launchShoudReturn2IfNoProjectFound() {
            Path dir = Path.of("src/test/projects/nonexistent");
            Launcher launcher = new Launcher(dir);
            int actual = launcher.launch();
            assertThat(actual).isEqualTo(-1);
        }
    }
}
