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

package org.leadpony.duel.core.internal.node;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;

import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class ResponseBodyImpl implements ResponseBody {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final byte[] byteArray;
    private final Optional<MediaType> mediaType;
    private final TestExecutionContext context;

    private JsonValue cachedJson;

    ResponseBodyImpl(byte[] byteArray, Optional<MediaType> mediaType, TestExecutionContext context) {
        this.byteArray = byteArray;
        this.mediaType = mediaType;
        this.context = context;
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
        JsonReaderFactory readerFactory = context.getJsonReaderFactory();
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        try (JsonReader reader = readerFactory.createReader(in, guessEncoding())) {
            return reader.readValue();
        }
    }

    private Charset guessEncoding() {
        return mediaType
                .map(type -> type.getCharset(DEFAULT_CHARSET))
                .orElse(DEFAULT_CHARSET);
    }
}
