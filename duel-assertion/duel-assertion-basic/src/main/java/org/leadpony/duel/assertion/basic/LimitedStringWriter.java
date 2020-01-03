/*
 * Copyright 2020 the original author or authors.
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

import java.io.Writer;

/**
 * A string writer accepting limited number of lines.
 *
 * @author leadpony
 */
class LimitedStringWriter extends Writer {

    private static final String ELLIPSIS = "...";

    private final StringBuilder builder = new StringBuilder();
    private final int maxLines;
    private final String indent;
    private int lines;
    private boolean omitting;

    LimitedStringWriter(int maxLines, String indent) {
        this.maxLines = maxLines;
        this.indent = indent;
        builder.append(indent);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if (omitting) {
            return;
        }
        final int end = off + len;
        for (int i = off; i < end; i++) {
            if (lines >= maxLines) {
                omitting = true;
                break;
            }
            final char c = cbuf[i];
            builder.append(c);
            if (c == '\n') {
                lines++;
                builder.append(indent);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        if (omitting) {
            builder.append(ELLIPSIS);
        }
        return builder.toString();
    }
}
