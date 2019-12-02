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

/**
 * @author leadpony
 */
public interface Node {

    /**
     * Returns the name of this node for display.
     *
     * @return the name of this node, never be {@code null}.
     */
    String getName();

    /**
     * Returns the identifier of this node.
     *
     * @return the identifier of this node, never be {@code null}.
     */
    URI getId();

    /**
     * Returns the path to this node.
     *
     * @return the path to this node.
     */
    Path getPath();

    /**
     * Returns the value of the specified parameter.
     *
     * @param parameter the parameter to retrieve.
     * @return the value of the specified parameter.
     * @throws NullPointerException if the specified {@code parameter} is {@code null}.
     */
    Object get(Parameter parameter);

    String getAsString(Parameter parameter);

    /**
     * Returns all the properties defined in this node.
     *
     * @return the immutable map containing all of the properties.
     */
    Map<String, String> getProperties();
}