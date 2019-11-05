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

package org.leadpony.duel.core.spi;

import java.util.Optional;

import javax.json.JsonValue;

/**
 * @author leadpony
 */
public interface ResponseBody {

    boolean isEmpty();

    Optional<MediaType> getMediaType();

    /**
     * Returns this response body as a byte array.
     * @return the byte array.
     */
    byte[] asByteArray();

    /**
     * Returns this response body as a JSON value.
     * @return the JSON value.
     * @throws JsonException if the body is not a JSON.
     */
    JsonValue asJson();
}
