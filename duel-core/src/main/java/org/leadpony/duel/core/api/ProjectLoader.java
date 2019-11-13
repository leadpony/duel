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

package org.leadpony.duel.core.api;

import java.nio.file.Path;
import java.util.Objects;

import org.leadpony.duel.core.internal.project.ProjectLoaderImpl;

/**
 * A loader of a project.
 *
 * @author leadpony
 */
public interface ProjectLoader {

    /**
     * Loads a project from the specified path.
     *
     * @param startPath the path from which the project will be loaded.
     * @return the loaded project.
     * @throws ProjectException if an I/O error occurred while loading the project.
     * @throws NullPointerException if {@code startPath} is {@code null}.
     */
    static Project loadFrom(Path startPath) {
        Objects.requireNonNull(startPath, "startPath must not be null.");
        return new ProjectLoaderImpl(startPath).load();
    }

    /**
     * Loads a project.
     *
     * @return the loaded project.
     * @throws ProjectException if an I/O error occurred while loading the project.
     */
    Project load();
}
