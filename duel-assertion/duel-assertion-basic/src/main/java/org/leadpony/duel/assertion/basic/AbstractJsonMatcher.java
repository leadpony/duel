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

package org.leadpony.duel.assertion.basic;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * A skeletal implementation of {@link JsonMatcher}.
 *
 * @author leadpony
 */
abstract class AbstractJsonMatcher implements JsonMatcher {

    private final String annotationPrefix;

    protected AbstractJsonMatcher(String annotationPrefix) {
        this.annotationPrefix = annotationPrefix;
    }

    @Override
    public boolean match(JsonValue source, JsonValue target) {
        JsonContainerType containerType = getContainerTypeOf(source);
        if (containerType != JsonContainerType.NONE) {
            source = unwrapValue(source, containerType);
        }

        if (source.getValueType() == target.getValueType()) {
            return matchValuesOfSameType(source, target, containerType);
        } else {
            return matchValuesOfDifferentType(source, target);
        }
    }

    protected boolean matchValuesOfSameType(JsonValue source, JsonValue target, JsonContainerType containerType) {
        switch (source.getValueType()) {
        case ARRAY:
            return matchArrays(source.asJsonArray(), target.asJsonArray(), containerType);
        case OBJECT:
            return matchObjects(source.asJsonObject(), target.asJsonObject());
        default:
            return matchSimpleValue(source, target);
        }
    }

    protected boolean matchValuesOfDifferentType(JsonValue source, JsonValue target) {
        return isWildcard(source);
    }

    protected boolean matchSimpleValue(JsonValue source, JsonValue target) {
        return isWildcard(source) || source.equals(target);
    }

    protected abstract boolean matchArrays(JsonArray source, JsonArray target, JsonContainerType containerType);

    protected abstract boolean matchObjects(JsonObject source, JsonObject target);

    private boolean isAnnotation(String value) {
        return value.startsWith(annotationPrefix);
    }

    private JsonAnnotation parseAnnotation(String value) {
        assert isAnnotation(value);
        return JsonAnnotation.valueOf(
                value.substring(annotationPrefix.length()).toUpperCase());
    }

    private boolean isWildcard(JsonValue value) {
        if (value.getValueType() != ValueType.STRING) {
            return false;
        }
        String string = ((JsonString) value).getString();
        if (!isAnnotation(string)) {
            return false;
        }
        try {
            return parseAnnotation(string) == JsonAnnotation.ANY;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private JsonContainerType getContainerTypeOf(JsonValue value) {
        switch (value.getValueType()) {
        case ARRAY:
            return JsonContainerType.LIST;
        case OBJECT:
            JsonObject object = value.asJsonObject();
            if (object.size() == 1) {
                String keyword = object.keySet().iterator().next();
                return getContainerTypeOf(keyword);
            }
            return JsonContainerType.NONE;
        default:
            return JsonContainerType.NONE;
        }
    }

    private JsonContainerType getContainerTypeOf(String keyword) {
        if (isAnnotation(keyword)) {
            try {
                return JsonContainerType.of(parseAnnotation(keyword));
            } catch (IllegalArgumentException e) {
            }
        }
        return JsonContainerType.NONE;
    }

    private JsonValue unwrapValue(JsonValue value, JsonContainerType containerType) {
        if (value.getValueType() == ValueType.OBJECT) {
            JsonObject object = value.asJsonObject();
            String keyword = object.keySet().iterator().next();
            value = object.get(keyword);
        }
        return value;
    }
}
