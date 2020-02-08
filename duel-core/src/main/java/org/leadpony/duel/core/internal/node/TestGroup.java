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

package org.leadpony.duel.core.internal.node;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.leadpony.duel.core.api.CaseExecution;
import org.leadpony.duel.core.api.GroupExecution;
import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.api.GroupNode;
import org.leadpony.duel.core.api.Node;

/**
 * @author leadpony
 */
class TestGroup extends AbstractNode implements GroupNode {

    static final String FILE_NAME = "group.json";

    private final List<TestCase> testCases;
    private final List<TestGroup> subgroups;

    TestGroup(Path dir,
            JsonObject json,
            JsonObject merged,
            JsonObject expanded,
            List<TestCase> testCases,
            List<TestGroup> subgroups
            ) {
        super(dir, json, merged, expanded);
        this.testCases = testCases;
        this.subgroups = subgroups;
    }

    /* As a Iterable<TestNode> */

    @Override
    public Iterator<Node> iterator() {
        return Stream.concat(getTestCases().stream(), getSubgroups().stream())
                .iterator();
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return name;
        }
        return getNodePath().getFileName().toString();
    }

    /* As a TestGroup */

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public Collection<CaseNode> getTestCases() {
        return Collections.unmodifiableCollection(testCases);
    }

    @Override
    public Collection<GroupNode> getSubgroups() {
        return Collections.unmodifiableCollection(subgroups);
    }

    @Override
    public GroupExecution createExecution() {
        throw new IllegalStateException("Group is not the root.");
    }

    /* */

    GroupExecution createExecution(TestExecutionContext context) {
        return new TestGroupExecution(context);
    }

    private class TestGroupExecution implements GroupExecution {

        private final TestExecutionContext context;

        private TestGroupExecution(TestExecutionContext context) {
            this.context = context;
        }

        @Override
        public Node getNode() {
            return TestGroup.this;
        }

        @Override
        public Stream<CaseExecution> testCases() {
            TestExecutionContext context = this.context;
            return testCases.stream().map(testCase -> {
                return testCase.createExecution(context);
            });
        }

        @Override
        public Stream<GroupExecution> subgroups() {
            TestExecutionContext context = this.context;
            return subgroups.stream().map(testGroup -> {
                return testGroup.createExecution(context);
            });
        }
    }
}
