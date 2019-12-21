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

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.leadpony.duel.fake.server.servlets.EchoServlet;
import org.leadpony.duel.fake.server.servlets.ReportServlet;
import org.leadpony.duel.fake.server.servlets.StaticResourceServlet;
import org.leadpony.duel.fake.server.servlets.StatusServlet;

/**
 * A fake web server.
 *
 * @author leadpony
 */
public class FakeServer extends Server {

    public static final String SHUTDOWN_TOKEN = "secret";

    public FakeServer(int port) {
        super(new InetSocketAddress("localhost", port));
        HandlerList handlers = new HandlerList();
        handlers.addHandler(createShutdownHandler());
        handlers.addHandler(createServletHandler());
        setHandler(handlers);
    }

    private static Handler createShutdownHandler() {
        return new ShutdownHandler(SHUTDOWN_TOKEN);
    }

    private static Handler createServletHandler() {
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(ReportServlet.class, "/report/*");
        handler.addServletWithMapping(EchoServlet.class, "/echo");
        handler.addServletWithMapping(StatusServlet.class, "/status");
        handler.addServletWithMapping(StaticResourceServlet.class, "/*");
        return handler;
    }
}
