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
import java.nio.file.Files;
import java.nio.file.Path;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.api.TestConfigurationException;
import org.leadpony.duel.core.api.TestException;
import org.leadpony.duel.core.internal.config.ProjectConfig;

/**
 * @author leadpony
 */
public class ProjectLoaderImpl implements ProjectLoader {

    private final Path startPath;
    private final Jsonb jsonb;

    public ProjectLoaderImpl(Path startPath) {
        this.startPath = startPath;
        this.jsonb = JsonbBuilder.create();
    }

    @Override
    public Project load() {
        try {
            return loadProject(this.startPath);
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        }
    }

    private Project loadProject(Path startPath) throws IOException {
        for (Path dir = startPath; dir != null; dir = dir.getParent()) {
            Path projectPath = dir.resolve(Project.FILE_NAME);
            if (Files.exists(projectPath) && Files.isRegularFile(projectPath)) {
                return createProject(projectPath, startPath);
            }
        }
        throw new TestConfigurationException(Message.PROJECT_NOT_FOUND.asString());
    }

    private ProjectImpl createProject(Path projectPath, Path startPath) throws IOException {
        ProjectConfig config = loadProjectConfig(projectPath);
        return new ProjectImpl(projectPath, startPath, config);
    }

    private ProjectConfig loadProjectConfig(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return jsonb.fromJson(in, ProjectConfig.class);
        }
    }
}
