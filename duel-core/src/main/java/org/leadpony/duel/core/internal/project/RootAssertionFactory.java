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

package org.leadpony.duel.core.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.api.ExecutionContext;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.AssertionFactory;

/**
 * A factory for producing assertions on the Web responses.
 *
 * @author leadpony
 */
class RootAssertionFactory implements AssertionFactory {

    private Collection<AssertionFactory> factories;

    @Override
    public void initializeFactory(ExecutionContext context) {
        this.factories = findFactories(context);
    }

    @Override
    public Stream<Assertion> createAssertions(CaseNode node) {
        return this.factories.stream()
            .flatMap(f -> f.createAssertions(node));
    }

    private static Collection<AssertionFactory> findFactories(ExecutionContext context) {
        List<AssertionFactory> factories = new ArrayList<>();
        for (AssertionFactory factory : ServiceLoader.load(AssertionFactory.class)) {
            factory.initializeFactory(context);
            factories.add(factory);
        }
        return factories;
    }
}
