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

package org.leadpony.duel.assertion.basic;

import org.leadpony.duel.core.spi.Assertion;
import org.opentest4j.AssertionFailedError;

/**
 * @author leadpony
 */
abstract class AbstractAssertion implements Assertion {

    private static final StackTraceElement[] STACK_TRACE = new StackTraceElement[0];

    protected static void fail(String message, Object expected, Object actual) {
        AssertionFailedError error = new AssertionFailedError(message, expected, actual);
        error.setStackTrace(STACK_TRACE);
        throw error;
    }
}
