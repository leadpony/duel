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

package org.leadpony.duel.core.internal.project;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.json.JsonValue;

import org.leadpony.duel.core.internal.common.Json;
import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class ResponseBodyImpl implements ResponseBody {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final byte[] byteArray;
    private final Optional<MediaType> mediaType;

    private JsonValue cachedJson;

    ResponseBodyImpl(byte[] byteArray, Optional<MediaType> mediaType) {
        this.byteArray = byteArray;
        this.mediaType = mediaType;
    }

    @Override
    public boolean isEmpty() {
        return byteArray.length == 0;
    }

    @Override
    public Optional<MediaType> getMediaType() {
        return mediaType;
    }

    @Override
    public byte[] asByteArray() {
        return byteArray;
    }

    @Override
    public JsonValue asJson() {
        if (cachedJson == null) {
            cachedJson = getJsonValue();
        }
        return cachedJson;
    }

    private JsonValue getJsonValue() {
        return Json.readFrom(byteArray, guessEncoding());
    }

    private Charset guessEncoding() {
        return mediaType
                .map(type -> type.getCharset(DEFAULT_CHARSET))
                .orElse(DEFAULT_CHARSET);
    }
}
