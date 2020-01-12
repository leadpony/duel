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

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.assertion.basic.JsonProblem.ProblemType;

/**
 * A utility type for providing various kinds of JSON problems.
 *
 * @author leadpony
 */
class JsonProblemFactory {

    private final JsonBuilderFactory jsonBuilderFactory;
    private final JsonRenderer jsonRenderer;

    JsonProblemFactory(JsonProvider jsonProvider) {
        this.jsonBuilderFactory = jsonProvider.createBuilderFactory(null);
        this.jsonRenderer = JsonRenderer.omittingRenderer(jsonProvider);
    }

    JsonProblem typeMismatch(String pointer, ValueType expected, ValueType actual) {
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

    JsonProblem replaced(String pointer, JsonValue expected, JsonValue actual) {
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

    JsonProblem arrayTooLong(String pointer, int expected, int actual) {
        return new ArraySizeProblem(ProblemType.ARRAY_TOO_LONG, pointer, expected, actual) {
            @Override
            public String getDescription() {
                return Message.thatArrayIsLongerThanExpected(expected, actual);
            }
        };
    }

    JsonProblem arrayTooShort(String pointer, int expected, int actual) {
        return new ArraySizeProblem(ProblemType.ARRAY_TOO_SHORT, pointer, expected, actual) {
            @Override
            public String getDescription() {
                return Message.thatArrayIsShorterThanExpected(expected, actual);
            }
        };
    }

    JsonProblem listItemAdded(String pointer, JsonValue value, int index) {
        return new ArrayProblem(ProblemType.LIST_ITEM_ADDED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatUnexpectedItemIsFoundInList(renderJson(value));
            }
        };
    }

    JsonProblem listItemRemoved(String pointer, JsonValue value, int index) {
        return new ArrayProblem(ProblemType.LIST_ITEM_REMOVED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatExpectedItemIsMissingInList(renderJson(value));
            }
        };
    }

    JsonProblem setItemAdded(String pointer, JsonValue value) {
        return new ArrayProblem(ProblemType.SET_ITEM_ADDED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatUnexpectedItemIsFoundInSet(renderJson(value));
            }
        };
    }

    JsonProblem setItemRemoved(String pointer, JsonValue value) {
        return new ArrayProblem(ProblemType.SET_ITEM_REMOVED, pointer, value) {
            @Override
            public String getDescription() {
                return Message.thatExpectedItemIsMissingInSet(renderJson(value));
            }
        };
    }

    JsonProblem propertyAdded(String pointer, String propertyName) {
        return new PropertyJsonProblem(ProblemType.PROPERTY_ADDED, pointer, propertyName) {
            @Override
            public String getDescription() {
                return Message.thatUnexpectedPropertyIsFound(propertyName);
            }
        };
    }

    JsonProblem propertyRemoved(String pointer, String propertyName) {
        return new PropertyJsonProblem(ProblemType.PROPERTY_REMOVED, pointer, propertyName) {
            @Override
            public String getDescription() {
                return Message.thatExpectedPropertyIsMissing(propertyName);
            }
        };
    }

    private String renderJson(JsonValue value) {
        return this.jsonRenderer.renderJson(value);
    }

    private abstract class AbstractJsonProblem implements JsonProblem {

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
            JsonObjectBuilder builder = jsonBuilderFactory.createObjectBuilder();
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

    private abstract class ArraySizeProblem extends AbstractJsonProblem {

        private final int expected;
        private final int actual;

        protected ArraySizeProblem(ProblemType type, String pointer, int expected, int actual) {
            super(type, pointer);
            this.expected = expected;
            this.actual = actual;
        }

        @Override
        protected void populateJson(JsonObjectBuilder builder) {
            builder.add("expected", expected);
            builder.add("actual", actual);
        }
    }

    private abstract class ArrayProblem extends AbstractJsonProblem {

        private final JsonValue value;

        protected ArrayProblem(ProblemType type, String pointer, JsonValue value) {
            super(type, pointer);
            this.value = value;
        }

        @Override
        protected void populateJson(JsonObjectBuilder builder) {
            builder.add("value", value);
        }
    }

    private abstract class PropertyJsonProblem extends AbstractJsonProblem {

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
