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

package org.leadpony.duel.core.internal.config;

import java.util.Collections;
import java.util.Map;

import org.leadpony.duel.core.api.Parameter;

/**
 * @author leadpony
 */
public class Config {

    private static final Config EMPTY = new Config();
    private static final String DEFAULT_METHOD = "GET";

    private String name;
    private String scheme;
    private String host;
    private Integer port;
    private String basePath;
    private String baseUrl;
    private String method = DEFAULT_METHOD;

    private Map<String, String> properties = Collections.emptyMap();

    public static Config empty() {
        return EMPTY;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String displayName) {
        this.name = displayName;
    }

    public final String getScheme() {
        return scheme;
    }

    public final void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public final String getHost() {
        return host;
    }

    public final void setHost(String host) {
        this.host = host;
    }

    public final Integer getPort() {
        return port;
    }

    public final void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the basePath
     */
    public final String getBasePath() {
        return basePath;
    }

    /**
     * @param basePath the basePath to set
     */
    public final void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Object getParameter(Parameter parameter) {
        switch (parameter) {
        case SCHEME:
            return getScheme();
        case HOST:
            return getHost();
        case PORT:
            return getPort();
        case BASE_PATH:
            return getBasePath();
        case METHOD:
            return getMethod();
        default:
            throw new IllegalArgumentException();
        }
    }
}
