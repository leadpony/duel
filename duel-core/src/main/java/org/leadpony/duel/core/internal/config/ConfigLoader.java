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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonValue;

//import org.apache.commons.beanutils.BeanMap;
import org.leadpony.duel.core.api.PropertyFinder;
import org.leadpony.duel.core.internal.common.Beans;
import org.leadpony.duel.core.internal.common.Json;

/**
 * @author leadpony
 */
public class ConfigLoader {

    private final PropertyFinder propertyFinder;

    @SuppressWarnings("serial")
    private static final Set<String> EXCLUDES = new HashSet<>() {{
        add("properties");
    }};

    public ConfigLoader() {
        this(PropertyFinder.empty());
    }

    public ConfigLoader(PropertyFinder propertyFinder) {
        this.propertyFinder = propertyFinder;
    }

    public <T extends Config> T load(Path path, Class<T> type) throws IOException {
        T config = Json.readFrom(path, type);
        return expandAll(config);
    }

    private <T extends Config> T expandAll(T config) {
        var properties = expandProperties(config.getProperties());
        config.setProperties(properties);

        PropertyExpander expander = PropertyExpander
                .withFinders(PropertyFinder.ofMap(properties), propertyFinder);

        new ConfigExpander(expander).expand(config, EXCLUDES);

        return config;
    }

    private Map<String, String> expandProperties(Map<String, String> properties) {
        return new PropertiesExpander(properties, propertyFinder)
                .expandAll();
    }

    /**
     * @author leadpony
     */
    private static class ConfigExpander {

        private final PropertyExpander expander;

        ConfigExpander(PropertyExpander expander) {
            this.expander = expander;
        }

        void expand(Object value) {
            expand(value, Collections.emptySet());
        }

        void expand(Object object, Set<String> excludes) {
            var map = Beans.asMap(object);
            map.forEach((key, value) -> {
                if (!excludes.contains(key) && value != null) {
                    map.put(key, expandValue(value));
                }
            });
        }

        private Object expandValue(Object value) {
            if (value instanceof String) {
                return expandString((String) value);
            } else if (value instanceof JsonValue) {
                return expandJson((JsonValue) value);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) value;
                expandMap(map);
                return map;
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                expandList(list);
                return list;
            } else {
                expand(value);
                return value;
            }
        }

        private String expandString(String value) {
            return expander.expand(value);
        }

        private JsonValue expandJson(JsonValue value) {
            return value;
        }

        private void expandMap(Map<Object, Object> map) {
            map.replaceAll((k, v) -> expandValue(v));
        }

        private void expandList(List<Object> list) {
            list.replaceAll(this::expandValue);
        }
    }
}
