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
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.spi.ToolProvider;

import org.leadpony.duel.core.api.Problem;
import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectException;
import org.leadpony.duel.core.api.ProjectLoader;

/**
 * @author leadpony
 */
public class Launcher {

    private static final int FAILED = 2;

    private static final String LOG_CONFIG_FILE = "/logging.properties";

    private final Path dir;

    private PrintStream out = System.out;
    private PrintStream err = System.err;

    private static final String[] ARGS = {
            "--disable-banner",
            "-c=" + ProjectTest.class.getName()
    };

    static {
        configureLogging();
    }

    public Launcher() {
        this(Path.of(System.getProperty("user.dir")));
    }

    Launcher(Path dir) {
        this.dir = dir;
    }

    int launch(String... args) {
        try {
            Project project = loadProject(dir);
            ProjectTest.setProject(project);
            return runConsole(ARGS);
        } catch (ProjectException e) {
            return fail(e);
        } catch (Exception e) {
            return fail(e);
        }
    }

    private int runConsole(String... args) {
        Optional<ToolProvider> tool = ToolProvider.findFirst("junit");
        if (tool.isPresent()) {
            return tool.get().run(out, err, args);
        }
        return fail("No ToolProvider found.");
    }

    private static Project loadProject(Path path) {
        return ProjectLoader.loadFrom(path);
    }

    private int fail(String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("[ERROR] ")
               .append(message);
        err.println(builder.toString());
        return FAILED;
    }

    private int fail(ProjectException e) {
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
        e.printStackTrace(err);
        return FAILED;
    }

    private static void configureLogging() {
        LogManager logManager = LogManager.getLogManager();
        try (InputStream in = Launcher.class.getResourceAsStream(LOG_CONFIG_FILE)) {
            logManager.readConfiguration(in);
        } catch (Exception e) {
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.exit(new Launcher().launch(args));
    }
}
