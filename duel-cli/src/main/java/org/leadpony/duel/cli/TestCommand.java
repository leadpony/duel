/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.spi.ToolProvider;

import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.api.Problem;
import org.leadpony.duel.core.api.TestLoadingException;
import org.leadpony.duel.core.api.TestLoader;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A subcommand "test"
 *
 * @author leadpony
 */
@Command(name = "test")
class TestCommand extends AbstractCommand implements Callable<Integer> {

    private static final String[] ARGS = {
            "--disable-banner",
            "-c=" + ProjectTest.class.getName()
    };

    private final Console console;
    private Path path;

    TestCommand(Console console) {
        this.console = console;
        this.path = Path.of(System.getProperty("user.dir"));
    }

    @Option(names = {"-p", "--path"},
            paramLabel = "DIRECTORY")
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {
        try {
            GroupNode root = loadTests(this.path);
            ProjectTest.setRootGroup(root);
            return runConsole(ARGS);
        } catch (TestLoadingException e) {
            return fail(e);
        } catch (Exception e) {
            return fail(e);
        } finally {
            ProjectTest.setRootGroup(null);
        }
    }

    private PrintWriter getOutputWriter() {
        return console.getOutputWriter();
    }

    private PrintWriter getErrorWriter() {
        return console.getErrorWriter();
    }

    private int runConsole(String... args) {
        Optional<ToolProvider> tool = ToolProvider.findFirst("junit");
        if (tool.isPresent()) {
            return tool.get().run(getOutputWriter(), getErrorWriter(), args);
        }
        return fail("No ToolProvider found.");
    }

    private static GroupNode loadTests(Path path) {
        return TestLoader.loadFrom(path);
    }

    private int fail(String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("[ERROR] ")
               .append(message);
        getErrorWriter().println(builder.toString());
        return FAILED;
    }

    private int fail(TestLoadingException e) {
        PrintWriter err = getErrorWriter();
        err.println(e.getMessage());
        StringBuilder builder = new StringBuilder();
        for (Problem problem : e.getProblems()) {
            builder.setLength(0);
            problem.getPath().ifPresent(path -> {
                builder.append(path.toString()).append(": ");
            });
            builder.append(problem.getDescription());
            err.println(builder.toString());
        }
        return FAILED;
    }

    private int fail(Exception e) {
        fail(e.getMessage());
        e.printStackTrace(getErrorWriter());
        return FAILED;
    }
}
