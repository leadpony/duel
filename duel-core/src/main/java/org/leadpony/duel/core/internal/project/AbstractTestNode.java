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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.leadpony.duel.core.api.Parameter;
import org.leadpony.duel.core.api.TestNode;

/**
 * A skeletal implementation of {@link TestNode}.
 *
 * @author leadpony
 */
abstract class AbstractTestNode implements TestNode {

    private final Path path;

    protected AbstractTestNode(Path path) {
        this.path = path;
    }

    @Override
    public URI getId() {
        return path.toUri();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public Optional<TestNode> getParent() {
        return Optional.empty();
    }

    @Override
    public Object get(Parameter parameter) {
        return getParentParameter(parameter);
    }

    @Override
    public String getAsString(Parameter parameter) {
        return get(parameter).toString();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Optional<String> findProperty(String name) {
        requireNonNull(name, "name must not be null.");
        var properties = getProperties();
        if (properties.containsKey(name)) {
            return Optional.of(properties.get(name));
        }
        Optional<TestNode> parent = getParent();
        if (parent.isPresent()) {
            return parent.get().findProperty(name);
        } else {
            return Optional.empty();
        }
    }

    protected Object getParentParameter(Parameter parameter) {
        return getParent().map(parent -> parent.get(parameter))
                .orElse(parameter.defaultValue());
    }
}
