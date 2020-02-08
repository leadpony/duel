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

package org.leadpony.duel.core.internal.node;

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
    private final JsonObject json;
    @SuppressWarnings("unused")
    private final JsonObject merged;
    private final JsonObject expanded;

    protected AbstractNode(Path path, JsonObject json, JsonObject merged, JsonObject expanded) {
        this.path = path;
        this.json = json;
        this.merged = merged;
        this.expanded = expanded;
    }

    @Override
    public int getVersion() {
        return (int) get(Parameter.VERSION);
    }

    @Override
    public String getName() {
        return getValueAsString("name");
    }

    @Override
    public URI getId() {
        return path.toUri();
    }

    @Override
    public Path getNodePath() {
        return path;
    }

    @Override
    public String getAnnotationPrefix() {
        return getValueAsString("annotationPrefix");
    }

    @Override
    public Object get(Parameter parameter) {
        requireNonNull(parameter, "parameter must not be null.");
        JsonValue value = getValue(parameter.key());
        switch (value.getValueType()) {
        case STRING:
            JsonString string = (JsonString) value;
            return string.getString();
        case NUMBER:
            JsonNumber number = (JsonNumber) value;
            return number.intValue();
        case TRUE:
            return Boolean.TRUE;
        case FALSE:
            return Boolean.FALSE;
        case NULL:
            return null;
        default:
            throw new IllegalArgumentException(parameter.name());
        }
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
    public final JsonObject getOriginalConfigurationAsJson() {
        return json;
    }

    @Override
    public final JsonObject getEffectiveConfigurarionAsJson() {
        return expanded;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected static String valueToString(JsonValue value) {
        if (value.getValueType() == ValueType.STRING) {
            return ((JsonString) value).getString();
        } else {
            return value.toString();
        }
    }

    protected JsonObject getParameterAsObject(String name) {
        JsonValue value = getValue(name);
        if (value.getValueType() == ValueType.OBJECT) {
            return value.asJsonObject();
        }
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    protected JsonValue getValue(String name) {
        return expanded.getOrDefault(name, JsonValue.NULL);
    }

    protected String getValueAsString(String name) {
        JsonValue value = getValue(name);
        if (value.getValueType() == ValueType.STRING) {
            JsonString string = (JsonString) value;
            return string.getString();
        } else if (value == JsonValue.NULL) {
            return null;
        } else {
            return value.toString();
        }
    }
}
