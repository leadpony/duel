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

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
public final class TestCaseConfig extends TestNodeConfig {

    private URI path;
    private String method = "GET";
    private JsonObject request = JsonValue.EMPTY_JSON_OBJECT;
    private JsonObject response = JsonValue.EMPTY_JSON_OBJECT;

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonObject getRequest() {
        return request;
    }

    public void setRequest(JsonObject request) {
        this.request = request;
    }

    public JsonObject getResponse() {
        return response;
    }

    public void setResponse(JsonObject response) {
        this.response = response;
    }
}
