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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class for operating on Java beans.
 *
 * @author leadpony
 */
public final class Beans {

    private Beans() {
    }

    public static Map<String, Object> asMap(Object bean) {
        Objects.requireNonNull(bean, "bean must not be null.");
        return new BeanMap(bean);
    }

    /**
     * A map wrapper of the bean.
     *
     * @author leadpony
     */
    private static class BeanMap extends AbstractMap<String, Object> {

        private final Object bean;
        private final Set<Entry<String, Object>> entries;
        private final Map<String, Entry<String, Object>> entryMap;

        /**
         * Constructs this map.
         *
         * @param bean the bean wrapped by this map.
         */
        BeanMap(Object bean) {
            this.bean = bean;
            this.entries = createMapEntries(bean);
            this.entryMap = this.entries.stream().collect(
                    Collectors.toMap(Entry::getKey, Function.identity()));
        }

        @Override
        public Object put(String key, Object value) {
            if (!entryMap.containsKey(key)) {
                throw new IllegalArgumentException();
            }
            return entryMap.get(key).setValue(value);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return entries;
        }

        private Set<Entry<String, Object>> createMapEntries(Object bean) {
            Set<Entry<String, Object>> entries = new HashSet<>();
            Class<?> beanType = bean.getClass();
            for (Method method : beanType.getMethods()) {
                if (isGetter(method)) {
                    Method setter = findSetter(beanType, method);
                    if (setter != null) {
                        MapEntry entry = new MapEntry(method, setter);
                        entries.add(entry);
                    }
                }
            }
            return Collections.unmodifiableSet(entries);
        }

        private static boolean isGetter(Method method) {
            if (method.getParameterCount() > 0) {
                return false;
            }
            String name = method.getName();
            if (!name.startsWith("get") || name.length() <= 3) {
                return false;
            }
            return Character.isJavaIdentifierStart(name.charAt(3));
        }

        private static Method findSetter(Class<?> type, Method getter) {
            Class<?> parameterType = getter.getReturnType();
            String methodName = "set" + getter.getName().substring(3);
            try {
                return type.getMethod(methodName, parameterType);
            } catch (NoSuchMethodException | SecurityException e) {
                return null;
            }
        }

        /**
         * An entry of the map.
         *
         * @author leadpony
         */
        private class MapEntry implements Map.Entry<String, Object> {

            private final String name;
            private final Method getter;
            private final Method setter;

            /**
             * Constructs this entry.
             *
             * @param getter the getter method of the property.
             * @param setter the setter method of the property.
             */
            MapEntry(Method getter, Method setter) {
                this.name = extractPropertyName(getter);
                this.getter = getter;
                this.setter = setter;
            }

            @Override
            public String getKey() {
                return name;
            }

            @Override
            public Object getValue() {
                try {
                    return getter.invoke(bean);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public Object setValue(Object value) {
                Object oldValue = getValue();
                try {
                    setter.invoke(bean, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
                return oldValue;
            }

            @Override
            public int hashCode() {
                int hash = getKey().hashCode();
                Object value = getValue();
                return hash ^ (value == null ? 0 : value.hashCode());
            }

            @Override
            public boolean equals(Object other) {
                if (this == other) {
                    return true;
                } else if (!(other instanceof Map.Entry)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) other;
                if (!getKey().equals(entry.getKey())) {
                    return false;
                }
                Object thisValue = getValue();
                Object otherValue = entry.getValue();
                return (thisValue == null && otherValue == null)
                       || thisValue.equals(otherValue);
            }

            private String extractPropertyName(Method method) {
                String methodName = method.getName();
                return methodName.substring(3, 4).toLowerCase()
                        + methodName.substring(4);
            }
        }
    }
}
