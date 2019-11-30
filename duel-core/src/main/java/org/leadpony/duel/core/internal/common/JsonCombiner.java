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

package org.leadpony.duel.core.internal.common;

import java.util.function.BiFunction;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author leadpony
 */
public enum JsonCombiner implements BiFunction<JsonValue, JsonValue, JsonValue> {
    MERGE() {

        final JsonBuilderFactory builderFactory = JsonService.SINGLETON.createBuilderFactory();

        @Override
        public JsonValue apply(JsonValue base, JsonValue value) {
            final ValueType type = value.getValueType();
            if (type != base.getValueType()) {
                return value;
            }
            switch (type) {
            case ARRAY:
                return apply(base.asJsonArray(), value.asJsonArray());
            case OBJECT:
                return apply(base.asJsonObject(), value.asJsonObject());
            default:
                return value;
            }
        }

        @Override
        public JsonArray apply(JsonArray base, JsonArray array) {
            if (base.isEmpty()) {
                return array;
            } else if (array.isEmpty()) {
                return base;
            }
            JsonArrayBuilder builder = builderFactory.createArrayBuilder(base);
            array.forEach(builder::add);
            return builder.build();
        }

        @Override
        public JsonObject apply(JsonObject base, JsonObject object) {
            if (base.isEmpty()) {
                return object;
            } else if (object.isEmpty()) {
                return base;
            }
            JsonObjectBuilder builder = builderFactory.createObjectBuilder();
            base.forEach((name, value) -> {
                if (!object.containsKey(name)) {
                    builder.add(name, value);
                }
            });
            object.forEach((name, value) -> {
                if (base.containsKey(name)) {
                    builder.add(name, apply(base.get(name), value));
                } else {
                    builder.add(name, value);
                }
            });
            return builder.build();
        }
    };

    public JsonArray apply(JsonArray base, JsonArray array) {
        return apply((JsonValue) base, (JsonValue) array).asJsonArray();
    }

    public JsonObject apply(JsonObject base, JsonObject object) {
        return apply((JsonValue) base, (JsonValue) object).asJsonObject();
    }
}
