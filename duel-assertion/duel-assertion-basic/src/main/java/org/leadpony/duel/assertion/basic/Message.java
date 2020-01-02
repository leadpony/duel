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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * Predefined messages.
 *
 * @author leadpony
 */
final class Message {

    private static final String BUNDLE_BASE_NAME =
            Message.class.getPackage().getName() + ".messages";

    private Message() {
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
        return format("JsonValueTypeDoesNotMatch", nameOf(expected), nameOf(actual));
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

    public static String thatExpectedListItemIsMissing(JsonValue value, int index) {
        return format("ExpectedListItemIsMissing", index);
    }

    public static String thatUnexpectedListItemIsFound(JsonValue value, int index) {
        return format("UnexpectedListItemIsFound", index);
    }

    public static String thatExpectedSetItemIsMissing(JsonValue value) {
        return format("ExpectedSetItemIsMissing", value);
    }

    public static String thatUnexpectedSetItemIsFound(JsonValue value) {
        return format("UnexpectedSetItemIsFound", value);
    }

    public static String thatExpectedPropertyIsMissing(String propertyName) {
        return format("ExpectedPropertyIsMissing", propertyName);
    }

    public static String thatUnexpectedPropertyIsFound(String propertyName) {
        return format("UnexpectedPropertyIsFound", propertyName);
    }

    private static String nameOf(Enum<?> enumerator) {
        return enumerator.name().toLowerCase();
    }

    private static String format(String key, Object... arguments) {
        String pattern = getBundle().getString(key);
        return MessageFormat.format(pattern, arguments);
    }

    private static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME,
                Locale.getDefault(),
                Message.class.getClassLoader());
    }
}
