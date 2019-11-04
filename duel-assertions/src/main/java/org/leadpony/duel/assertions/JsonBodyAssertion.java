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

package org.leadpony.duel.assertions;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.api.MediaType;

/**
 * @author leadpony
 */
class JsonBodyAssertion extends AbstractAssertion {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final JsonValue expected;
    private final JsonProvider jsonProvider;
    private final JsonReaderFactory readerFactory;
    private final JsonWriterFactory writerFactory;

    JsonBodyAssertion(JsonValue expected, JsonProvider jsonProvider, JsonReaderFactory readerFactory,
            JsonWriterFactory writerFactory) {
        this.expected = expected;
        this.jsonProvider = jsonProvider;
        this.readerFactory = readerFactory;
        this.writerFactory = writerFactory;
    }

    @Override
    public void doAssert(HttpResponse<byte[]> response, Optional<MediaType> mediaType) {
        Charset encoding = guessCharset(mediaType);
        JsonValue actual = mapToJson(response.body(), encoding);
        testValueType(actual.getValueType());
        testValue(actual);
    }

    private JsonValue mapToJson(byte[] body, Charset encoding) {
        ByteArrayInputStream in = new ByteArrayInputStream(body);
        try (JsonReader reader = readerFactory.createReader(in, encoding)) {
            return reader.readValue();
        }
    }

    private static Charset guessCharset(Optional<MediaType> mediaType) {
        return mediaType.map(type -> {
            Map<String, String> parameters = type.getParameters();
            if (parameters.containsKey("charset")) {
                return Charset.forName(parameters.get("charset"));
            } else {
                return DEFAULT_CHARSET;
            }
        }).orElse(DEFAULT_CHARSET);
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
                .format(diffToString(diff));
    }

    private String diffToString(JsonArray diff) {
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = writerFactory.createWriter(stringWriter)) {
            writer.writeArray(diff);
        }
        return stringWriter.toString();
    }
}
