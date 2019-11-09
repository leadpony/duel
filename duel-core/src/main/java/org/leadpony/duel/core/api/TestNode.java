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

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * @author leadpony
 */
public interface TestNode extends PropertyFinder, Comparable<TestNode> {

    /**
     * Returns the identifier of this node.
     *
     * @return the identifier of this node, never be {@code null}.
     */
    URI getId();

    Path getPath();

    Path getDirectory();

    /**
     * Returns the name of this node for display.
     *
     * @return the name of this node, never be {@code null}.
     */
    String getName();

    /**
     * Returns the parent of this node.
     *
     * @return the parent of this node, may be empty.
     */
    Optional<TestNode> getParent();

    URI getBaseUrl();

    Map<String, String> getProperties();

    @Override
    default int compareTo(TestNode other) {
        return getId().compareTo(other.getId());
    }
}
