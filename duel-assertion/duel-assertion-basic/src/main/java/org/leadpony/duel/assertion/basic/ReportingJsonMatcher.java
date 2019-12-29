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
import javax.json.JsonValue;

/**
 * @author leadpony
 */
class ReportingJsonMatcher extends AbstractJsonMatcher {

    private final JsonPointerBuilder pointerBuilder = new JsonPointerBuilder();
    private List<JsonProblem> problems;

    public List<JsonProblem> getProblems() {
        assert problems != null;
        return problems;
    }

    @Override
    protected boolean matchValuesOfDifferentType(JsonValue source, JsonValue target) {
        if (super.matchValuesOfDifferentType(source, target)) {
            return true;
        } else {
            addProblem(JsonProblems.typeMismatch(currentPointer(),
                    source.getValueType(),
                    target.getValueType()));
            return false;
        }
    }

    @Override
    protected boolean matchSimpleValue(JsonValue source, JsonValue target) {
        if (super.matchSimpleValue(source, target)) {
            return true;
        } else {
            addProblem(JsonProblems.replaced(currentPointer(), source, target));
            return false;
        }
    }

    @Override
    protected boolean matchArrays(JsonArray source, JsonArray target) {
        final int sourceSize = source.size();
        final int targetSize = target.size();

        if (sourceSize != targetSize) {
            addProblem(JsonProblems.arraySizeUnmatch(currentPointer(), sourceSize, targetSize));
            return false;
        }

        boolean result = true;
        for (int i = 0; i < targetSize; i++) {
            pointerBuilder.append(i);
            if (!match(source.get(i), target.get(i))) {
                result = false;
            }
            pointerBuilder.remove();
        }

        return result;
    }

    @Override
    protected boolean matchObjects(JsonObject source, JsonObject target) {
        boolean result = true;

        for (String key : source.keySet()) {
            if (target.containsKey(key)) {
                pointerBuilder.append(key);
                if (!match(source.get(key), target.get(key))) {
                    result = false;
                }
                pointerBuilder.remove();
            } else {
                addProblem(JsonProblems.propertyRemoved(currentPointer(), key));
                result = false;
            }
        }

        for (String key : target.keySet()) {
            if (!source.containsKey(key)) {
                pointerBuilder.append(key);
                addProblem(JsonProblems.propertyAdded(currentPointer(), key));
                pointerBuilder.remove();
                result = false;
            }
        }

        return result;
    }

    protected final void addProblem(JsonProblem problem) {
        if (problems == null) {
            problems = new ArrayList<>();
        }
        problems.add(problem);
    }

    private String currentPointer() {
        return pointerBuilder.toString();
    }
}
