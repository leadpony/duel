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

package org.leadpony.duel.core.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A media type such as a "application/json".
 *
 * @author leadpony
 */
public interface MediaType {

    String getType();

    String getSubtype();

    Optional<String> getTree();

    Optional<String> getSuffix();

    Map<String, String> getParameters();

    /**
     * Creates a new instance by parsing the supplied string.
     *
     * @param value the media type string.
     * @return the newly created instance of {@code MediaType}.
     */
    static MediaType valueOf(String value) {
        Objects.requireNonNull(value, "value must not be null.");
        return new MediaTypeParser(value).parse();
    }
}
