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

package org.leadpony.duel.core.internal.node;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.api.Problem;
import org.leadpony.duel.core.api.TestLoadingException;
import org.leadpony.duel.core.api.TestLoader;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.JsonCombiner;

/**
 * An implementation of {@link TestLoader}.
 *
 * @author leadpony
 */
public class TestLoaderImpl implements TestLoader {

    private static final String DEFAULT_JSON_NAME = "default.json";

    private final Path startPath;
    private final JsonProvider jsonProvider;
    private final JsonExpander jsonExpander;
    private final JsonCombiner jsonCombiner;

    private final JsonObject defaultProject;

    private final List<Problem> problems = new ArrayList<>();

    public TestLoaderImpl(Path startPath) {
        this.startPath = startPath;
        this.jsonProvider = loadJsonProvider();
        this.jsonExpander = new JsonExpander(this.jsonProvider);
        this.jsonCombiner = JsonCombiner.merging(this.jsonProvider);
        this.defaultProject = loadDefaultJson();
    }

    @Override
    public GroupNode load() {
        try {
            Path path = findRootGroup(this.startPath);
            return loadRootGroup(path, this.startPath);
        } catch (InternalException e) {
            throw new TestLoadingException(
                    Message.thatLoadingProjectFailed(problems.size()),
                    Collections.unmodifiableList(problems));
        }
    }

    private Path findRootGroup(Path startDir) {
        for (Path dir = startDir; dir != null; dir = dir.getParent()) {
            Path path = dir.resolve(GroupNode.ROOT_FILE_NAME);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return path;
            }
        }
        addProblem(Message.thatProjectIsNotFound(startDir));
        throw fail();
    }

    private GroupNode loadRootGroup(Path projectPath, Path startPath) {
        JsonObject config = loadJson(projectPath);
        JsonObject merged = mergeJson(defaultProject, config);
        JsonObject expanded = expandJson(projectPath, merged);

        Path dir = projectPath.getParent();

        List<TestCase> testCases = loadTestCases(dir, merged);
        List<TestGroup> subgroups = loadSubgroups(dir, merged);

        if (problems.isEmpty()) {
            return new RootTestGroup(
                    dir, startPath, config, merged, expanded, testCases, subgroups, this.jsonProvider);
        } else {
            throw new InternalException();
        }
    }

    private JsonObject loadJson(Path path) {
        try (InputStream in = Files.newInputStream(path);
             JsonReader reader = jsonProvider.createReader(in)) {
            JsonValue value = reader.readValue();
            if (value.getValueType() == ValueType.OBJECT) {
                return value.asJsonObject();
            }
            addProblem(path, Message.thatJsonValueTypeMismatched(value.getValueType()));
        } catch (JsonException e) {
            addProblem(path, Message.thatJsonIsIllFormed(e));
        } catch (IOException e) {
            addProblem(path, Message.thatReadingFileFailed(path));
        }
        throw fail();
    }

    private JsonObject mergeJson(JsonObject base, JsonObject json) {
        return jsonCombiner.apply(base, json);
    }

    private JsonObject expandJson(Path path, JsonObject json) {
        try {
            return jsonExpander.apply(json);
        } catch (PropertyException e) {
            addProblem(path, Message.thatPropertyIsIllegal(e));
            throw fail();
        }
    }

    private TestGroup createTestGroup(Path dir, JsonObject json, JsonObject merged, JsonObject expanded) {
        List<TestCase> testCases = loadTestCases(dir, merged);
        List<TestGroup> subgroups = loadSubgroups(dir, merged);
        return new TestGroup(dir, json, merged, expanded, testCases, subgroups);
    }

    private List<TestCase> loadTestCases(Path dir, JsonObject base) {
        List<TestCase> cases = new ArrayList<>();
        for (Path path : findTestCases(dir)) {
            try {
                cases.add(createTestCase(path, base));
            } catch (InternalException e) {
                // Continues
            }
        }
        return cases;
    }

    private List<TestGroup> loadSubgroups(Path dir, JsonObject base) {
        List<TestGroup> groups = new ArrayList<>();
        for (Path path : findSubgroups(dir)) {
            try {
                groups.add(createSubgroup(path, base));
            } catch (InternalException e) {
                // Continues
            }
        }
        return groups;
    }

    private List<Path> findTestCases(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, TestCase.FILE_PATTERN)) {
            List<Path> children = new ArrayList<>();
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    children.add(path);
                }
            }
            Collections.sort(children);
            return children;
        } catch (IOException e) {
            addProblem(dir, Message.thatReadingDirectoryFailed(dir));
            return Collections.emptyList();
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
            addProblem(dir, Message.thatReadingDirectoryFailed(dir));
            return Collections.emptyList();
        }
    }

    private TestCase createTestCase(Path path, JsonObject base) {
        JsonObject json = loadJson(path);
        JsonObject merged = mergeJson(base, json);
        JsonObject expanded = expandJson(path, merged);
        return new TestCase(path, json, merged, expanded);
    }

    private TestGroup createSubgroup(Path dir, JsonObject base) {
        Path path = dir.resolve(GroupNode.FILE_NAME);
        JsonObject json = loadJson(path);
        JsonObject merged = mergeJson(base, json);
        JsonObject expanded = expandJson(path, merged);
        return createTestGroup(path, json, merged, expanded);
    }

    private static boolean isSubgroup(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        Path path = dir.resolve(GroupNode.FILE_NAME);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    private InternalException fail() {
        return new InternalException();
    }

    private void addProblem(String message) {
        addProblem(new ProjectProblem(message));
    }

    private void addProblem(Path path, String message) {
        addProblem(new ProjectProblem(path, message));
    }

    private void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    private JsonObject loadDefaultJson() {
        InputStream in = TestLoaderImpl.class.getResourceAsStream(DEFAULT_JSON_NAME);
        try (JsonReader reader = jsonProvider.createReader(in)) {
            return reader.readObject();
        }
    }

    private static JsonProvider loadJsonProvider() {
        return JsonProvider.provider();
    }

    @SuppressWarnings("serial")
    private static class InternalException extends RuntimeException {
    }

    /**
     * A problem detected by this loader.
     *
     * @author leadpony
     */
    private static class ProjectProblem implements Problem {

        private final Optional<Path> path;
        private final String description;

        ProjectProblem(String description) {
            this.path = Optional.empty();
            this.description = description;
        }

        ProjectProblem(Path path, String description) {
            this.path = Optional.of(path);
            this.description = description;
        }

        @Override
        public Optional<Path> getPath() {
            return path;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
