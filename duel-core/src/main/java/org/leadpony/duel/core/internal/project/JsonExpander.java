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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.internal.common.JsonService;

/**
 * @author leadpony
 */
public enum JsonExpander implements Function<JsonObject, JsonObject> {
    SIMPLE;

    static final String PROPERTIES_KEY = "properties";

    private final JsonService jsonService = JsonService.SINGLETON;
    private final JsonBuilderFactory builderFactory = jsonService.createBuilderFactory();

    @Override
    public JsonObject apply(JsonObject object) {
        JsonObject properties = extractProperties(object);
        if (properties.isEmpty()) {
            return object;
        }
        return expandObject(object, expandProperties(properties));
    }

    private static JsonObject extractProperties(JsonObject object) {
        if (object.containsKey(PROPERTIES_KEY)) {
            JsonValue value = object.get(PROPERTIES_KEY);
            if (value.getValueType() == ValueType.OBJECT) {
                return value.asJsonObject();
            }
        }
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    private JsonObject expandProperties(JsonObject properties) {
        Function<String, String> finder = new ExpandingPropertyFinder(properties, this.jsonService);
        ValueExpander expander = new ValueExpander(finder, this.jsonService);
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        properties.forEach((name, value) -> {
            if (value.getValueType() == ValueType.STRING) {
                builder.add(name, expander.expand((JsonString) value));
            } else {
                builder.add(name, value);
            }
        });
        return builder.build();
    }

    private JsonObject expandObject(JsonObject object, JsonObject properties) {
        Function<String, String> finder = new SimplePropertyFinder(properties);
        ValueExpander expander = new ValueExpander(finder, this.jsonService);
        JsonObjectBuilder builder = this.builderFactory.createObjectBuilder();
        builder.add(PROPERTIES_KEY, properties);
        object.forEach((name, value) -> {
            if (!PROPERTIES_KEY.equals(name)) {
                builder.add(name, expander.expand(value));
            }
        });
        return builder.build();
    }

    private static class SimplePropertyFinder implements Function<String, String> {

        private final JsonObject properties;

        SimplePropertyFinder(JsonObject properties) {
            this.properties = properties;
        }

        @Override
        public String apply(String name) {
            if (properties.containsKey(name)) {
                JsonValue value = properties.get(name);
                if (value.getValueType() == ValueType.STRING) {
                    return ((JsonString) value).getString();
                } else {
                    return value.toString();
                }
            } else {
                return System.getProperty(name);
            }
        }
    }

    private static class ExpandingPropertyFinder extends SimplePropertyFinder {

        final ValueExpander expander;
        final Set<String> nameSet = new HashSet<>();

        ExpandingPropertyFinder(JsonObject properties, JsonService jsonService) {
            super(properties);
            this.expander = new ValueExpander(this, jsonService);
        }

        @Override
        public String apply(String name) {
            if (nameSet.contains(name)) {
                throw new IllegalArgumentException(name);
            }
            nameSet.add(name);
            String value = super.apply(name);
            if (value != null) {
                value = expander.expand(value);
            }
            nameSet.remove(name);
            return value;
        }
    }
}
