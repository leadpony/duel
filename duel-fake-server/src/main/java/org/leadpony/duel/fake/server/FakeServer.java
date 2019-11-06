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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @author leadpony
 */
public class FakeServer extends Server {

    private static final int PORT = 8080;
    private static final String SHUTDOWN_TOKEN = "secret";

    public FakeServer(int port) {
        super(port);
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler(SHUTDOWN_TOKEN));
        ServletHandler handler = new ServletHandler();
        addServlets(handler);
        handlers.addHandler(handler);
        setHandler(handlers);
    }

    private static void addServlets(ServletHandler handler) {
        handler.addServletWithMapping(JsonResourceServlet.class, "/json/*");
        handler.addServletWithMapping(DiagnosticServlet.class, "/diagnostic/*");
        handler.addServletWithMapping(EchoServlet.class, "/echo/*");
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("stop")) {
            shutdown(PORT);
        } else {
            FakeServer server = new FakeServer(PORT);
            server.start();
            server.join();
        }
    }

    private static void shutdown(int port) {
        try {
           URL url = new URL("http://localhost:" + port + "/shutdown?token=" + SHUTDOWN_TOKEN);
           HttpURLConnection connection = (HttpURLConnection) url.openConnection();
           connection.setRequestMethod("POST");
           connection.getResponseCode();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
