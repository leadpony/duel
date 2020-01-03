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

import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.AssertionProvider;

/**
 * A factory for producing assertions on the Web responses.
 *
 * @author leadpony
 */
class AssertionFactory {

    private final Collection<AssertionProvider> providers;

    AssertionFactory() {
        this.providers = findProviders();
    }

    Assertion createAssertion(CaseNode node) {
        Collection<Assertion> assertions = findAssertions(node);
        return response -> {
            for (Assertion assertion : assertions) {
                assertion.assertOn(response);
            }
        };
    }

    private static Collection<AssertionProvider> findProviders() {
        List<AssertionProvider> providers = new ArrayList<>();
        ServiceLoader.load(AssertionProvider.class)
            .forEach(providers::add);
        return providers;
    }

    private Collection<Assertion> findAssertions(CaseNode node) {
        List<Assertion> assertions = new ArrayList<>();
        for (AssertionProvider provider : this.providers) {
            provider.provideAssertions(node, assertions);
        }
        return assertions;
    }
}
