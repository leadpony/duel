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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.json.bind.Jsonb;

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestContainer;
import org.leadpony.duel.core.internal.config.TestCaseConfig;

/**
 * @author leadpony
 */
class BasicTestCase extends BasicTestNode implements TestCase {

    static final String FILE_PATTERN = "*.test.json";

    private final TestCaseConfig config;

    BasicTestCase(Path path, TestContext context, TestContainer parent) {
        super(path, context, parent);
        this.config = loadConfig(path);
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = config.getDisplayName();
        if (name == null) {
            name = super.getName();
        }
        return name;
    }

    /* As a TestCase */

    @Override
    public URI getEndpointUrl() {
        URI path = config.getPath();
        if (path.isAbsolute()) {
            return path;
        } else {
            URI baseUrl = getContext().getProject().getBaseUrl();
            return baseUrl.resolve(path);
        }
    }

    @Override
    public void run() {
    }

    private TestCaseConfig loadConfig(Path path) {
        Jsonb jsonb = getContext().getJsonBinder();
        try (InputStream in = Files.newInputStream(path)) {
            return jsonb.fromJson(in, TestCaseConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
