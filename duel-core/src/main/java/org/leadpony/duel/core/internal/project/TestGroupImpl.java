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
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.json.bind.Jsonb;

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;
import org.leadpony.duel.core.api.TestException;
import org.leadpony.duel.core.api.TestNode;
import org.leadpony.duel.core.internal.config.Config;
import org.leadpony.duel.core.internal.config.TestCaseConfig;

/**
 * @author leadpony
 */
class TestGroupImpl extends AbstractTestNode implements TestGroup {

    static final String FILE_NAME = "group.json";

    private final Config config;

    TestGroupImpl(Path path, Config config, TestContext context) {
        super(path, context);
        this.config = config;
    }

    /* As a Iterable<TestNode> */

    @Override
    public Iterator<TestNode> iterator() {
        return Stream.concat(testCases(), subgroups()).iterator();
    }

    /* As a TestGroup */

    @Override
    public Stream<TestCase> testCases() {
        return findTestCases(getDirectory()).stream();
    }

    @Override
    public Stream<TestGroup> subgroups() {
        return findSubgroups(getDirectory()).stream();
    }

    /* As a AbstractTestNode */

    @Override
    Config getConfig() {
        return config;
    }

    private List<TestCase> findTestCases(Path dir) {
        List<TestCase> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, TestCaseImpl.FILE_PATTERN)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    children.add(createTestCase(path));
                }
            }
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        }
        Collections.sort(children);
        return children;
    }

    private List<TestGroup> findSubgroups(Path dir) {
        List<TestGroup> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (isSubgroup(path)) {
                    children.add(createSubgroup(path));
                }
            }
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        }
        Collections.sort(children);
        return children;
    }

    private TestCase createTestCase(Path path) {
        TestCaseConfig config = loadCaseConfig(path);
        return new TestCaseImpl(path, config, getContext(), this);
    }

    private TestGroup createSubgroup(Path dir) {
        TestGroup parent = this;
        Path path = dir.resolve(FILE_NAME);
        Config config = loadGroupConfig(path);
        return new TestGroupImpl(path, config, getContext()) {
            @Override
            public Optional<TestNode> getParent() {
                return Optional.of(parent);
            }
        };
    }

    private static boolean isSubgroup(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        Path path = dir.resolve(FILE_NAME);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    private TestCaseConfig loadCaseConfig(Path path) {
        Jsonb jsonb = getContext().getJsonBinder();
        try (InputStream in = Files.newInputStream(path)) {
            return jsonb.fromJson(in, TestCaseConfig.class);
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        }
    }

    private Config loadGroupConfig(Path path) {
        return Config.EMPTY;
    }
}