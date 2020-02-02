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

package org.leadpony.duel.core.spi;

import java.util.stream.Stream;

import org.leadpony.duel.core.api.CaseNode;

/**
 * A factory of {@link Assertion} instances.
 * <p>
 * Note that this factory is instantiated once at startup and the single
 * instance is shared between all test cases in a project. In most cases the
 * factory should be implemented as a stateless object.
 * </p>
 *
 * @author leadpony
 */
public interface AssertionFactory {

    /**
     * Creates a stream of assertions for the specified case node.
     *
     * @param node the test node which will be tested, never be {@code null}.
     * @return the stream of assertions.
     */
    Stream<Assertion> createAssertions(CaseNode node);
}
