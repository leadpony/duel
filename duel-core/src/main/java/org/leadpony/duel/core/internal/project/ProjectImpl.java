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

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.TestContainer;
import org.leadpony.duel.core.internal.config.ProjectConfig;

/**
 * @author leadpony
 */
class ProjectImpl implements Project {

    private final Path projectPath;
    private final Path startPath;
    private final ProjectConfig config;

    private final Jsonb jsonb = JsonbBuilder.create();

    ProjectImpl(Path projectPath, Path startPath, ProjectConfig config) {
        this.projectPath = projectPath;
        this.startPath = startPath;
        this.config = config;
    }

    @Override
    public URI getId() {
        return projectPath.toUri();
    }

    @Override
    public int getVersion() {
        return config.getVersion();
    }

    @Override
    public URI getBaseUrl() {
        return config.getBaseUrl();
    }

    @Override
    public TestContainer generateTests() {
        TestContext context = new ProjectTestContext();
        return new BasicTestContainer(this.startPath, context, null);
    }

    /**
     * @author leadpony
     */
    class ProjectTestContext implements TestContext {

        private final HttpClient httpClient;

        ProjectTestContext() {
            this.httpClient = buildHttpClient();
        }

        @Override
        public Project getProject() {
            return ProjectImpl.this;
        }

        @Override
        public Jsonb getJsonBinder() {
            return jsonb;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        private HttpClient buildHttpClient() {
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
        }
    }
}
