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

package org.leadpony.duel.tests.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.TestCase;

/**
 * @author leadpony
 */
public class ProjectArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ProjectSource> {

    private static final String BASE_PATH = "src/main/resources/projects";

    Path startPath;

    @Override
    public void accept(ProjectSource annotation) {
        this.startPath = Paths.get(BASE_PATH, annotation.value());
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Project project = ProjectLoader.loadFrom(startPath);
        AtomicInteger index = new AtomicInteger(0);
        return project.generateTests().children()
                .filter(node -> node instanceof TestCase)
                .map(node -> (TestCase) node)
                .map(node -> Arguments.of(node, index.getAndIncrement()));
    }
}
