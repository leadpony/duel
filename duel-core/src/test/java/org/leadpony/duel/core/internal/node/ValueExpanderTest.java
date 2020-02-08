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

package org.leadpony.duel.core.internal.node;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.duel.core.internal.JsonObjectSource;

/**
 * @author leadpony
 */
public class ValueExpanderTest {

    private static JsonProvider jsonProvider;
    private static JsonBuilderFactory builderFactory;

    @BeforeAll
    public static void setUpOnce() {
        jsonProvider = JsonProvider.provider();
        builderFactory = jsonProvider.createBuilderFactory(Collections.emptyMap());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @JsonObjectSource
    public void expandShouldExpandString(String name, JsonObject object) {
        JsonObject properties = object.getJsonObject("properties");
        var expander = new ValueExpander(
                p -> properties.getString(p, null),
                jsonProvider,
                builderFactory);

        String actual = expander.expand(object.getString("data"));

        assertThat(actual).isEqualTo(object.getString("expected"));
    }
}
