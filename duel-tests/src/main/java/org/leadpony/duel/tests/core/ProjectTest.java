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
import org.leadpony.duel.core.api.TestContainer;

/**
 * @author leadpony
 */
public class ProjectTest {

    private static final String BASE_PATH = "src/main/resources/projects/project";

    /**
     * @author leadpony
     */
    public enum ProjectTestCase {
        EMPTY(1, 0),
        SINGLE(1, 1),
        MULTIPLE(1, 3);

        final int containers;
        final int cases;

        ProjectTestCase(int containers, int cases) {
            this.containers = containers;
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
    public void getBaseUrlShouldReturnExpectedUrl(ProjectTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());

        assertThat(project.getBaseUrl()).isEqualTo(test.getBaseUrl());
    }

    @ParameterizedTest
    @EnumSource(ProjectTestCase.class)
    public void generateTestsShouldGenerateExpectedTests(ProjectTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        TestContainer root = project.generateTests();

        assertThat((Object) root).isNotNull();
        assertThat(countContainers(root)).isEqualTo(test.containers);
        assertThat(countCases(root)).isEqualTo(test.cases);
    }

    private int countContainers(TestContainer container) {
        return container.children()
                .filter(node -> node instanceof TestContainer)
                .map(node -> (TestContainer) node)
                .reduce(1,
                    (sum, node) -> sum + countContainers(node),
                    Integer::sum);
    }

    private int countCases(TestContainer container) {
        return container.children()
                .reduce(0,
                    (sum, node) -> {
                        if (node instanceof TestContainer) {
                            return sum + countCases((TestContainer) node);
                        } else {
                            return sum + 1;
                        }
                    },
                    Integer::sum);
    }
}
