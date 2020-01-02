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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
class ReportingJsonMatcher extends AbstractJsonMatcher {

    private final JsonPointerBuilder pointerBuilder = new JsonPointerBuilder();
    private final SimpleJsonMatcher simpleMatcher = new SimpleJsonMatcher();
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
    protected boolean matchArrays(JsonArray source, JsonArray target, JsonContainerType containerType) {
        assert containerType != null;

        if (source.isEmpty() && target.isEmpty()) {
            return true;
        }

        matchArraySizes(source.size(), target.size());

        switch (containerType) {
        case LIST:
            return matchOrderedArray(source, target);
        case SET:
            return matchUnorderedArray(source, target);
        default:
            assert false;
            return false;
        }
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

    private boolean matchArraySizes(int sourceSize, int targetSize) {
        if (sourceSize < targetSize) {
            addProblem(JsonProblems.arrayTooLong(currentPointer(), sourceSize, targetSize));
            return false;
        } else if (sourceSize > targetSize) {
            addProblem(JsonProblems.arrayTooShort(currentPointer(), sourceSize, targetSize));
            return false;
        }
        return true;
    }

    private boolean matchOrderedArray(JsonArray source, JsonArray target) {

        int length = matchArraysBackward(source, target);
        int i = source.size() - length;
        int j = target.size() - length;

        // perfect match
        if (i == 0 && j == 0) {
            return true;
        }

        LcsTable table = LcsTable.build(source, target, i, j, simpleMatcher);

        List<Runnable> dispatchers = new ArrayList<>();
        while (i > 0 || j > 0) {
            if (table.getMatchResult(i, j)) {
                i--;
                j--;
            } else if (i > 0 && (j == 0 || table.getLength(i - 1, j) > table.getLength(i, j - 1))) {
                pointerBuilder.append(j);
                JsonProblem problem = JsonProblems.listItemRemoved(currentPointer(), source.get(--i), j);
                pointerBuilder.remove();
                dispatchers.add(() -> addProblem(problem));
            } else if (j > 0 && (i == 0 || table.getLength(i - 1, j) < table.getLength(i, j - 1))) {
                pointerBuilder.append(--j);
                JsonProblem problem = JsonProblems.listItemAdded(currentPointer(), target.get(j), j);
                pointerBuilder.remove();
                dispatchers.add(() -> addProblem(problem));
            } else { // i > 0 && j > 0
                final int i0 = --i;
                final int j0 = --j;
                dispatchers.add(() -> {
                    pointerBuilder.append(j0);
                    match(source.get(i0), target.get(j0));
                    pointerBuilder.remove();
                });
            }
        }

        Collections.reverse(dispatchers);
        dispatchers.forEach(Runnable::run);

        return false;
    }

    private int matchArraysBackward(JsonArray source, JsonArray target) {
        int i = source.size();
        int j = target.size();

        while (i > 0 && j > 0 && matchFast(source.get(i - 1), target.get(j - 1))) {
            i--;
            j--;
        }

        return source.size() - i;
    }

    private boolean matchUnorderedArray(JsonArray source, JsonArray target) {

        List<JsonValue> sourceRemains = new LinkedList<>(source);
        List<Integer> targetRemains = new ArrayList<>();

        for (int i = 0; i < target.size(); i++) {
            JsonValue targetValue = target.get(i);
            JsonValue matched = null;
            for (JsonValue sourceValue : sourceRemains) {
                if (matchFast(sourceValue, targetValue)) {
                    matched = sourceValue;
                    break;
                }
            }
            if (matched != null) {
                sourceRemains.remove(matched);
            } else {
                targetRemains.add(i);
            }
        }

        if (sourceRemains.isEmpty() && targetRemains.isEmpty()) {
            return true;
        }

        for (int i : targetRemains) {
            JsonValue value = target.get(i);
            pointerBuilder.append(i);
            addProblem(JsonProblems.setItemAdded(currentPointer(), value));
            pointerBuilder.remove();
        }

        for (JsonValue value : sourceRemains) {
            addProblem(JsonProblems.setItemRemoved(currentPointer(), value));
        }

        return false;
    }

    private boolean matchFast(JsonValue source, JsonValue target) {
        return simpleMatcher.match(source, target);
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
