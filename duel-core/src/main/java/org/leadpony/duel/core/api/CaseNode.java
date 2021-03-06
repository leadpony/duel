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

import java.net.URI;

/**
 * @author leadpony
 */
public interface CaseNode extends Node {

    String FILE_SUFFIX = ".test.json";

    /**
     * Returns the target URL of this test case including query strings.
     *
     * @return the target URL of this test case.
     */
    URI getEndpointUrl();
}
