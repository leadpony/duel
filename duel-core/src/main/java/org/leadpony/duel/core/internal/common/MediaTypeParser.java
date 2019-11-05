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

package org.leadpony.duel.core.internal.common;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.leadpony.duel.core.spi.MediaType;

/**
 * A parser for producing an instance of {@link MediaType}.
 *
 * @author leadpony
 */
public final class MediaTypeParser {

    private final String value;

    private String type;
    private String subtype;
    private String tree;
    private String suffix;
    private Map<String, String> parameters;

    public MediaTypeParser(String value) {
        this.value = value;
    }

    public MediaType parse() {
        String[] parts = this.value.split(";");
        parseTypePart(parts[0]);
        this.parameters = parseParameters(parts);
        return new MediaTypeImpl(this);
    }

    private void parseTypePart(String types) {
        String[] parts = types.split("/");
        if (parts.length != 2) {
            throw newIllegalArgumentException();
        }

        String type = parts[0].trim();
        if (type.isEmpty()) {
            throw newIllegalArgumentException();
        }
        this.type = type;

        String subtype = parts[1];
        int beforeSuffix = subtype.indexOf('+');
        if (beforeSuffix >= 0) {
            this.suffix = subtype.substring(beforeSuffix + 1).trim();
            subtype = subtype.substring(0, beforeSuffix);
        }

        int afterTree = subtype.indexOf('.');
        if (afterTree >= 0) {
            this.tree = subtype.substring(0, afterTree).trim();
            subtype = subtype.substring(afterTree + 1);
        }

        subtype = subtype.trim();
        if (subtype.isEmpty()) {
            throw newIllegalArgumentException();
        }
        this.subtype = subtype;
    }

    private Map<String, String> parseParameters(String[] parameters) {
        if (parameters.length < 2) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < parameters.length; i++) {
            String[] tokens = parameters[i].split("=");
            if (tokens.length != 2) {
                throw newIllegalArgumentException();
            }
            map.put(tokens[0].trim(), tokens[1].trim());
        }
        return Collections.unmodifiableMap(map);
    }

    private IllegalArgumentException newIllegalArgumentException() {
        return new IllegalArgumentException(this.value);
    }

    /**
     * An implementation of {@link MediaType}.
     *
     * @author leadpony
     */
    private static final class MediaTypeImpl implements MediaType {

        private final String type;
        private final String subtype;
        private final Optional<String> tree;
        private final Optional<String> suffix;
        private final Map<String, String> parameters;

        MediaTypeImpl(MediaTypeParser parser) {
            this.type = parser.type;
            this.subtype = parser.subtype;
            this.tree = Optional.ofNullable(parser.tree);
            this.suffix = Optional.ofNullable(parser.suffix);
            this.parameters = parser.parameters;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getSubtype() {
            return subtype;
        }

        @Override
        public Optional<String> getTree() {
            return tree;
        }

        @Override
        public Optional<String> getSuffix() {
            return suffix;
        }

        @Override
        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public Charset getCharset(Charset defaultValue) {
            if (parameters.containsKey("charset")) {
                return Charset.forName(parameters.get("charset"));
            } else {
                return defaultValue;
            }
        }
    }
}
