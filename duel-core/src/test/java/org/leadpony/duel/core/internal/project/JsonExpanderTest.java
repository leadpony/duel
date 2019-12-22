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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.logging.Logger;

import javax.json.JsonObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.duel.core.internal.JsonObjectSource;
import org.leadpony.duel.core.internal.Logging;

/**
 * @author leadpony
 */
@Logging
public class JsonExpanderTest {

    private static final Logger LOG = Logger.getLogger(JsonExpanderTest.class.getName());

    @ParameterizedTest(name = "[{index}] {0}")
    @JsonObjectSource
    public void applyShouldExpandProperties(String name, JsonObject object) {
        JsonObject data = object.getJsonObject("data");
        JsonObject expected = object.getJsonObject("expected");

        JsonObject actual = JsonExpander.SIMPLE.apply(data);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @JsonObjectSource
    public void applyShouldThrowException(String name, JsonObject object) {
        JsonObject data = object.getJsonObject("data");
        Throwable thrown = catchThrowable(() -> {
            JsonExpander.SIMPLE.apply(data);
         });

        assertThat(thrown).isInstanceOf(PropertyException.class);
        LOG.info(thrown.getMessage());
    }
}