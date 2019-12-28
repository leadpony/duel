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

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.assertion.basic.JsonProblem.ProblemType;

/**
 * A utility type for providing various kinds of JSON problems.
 *
 * @author leadpony
 */
class JsonProblems {

    static JsonProblem typeMismatch(String pointer, ValueType expected, ValueType actual) {
        return new AbstractJsonProblem(ProblemType.TYPE_MISMATCH, pointer) {
            @Override
            public String getDescription() {
                return Message.thatJsonValueTypeDoesNotMatch(expected, actual);
            }
        };
    }

    static JsonProblem replaced(String pointer, JsonValue expected, JsonValue actual) {
        return new AbstractJsonProblem(ProblemType.REPLACED, pointer) {
            @Override
            public String getDescription() {
                return Message.thatJsonValueIsReplaced(expected, actual);
            }
        };
    }

    static JsonProblem propertyAdded(String pointer, String propertyName) {
        return new AbstractJsonProblem(ProblemType.PROPERTY_ADDED, pointer) {
            @Override
            public String getDescription() {
                return Message.thatRedundantPropertyExists(propertyName);
            }
        };
    }

    static JsonProblem propertyRemoved(String pointer, String propertyName) {
        return new AbstractJsonProblem(ProblemType.PROPERTY_REMOVED, pointer) {
            @Override
            public String getDescription() {
                return Message.thatRequiredPropertyIsMissing(propertyName);
            }
        };
    }

    static JsonProblem arraySizeUnmatch(String pointer, int expectedSize, int actualSize) {
        return new AbstractJsonProblem(ProblemType.ARRAY_SIZE_UNMATCH, pointer) {
            @Override
            public String getDescription() {
                return Message.thatArraySizeDoesNotMatch(expectedSize, actualSize);
            }
        };
    }

    private abstract static class AbstractJsonProblem implements JsonProblem {

        private final ProblemType type;
        private final String pointer;

        protected AbstractJsonProblem(ProblemType type, String pointer) {
            this.type = type;
            this.pointer = pointer;
        }

        @Override
        public ProblemType getProblemType() {
            return type;
        }

        @Override
        public String getPointer() {
            return pointer;
        }
    }
}
