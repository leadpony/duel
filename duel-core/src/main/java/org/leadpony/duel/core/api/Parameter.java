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

/**
 * @author leadpony
 */
public enum Parameter {
    VERSION("version"),
    NAME("name"),
    SCHEME("scheme"),
    HOST("host"),
    PORT("port"),
    BASE_PATH("basePath"),
    METHOD("method"),
    PATH("path");

    private final String key;

    Parameter(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
