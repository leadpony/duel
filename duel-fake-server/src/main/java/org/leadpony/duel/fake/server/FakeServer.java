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

package org.leadpony.duel.fake.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @author leadpony
 */
public class FakeServer extends Server {

    public FakeServer(int port) {
        super(port);
        ServletHandler handler = new ServletHandler();
        setHandler(handler);
        handler.addServletWithMapping(JsonResourceServlet.class, "/json/*");
        handler.addServletWithMapping(DiagnosticServlet.class, "/diagnostic");
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.valueOf(args[0]) : 8080;
        FakeServer server = new FakeServer(port);
        server.start();
        server.join();
    }
}
