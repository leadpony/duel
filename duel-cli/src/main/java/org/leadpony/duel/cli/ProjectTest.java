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

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;

/**
 * @author leadpony
 */
public class ProjectTest {

    private static Project project;

    static void setProject(Project project) {
        ProjectTest.project = project;
    }

    @TestFactory
    @DisplayName("Projects")
    public Stream<DynamicNode> projects() {
        TestGroup root = project.createRootGroup();
        return Stream.of(createContainer(root));
    }

    private static DynamicTest createTest(TestCase testCase) {
        return DynamicTest.dynamicTest(
                testCase.getName(),
                testCase::run
                );
    }

    private static DynamicContainer createContainer(TestGroup group) {
        return DynamicContainer.dynamicContainer(
                group.getName(),
                createStream(group));
    }

    private static Stream<DynamicNode> createStream(TestGroup group) {
        Stream<DynamicNode> cases = group.testCases()
                .map(ProjectTest::createTest);
        Stream<DynamicNode> groups = group.subgroups()
                .map(ProjectTest::createContainer);
        return Stream.concat(cases, groups);
    }
}
