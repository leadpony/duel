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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author leadpony
 */
enum Message {
    STATUS_CODE_NOT_EQUAL,
    HEADER_FIELD_NOT_EQUAL,
    HEADER_FIELD_MISSING,
    JSON_BODY_TYPE_MISMATCH,
    JSON_BODY_STRUCTURE_NOT_EQUAL;

    private static final String BASE_NAME =
            Message.class.getPackage().getName() + ".messages";

    public String format(Object... arguments) {
        return MessageFormat.format(asString(), arguments);
    }

    public String asString() {
        return getBundle().getString(name());
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME,
                Locale.getDefault(), getClass().getClassLoader());
    }
}
