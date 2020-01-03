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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriterFactory;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

/**
 * Predefined messages.
 *
 * @author leadpony
 */
final class Message {

    private static final String BUNDLE_BASE_NAME =
            Message.class.getPackage().getName() + ".messages";

    private static final JsonWriterFactory JSON_WRITER_FACTORY = createJsonWriterFactory();
    private static final int QUOTE_MAX_LINES = 10;
    private static final String QUOTE_INDENT = "    ";

    private Message() {
    }

    public static String byKey(String key) {
        return getBundle().getString(key);
    }

    public static String byKey(Enum<?> enumerator) {
        return byKey(enumerator.name());
    }

    public static String thatStatusCodeDoesNotMuch(int expected, int actual) {
        return format("StatusCodeDoesNotMuch", expected, actual);
    }

    public static String thatHeaderFieldDoesNotMatch(String name, String expected, String actual) {
        return format("HeaderFieldDoesNotMatch", name, expected, actual);
    }

    public static String thatHeaderFieldsAreMissing(Set<String> name) {
        return format("HeaderFieldsAreMissing", name);
    }

    public static String thatJsonBodyDoesNotMatch(String detail) {
        return format("JsonBodyDoesNotMatch", detail);
    }

    /* Messages for JSON problem */

    public static String thatJsonValueTypeDoesNotMatch(ValueType expected, ValueType actual) {
        return format("JsonValueTypeDoesNotMatch", byKey(expected), byKey(actual));
    }

    public static String thatJsonValueIsReplaced(JsonValue expected, JsonValue actual) {
        assert expected.getValueType() == actual.getValueType();
        return format("JsonValueIsReplaced", expected, actual);
    }

    public static String thatArrayIsLongerThanExpected(int expectedSize, int actualSize) {
        return format("ArrayIsLongerThanExpected", expectedSize, actualSize);
    }

    public static String thatArrayIsShorterThanExpected(int expectedSize, int actualSize) {
        return format("ArrayIsShorterThanExpected", expectedSize, actualSize);
    }

    public static String thatExpectedItemIsMissingInList(JsonValue value) {
        return format("ExpectedItemIsMissingInList", renderJson(value));
    }

    public static String thatUnexpectedItemIsFoundInList(JsonValue value) {
        return format("UnexpectedItemIsFoundInList", renderJson(value));
    }

    public static String thatExpectedItemIsMissingInSet(JsonValue value) {
        return format("ExpectedItemIsMissingInSet", renderJson(value));
    }

    public static String thatUnexpectedItemIsFoundInSet(JsonValue value) {
        return format("UnexpectedItemIsFoundInSet", renderJson(value));
    }

    public static String thatExpectedPropertyIsMissing(String propertyName) {
        return format("ExpectedPropertyIsMissing", propertyName);
    }

    public static String thatUnexpectedPropertyIsFound(String propertyName) {
        return format("UnexpectedPropertyIsFound", propertyName);
    }

    private static String format(String key, Object... arguments) {
        return MessageFormat.format(byKey(key), arguments);
    }

    private static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME,
                Locale.getDefault(),
                Message.class.getClassLoader());
    }

    private static String renderJson(JsonValue value) {
        if (value instanceof JsonStructure) {
            LimitedStringWriter stringWriter = new LimitedStringWriter(QUOTE_MAX_LINES, QUOTE_INDENT);
            try (JsonWriter writer = JSON_WRITER_FACTORY.createWriter(stringWriter)) {
                writer.write((JsonStructure) value);
            }
            return "\n" + stringWriter.toString();
        } else {
            return value.toString();
        }
    }

    private static JsonWriterFactory createJsonWriterFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        return JsonProviderCache.get().createWriterFactory(config);
    }
}
