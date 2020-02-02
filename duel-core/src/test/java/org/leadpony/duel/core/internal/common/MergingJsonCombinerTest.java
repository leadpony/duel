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

package org.leadpony.duel.core.internal.common;

import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.duel.core.internal.JsonObjectSource;

/**
 * @author leadpony
 */
public class MergingJsonCombinerTest {

    private static JsonProvider jsonProvider;
    private JsonCombiner combiner;

    @BeforeAll
    public static void setUpOnce() {
        jsonProvider = JsonProvider.provider();
    }

    @BeforeEach
    public void setUp() {
        combiner = JsonCombiner.merging(jsonProvider);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @JsonObjectSource
    public void applyShouldMergeJsonAsExpected(String name, JsonObject object) {
        JsonValue base = object.get("base");
        JsonValue value = object.get("value");
        JsonValue expected = object.get("expected");

        JsonValue actual = combiner.apply(base, value);

        assertThat(actual).isEqualTo(expected);
    }
}
