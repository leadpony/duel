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

package org.leadpony.duel.core.internal.project;

import java.net.http.HttpClient;

import javax.json.JsonReaderFactory;

import org.leadpony.duel.core.api.ExecutionContext;
import org.leadpony.duel.core.spi.AssertionFactory;

/**
 * A context of project execution.
 *
 * @author leadpony
 */
interface ProjectExecutionContext extends ExecutionContext {

    JsonReaderFactory getJsonReaderFactory();

    HttpClient getHttpClient();

    AssertionFactory getAssertionFactory();
}
