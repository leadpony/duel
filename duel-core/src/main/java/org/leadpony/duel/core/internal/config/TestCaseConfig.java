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

package org.leadpony.duel.core.internal.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonValue;

/**
 * @author leadpony
 */
public final class TestCaseConfig extends Config {

    private static final Request DEFAULT_REQUEST = new Request();

    private String path;
    private Request request = DEFAULT_REQUEST;
    private Map<String, JsonValue> response = Collections.emptyMap();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Map<String, JsonValue> getResponse() {
        return response;
    }

    public void setResponse(Map<String, JsonValue> response) {
        this.response = response;
    }

    /**
     * @author leadpony
     */
    public static class Request {

        private Map<String, List<String>> query = Collections.emptyMap();
        private Map<String, List<String>> header = Collections.emptyMap();
        private JsonValue body;

        public Map<String, List<String>> getQuery() {
            return query;
        }

        public void setQuery(Map<String, List<String>> query) {
            this.query = query;
        }

        public Map<String, List<String>> getHeader() {
            return header;
        }

        public void setHeader(Map<String, List<String>> header) {
            this.header = header;
        }

        public Optional<JsonValue> getBody() {
            return Optional.ofNullable(body);
        }

        public void setBody(JsonValue body) {
            if (body == null) {
                body = JsonValue.NULL;
            }
            this.body = body;
        }
    }
}
