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

package org.leadpony.duel.tests.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.duel.core.api.MediaType;

/**
 * @author leadpony
 */
public class MediaTypeTest {

    /**
     * @author leadpony
     */
    public enum MediaTypeTestCase {
        APPLICATION_JSON("application/json", "application", "json"),

        TEXT_HTML_WITH_CHARSET("text/html; charset=UTF-8", "text", "html") {{
            parameters.put("charset", "UTF-8");
        }},

        JSON_API("application/vnd.api+json", "application", "api") {
            @Override
            Optional<String> getTree() {
                return Optional.of("vnd");
            }
            @Override
            Optional<String> getSuffix() {
                return Optional.of("json");
            }
        },

        OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text", "application", "oasis.opendocument.text") {
            @Override
            Optional<String> getTree() {
                return Optional.of("vnd");
            }
        },

        MULTIPLE_PARAMETERS("text/plain; charset=iso-2022-jp; format=flowed; delsp=yes", "text", "plain") {{
            parameters.put("charset", "iso-2022-jp");
            parameters.put("format", "flowed");
            parameters.put("delsp", "yes");
        }};

        final String value;

        final String type;
        final String subtype;
        final Map<String, String> parameters = new HashMap<>();

        MediaTypeTestCase(String value, String type, String subtype) {
            this.value = value;
            this.type = type;
            this.subtype = subtype;
        }

        Optional<String> getTree() {
            return Optional.empty();
        }

        Optional<String> getSuffix() {
            return Optional.empty();
        }
    }

    @ParameterizedTest
    @EnumSource(MediaTypeTestCase.class)
    public void valueOfShouldReturnExpectedMediaType(MediaTypeTestCase test) {
        MediaType mediaType = MediaType.valueOf(test.value);

        assertThat(mediaType.getType()).isEqualTo(test.type);
        assertThat(mediaType.getSubtype()).isEqualTo(test.subtype);

        assertThat(mediaType.getParameters()).isEqualTo(test.parameters);

        assertThat(mediaType.getTree()).isEqualTo(test.getTree());
        assertThat(mediaType.getSuffix()).isEqualTo(test.getSuffix());
    }
}
