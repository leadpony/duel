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

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

/**
 * @author leadpony
 */
interface JsonRenderer {

    String renderJson(JsonValue value);

    static JsonRenderer omittingRenderer(JsonProvider jsonProvider) {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        JsonWriterFactory writerFactory = jsonProvider.createWriterFactory(config);

        return new JsonRenderer() {

            private static final int QUOTE_MAX_LINES = 10;
            private static final String QUOTE_INDENT = "    ";

            @Override
            public String renderJson(JsonValue value) {
                if (value instanceof JsonStructure) {
                    LimitedStringWriter stringWriter = new LimitedStringWriter(QUOTE_MAX_LINES, QUOTE_INDENT);
                    try (JsonWriter writer = writerFactory.createWriter(stringWriter)) {
                        writer.write((JsonStructure) value);
                    }
                    return "\n" + stringWriter.toString();
                } else {
                    return value.toString();
                }
            }
        };
    }
}
