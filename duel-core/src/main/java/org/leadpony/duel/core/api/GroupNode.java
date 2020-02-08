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

package org.leadpony.duel.core.api;

import java.util.Collection;

/**
 * A group of test cases.
 *
 * @author leadpony
 */
public interface GroupNode extends Node, Iterable<Node> {

    /**
     * The name of the group files.
     */
    String FILE_NAME = "group.json";

    /**
     * The name of the root group file.
     */
    String ROOT_FILE_NAME = "root.json";

    boolean isRoot();

    /**
     * Returns the test cases in this group as a collection.
     *
     * @return all the test cases in this group.
     */
    Collection<CaseNode> getTestCases();

    /**
     * Returns the subgroups of this group as a collection.
     *
     * @return the subgroups of this group.
     */
    Collection<GroupNode> getSubgroups();

    /**
     * Creates an execution of this group.
     *
     * @return the newly created execution of this group.
     * @throws IllegalStateException if this group is not the root of the groups.
     */
    GroupExecution createExecution();
}
