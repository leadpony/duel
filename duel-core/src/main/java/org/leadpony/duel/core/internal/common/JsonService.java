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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.json.JsonBuilderFactory;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

/**
 * @author leadpony
 *
 */
public enum JsonService {
    SINGLETON;

    private final JsonProvider provider;
    private final JsonReaderFactory readerFactory;
    private final JsonBuilderFactory builderFactory;

    JsonService() {
        this.provider = JsonProvider.provider();
        this.readerFactory = this.provider.createReaderFactory(Collections.emptyMap());
        this.builderFactory = this.provider.createBuilderFactory(Collections.emptyMap());
    }

    public JsonValue readFrom(Path path) throws IOException {
        return readFrom(Files.newInputStream(path));
    }

    public JsonValue readFrom(byte[] byteArray, Charset charset) {
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        return readFrom(in, charset);
    }

    public JsonValue readFrom(InputStream in) {
        try (JsonReader reader = readerFactory.createReader(in)) {
            return reader.readValue();
        }
    }

    public JsonValue readFrom(InputStream in, Charset charset) {
        try (JsonReader reader = readerFactory.createReader(in, charset)) {
            return reader.readValue();
        }
    }

    public JsonBuilderFactory createBuilderFactory() {
        return builderFactory;
    }

    public JsonString createValue(String value) {
        return provider.createValue(value);
    }
}
