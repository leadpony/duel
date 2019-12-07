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

package org.leadpony.duel.cli;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.LogManager;
import java.util.spi.ToolProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * The top-level command.
 *
 * @author leadpony
 */
@Command(
        name = "duel",
        description = "Duel CLI",
        mixinStandardHelpOptions = true,
        version = {
                "Duel CLI ${project.version}",
                "Duel Home: ${duel.home:-Unknown}",
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"
        }
)
public class DuelCommand implements ToolProvider, Callable<Integer> {

    private static final String LOG_CONFIG_FILE = "/logging.properties";
    private static final String BUNDLE_BASE_NAME =
            DuelCommand.class.getPackageName() + ".messages";

    private Runnable defaultCommand;

    @Override
    public String name() {
        return "Duel";
    }

    @Override
    public Integer call() {
        defaultCommand.run();
        return 0;
    }

    @Override
    public int run(PrintWriter out, PrintWriter err, String... args) {
        configureLogging();
        Console console = new Console(out, err);

        CommandLine commandLine = new CommandLine(this)
                .addSubcommand(new CommandLine.HelpCommand())
                .addSubcommand(new TestCommand(console))
                .setResourceBundle(getResourceBundle())
                .setOut(out)
                .setErr(err);

        defaultCommand = () -> commandLine.usage(out);

        return commandLine.execute(args);
    }

    private static void configureLogging() {
        LogManager logManager = LogManager.getLogManager();
        try (InputStream in = DuelCommand.class.getResourceAsStream(LOG_CONFIG_FILE)) {
            logManager.readConfiguration(in);
        } catch (Exception e) {
        }
    }

    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(
                BUNDLE_BASE_NAME,
                Locale.getDefault(),
                DuelCommand.class.getClassLoader());
    }

    /**
     * The entry point of the application.
     *
     * @param args the arguments given to the application.
     */
    public static void main(String[] args) {
        System.exit(new DuelCommand().run(System.out, System.err, args));
    }
}
