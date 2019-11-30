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
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A wrapper class of {@code javax.json.Json}.
 *
 * @author leadpony
 */
public class Json {

    private static JsonProvider provider;
    private static JsonReaderFactory readerFactory;
    private static JsonBuilderFactory builderFactory;
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

    public static JsonBuilderFactory createBuilderFactory() {
        return getBuilderFactory();
    }

    public static JsonString createValue(String value) {
        return getProvider().createValue(value);
    }

    /* */

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

    private static JsonBuilderFactory getBuilderFactory() {
        if (builderFactory == null) {
            builderFactory = getProvider()
                    .createBuilderFactory(Collections.emptyMap());
        }
        return builderFactory;
    }

    private static Jsonb getBinder() {
        if (jsonb == null) {
            jsonb = createBinder();
        }
        return jsonb;
    }

    private static Jsonb createBinder() {
        JsonbConfig config = new JsonbConfig();
        config.withDeserializers(MAP_DESERIALIZER);
        return JsonbBuilder.newBuilder()
                .withProvider(getProvider())
                .withConfig(config)
                .build();
    }

    private static final MapDeserializer MAP_DESERIALIZER = new MapDeserializer();

    private static class MapDeserializer implements JsonbDeserializer<Map<String, List<String>>> {

        @SuppressWarnings("serial")
        private static final Type LIST_TYPE = new ArrayList<String>() { }.getClass().getGenericSuperclass();

        @Override
        public Map<String, List<String>> deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            Map<String, List<String>> map = new LinkedHashMap<>();
            while (parser.hasNext()) {
                Event event = parser.next();
                if (event == Event.END_OBJECT) {
                    break;
                }
                if (event == Event.KEY_NAME) {
                    String key = parser.getString();
                    event = parser.next();
                    if (event == Event.START_ARRAY) {
                        List<String> values = ctx.deserialize(LIST_TYPE, parser);
                        map.put(key, values);
                    } else {
                        List<String> values = new ArrayList<>();
                        JsonValue value = parser.getValue();
                        if (value.getValueType() == ValueType.STRING) {
                            values.add(((JsonString) value).getString());
                        } else {
                            values.add(value.toString());
                        }
                        map.put(key, values);
                    }
                }
            }
            return map;
        }
    }
}
