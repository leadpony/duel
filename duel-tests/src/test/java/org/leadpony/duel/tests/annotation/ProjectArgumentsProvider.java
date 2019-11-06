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

package org.leadpony.duel.tests.annotation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.TestGroup;

/**
 * @author leadpony
 */
public class ProjectArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ProjectSource> {

    private static final String BASE_PATH = "src/test/projects";

    private String[] values;

    @Override
    public void accept(ProjectSource annotation) {
        this.values = annotation.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(values)
            .map(value -> Paths.get(BASE_PATH, value))
            .flatMap(this::testsFromProject);
    }

    private Stream<Arguments> testsFromProject(Path path) {
        Project project = ProjectLoader.loadFrom(path);
        return testsFromGroup(project.createRootGroup());
    }

    private Stream<Arguments> testsFromGroup(TestGroup group) {
        Stream<Arguments> children = group.testCases().map(Arguments::of);
        Stream<Arguments> descendants = group.subgroups()
                .flatMap(this::testsFromGroup);
        return Stream.concat(children, descendants);
    }
}