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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.leadpony.duel.core.api.PropertyFinder;

/**
 * @author leadpony
 */
interface PropertyExpander {

    Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^{}]+)\\}");

    /**
     * Expands a string value with properties.
     *
     * @param value the value to expand.
     * @return the expanded value.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    String expand(String value);

    static PropertyExpander withFinder(PropertyFinder finder) {
        return value -> {
            Objects.requireNonNull(value, "value must not be null.");
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
            return matcher.replaceAll(result -> {
                String replacement = finder.findProperty(result.group(1))
                        .orElse(result.group(0));
                return Matcher.quoteReplacement(replacement);
            });
        };
    }

    static PropertyExpander withFinders(PropertyFinder... finders) {
        return withFinder(PropertyFinder.of(finders));
    }
}
