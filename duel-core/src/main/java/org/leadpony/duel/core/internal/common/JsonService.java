/*
 * Copyright 2020 the original author or authors.
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

package org.leadpony.duel.core.internal.common;

import java.util.Collections;

import javax.json.JsonBuilderFactory;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;

/**
 * @author leadpony
 */
public class JsonService {

    public static final JsonProvider PROVIDER = JsonProvider.provider();
    public static final JsonReaderFactory READER_FACTORY = PROVIDER.createReaderFactory(Collections.emptyMap());
    public static final JsonBuilderFactory BUILDER_FACTORY = PROVIDER.createBuilderFactory(Collections.emptyMap());
}
