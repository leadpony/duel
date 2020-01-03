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
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectException;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.JsonCombiner;
import org.leadpony.duel.core.internal.common.JsonService;

/**
 * An implementation of {@link ProjectLoader}.
 *
 * @author leadpony
 */
public class ProjectLoaderImpl implements ProjectLoader {

    private static final String DEFAULT_PROJECT_NAME = "project.default.json";
    private static final JsonObject DEFAULT_PROJECT = loadDefaultProject();

    private final Path startPath;
    private final JsonProvider jsonProvider = JsonService.PROVIDER;

    private final List<Problem> problems = new ArrayList<>();

    public ProjectLoaderImpl(Path startPath) {
        this.startPath = startPath;
    }

    @Override
    public Project load() {
        try {
            Path path = findProject(this.startPath);
            return loadProject(path, this.startPath);
        } catch (LoadingException e) {
            throw new ProjectException(
                    Message.PROJECT_LOADING_FAILED.format(problems.size()),
                    Collections.unmodifiableList(problems));
        }
    }

    private Path findProject(Path startDir) {
        for (Path dir = startDir; dir != null; dir = dir.getParent()) {
            Path path = dir.resolve(Project.FILE_NAME);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return path;
            }
        }
        addProblem(Message.PROJECT_NOT_FOUND, startDir);
        throw fail();
    }

    private Project loadProject(Path projectPath, Path startPath) {
        JsonObject config = loadJson(projectPath);
        JsonObject merged = mergeJson(DEFAULT_PROJECT, config);
        JsonObject expanded = expandJson(projectPath, merged);

        Path dir = projectPath.getParent();

        List<TestCase> testCases = loadTestCases(dir, merged);
        List<TestGroup> subgroups = loadSubgroups(dir, merged);

        if (problems.isEmpty()) {
            return new ProjectImpl(dir, startPath, config, merged, expanded, testCases, subgroups);
        } else {
            throw new LoadingException();
        }
    }

    private JsonObject loadJson(Path path) {
        try (InputStream in = Files.newInputStream(path);
             JsonReader reader = jsonProvider.createReader(in)) {
            JsonValue value = reader.readValue();
            if (value.getValueType() == ValueType.OBJECT) {
                return value.asJsonObject();
            }
            addProblem(path, Message.JSON_UNEXPECTED_VALUE_TYPE,
                    value.getValueType());
        } catch (JsonException e) {
            addProblem(path, Message.JSON_ILL_FORMED, e.getMessage());
        } catch (IOException e) {
            addProblem(path, Message.IO_ERROR_FILE);
        }
        throw fail();
    }

    private JsonObject mergeJson(JsonObject base, JsonObject json) {
        return JsonCombiner.MERGE.apply(base, json);
    }

    private JsonObject expandJson(Path path, JsonObject json) {
        try {
            return JsonExpander.SIMPLE.apply(json);
        } catch (PropertyException e) {
            addProblem(path, Message.PROPERTY_ILLEGAL, e.getMessage());
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
            } catch (LoadingException e) {
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
            } catch (LoadingException e) {
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
            addProblem(dir, Message.IO_ERROR_DIRECTORY);
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
            addProblem(dir, Message.IO_ERROR_DIRECTORY, dir);
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

    private LoadingException fail() {
        return new LoadingException();
    }

    private void addProblem(Message message, Object... arguments) {
        addProblem(new ProjectProblem(message, arguments));
    }

    private void addProblem(Path path, Message message, Object... arguments) {
        addProblem(new ProjectProblem(path, message, arguments));
    }

    private void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    private static JsonObject loadDefaultProject() {
        InputStream in = ProjectLoaderImpl.class.getResourceAsStream(DEFAULT_PROJECT_NAME);
        try (JsonReader reader = JsonService.READER_FACTORY.createReader(in)) {
            return reader.readObject();
        }
    }

    @SuppressWarnings("serial")
    private static class LoadingException extends RuntimeException {
    }

    private static class ProjectProblem implements Problem {

        private final Optional<Path> path;
        private final String description;

        ProjectProblem(Message message, Object... arguments) {
            this.path = Optional.empty();
            this.description = message.format(arguments);
        }

        ProjectProblem(Path path, Message message, Object... arguments) {
            this.path = Optional.of(path);
            this.description = message.format(arguments);
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
