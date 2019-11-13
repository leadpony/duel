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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * A builder of a hierarchical URI.
 *
 * @author leadpony
 */
public class UrlBuilder {

    private String scheme;
    private String host;
    private Integer port;
    private String path;
    private String query;

    public UrlBuilder withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public UrlBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public UrlBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public UrlBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public URI build() throws URISyntaxException {
        StringBuilder builder = new StringBuilder();
        builder.append(scheme)
               .append("://")
               .append(host);
        if (port != null) {
            builder.append(':').append(port);
        }
        builder.append(path);
        if (query != null && !query.isBlank()) {
            builder.append('?').append(query);
        }
        return new URI(builder.toString());
    }

    public UrlBuilder withQuery(Map<String, List<String>> query) {
        requireNonNull(query, "query must not be null.");
        Escaper escaper = Escaper.QUERY_ESCAPER;
        StringBuilder builder = new StringBuilder();
        query.forEach((name, values) -> {
            if (builder.length() > 0) {
                builder.append('&');
            }
            for (String value : values) {
                builder.append(escaper.apply(name))
                       .append('=')
                       .append(escaper.apply(value));
            }
        });
        this.query = builder.toString();
        return this;
    }
}
