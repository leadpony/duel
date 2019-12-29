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

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author leadpony
 */
class JsonPointerBuilder {

    private final Deque<String> tokens = new LinkedList<>();

    void append(int index) {
        tokens.addLast(String.valueOf(index));
    }

    void append(String key) {
        tokens.addLast(key);
    }

    void remove() {
        tokens.removeLast();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            builder.append('/');
            int start = 0;
            final int tokenLength = token.length();
            for (int i = 0; i < tokenLength; i++) {
                char c = token.charAt(i);
                if (c == '~' || c == '/') {
                    builder.append(token, start, i)
                           .append('~')
                           .append((c == '~') ? '0' : '1');
                    start = i + 1;
                }
            }
            if (start < tokenLength) {
                builder.append(token, start, tokenLength);
            }
        }
        return builder.toString();
    }
}
