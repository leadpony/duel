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

import java.net.URI;
import java.nio.file.Path;

import org.leadpony.duel.core.api.TestContainer;
import org.leadpony.duel.core.api.TestNode;

/**
 * @author leadpony
 */
abstract class BasicTestNode implements TestNode {

    private final Path path;
    private final TestContext context;
    private final TestContainer parent;

    protected BasicTestNode(Path path, TestContext context, TestContainer parent) {
        this.path = path;
        this.context = context;
        this.parent = parent;
    }

    @Override
    public final URI getId() {
        return getPath().toUri();
    }

    @Override
    public String getName() {
        return getPath().getFileName().toString();
    }

    @Override
    public final Path getPath() {
        return path;
    }

    @Override
    public TestContainer getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected TestContext getContext() {
        return context;
    }
}
