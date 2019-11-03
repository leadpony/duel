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

package org.leadpony.duel.core.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import javax.json.JsonObject;

import org.leadpony.duel.core.spi.ResponseValidator;
import org.leadpony.duel.core.spi.ResponseValidatorProvider;

/**
 * @author leadpony
 */
class ResponseValidatorFactory {

    private Collection<ResponseValidatorProvider> providers;

    ResponseValidatorFactory() {
        this.providers = findProviders();
    }

    ResponseValidator createValidator(JsonObject config) {
        Collection<ResponseValidator> validators = findValidators(config);
        return response -> {
            for (ResponseValidator validator : validators) {
                validator.validateResponse(response);
            }
        };
    }

    private static Collection<ResponseValidatorProvider> findProviders() {
        List<ResponseValidatorProvider> providers = new ArrayList<>();
        ServiceLoader.load(ResponseValidatorProvider.class)
            .forEach(providers::add);
        return providers;
    }

    private Collection<ResponseValidator> findValidators(JsonObject config) {
        List<ResponseValidator> validators = new ArrayList<>();
        for (ResponseValidatorProvider provider : providers) {
            provider.provideValidators(config, validators);
        }
        return validators;
    }
}
