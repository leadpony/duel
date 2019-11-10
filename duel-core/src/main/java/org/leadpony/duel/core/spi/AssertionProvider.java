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

package org.leadpony.duel.core.spi;

import java.util.Collection;
import java.util.Map;

import javax.json.JsonValue;

/**
 * A provider of {@link Assertion} instances.
 *
 * @author leadpony
 */
public interface AssertionProvider {

    /**
     * Provides assertions configured by the specified JSON object.
     *
     * @param config     the configuration for the assertions, never be
     *                   {@code null}.
     * @param assertions the collection to which the assertions will be added, never
     *                   be {@code null}.
     */
    void provideAssertions(Map<String, JsonValue> config, Collection<Assertion> assertions);
}
