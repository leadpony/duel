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

package org.leadpony.duel.core.internal.project;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.leadpony.duel.core.internal.common.JsonService;

/**
 * @author leadpony
 *
 */
class ValueExpander {

    static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^{}]+)\\}");

    private final Function<String, String> finder;
    private final JsonService jsonService;
    private final JsonBuilderFactory builderFactory;

    ValueExpander(Function<String, String> finder) {
        this.finder = finder;
        this.jsonService = JsonService.SINGLETON;
        this.builderFactory = jsonService.createBuilderFactory();
    }

    JsonValue expand(JsonValue value) {
        switch (value.getValueType()) {
        case ARRAY:
            return expand(value.asJsonArray());
        case OBJECT:
            return expand(value.asJsonObject());
        case STRING:
            return expand((JsonString) value);
        default:
            return value;
        }
    }

    String expand(String string) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(string);
        return matcher.replaceAll(result -> {
            String replacement = finder.apply(result.group(1));
            if (replacement == null) {
                replacement = result.group(0);
            }
            return Matcher.quoteReplacement(replacement);
        });
    }

    JsonArray expand(JsonArray array) {
        if (array.isEmpty()) {
            return array;
        }
        JsonArrayBuilder builder = builderFactory.createArrayBuilder();
        array.forEach(item -> builder.add(expand(item)));
        return builder.build();
    }

    JsonObject expand(JsonObject object) {
        if (object.isEmpty()) {
            return object;
        }
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        object.forEach((name, value) -> {
            builder.add(name, expand(value));
        });
        return builder.build();
    }

    JsonString expand(JsonString value) {
        String oldString = value.getString();
        String newString = expand(oldString);
        if (newString == oldString) {
            return value;
        }
        return jsonService.createValue(newString);
    }
}
