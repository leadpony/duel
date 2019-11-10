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

package org.leadpony.duel.fake.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author leadpony
 */
@SuppressWarnings("serial")
public class DiagnosticServlet extends HttpServlet {

    private JsonBuilderFactory builderFactory;
    private JsonWriterFactory writerFactory;

    private static final Map<String, ?> WRITER_CONFIG;

    static {
        Map<String, Object> writerConfig = new HashMap<>();
        writerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
        WRITER_CONFIG = Collections.unmodifiableMap(writerConfig);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        builderFactory = Json.createBuilderFactory(Collections.emptyMap());
        writerFactory = Json.createWriterFactory(WRITER_CONFIG);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        JsonObject json = buildJson(request);
        writeJson(response.getOutputStream(), json);
    }

    private JsonObject buildJson(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }

        final boolean full = path.equals("/");

        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        if (full || path.startsWith("/url")) {
            builder.add("url", request.getRequestURI());
        }

        if (full || path.startsWith("/method")) {
            builder.add("method", request.getMethod());
        }

        if (full || path.startsWith("/header")) {
            builder.add("header", buildHeaderObject(request));
        }

        if (full || path.startsWith("/parameters")) {
            builder.add("parameters", buildParametersObject(request));
        }

        return builder.build();
    }

    private JsonObject buildHeaderObject(HttpServletRequest request) {
        JsonObjectBuilder header = builderFactory.createObjectBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(name -> {
            List<String> values = asList(request.getHeaders(name));
            if (values.size() > 1) {
                JsonArrayBuilder array = builderFactory.createArrayBuilder();
                values.forEach(array::add);
                header.add(name, array);
            } else {
                header.add(name, values.get(0));
            }
        });
        return header.build();
    }

    private JsonObject buildParametersObject(HttpServletRequest request) {
        JsonObjectBuilder header = builderFactory.createObjectBuilder();
        request.getParameterNames().asIterator().forEachRemaining(name -> {
            String[] values = request.getParameterValues(name);
            if (values.length > 1) {
                JsonArrayBuilder array = builderFactory.createArrayBuilder();
                for (String value : values) {
                    array.add(value);
                }
                header.add(name, array);
            } else {
                header.add(name, values[0]);
            }
        });
        return header.build();
    }

    private void writeJson(OutputStream out, JsonObject json) {
        try (JsonWriter writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)) {
            writer.writeObject(json);
        }
    }

    private static List<String> asList(Enumeration<String> e) {
        List<String> list = new ArrayList<>();
        while (e.hasMoreElements()) {
            list.add(e.nextElement());
        }
        return list;
    }
}
