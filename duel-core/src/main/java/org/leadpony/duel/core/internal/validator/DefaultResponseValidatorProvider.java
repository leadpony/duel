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

package org.leadpony.duel.core.internal.validator;

import java.util.Collection;
import java.util.stream.Stream;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.spi.ResponseValidator;
import org.leadpony.duel.core.spi.ResponseValidatorProvider;

/**
 * @author leadpony
 */
public class DefaultResponseValidatorProvider implements ResponseValidatorProvider {

    @Override
    public void provideValidators(JsonObject config, Collection<ResponseValidator> validators) {
        new ValiadtorCollector(validators).collect(config);
    }

    /**
     * @author leadpony
     */
    static final class ValiadtorCollector {

        private final Collection<ResponseValidator> validators;

        ValiadtorCollector(Collection<ResponseValidator> validators) {
            this.validators = validators;
        }

        void collect(JsonObject config) {
            addStatusValidator(config);
            addHeaderValidators(config);
        }

        private void add(ResponseValidator validator) {
            validators.add(validator);
        }

        private void addStatusValidator(JsonObject config) {
            if (!config.containsKey("status")) {
                return;
            }
            JsonValue value = config.get("status");
            switch (value.getValueType()) {
            case NUMBER:
                JsonNumber number = (JsonNumber) value;
                add(new SimpleStatusValidator(number.intValue()));
                break;
            case STRING:
                JsonString string = (JsonString) value;
                int status = Integer.valueOf(string.getString());
                add(new SimpleStatusValidator(status));
                break;
            default:
                break;
            }
        }

        private void addHeaderValidators(JsonObject config) {
            if (config.containsKey("header")) {
                JsonObject header = config.getJsonObject("header");
                addHeaderFieldValidators(header);
                addRequiredHeaderFieldValiadtor(header);
            }
        }

        private void addRequiredHeaderFieldValiadtor(JsonObject config) {
            if (!config.containsKey("required")) {
                return;
            }
            Stream<String> names = config.getJsonArray("required")
                    .stream()
                    .filter(v -> v.getValueType() == ValueType.STRING)
                    .map(v -> (JsonString) v)
                    .map(JsonString::getString);
            add(new RequiredHeaderFieldValidator(names));
        }

        private void addHeaderFieldValidators(JsonObject config) {
            if (!config.containsKey("fields")) {
                return;
            }
            JsonObject fields = config.getJsonObject("fields");
            fields.forEach((name, value) -> {
                switch (value.getValueType()) {
                case STRING:
                    JsonString string = (JsonString) value;
                    add(new HeaderFieldValidator(name, string.getString()));
                    break;
                default:
                    break;
                }
            });
        }
    }
}
