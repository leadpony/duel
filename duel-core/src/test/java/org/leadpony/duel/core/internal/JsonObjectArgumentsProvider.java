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

package org.leadpony.duel.core.internal;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * @author leadpony
 */
class JsonObjectArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String className = testClass.getSimpleName();
        String methodName = testMethod.getName();
        String name = className + "_" + methodName + ".json";
        return readObjectsFrom(testClass.getResourceAsStream(name));
    }

    private Stream<Arguments> readObjectsFrom(InputStream in) {
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                    .map(JsonValue::asJsonObject)
                    .map(JsonObjectArgumentsProvider::toArguments);
        }
    }

    private static Arguments toArguments(JsonObject object) {
        return Arguments.of(object.getString("name"), object);
    }
}
