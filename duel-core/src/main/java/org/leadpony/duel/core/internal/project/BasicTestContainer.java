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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestContainer;
import org.leadpony.duel.core.api.TestNode;

/**
 * @author leadpony
 */
class BasicTestContainer extends BasicTestNode implements TestContainer {

    BasicTestContainer(Path path, TestContext context, TestContainer parent) {
        super(path, context, parent);
    }

    /* As a Iterable<TestNode> */

    @Override
    public Iterator<TestNode> iterator() {
        return getChildrenAsList().iterator();
    }

    /* As a TestContainer */

    @Override
    public Stream<TestNode> children() {
        return getChildrenAsList().stream();
    }

    private List<TestNode> getChildrenAsList() {
        Path dir = getPath();
        List<TestNode> children = new ArrayList<>();
        children.addAll(findCases(dir));
        children.addAll(findContainers(dir));
        return children;
    }

    private List<TestCase> findCases(Path dir) {
        List<TestCase> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, BasicTestCase.FILE_PATTERN)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    children.add(createTestCase(path));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Collections.sort(children);
        return children;
    }

    private TestCase createTestCase(Path path) {
        return new BasicTestCase(path, getContext(), this);
    }

    private List<TestContainer> findContainers(Path dir) {
        return Collections.emptyList();
    }
}
