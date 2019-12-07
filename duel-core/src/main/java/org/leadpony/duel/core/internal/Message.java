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

package org.leadpony.duel.core.internal;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localized messages.
 *
 * @author leadpony
 */
public enum Message {
    PROJECT_NOT_FOUND,
    JSON_ILL_FORMED,
    JSON_UNEXPECTED_VALUE_TYPE,
    INFINTE_PROPERTY_EXPANSION,
    ILLEGAL_PROPERTY_EXPANSION,
    IO_ERROR_FILE,
    IO_ERROR_DIRECTORY,
    BAD_ENDPOINT_URL,
    NETWORK_FAILURE,
    NETWORK_OPERATION_INTERRUPTED,
    CYCLIC_PROPERTY_EXPANSION;

    private static final String BASE_NAME =
            Message.class.getPackage().getName() + ".messages";

    public String format(Object... arguments) {
        return MessageFormat.format(asString(), arguments);
    }

    /**
     * Returns this message as a string.
     *
     * @return this message as a string.
     */
    public String asString() {
        return getBundle().getString(name());
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME,
                Locale.getDefault(), getClass().getClassLoader());
    }
}
