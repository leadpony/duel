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
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectException;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.JsonCombiner;
import org.leadpony.duel.core.internal.common.JsonService;

/**
 * @author leadpony
 */
public class ProjectLoaderImpl implements ProjectLoader {

    private final Path startPath;
    private final JsonService jsonService = JsonService.SINGLETON;

    public ProjectLoaderImpl(Path startPath) {
        this.startPath = startPath;
    }

    @Override
    public Project load() {
        return loadProject(this.startPath);
    }

    private Project loadProject(Path startPath) {
        for (Path dir = startPath; dir != null; dir = dir.getParent()) {
            Path projectPath = dir.resolve(Project.FILE_NAME);
            if (Files.exists(projectPath) && Files.isRegularFile(projectPath)) {
                return createProject(projectPath, startPath);
            }
        }
        throw new ProjectException(Message.PROJECT_NOT_FOUND.asString());
    }

    private ProjectImpl createProject(Path projectPath, Path startPath) {
        JsonObject config = loadJson(projectPath);
        JsonObject expanded = JsonExpander.SIMPLE.apply(config);

        Path dir = projectPath.getParent();

        List<TestCase> testCases = loadTestCases(dir, config);
        List<TestGroup> subgroups = loadSubgroups(dir, config);

        return new ProjectImpl(dir, startPath, config, expanded, testCases, subgroups);
    }

    private JsonObject loadJson(Path path) {
        try {
            return (JsonObject) jsonService.readFrom(path);
        } catch (ClassCastException e) {
            // TODO:
            throw new ProjectException(Message.BAD_PROJECT.format(path), e);
        } catch (JsonException e) {
            // TODO:
            throw new ProjectException(Message.BAD_PROJECT.format(path), e);
        } catch (IOException e) {
            throw new ProjectException(Message.FILE_READ_FAILURE.format(path), e);
        }
    }

    private TestGroup createTestGroup(Path dir, JsonObject json, JsonObject merged, JsonObject expanded) {
        List<TestCase> testCases = loadTestCases(dir, merged);
        List<TestGroup> subgroups = loadSubgroups(dir, merged);
        return new TestGroup(dir, json, merged, expanded, testCases, subgroups);
    }

    private List<TestCase> loadTestCases(Path dir, JsonObject base) {
        return findTestCases(dir).stream()
                .map(path -> createTestCase(path, base))
                .collect(Collectors.toList());
    }

    private List<TestGroup> loadSubgroups(Path dir, JsonObject base) {
        return findSubgroups(dir).stream()
                .map(path -> createSubgroup(path))
                .collect(Collectors.toList());
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
            throw new ProjectException(Message.DIRECTORY_READ_FAILURE.format(dir), e);
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
            throw new ProjectException(Message.DIRECTORY_READ_FAILURE.format(dir), e);
        }
    }

    private TestCase createTestCase(Path path, JsonObject base) {
        JsonObject json = loadJson(path);
        JsonObject merged = JsonCombiner.MERGE.apply(base, json);
        JsonObject expanded = JsonExpander.SIMPLE.apply(merged);
        return new TestCase(path, json, merged, expanded);
    }

    private TestGroup createSubgroup(Path dir) {
        Path path = dir.resolve(GroupNode.FILE_NAME);
        JsonObject json = JsonValue.EMPTY_JSON_OBJECT;
        return createTestGroup(path, json, json, json);
    }

    private static boolean isSubgroup(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        Path path = dir.resolve(GroupNode.FILE_NAME);
        return Files.exists(path) && Files.isRegularFile(path);
    }
}
