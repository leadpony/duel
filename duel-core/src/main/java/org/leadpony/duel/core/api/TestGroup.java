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

package org.leadpony.duel.core.api;

import java.util.stream.Stream;

/**
 * A group of test cases.
 *
 * @author leadpony
 */
public interface TestGroup extends TestNode, Iterable<TestNode> {

    String FILE_NAME = "group.json";

    @Override
    default String getName() {
        return getPath().getFileName().toString();
    }

    /**
     * Returns the test cases in this group as a stream.
     *
     * @return the stream of the test cases.
     */
    Stream<TestCase> testCases();

    /**
     * Returns the subgroups of this group as a stream.
     *
     * @return the stream of the test groups.
     */
    Stream<TestGroup> subgroups();
}
