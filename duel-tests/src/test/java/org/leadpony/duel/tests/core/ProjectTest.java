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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.TestGroup;

/**
 * @author leadpony
 */
public class ProjectTest {

    private static final String BASE_PATH = "src/test/projects/project";

    /**
     * @author leadpony
     */
    public enum ProjectTestCase {
        EMPTY(1, 0),
        SINGLE(1, 1),
        MULTIPLE(1, 3);

        final int groups;
        final int cases;

        ProjectTestCase(int groups, int cases) {
            this.groups = groups;
            this.cases = cases;
        }

        Path getStartPath() {
            return Paths.get(BASE_PATH, name().toLowerCase());
        }

        URI getBaseUrl() {
            return URI.create("http://example.org/example-api/");
        }
    }

    @ParameterizedTest
    @EnumSource(ProjectTestCase.class)
    public void getIdShouldReturnExpectedId(ProjectTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());

        String suffix = "/" + test.name().toLowerCase() + "/project.json";
        assertThat(project.getId().toString()).endsWith(suffix);
    }

    @ParameterizedTest
    @EnumSource(ProjectTestCase.class)
    public void getPathShouldReturnExpectedPath(ProjectTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        Path path = project.getPath();
        assertThat(path.getFileName().toString()).isEqualTo("project.json");
    }

    @ParameterizedTest
    @EnumSource(ProjectTestCase.class)
    public void createRootGroupShouldGenerateExpectedTests(ProjectTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        TestGroup group = project.createRootGroup();
        assertThat(countGroups(group)).isEqualTo(test.groups);
        assertThat(countCases(group)).isEqualTo(test.cases);
    }

    private long countGroups(TestGroup group) {
        return group.subgroups()
                .reduce(1L,
                    (sum, node) -> sum + countGroups(node),
                    Long::sum);
    }

    private long countCases(TestGroup group) {
        long count = group.testCases().count();
        return group.subgroups()
                .reduce(count,
                    (sum, node) -> sum + countCases(node),
                    Long::sum);
    }
}
