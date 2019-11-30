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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.api.Parameter;
import org.leadpony.duel.core.api.Node;

/**
 * A skeletal implementation of {@link Node}.
 *
 * @author leadpony
 */
abstract class AbstractNode implements Node {

    private final Path path;
    @SuppressWarnings("unused")
    private final JsonObject json;
    @SuppressWarnings("unused")
    private final JsonObject merged;
    private final JsonObject expanded;

    protected AbstractNode(Path path) {
        this(path, JsonValue.EMPTY_JSON_OBJECT, JsonValue.EMPTY_JSON_OBJECT);
    }

    protected AbstractNode(Path path, JsonObject json, JsonObject expanded) {
        this(path, json, json, expanded);
    }

    protected AbstractNode(Path path, JsonObject json, JsonObject merged, JsonObject expanded) {
        this.path = path;
        this.json = json;
        this.merged = merged;
        this.expanded = expanded;
    }

    @Override
    public String getName() {
        return getParameterOrDefault("name", (String) null);
    }

    @Override
    public URI getId() {
        return path.toUri();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public Object get(Parameter parameter) {
        requireNonNull(parameter, "parameter must not be null.");
        String key = getParameterKey(parameter);
        Object defaultValue = parameter.defaultValue();
        Class<?> valueType = parameter.valueType();
        if (valueType == String.class) {
            return getParameterOrDefault(key, (String) defaultValue);
        } else if (valueType == Integer.class) {
            return getParameterOrDefault(key, (Integer) defaultValue);
        }
        throw new IllegalArgumentException(parameter.name());
    }

    @Override
    public String getAsString(Parameter parameter) {
        Object value = get(parameter);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public Map<String, String> getProperties() {
        JsonValue value = expanded.get("properties");
        if (value.getValueType() == ValueType.OBJECT) {
            JsonObject object = value.asJsonObject();
            if (!object.isEmpty()) {
                Map<String, String> map = new LinkedHashMap<>();
                object.forEach((k, v) -> {
                    map.put(k, valueToString(v));
                });
                return Collections.unmodifiableMap(map);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return getName();
    }

    private static String getParameterKey(Parameter parameter) {
        switch (parameter) {
        case SCHEME:
            return "scheme";
        case HOST:
            return "host";
        case PORT:
            return "port";
        case BASE_PATH:
            return "basePath";
        case METHOD:
            return "method";
        case PATH:
            return "path";
        default:
            throw new IllegalArgumentException(parameter.name());
        }
    }

    private String getParameterOrDefault(String name, String defaultValue) {
        JsonValue value = getParameter(name);
        switch (value.getValueType()) {
        case STRING:
            return ((JsonString) value).getString();
        case NULL:
            return defaultValue;
        default:
            return value.toString();
        }
    }

    private Integer getParameterOrDefault(String name, Integer defaultValue) {
        JsonValue value = getParameter(name);
        switch (value.getValueType()) {
        case NUMBER:
            return ((JsonNumber) value).intValue();
        case STRING:
            try {
                return Integer.parseInt(((JsonString) value).getString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        default:
            return defaultValue;
        }
    }

    protected static String valueToString(JsonValue value) {
        if (value.getValueType() == ValueType.STRING) {
            return ((JsonString) value).getString();
        } else {
            return value.toString();
        }
    }

    protected JsonValue getParameter(String name) {
        return expanded.getOrDefault(name, JsonValue.NULL);
    }

    protected JsonObject getParameterAsObject(String name) {
        JsonValue value = getParameter(name);
        if (value.getValueType() == ValueType.OBJECT) {
            return value.asJsonObject();
        }
        return JsonValue.EMPTY_JSON_OBJECT;
    }
}
