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

package org.leadpony.duel.assertion.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.api.ExecutionContext;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.AssertionProvider;

/**
 * A provider of basic assertions.
 *
 * @author leadpony
 */
public class BasicAssertionProvider implements AssertionProvider {

    private JsonProvider jsonProvider;
    private JsonProblemFactory jsonProblemFactory;

    @Override
    public void initializeProvider(ExecutionContext context) {
        this.jsonProvider = context.getJsonProvider();
        this.jsonProblemFactory = new JsonProblemFactory(this.jsonProvider);
    }

    @Override
    public Stream<Assertion> provideAssertions(CaseNode node) {
        List<Assertion> assertions = new ArrayList<>();
        JsonObject config = node.getEffectiveConfigurarionAsJson();
        if (config.containsKey("response")) {
            JsonValue response = config.get("response");
            if (response.getValueType() == ValueType.OBJECT) {
                addBasicAssertions(node, response.asJsonObject(), assertions);
            }
        }
        return assertions.stream();
    }

    private void addBasicAssertions(CaseNode node, JsonObject response, Collection<Assertion> assertions) {
        for (String key : response.keySet()) {
            JsonValue value = response.get(key);
            switch (key) {
            case "status":
                addStatusAssertion(value, assertions);
                break;
            case "header":
                if (value.getValueType() == ValueType.OBJECT) {
                    addHeaderFieldAssertions(value.asJsonObject(), assertions);
                }
                break;
            case "required":
                if (value.getValueType() == ValueType.ARRAY) {
                    addRequiredHeaderFieldAssertion(value.asJsonArray(), assertions);
                }
                break;
            case "body":
                if (value.getValueType() == ValueType.OBJECT) {
                    addJsonBodyAssertions(node, value.asJsonObject(), assertions);
                }
                break;
            default:
                break;
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

    private void addJsonBodyAssertions(CaseNode node, JsonObject body, Collection<Assertion> assertions) {
        for (var entry : body.entrySet()) {
            switch (entry.getKey()) {
            case "data":
                addJsonDataAssertion(node, entry.getValue(), assertions);
                break;
            default:
                break;
            }
        }
    }

    private void addJsonDataAssertion(CaseNode node, JsonValue config, Collection<Assertion> assertions) {
        assertions.add(new JsonBodyAssertion(config, node.getAnnotationPrefix(), jsonProblemFactory));
    }
}
