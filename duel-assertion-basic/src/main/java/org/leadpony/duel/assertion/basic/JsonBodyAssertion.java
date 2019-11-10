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

import java.io.StringWriter;
import java.net.http.HttpResponse;

import javax.json.JsonArray;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class JsonBodyAssertion extends AbstractAssertion {

    private final JsonValue expected;
    private final JsonProvider jsonProvider;
    private final JsonWriterFactory writerFactory;

    JsonBodyAssertion(JsonValue expected, JsonProvider jsonProvider, JsonWriterFactory writerFactory) {
        this.expected = expected;
        this.jsonProvider = jsonProvider;
        this.writerFactory = writerFactory;
    }

    @Override
    public void assertOn(HttpResponse<ResponseBody> response) {
        JsonValue actual = response.body().asJson();
        testValueType(actual.getValueType());
        testValue(actual);
    }

    private void testValueType(ValueType actual) {
        ValueType expected = this.expected.getValueType();
        if (expected != actual) {
            String message = Message.JSON_BODY_TYPE_MISMATCH.format(expected, actual);
            fail(message, expected, actual);
        }
    }

    private void testValue(JsonValue actual) {
        ValueType type = actual.getValueType();
        if (type == ValueType.ARRAY || type == ValueType.OBJECT) {
            testValue((JsonStructure) actual);
            return;
        }
    }

    private void testValue(JsonStructure actual) {
        JsonStructure expected = (JsonStructure) this.expected;
        JsonPatch diff = jsonProvider.createDiff(actual, expected);
        JsonArray array = diff.toJsonArray();
        if (!array.isEmpty()) {
            fail(buildMessage(array), expected, actual);
        }
    }

    private String buildMessage(JsonArray diff) {
        return Message.JSON_BODY_STRUCTURE_NOT_EQUAL
                .format(stringifyDiff(diff));
    }

    private String stringifyDiff(JsonArray diff) {
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = writerFactory.createWriter(stringWriter)) {
            writer.writeArray(diff);
        }
        return stringWriter.toString();
    }
}
