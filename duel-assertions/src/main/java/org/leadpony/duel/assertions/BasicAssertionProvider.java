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

package org.leadpony.duel.assertions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
    public void provideAssertions(JsonObject config, Collection<Assertion> assertions) {
        new AssertionCollector(assertions).collect(config);
    }

    /**
     * @author leadpony
     */
    final class AssertionCollector {

        private final Collection<Assertion> assertions;

        AssertionCollector(Collection<Assertion> assertions) {
            this.assertions = assertions;
        }

        void collect(JsonObject config) {
            addStatusAssertion(config);
            addHeaderAssertions(config);
            addBodyAssertions(config);
        }

        private void add(Assertion validator) {
            assertions.add(validator);
        }

        private void addStatusAssertion(JsonObject config) {
            if (!config.containsKey("status")) {
                return;
            }
            JsonValue value = config.get("status");
            switch (value.getValueType()) {
            case NUMBER:
                JsonNumber number = (JsonNumber) value;
                add(new SimpleStatusAssertion(number.intValue()));
                break;
            case STRING:
                JsonString string = (JsonString) value;
                int status = Integer.valueOf(string.getString());
                add(new SimpleStatusAssertion(status));
                break;
            default:
                break;
            }
        }

        private void addHeaderAssertions(JsonObject config) {
            if (config.containsKey("header")) {
                JsonObject header = config.getJsonObject("header");
                addHeaderFieldAssertions(header);
                addRequiredHeaderFieldAssertion(header);
            }
        }

        private void addRequiredHeaderFieldAssertion(JsonObject config) {
            if (!config.containsKey("required")) {
                return;
            }
            Stream<String> names = config.getJsonArray("required")
                    .stream()
                    .filter(v -> v.getValueType() == ValueType.STRING)
                    .map(v -> (JsonString) v)
                    .map(JsonString::getString);
            add(new RequiredHeaderFieldAssertion(names));
        }

        private void addHeaderFieldAssertions(JsonObject config) {
            if (!config.containsKey("fields")) {
                return;
            }
            JsonObject fields = config.getJsonObject("fields");
            fields.forEach((name, value) -> {
                switch (value.getValueType()) {
                case STRING:
                    JsonString string = (JsonString) value;
                    add(new HeaderFieldAssertion(name, string.getString()));
                    break;
                default:
                    break;
                }
            });
        }

        private void addBodyAssertions(JsonObject config) {
            if (!config.containsKey("body")) {
                return;
            }
            JsonObject body = config.getJsonObject("body");
            if (body.containsKey("data")) {
                add(new JsonBodyAssertion(body.get("data"),
                        jsonProvider,
                        jsonWriterFactory));
            }
        }
    }
}
