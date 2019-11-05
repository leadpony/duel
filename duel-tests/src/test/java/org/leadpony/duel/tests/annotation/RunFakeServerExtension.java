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

package org.leadpony.duel.tests.annotation;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.leadpony.duel.fake.server.FakeServer;

/**
 * @author leadpony
 */
class RunFakeServerExtension implements BeforeAllCallback, AfterAllCallback {

    private FakeServer server;

    RunFakeServerExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        server = new FakeServer(8080);
        server.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        server.stop();
        server = null;
    }
}
