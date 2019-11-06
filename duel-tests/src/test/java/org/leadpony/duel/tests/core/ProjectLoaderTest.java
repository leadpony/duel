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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.TestException;
import org.leadpony.duel.core.api.ProjectLoader;

/**
 * @author leadpony
 */
public class ProjectLoaderTest {

    private static final String BASE_PATH = "src/test/projects/loader";

    @ParameterizedTest
    @ValueSource(strings = {
            "empty",
            "simple"
    })
    public void loadProjectShouldLoadValidProject(String dir) {
        Path path = Paths.get(BASE_PATH, "valid", dir);
        Project project = ProjectLoader.loadFrom(path);

        assertThat((Object) project).isNotNull();
        assertThat(project.getVersion()).isEqualTo(1);
        assertThat(project.getId().toString()).endsWith("/" + dir + "/project.json");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nonexistent"
    })
    public void loadProjectShouldThrowExceptionIfInvalid(String dir) {
        Path path = Paths.get(BASE_PATH, "invalid", dir);
        Throwable thrown = catchThrowable(() -> {
            ProjectLoader.loadFrom(path);
        });
        assertThat(thrown).isInstanceOf(TestException.class);
    }
}
