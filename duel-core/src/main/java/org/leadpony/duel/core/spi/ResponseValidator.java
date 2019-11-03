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

package org.leadpony.duel.core.spi;

import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * @author leadpony
 */
public interface ResponseValidator {

    void validateResponse(HttpResponse<?> response);

    static ResponseValidator alwaysValid() {
        return reponse -> { };
    }

    static ResponseValidator collected(Collection<? extends ResponseValidator> validators) {
        return response -> {
            for (ResponseValidator validator : validators) {
                validator.validateResponse(response);
            }
        };
    }
}
