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

package org.leadpony.duel.assertion.basic;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author leadpony
 */
abstract class AbstractJsonMatcher implements JsonMatcher {

    private final String wildcard = "@*";

    @Override
    public boolean match(JsonValue source, JsonValue target) {
        if (source.getValueType() == target.getValueType()) {
            return matchValuesOfSameType(source, target);
        } else {
            return matchValuesOfDifferentType(source, target);
        }
    }

    protected boolean matchValuesOfSameType(JsonValue source, JsonValue target) {
        switch (source.getValueType()) {
        case ARRAY:
            return matchArrays(source.asJsonArray(), target.asJsonArray());
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

    protected abstract boolean matchArrays(JsonArray source, JsonArray target);

    protected abstract boolean matchObjects(JsonObject source, JsonObject target);

    private boolean isWildcard(JsonValue value) {
        if (value.getValueType() != ValueType.STRING) {
            return false;
        }
        JsonString string = (JsonString) value;
        return string.getString().equals(wildcard);
    }
}
