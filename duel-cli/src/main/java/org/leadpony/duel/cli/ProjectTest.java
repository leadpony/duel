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
import org.junit.jupiter.api.function.Executable;
import org.leadpony.duel.core.api.CaseExecution;
import org.leadpony.duel.core.api.GroupExecution;
import org.leadpony.duel.core.api.Project;
import org.opentest4j.AssertionFailedError;

/**
 * @author leadpony
 */
public class ProjectTest {

    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    private static final ThreadLocal<Project> PROJECTS = new ThreadLocal<>();

    static Project getProject() {
        return PROJECTS.get();
    }

    static void setProject(Project project) {
        PROJECTS.set(project);
    }

    @TestFactory
    @DisplayName("Projects")
    public Stream<DynamicNode> projects() {
        Project project = getProject();
        GroupExecution root = project.createExecution();
        return Stream.of(createContainer(root));
    }

    private static DynamicTest createTest(CaseExecution testCase) {
        return DynamicTest.dynamicTest(
                testCase.getName(), executable(testCase));
    }

    private static DynamicContainer createContainer(GroupExecution group) {
        return DynamicContainer.dynamicContainer(
                group.getName(),
                createStream(group));
    }

    private static Stream<DynamicNode> createStream(GroupExecution group) {
        Stream<DynamicNode> cases = group.testCases()
                .map(ProjectTest::createTest);
        Stream<DynamicNode> groups = group.subgroups()
                .map(ProjectTest::createContainer);
        return Stream.concat(cases, groups);
    }

    private static Executable executable(CaseExecution execution) {
        return () -> {
            try {
                execution.run();
            } catch (AssertionFailedError e) {
                e.setStackTrace(EMPTY_STACK_TRACE);
                throw e;
            }
        };
    }
}
