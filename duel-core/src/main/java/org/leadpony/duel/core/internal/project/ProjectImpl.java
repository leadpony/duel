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

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.nio.file.Path;
import java.util.List;

import javax.json.JsonObject;

import org.leadpony.duel.core.api.GroupExecution;
import org.leadpony.duel.core.api.Project;

/**
 * @author leadpony
 */
class ProjectImpl extends TestGroup implements Project {

    private static final int DEFAULT_VERSION = 1;

    @SuppressWarnings("unused")
    private final Path startPath;

    ProjectImpl(Path dir,
            Path startPath,
            JsonObject json,
            JsonObject expanded,
            List<TestCase> testCases,
            List<TestGroup> subgroups
            ) {
        super(dir, json, json, expanded, testCases, subgroups);
        this.startPath = startPath;
    }

    /* As a Project */

    @Override
    public int getVersion() {
        return getOrDefault("version", DEFAULT_VERSION);
    }

    @Override
    public GroupExecution createExecution() {
        ExecutionContext context = new ProjectTestContext();
        return createExecution(context);
    }

    /**
     * @author leadpony
     */
    class ProjectTestContext implements ExecutionContext {

        private final HttpClient httpClient;
        private final AssertionFactory responseValidatorFactory;

        ProjectTestContext() {
            this.httpClient = buildHttpClient();
            this.responseValidatorFactory = new AssertionFactory();
        }

        @Override
        public Project getProject() {
            return ProjectImpl.this;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public AssertionFactory getAssertionFactory() {
            return responseValidatorFactory;
        }

        private HttpClient buildHttpClient() {
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(Redirect.NORMAL)
                    .build();
        }
    }
}
