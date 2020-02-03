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

package org.leadpony.duel.core.internal;

import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.json.JsonException;
import javax.json.JsonValue.ValueType;

/**
 * Localized messages.
 *
 * @author leadpony
 */
public final class Message {

    private static final String BUNDLE_BASE_NAME =
            Message.class.getPackage().getName() + ".messages";

    private Message() {
    }

    public static String thatProjectIsNotFound(Path path) {
        return format("ProjectIsNotFound", path);
    }

    public static String thatLoadingProjectFailed(int errors) {
        return format("LoadingProjectFailed", errors);
    }

    public static String thatJsonIsIllFormed(JsonException e) {
        return format("JsonIsIllFormed", e.getMessage());
    }

    public static String thatJsonValueTypeMismatched(ValueType actual) {
        return format("JsonValueTypeMismatched", actual);
    }

    public static String thatPropertyExpansionLoopsInfinite(String property, Exception e) {
        return format("PropertyExpansionLoopsInfinite", property, e.getMessage());
    }

    public static String thatPropertyIsIllegal(Exception e) {
        return format("PropertyIsIllegal", e.getMessage());
    }

    public static String thatReadingFileFailed(Path path) {
        return format("ReadingFileFailed", path);
    }

    public static String thatReadingDirectoryFailed(Path path) {
        return format("ReadingDirectoryFailed", path);
    }

    public static String thatEndpointUrlIsInValid() {
        return format("EndpointUrlIsInValid");
    }

    public static String thatNetworkConnectionFailed(URI remote) {
        return format("NetworkConnectionFailed", remote);
    }

    public static String thatNetworkIsInterrupted() {
        return format("NetworkIsInterrupted");
    }

    /**
     * Formats the message without any arguments.
     *
     * @param key the key of the message.
     * @return the formatted message.
     */
    private static String format(String key) {
        return getBundle().getString(key);
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
