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

package org.leadpony.duel.core.internal.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.leadpony.duel.core.api.PropertyFinder;
import org.leadpony.duel.core.internal.Message;

/**
 * @author leadpony
 */
class PropertiesExpander implements PropertyFinder {

    private final Map<String, String> original;
    private final Set<String> expanding;
    private final PropertyExpander expander;

    PropertiesExpander(Map<String, String> original) {
        this(original, PropertyFinder.empty());
    }

    PropertiesExpander(Map<String, String> original, PropertyFinder finder) {
        this.original = original;
        this.expanding = new HashSet<>();
        this.expander = PropertyExpander.withFinders(this, finder);
    }

    /**
     * Expands all of the properties.
     *
     * @return the expanded properties.
     * @throws IllegalStateException if an infinite expansion was found.
     */
    Map<String, String> expandAll() {
        Map<String, String> expanded = new HashMap<>();
        original.forEach((key, value) -> {
            expanding.clear();
            String newValue = expand(key, value);
            expanded.put(key, newValue);
        });
        return expanded;
    }

    @Override
    public Optional<String> findProperty(String name) {
        if (original.containsKey(name)) {
            String value = expand(name, original.get(name));
            return Optional.of(value);
        }

        return Optional.empty();
    }

    private String expand(String name, String value) {
        if (expanding.contains(name)) {
            throw new IllegalStateException(
                    Message.CYCLIC_PROPERTY_EXPANSION.format(name));
        }
        expanding.add(name);
        String newValue = expander.expand(value);
        expanding.remove(name);
        return newValue;
    }
}
