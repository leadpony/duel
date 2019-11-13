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
import java.nio.file.Files;
import java.nio.file.Path;

import javax.json.bind.JsonbException;

import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectException;
import org.leadpony.duel.core.api.ProjectLoader;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.config.ConfigLoader;
import org.leadpony.duel.core.internal.config.ProjectConfig;

/**
 * @author leadpony
 */
public class ProjectLoaderImpl implements ProjectLoader {

    private final Path startPath;

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
        try {
            ProjectConfig config = new ConfigLoader().load(projectPath, ProjectConfig.class);
            return new ProjectImpl(projectPath.getParent(), startPath, config);
        } catch (JsonbException e) {
            throw new ProjectException(
                    Message.BAD_PROJECT.format(projectPath),
                    e);
        } catch (IOException e) {
            throw new ProjectException(
                    Message.FILE_READ_FAILURE.format(projectPath),
                    e);
        }
    }
}
