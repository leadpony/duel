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

import java.util.Collection;

/**
 * An exception thrown while loading a project.
 *
 * @author leadpony
 */
public class ProjectException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Collection<Problem> problems;

    public ProjectException(String message, Collection<Problem> problems) {
        super(message);
        this.problems = problems;
    }

    public Collection<Problem> getProblems() {
        return problems;
    }
}
