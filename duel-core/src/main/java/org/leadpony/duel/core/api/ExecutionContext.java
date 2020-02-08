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

package org.leadpony.duel.core.api;

import javax.json.spi.JsonProvider;

/**
 * A context shared between the tests.
 *
 * @author leadpony
 */
public interface ExecutionContext {

    /**
     * Returns the root group of the tests.
     *
     * @return the root group of the tests.
     */
    GroupNode getRootGroup();

    /**
     * Returns the JSON provider.
     *
     * @return the JSON provider.
     */
    JsonProvider getJsonProvider();
}
