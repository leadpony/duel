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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
public final class TestCaseConfig extends TestNodeConfig {

    private static final Request DEFAULT_REQUEST = new Request();

    private URI path;
    private Request request = DEFAULT_REQUEST;
    private JsonObject response = JsonValue.EMPTY_JSON_OBJECT;

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public JsonObject getResponse() {
        return response;
    }

    public void setResponse(JsonObject response) {
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

        public void setQuery(Map<String, ?> query) {
            this.query = toMultiMap(query);
        }

        public Map<String, List<String>> getHeader() {
            return header;
        }

        public void setHeader(Map<String, ?> header) {
            this.header = toMultiMap(header);
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

        private static Map<String, List<String>> toMultiMap(Map<String, ?> map) {
            Map<String, List<String>> newMap = new HashMap<>();
            map.forEach((key, value) -> {
                List<String> values = new ArrayList<>();
                if (value instanceof List) {
                    ((List<?>) value).stream()
                        .map(Object::toString)
                        .forEach(values::add);
                } else {
                    values.add(value.toString());
                }
                newMap.put(key, values);
            });
            return newMap;
        }
    }
}
