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

package org.leadpony.duel.core.internal.common;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * An escaping object of URL components.
 *
 * @author leadpony
 */
public enum Escaper implements Function<String, String> {
    QUERY_ESCAPER {
        @Override
        protected boolean requiresEncoding(char c) {
            if (c == '&') {
                return true;
            }
            if (isUnreserved(c) || isSubdelim(c)
                || c == '/' || c == '?' || c == ':' || c == '@') {
                return false;
            }
            return true;
        }
    };

    @Override
    public String apply(String string) {
        requireNonNull(string, "string must not be null.");
        StringBuilder builder = new StringBuilder();
        final int length = string.length();
        int nextStart = 0;
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (requiresEncoding(c)) {
                builder.append(string.substring(nextStart, i));
                encode(builder, c);
                nextStart = i + 1;
            }
        }
        if (nextStart < length) {
            builder.append(string.substring(nextStart));
        }
        return builder.toString();
    }

    protected abstract boolean requiresEncoding(char c);

    protected static boolean isUnreserved(char c) {
        if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
            return true;
        }
        if ('0' <= c && c <= '9') {
            return true;
        }
        return c == '-' || c == '.' || c == '_' || c == '~';
    }

    protected static boolean isSubdelim(char c) {
        return c == '!' || c == '$'
            || c == '&' || c == '\''
            || c == '(' || c == ')'
            || c == '*' || c == '+'
            || c == ',' || c == ';'
            || c == '=';
    }

    private static void encode(StringBuilder builder, char c) {
        byte[] bytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
        builder.append('%');
        for (byte b : bytes) {
            builder.append(toHexdigit((b & 0xf0) >> 4))
                   .append(toHexdigit(b & 0x0f));
        }
    }

    private static char toHexdigit(int n) {
        if (n < 10) {
            return (char) ('0' + n);
        } else if (n < 16) {
            return (char) ('A' + (n - 10));
        }
        throw new IllegalArgumentException();
    }
}
