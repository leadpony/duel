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

package org.leadpony.duel.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * @author leadpony
 */
class ProjectArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ProjectSource> {

    private static final String ROOT_GROUP_FILENAME = "root.json";
    private String[] dirs;

    @Override
    public void accept(ProjectSource source) {
        this.dirs = source.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(dirs).map(Path::of).flatMap(this::findProjects);
    }

    private Stream<Arguments> findProjects(Path baseDir) {
        List<Path> found = new ArrayList<>();
        try {
            Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Path path = dir.resolve(ROOT_GROUP_FILENAME);
                    if (Files.exists(path) && Files.isRegularFile(path)) {
                        found.add(dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return found.stream().map(path -> Arguments.of(baseDir.relativize(path).toString(), path));
    }
}
