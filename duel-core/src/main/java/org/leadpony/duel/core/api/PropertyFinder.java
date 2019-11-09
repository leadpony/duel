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

package org.leadpony.duel.core.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A finder of properties.
 *
 * @author leadpony
 */
@FunctionalInterface
public interface PropertyFinder {

    Optional<String> findProperty(String name);

    /**
     * Returns an empty {@code PropertyFinder}.
     *
     * @return an empty {@code PropertyFinder}.
     */
    static PropertyFinder empty() {
        return name -> Optional.empty();
    }

    static PropertyFinder ofMap(Map<String, String> map) {
        Objects.requireNonNull(map, "map must not be null.");
        return name -> Optional.ofNullable(map.get(name));
    }

    static PropertyFinder of(PropertyFinder... finders) {
        return name -> {
            for (var finder : finders) {
                Optional<String> property = finder.findProperty(name);
                if (property.isPresent()) {
                    return property;
                }
            }
            return Optional.empty();
        };
    }
}
