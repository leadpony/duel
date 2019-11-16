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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.json.bind.JsonbException;

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;
import org.leadpony.duel.core.api.TestNode;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.config.Config;
import org.leadpony.duel.core.internal.config.ConfigLoader;
import org.leadpony.duel.core.internal.config.TestCaseConfig;
import org.opentest4j.IncompleteExecutionException;

/**
 * @author leadpony
 */
class RegularTestGroup extends AbstractRegularTestNode implements TestGroup {

    static final String FILE_NAME = "group.json";

    private final Config config;

    RegularTestGroup(Path dir, Config config, TestContext context) {
        super(dir, context);
        this.config = config;
    }

    /* As a Iterable<TestNode> */

    @Override
    public Iterator<TestNode> iterator() {
        return Stream.concat(testCases(), subgroups()).iterator();
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return name;
        }
        return TestGroup.super.getName();
    }

    /* As a TestGroup */

    @Override
    public Stream<TestCase> testCases() {
        return findTestCases(getPath()).stream().map(this::createTestCase);
    }

    @Override
    public Stream<TestGroup> subgroups() {
        return findSubgroups(getPath()).stream().map(this::createSubgroup);
    }

    /* As a AbstractTestNode */

    @Override
    Config getConfig() {
        return config;
    }

    private List<Path> findTestCases(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, RegularTestCase.FILE_PATTERN)) {
            List<Path> children = new ArrayList<>();
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    children.add(path);
                }
            }
            Collections.sort(children);
            return children;
        } catch (IOException e) {
            throw new IncompleteExecutionException(
                    Message.DIRECTORY_READ_FAILURE.format(dir),
                    e);
        }
    }

    private List<Path> findSubgroups(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Path> children = new ArrayList<>();
            for (Path path : stream) {
                if (isSubgroup(path)) {
                    children.add(path);
                }
            }
            Collections.sort(children);
            return children;
        } catch (IOException e) {
            throw new IncompleteExecutionException(
                    Message.DIRECTORY_READ_FAILURE.format(dir),
                    e);
        }
    }

    private TestCase createTestCase(Path path) {
        try {
            ConfigLoader loader = new ConfigLoader(this);
            TestCaseConfig config = loader.load(path, TestCaseConfig.class);
            return new RegularTestCase(path, config, getContext(), this);
        } catch (JsonbException e) {
            return createIrregularTestCase(path,
                    Message.BAD_TEST_CASE.format(path), e);
        } catch (IOException e) {
            return createIrregularTestCase(path,
                    Message.FILE_READ_FAILURE.format(path), e);
        }
    }

    private TestCase createIrregularTestCase(Path path, String message, Throwable cause) {
        return new BadTestCase(path, this, message, cause);
    }

    private TestGroup createSubgroup(Path dir) {
        TestGroup parent = this;
        Path path = dir.resolve(FILE_NAME);
        Config config = loadGroupConfig(path);
        return new RegularTestGroup(path, config, getContext()) {
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

    private Config loadGroupConfig(Path path) {
        return Config.empty();
    }
}
