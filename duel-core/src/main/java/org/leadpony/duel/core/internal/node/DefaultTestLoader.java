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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.leadpony.duel.core.api.TestLoader;
import org.leadpony.duel.core.api.TestLoadingException;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.JsonCombiner;

/**
 * An implementation of {@link TestLoader}.
 *
 * @author leadpony
 */
public class DefaultTestLoader implements TestLoader {

    private static final String DEFAULT_JSON_NAME = "default.json";

    private final Path startPath;

    private final JsonProvider jsonProvider;
    private final JsonObject defaultProject;

    private final JsonExpander jsonExpander;
    private final JsonCombiner jsonCombiner;

    private final List<Problem> problems = new ArrayList<>();

    public DefaultTestLoader(Path startPath) {
        this.startPath = startPath;
        this.jsonProvider = loadJsonProvider();
        this.defaultProject = loadDefaultJson(this.jsonProvider);
        this.jsonExpander = new JsonExpander(this.jsonProvider);
        this.jsonCombiner = JsonCombiner.merging(this.jsonProvider);
    }

    @Override
    public GroupNode load() {
        GroupNode loaded = null;
        List<Path> paths = findRootGroup(this.startPath);
        if (!paths.isEmpty()) {
            loaded = loadGroup(paths.iterator(), true);
        }
        if (problems.isEmpty()) {
            return loaded;
        }
        throw new TestLoadingException(
                Message.thatLoadingProjectFailed(problems.size()),
                Collections.unmodifiableList(problems));
    }

    private List<Path> findRootGroup(Path startDir) {
        var paths = new LinkedList<Path>();
        for (Path dir = startDir; dir != null; dir = dir.getParent()) {
            Path path = dir.resolve(GroupNode.ROOT_FILE_NAME);
            if (Files.isRegularFile(path)) {
                paths.addFirst(path);
                return paths;
            } else {
                path = dir.resolve(GroupNode.FILE_NAME);
                if (Files.isRegularFile(path)) {
                    paths.addFirst(path);
                } else {
                    break;
                }
            }
        }
        addProblem(Message.thatProjectIsNotFound(startDir));
        return Collections.emptyList();
    }

    private TestGroup loadGroup(Iterator<Path> it, boolean isRoot) {
        final Path path = it.next();

        final JsonObject raw = loadConfig(path);
        final JsonObject merged = mergeJson(this.defaultProject, raw);
        final JsonObject expanded = expandJson(path, merged);

        final Path dir = path.getParent();

        List<TestCase> cases;
        List<TestGroup> subgroups;

        if (it.hasNext()) {
            cases = Collections.emptyList();
            subgroups = Arrays.asList(loadGroup(it, false));
        } else {
            cases = loadCases(dir, merged);
            subgroups = loadSubgroups(dir, merged);
        }

        if (isRoot) {
            return new RootTestGroup(
                    dir, raw, merged, expanded, cases, subgroups, this.jsonProvider);
        } else {
            return new TestGroup(dir, raw, merged, expanded, cases, subgroups);
        }
    }

    private List<TestCase> loadCases(Path dir, JsonObject base) {
        List<TestCase> cases = new ArrayList<>();
        for (Path path : findCases(dir)) {
            cases.add(createCase(path, base));
        }
        return cases;
    }

    private List<TestGroup> loadSubgroups(Path dir, JsonObject base) {
        List<TestGroup> groups = new ArrayList<>();
        for (Path path : findSubgroups(dir)) {
            groups.add(createSubgroup(path, base));
        }
        return groups;
    }

    private List<Path> findCases(Path dir) {
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

    private TestCase createCase(Path path, JsonObject base) {
        JsonObject json = loadConfig(path);
        JsonObject merged = mergeJson(base, json);
        JsonObject expanded = expandJson(path, merged);

        return new TestCase(path, json, merged, expanded);
    }

    private TestGroup createSubgroup(Path dir, JsonObject base) {
        Path path = dir.resolve(GroupNode.FILE_NAME);

        JsonObject json = loadConfig(path);
        JsonObject merged = mergeJson(base, json);
        JsonObject expanded = expandJson(path, merged);

        List<TestCase> cases = loadCases(dir, merged);
        List<TestGroup> subgroups = loadSubgroups(dir, merged);

        return new TestGroup(dir, json, merged, expanded, cases, subgroups);
    }

    private static boolean isSubgroup(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        return Files.isRegularFile(dir.resolve(GroupNode.FILE_NAME));
    }

    /**
     * Loads a configuration JSON.
     *
     * @param path the path from which the configuration will be loaded.
     * @return the loaded configuration JSON if succeeded, otherwise empty JSON object.
     */
    private JsonObject loadConfig(Path path) {
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
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    private JsonObject mergeJson(JsonObject base, JsonObject json) {
        return jsonCombiner.apply(base, json);
    }

    /**
     * Expands all of the properties in the specified JSON.
     *
     * @param path the path to the JSON.
     * @param json the JSON to be expanded.
     * @return the expanded JSON if succeeded, otherwise original JSON.
     */
    private JsonObject expandJson(Path path, JsonObject json) {
        try {
            return jsonExpander.apply(json);
        } catch (PropertyException e) {
            addProblem(path, Message.thatPropertyIsIllegal(e));
            return json;
        }
    }

    private void addProblem(String message) {
        this.problems.add(new ConfigurationProblem(message));
    }

    private void addProblem(Path path, String message) {
        this.problems.add(new ConfigurationProblem(message, path));
    }

    private static JsonProvider loadJsonProvider() {
        return JsonProvider.provider();
    }

    private static JsonObject loadDefaultJson(JsonProvider jsonProvider) {
        InputStream in = DefaultTestLoader.class.getResourceAsStream(DEFAULT_JSON_NAME);
        try (JsonReader reader = jsonProvider.createReader(in)) {
            return reader.readObject();
        }
    }

    private static class ConfigurationProblem implements Problem {

        private final String description;
        private final Optional<Path> path;

        ConfigurationProblem(String description) {
            this.description = description;
            this.path = Optional.empty();
        }

        ConfigurationProblem(String description, Path path) {
            this.description = description;
            this.path = Optional.of(path);
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
