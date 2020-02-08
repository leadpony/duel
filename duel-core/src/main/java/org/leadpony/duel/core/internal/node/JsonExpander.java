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

import java.util.ArrayList;
import java.util.Collections;
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
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.internal.Message;

/**
 * @author leadpony
 */
public class JsonExpander implements Function<JsonObject, JsonObject> {

    static final String PROPERTIES_KEY = "properties";

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory builderFactory;

    JsonExpander(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.builderFactory = jsonProvider.createBuilderFactory(Collections.emptyMap());
    }

    /**
     * Expands all the properties in the specified JSON object.
     *
     * @return the JSON with the properties expanded.
     * @throws PropertyException if an illegal property was detected.
     */
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
        var finder = new ExpandingPropertyFinder(properties, jsonProvider, builderFactory);
        var builder = builderFactory.createObjectBuilder();
        var errors = new ArrayList<String>();

        properties.forEach((name, value) -> {
            if (value.getValueType() == ValueType.STRING) {
                try {
                    builder.add(name, finder.apply(name));
                } catch (ExpansionException e) {
                    errors.add(Message.thatPropertyExpansionLoopsInfinite(name, e));
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
        ValueExpander expander = new ValueExpander(finder, jsonProvider, builderFactory);
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

        ExpandingPropertyFinder(JsonObject properties, JsonProvider jsonProvider, JsonBuilderFactory builderFactory) {
            super(properties);
            this.expander = new ValueExpander(this, jsonProvider, builderFactory);
        }

        @Override
        public String apply(String name) {
            if (nameSet.contains(name)) {
                var names = new ArrayList<String>(nameSet);
                names.add(name);
                String m = names.stream().collect(Collectors.joining(" > "));
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
