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

package org.leadpony.duel.core.internal.project;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.api.GroupExecution;
import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.spi.AssertionFactory;

/**
 * @author leadpony
 */
class RootTestGroup extends TestGroup {

    @SuppressWarnings("unused")
    private final Path startPath;

    private final JsonProvider jsonProvider;

    RootTestGroup(Path dir,
            Path startPath,
            JsonObject json,
            JsonObject merged,
            JsonObject expanded,
            List<TestCase> testCases,
            List<TestGroup> subgroups,
            JsonProvider jsonProvider
            ) {
        super(dir, json, merged, expanded, testCases, subgroups);
        this.startPath = startPath;
        this.jsonProvider = jsonProvider;
    }

    /* As a GroupNode */

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public GroupExecution createExecution() {
        TestExecutionContext context = new RootExecutionContext(this.jsonProvider);
        return createExecution(context);
    }

    /**
     * @author leadpony
     */
    class RootExecutionContext implements TestExecutionContext {

        private final JsonProvider jsonProvider;
        private final JsonReaderFactory jsonReaderFactory;
        private final HttpClient httpClient;
        private final AssertionFactory assertionFactory;

        RootExecutionContext(JsonProvider jsonProvider) {
            this.jsonProvider = jsonProvider;
            this.jsonReaderFactory = jsonProvider.createReaderFactory(Collections.emptyMap());
            this.httpClient = buildHttpClient();
            this.assertionFactory = new RootAssertionFactory(this);
        }

        @Override
        public GroupNode getRootGroup() {
            return RootTestGroup.this;
        }

        @Override
        public JsonProvider getJsonProvider() {
            return jsonProvider;
        }

        @Override
        public JsonReaderFactory getJsonReaderFactory() {
            return jsonReaderFactory;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public AssertionFactory getAssertionFactory() {
            return assertionFactory;
        }

        private HttpClient buildHttpClient() {
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(Redirect.NORMAL)
                    .build();
        }
    }
}
