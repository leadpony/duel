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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.spi.JsonProvider;

/**
 * @author leadpony
 */
public class Json {

    private static JsonProvider provider;
    private static JsonReaderFactory readerFactory;
    private static Jsonb jsonb;

    public static JsonValue readFrom(Path path) throws IOException {
        return readFrom(Files.newInputStream(path));
    }

    public static JsonValue readFrom(byte[] byteArray, Charset charset) {
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        return readFrom(in, charset);
    }

    public static JsonValue readFrom(InputStream in) {
        try (JsonReader reader = getReaderFactory().createReader(in)) {
            return reader.readValue();
        }
    }

    public static JsonValue readFrom(InputStream in, Charset charset) {
        try (JsonReader reader = getReaderFactory().createReader(in, charset)) {
            return reader.readValue();
        }
    }

    public static <T> T readFrom(Path path, Class<T> type) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return readFrom(in, type);
        }
    }

    public static <T> T readFrom(InputStream in, Class<T> type) {
        return  getBinder().fromJson(in, type);
    }

    private static JsonProvider getProvider() {
        if (provider == null) {
            provider = JsonProvider.provider();
        }
        return provider;
    }

    private static JsonReaderFactory getReaderFactory() {
        if (readerFactory == null) {
            readerFactory = getProvider()
                    .createReaderFactory(Collections.emptyMap());
        }
        return readerFactory;
    }

    private static Jsonb getBinder() {
        if (jsonb == null) {
            jsonb = createBinder();
        }
        return jsonb;
    }

    private static Jsonb createBinder() {
        JsonbConfig config = new JsonbConfig();
        config.withAdapters(STRING_LIST_ADAPTER);
        return JsonbBuilder.newBuilder()
                .withProvider(getProvider())
                .withConfig(config)
                .build();
    }

    private static final JsonbAdapter<List<String>, String> STRING_LIST_ADAPTER = new JsonbAdapter<>() {

        @Override
        public String adaptToJson(List<String> obj) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> adaptFromJson(String obj) throws Exception {
            List<String> list = new ArrayList<>();
            list.add(obj);
            return list;
        }
    };
}
