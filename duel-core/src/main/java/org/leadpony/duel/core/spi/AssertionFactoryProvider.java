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

package org.leadpony.duel.core.spi;

import org.leadpony.duel.core.api.ExecutionContext;

/**
 * @author leadpony
 */
public interface AssertionFactoryProvider {

    /**
     * Provides an instance of {@link AssertionFactory}.
     *
     * @param context the context shared between all tests in the project.
     * @return the instance of {@link AssertionFactory}.
     */
    AssertionFactory provideAssertionFactory(ExecutionContext context);
}
