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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.GroupNode;

/**
 * @author leadpony
 *
 */
public class ProjectTest {

    private static final String BASE_PATH = "src/test/projects/project";

    /**
     * @author leadpony
     */
    public enum RootGroupTestCase {
        NO_TESTS(1, 0),
        SINGLE_TEST(1, 1),
        MULTIPLE_TESTS(1, 3);

        final int groups;
        final int cases;

        RootGroupTestCase(int groups, int cases) {
            this.groups = groups;
            this.cases = cases;
        }

        Path getStartPath() {
            return Paths.get(BASE_PATH, name().toLowerCase());
        }
    }

    @ParameterizedTest
    @EnumSource(RootGroupTestCase.class)
    public void createRootGroupShouldGenerateExpectedTests(RootGroupTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        GroupNode group = project.getRootGroup();

        assertThat((Object) group).isNotNull();
        assertThat(countGroups(group)).isEqualTo(test.groups);
        assertThat(countCases(group)).isEqualTo(test.cases);
    }

    private static long countGroups(GroupNode group) {
        return group.getSubgroups().stream()
                .reduce(1L,
                    (sum, node) -> sum + countGroups(node),
                    Long::sum);
    }

    private static long countCases(GroupNode group) {
        long count = group.getTestCases().size();
        return group.getSubgroups().stream()
                .reduce(count,
                    (sum, node) -> sum + countCases(node),
                    Long::sum);
    }

    /**
     * @author leadpony
     */
    public enum PropertiesTestCase {
        PROPERTIES() {{
            expected.put("firstName", "John");
            expected.put("lastName", "Smith");
        }},

        PROPERTIES_INTERPOLATE_FULL() {{
            expected.put("firstName", "John");
            expected.put("lastName", "Smith");
            expected.put("fullName", "John Smith");
        }},

        PROPERTIES_INTERPOLATE_PARTIAL() {{
            expected.put("lastName", "Smith");
            expected.put("fullName", "${firstName} Smith");
        }};

        final Map<String, String> expected = new HashMap<>();

        Path getStartPath() {
            return Paths.get(BASE_PATH, name().toLowerCase());
        }
    }

    @ParameterizedTest
    @EnumSource(PropertiesTestCase.class)
    public void getPropertiesShouldReturnPropertiesAsExpected(PropertiesTestCase test) {
        Project project = ProjectLoader.loadFrom(test.getStartPath());
        var actual = project.getProperties();

        assertThat(actual).isEqualTo(test.expected);
    }
}
