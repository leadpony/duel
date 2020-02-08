/*
 * Copyright 2019-2020 the original author or authors.
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

package org.leadpony.duel.core.internal.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.api.TestLoadingException;
import org.leadpony.duel.core.api.TestLoader;

/**
 * @author leadpony
 */
public class TestLoaderTest {

    private static final String BASE_PATH = "src/test/projects/project";

    @ParameterizedTest
    @ValueSource(strings = {
            "empty",
            "basic"
    })
    public void loadProjectShouldLoadValidProject(String dir) {
        Path path = Paths.get(BASE_PATH, dir);
        GroupNode root = TestLoader.loadFrom(path);

        assertThat((Object) root).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nonexistent"
    })
    public void loadProjectShouldThrowExceptionIfInvalid(String dir) {
        Path path = Paths.get(BASE_PATH, dir);
        Throwable thrown = catchThrowable(() -> {
            TestLoader.loadFrom(path);
        });
        assertThat(thrown).isInstanceOf(TestLoadingException.class);
    }
}
