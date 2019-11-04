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

package org.leadpony.duel.tests.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author leadpony
 */
@SuppressWarnings("serial")
public class JsonServlet extends HttpServlet {

    private final JsonObject object;
    private final JsonWriterFactory writerFactory;

    public JsonServlet() {
        this.object = buildJsonObject();
        this.writerFactory = Json.createWriterFactory(Collections.emptyMap());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        writeJson(response, this.object);
    }

    private void writeJson(HttpServletResponse response, JsonValue value) throws IOException {
        try (JsonWriter writer = writerFactory.createWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(value);
        }
    }

    private JsonObject buildJsonObject() {
        JsonBuilderFactory f = Json.createBuilderFactory(Collections.emptyMap());
        return f.createObjectBuilder()
                .add("Image", f.createObjectBuilder()
                        .add("Width", 800)
                        .add("Height", 600)
                        .add("Title", "View from 15th Floor")
                        .add("Thumbnail", f.createObjectBuilder()
                                .add("Url", "http://www.example.com/image/481989943")
                                .add("Height", 125)
                                .add("Width", 100)
                                )
                        .add("Animated", false)
                        .add("IDs", f.createArrayBuilder()
                                .add(116)
                                .add(943)
                                .add(234)
                                .add(38793)
                                )
                        )
                .build();
    }
}
