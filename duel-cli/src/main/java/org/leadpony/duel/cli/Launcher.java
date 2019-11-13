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

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.spi.ToolProvider;

import org.leadpony.duel.core.api.Project;
import org.leadpony.duel.core.api.ProjectLoader;

/**
 * @author leadpony
 */
public class Launcher {

    private static final int FAILED = -1;

    private final Path dir;

    private PrintStream out = System.out;
    private PrintStream err = System.err;

    private static final String[] ARGS = {
            "--disable-banner",
            "-c=" + ProjectTest.class.getName()
    };

    public Launcher() {
        this.dir = Paths.get(System.getProperty("user.dir"));
    }

    Launcher(Path dir) {
        this.dir = dir;
    }

    int launch(String... args) {
        try {
            Project project = loadProject(dir);
            ProjectTest.setProject(project);
            int exitCode = runConsole(ARGS);
            return exitCode;
        } catch (Exception e) {
            err.println(e.getMessage());
            return FAILED;
        }
    }

    private int runConsole(String... args) {
        Optional<ToolProvider> tool = ToolProvider.findFirst("junit");
        if (tool.isPresent()) {
            return tool.get().run(out, err, args);
        }
        err.println("No ToolProvider found.");
        return FAILED;
    }

    private static Project loadProject(Path path) {
        return ProjectLoader.loadFrom(path);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.exit(new Launcher().launch(args));
    }
}
