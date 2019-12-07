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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.internal.Message;
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
        var finder = new ExpandingPropertyFinder(properties);
        var builder = builderFactory.createObjectBuilder();
        var errors = new ArrayList<String>();

        properties.forEach((name, value) -> {
            if (value.getValueType() == ValueType.STRING) {
                try {
                    builder.add(name, finder.apply(name));
                } catch (ExpansionException e) {
                    String m = Message.INFINTE_PROPERTY_EXPANSION.format(
                            name,
                            e.getMessage());
                    errors.add(m);
                }
            } else {
                builder.add(name, value);
            }
        });

        if (errors.isEmpty()) {
            return builder.build();
        } else {
            throw new PropertyException(errors);
        }
    }

    private JsonObject expandObject(JsonObject object, JsonObject properties) {
        Function<String, String> finder = new SimplePropertyFinder(properties);
        ValueExpander expander = new ValueExpander(finder);
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
        final Set<String> nameSet = new LinkedHashSet<>();

        ExpandingPropertyFinder(JsonObject properties) {
            super(properties);
            this.expander = new ValueExpander(this);
        }

        @Override
        public String apply(String name) {
            if (nameSet.contains(name)) {
                String m = nameSet.stream().collect(Collectors.joining(", "));
                throw new ExpansionException(m);
            }

            try {
                nameSet.add(name);
                String value = super.apply(name);
                if (value != null) {
                    value = expander.expand(value);
                }
                return value;
            } finally {
                nameSet.remove(name);
            }
        }
    }

    @SuppressWarnings("serial")
    private static class ExpansionException extends RuntimeException {

        ExpansionException(String message) {
            super(message);
        }
    }
}
