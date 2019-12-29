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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * @author leadpony
 */
@Retention(RUNTIME)
@Target(METHOD)
@ArgumentsSource(JsonSource.JsonArgumentsProvider.class)
public @interface JsonSource {

    String value();

    class JsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonSource> {

        private String name;

        @Override
        public void accept(JsonSource annotation) {
            this.name = annotation.value();
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            Class<?> testClass = context.getRequiredTestClass();
            return readArray(name, testClass)
                    .stream()
                    .map(JsonValue::asJsonObject)
                    .flatMap(JsonArgumentsProvider::mapObject);
        }

        private static Stream<Arguments> mapObject(JsonObject container) {
            return container.getJsonArray("tests").stream()
                    .map(JsonValue::asJsonObject)
                    .map(test -> mapTest(test, container));
        }

        private static Arguments mapTest(JsonObject test, JsonObject container) {
            return Arguments.of(
                test.getString("title"),
                container.get("expected"),
                test.get("actual"),
                getProblems(test)
                );
        }

        private static List<JsonObject> getProblems(JsonObject test) {
            if (test.containsKey("problems")) {
                return test.getJsonArray("problems")
                        .stream()
                        .map(JsonValue::asJsonObject)
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }

        private static JsonArray readArray(String name, Class<?> type) {
            InputStream in = type.getResourceAsStream(name);
            try (JsonReader reader = Json.createReader(in)) {
                return reader.readArray();
            }
        }
    }
}
