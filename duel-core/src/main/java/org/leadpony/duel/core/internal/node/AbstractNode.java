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
import java.util.Optional;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.api.Parameter;
import org.leadpony.duel.core.internal.common.JsonValues;
import org.leadpony.duel.core.api.Node;

/**
 * A skeletal implementation of {@link Node}.
 *
 * @author leadpony
 */
abstract class AbstractNode implements Node {

    private static final int DEFAULT_VERSION = 1;

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
        Optional<JsonValue> optional = getValue(Parameter.VERSION);
        if (optional.isPresent()) {
            JsonValue value = optional.get();
            if (value.getValueType() == ValueType.NUMBER) {
                return ((JsonNumber) value).intValue();
            }
        }
        return DEFAULT_VERSION;
    }

    @Override
    public String getName() {
        return getValue(Parameter.NAME)
                .map(value -> JsonValues.asString(value))
                .orElse(getDefaultName());
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
        return getValueAsString("annotationPrefix", "@");
    }

    @Override
    public Optional<JsonValue> getValue(String name) {
        requireNonNull(name, "name must not be null.");
        if (expanded.containsKey(name)) {
            return Optional.of(expanded.get(name));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getValueAsString(String name, String defaultValue) {
        requireNonNull(name, "name must not be null.");
        requireNonNull(defaultValue, "defaultValue must not be null.");
        return getValue(name)
                .map(value -> JsonValues.asString(value))
                .orElse(defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        JsonValue value = expanded.get("properties");
        if (value.getValueType() == ValueType.OBJECT) {
            JsonObject object = value.asJsonObject();
            if (!object.isEmpty()) {
                Map<String, String> map = new LinkedHashMap<>();
                object.forEach((k, v) -> {
                    map.put(k, JsonValues.asString(v));
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
    public final JsonObject getEffectiveConfigurationAsJson() {
        return expanded;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected abstract String getDefaultName();
}
