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

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author leadpony
 */
class ExactJsonValidator implements JsonValidator {

    private final JsonValue expected;
    private final String wildcard = "@*";
    private List<JsonProblem> problems;

    ExactJsonValidator(JsonValue expected) {
        this.expected = expected;
    }

    @Override
    public boolean validate(JsonValue actual) {
        this.problems = null;
        matchValues("", expected, actual);
        return this.problems == null;
    }

    @Override
    public List<JsonProblem> getProblems() {
        assert problems != null;
        return problems;
    }

    protected final void matchValues(String path, JsonValue expected, JsonValue actual) {
        final ValueType expectedType = expected.getValueType();
        final ValueType actualType = actual.getValueType();
        if (expectedType == actualType) {
            matchValuesOfSameType(path, expected, actual);
        } else if (!isWildcard(expected)) {
            addProblem(JsonProblems.typeMismatch(path, expectedType, actualType));
        }
    }

    protected final void matchValuesOfSameType(String path, JsonValue expected, JsonValue actual) {
        switch (expected.getValueType()) {
        case ARRAY:
            matchArrays(path, expected.asJsonArray(), actual.asJsonArray());
            break;
        case OBJECT:
            matchObjects(path, expected.asJsonObject(), actual.asJsonObject());
            break;
        default:
            matchSimpleValue(path, expected, actual);
            break;
        }
    }

    protected void matchArrays(String base, JsonArray expected, JsonArray actual) {
        final int expectedSize = expected.size();
        final int actualSize = actual.size();

        if (actualSize != expectedSize) {
            addProblem(JsonProblems.arraySizeUnmatch(base, expectedSize, actualSize));
            return;
        }

        for (int i = 0; i < actualSize; i++) {
            matchValues(JsonPointers.append(base, i), expected.get(i), actual.get(i));
        }
    }

    protected void matchObjects(String base, JsonObject expected, JsonObject actual) {
        for (String key : actual.keySet()) {
            if (expected.containsKey(key)) {
                matchValues(JsonPointers.append(base, key), expected.get(key), actual.get(key));
            } else {
                addProblem(JsonProblems.propertyAdded(base, key));
            }
        }

        for (String key : expected.keySet()) {
            if (actual.containsKey(key)) {
                // already compared
            } else {
                addProblem(JsonProblems.propertyRemoved(base, key));
            }
        }
    }

    protected void matchSimpleValue(String path, JsonValue expected, JsonValue actual) {
        if (isWildcard(expected) || actual.equals(expected)) {
            return;
        }
        addProblem(JsonProblems.replaced(path, expected, actual));
    }

    protected final void addProblem(JsonProblem problem) {
        if (problems == null) {
            problems = new ArrayList<>();
        }
        problems.add(problem);
    }

    protected final boolean isWildcard(JsonValue value) {
        if (value.getValueType() != ValueType.STRING) {
            return false;
        }
        JsonString string = (JsonString) value;
        return string.getString().equals(wildcard);
    }
}
