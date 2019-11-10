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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.AssertionProvider;

/**
 * @author leadpony
 */
public class BasicAssertionProvider implements AssertionProvider {

    private final JsonProvider jsonProvider;
    private final JsonWriterFactory jsonWriterFactory;

    private static final Map<String, ?> WRITER_CONFIG;

    static {
        Map<String, Object> map = new HashMap<>();
        map.put(JsonGenerator.PRETTY_PRINTING, true);
        WRITER_CONFIG = Collections.unmodifiableMap(map);
    }

    public BasicAssertionProvider() {
        this.jsonProvider = JsonProvider.provider();
        this.jsonWriterFactory = this.jsonProvider.createWriterFactory(WRITER_CONFIG);
    }

    @Override
    public void provideAssertions(Map<String, JsonValue> config, Collection<Assertion> assertions) {

        if (config.containsKey("status")) {
            addStatusAssertion(config.get("status"), assertions);
        }

        if (config.containsKey("header")) {
            JsonValue value = config.get("header");
            if (value.getValueType() == ValueType.OBJECT) {
                addHeaderFieldAssertions(value.asJsonObject(), assertions);
            }
        }

        if (config.containsKey("required")) {
            JsonValue value = config.get("required");
            if (value.getValueType() == ValueType.ARRAY) {
                addRequiredHeaderFieldAssertion(value.asJsonArray(), assertions);
            }
        }

        if (config.containsKey("body")) {
            JsonValue value = config.get("body");
            if (value.getValueType() == ValueType.OBJECT) {
                JsonObject object = value.asJsonObject();
                if (object.containsKey("data")) {
                    addJsonBodyAssertion(object.get("data"), assertions);
                }
            }
        }
    }

    private void addStatusAssertion(JsonValue config, Collection<Assertion> assertions) {
        switch (config.getValueType()) {
        case NUMBER:
            JsonNumber number = (JsonNumber) config;
            assertions.add(new SimpleStatusAssertion(number.intValue()));
            break;
        case STRING:
            JsonString string = (JsonString) config;
            int status = Integer.valueOf(string.getString());
            assertions.add(new SimpleStatusAssertion(status));
            break;
        default:
            break;
        }
    }

    private void addHeaderFieldAssertions(JsonObject config, Collection<Assertion> assertions) {
        config.forEach((name, value) -> {
            switch (value.getValueType()) {
            case STRING:
                JsonString string = (JsonString) value;
                assertions.add(new HeaderFieldAssertion(name, string.getString()));
                break;
            default:
                break;
            }
        });
    }

    private void addRequiredHeaderFieldAssertion(JsonArray config, Collection<Assertion> assertions) {
        Stream<String> names = config.stream().filter(v -> v.getValueType() == ValueType.STRING)
                .map(v -> (JsonString) v).map(JsonString::getString);
        assertions.add(new RequiredHeaderAssertion(names));
    }

    private void addJsonBodyAssertion(JsonValue config, Collection<Assertion> assertions) {
        assertions.add(new JsonBodyAssertion(config, jsonProvider, jsonWriterFactory));
    }
}
