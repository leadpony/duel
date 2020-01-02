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

import java.util.LinkedList;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
final class SimpleJsonMatcher extends AbstractJsonMatcher {

    @Override
    protected boolean matchArrays(JsonArray source, JsonArray target, JsonContainerType containerType) {
        assert containerType != null;
        if (source.size() != target.size()) {
            return false;
        }
        switch (containerType) {
        case LIST:
            return matchOrderedArrays(source, target);
        case SET:
            return matchUnorderedArrays(source, target);
        default:
            assert false;
            return false;
        }
    }

    @Override
    protected boolean matchObjects(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            if (target.containsKey(key)) {
                if (!match(source.get(key), target.get(key))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        for (String key : target.keySet()) {
            if (!source.containsKey(key)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchOrderedArrays(JsonArray source, JsonArray target) {
        for (int i = 0; i < source.size(); i++) {
            if (!match(source.get(i), target.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean matchUnorderedArrays(JsonArray source, JsonArray target) {
        LinkedList<JsonValue> remains = new LinkedList<>(target);
        for (JsonValue sourceValue : source) {
            JsonValue matched = null;
            for (JsonValue targetValue : remains) {
                if (match(sourceValue, targetValue)) {
                    matched = targetValue;
                    break;
                }
            }
            if (matched == null) {
                return false;
            }
            remains.remove(matched);
        }
        return remains.isEmpty();
    }
}
