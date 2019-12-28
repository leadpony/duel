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

/**
 * A utility type for operating on JSON pointers.
 *
 * @author leadpony
 */
final class JsonPointers {

    private JsonPointers() {
    }

    static String append(String base, int index) {
        return new StringBuilder(base)
                .append('/')
                .append(index)
                .toString();
    }

    static String append(String base, String key) {
        final int keyLength = key.length();
        StringBuilder builder = new StringBuilder(base.length() + keyLength + 1);
        builder.append(base).append('/');

        int nextStart = 0;
        for (int i = 0; i < keyLength; i++) {
            final char c = key.charAt(i);
            if (c == '~' || c == '/') {
                builder.append(key, nextStart, i)
                       .append('~')
                       .append((c == '~') ? '0' : '1');
                nextStart = i + 1;
            }
        }

        if (nextStart < keyLength) {
            builder.append(key, nextStart, keyLength);
        }

        return builder.toString();
    }
}
