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

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.spi.ToolProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author leadpony
 */
@Command(
        name = "fakeserver",
        description = "Duel Fake Server",
        mixinStandardHelpOptions = true,
        version = {
                "Duel Fake Server ${project.version}",
                "Duel Home: ${duel.home:-Unknown}",
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"
        }
)
public class ServerCommand implements Runnable, ToolProvider {

    private static final String BUNDLE_BASE_NAME =
            ServerCommand.class.getPackageName() + ".messages";

    private Runnable defaultCommand;

    @Override
    public String name() {
        return "Fake Server";
    }

    @Override
    public int run(PrintWriter out, PrintWriter err, String... args) {
        CommandLine commandLine = new CommandLine(this)
                .addSubcommand(new CommandLine.HelpCommand())
                .addSubcommand(new Start())
                .addSubcommand(new Stop())
                .setResourceBundle(getResourceBundle())
                .setOut(out)
                .setErr(err);

        defaultCommand = () -> commandLine.usage(out);

        return commandLine.execute(args);
    }

    @Override
    public void run() {
        defaultCommand.run();
    }

    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(
                BUNDLE_BASE_NAME,
                Locale.getDefault(),
                ServerCommand.class.getClassLoader());
    }

    public static void main(String[] args) {
        System.exit(new ServerCommand().run(System.out, System.err, args));
    }

    private abstract static class AbstractCommand implements Callable<Integer> {

        @Option(names = {"--port"},
                paramLabel = "PORT",
                description = "port for the server to listen")
        public int port = 8080;
    }

    @Command(name = "start",
            description = "Starts the fake server")
    public static class Start extends AbstractCommand {

        @Override
        public Integer call() throws Exception {
            FakeServer server = new FakeServer(this.port);
            server.start();
            server.join();
            return 0;
        }
    }

    @Command(name = "stop",
            description = "Stops the fake server")
    public static class Stop extends AbstractCommand {

        @Override
        public Integer call() throws Exception {
            URL url = new URL("http://localhost:"
                    + this.port + "/shutdown?token="
                    + FakeServer.SHUTDOWN_TOKEN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.getResponseCode();
            return 0;
        }
    }
}
