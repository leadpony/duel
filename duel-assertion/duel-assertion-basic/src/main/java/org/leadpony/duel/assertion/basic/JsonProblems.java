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

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

            @Override
            protected void populateJson(JsonObjectBuilder builder) {
                builder.add("expected", expected.name().toLowerCase());
                builder.add("actual", actual.name().toLowerCase());
            }
        };
    }

    static JsonProblem replaced(String pointer, JsonValue expected, JsonValue actual) {
        return new AbstractJsonProblem(ProblemType.REPLACED, pointer) {
            @Override
            public String getDescription() {
                return Message.thatJsonValueIsReplaced(expected, actual);
            }

            @Override
            protected void populateJson(JsonObjectBuilder builder) {
                builder.add("expected", expected);
                builder.add("actual", actual);
            }
        };
    }

    static JsonProblem arraySizeUnmatch(String pointer, int expectedSize, int actualSize) {
        return new AbstractJsonProblem(ProblemType.ARRAY_SIZE_UNMATCH, pointer) {
            @Override
            public String getDescription() {
                return Message.thatArraySizeDoesNotMatch(expectedSize, actualSize);
            }

            @Override
            protected void populateJson(JsonObjectBuilder builder) {
                builder.add("expected", expectedSize);
                builder.add("actual", actualSize);
            }
        };
    }

    static JsonProblem itemAdded(String pointer, int index, JsonValue value) {
        return new ItemJsonProblem(ProblemType.ITEM_ADDED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatRedundantItemExists(index);
            }
        };
    }

    static JsonProblem itemRemoved(String pointer, int index, JsonValue value) {
        return new ItemJsonProblem(ProblemType.ITEM_REMOVED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatRequiredItemIsMissing(index);
            }
        };
    }

    static JsonProblem propertyAdded(String pointer, String propertyName) {
        return new PropertyJsonProblem(ProblemType.PROPERTY_ADDED, pointer, propertyName) {
            @Override
            public String getDescription() {
                return Message.thatRedundantPropertyExists(propertyName);
            }
        };
    }

    static JsonProblem propertyRemoved(String pointer, String propertyName) {
        return new PropertyJsonProblem(ProblemType.PROPERTY_REMOVED, pointer, propertyName) {
            @Override
            public String getDescription() {
                return Message.thatRequiredPropertyIsMissing(propertyName);
            }
        };
    }

    private static final JsonBuilderFactory JSON_BUILDER_FACTORY = Json.createBuilderFactory(null);

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

        @Override
        public JsonObject toJson() {
            JsonObjectBuilder builder = JSON_BUILDER_FACTORY.createObjectBuilder();
            builder.add("type", type.name());
            builder.add("path", getPointer());
            populateJson(builder);
            return builder.build();
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append('[')
                    .append(getPointer())
                    .append(']')
                    .append(' ')
                    .append(getDescription())
                    .toString();
        }

        protected void populateJson(JsonObjectBuilder builder) {
        }
    }

    private abstract static class ItemJsonProblem extends AbstractJsonProblem {

        private final JsonValue value;

        protected ItemJsonProblem(ProblemType type, String pointer, JsonValue value) {
            super(type, pointer);
            this.value = value;
        }

        @Override
        protected void populateJson(JsonObjectBuilder builder) {
            builder.add("value", value);
        }
    }

    private abstract static class PropertyJsonProblem extends AbstractJsonProblem {

        private final String propertyName;

        protected PropertyJsonProblem(ProblemType type, String pointer, String propertyName) {
            super(type, pointer);
            this.propertyName = propertyName;
        }

        @Override
        protected void populateJson(JsonObjectBuilder builder) {
            builder.add("propertyName", propertyName);
        }
    }
}
