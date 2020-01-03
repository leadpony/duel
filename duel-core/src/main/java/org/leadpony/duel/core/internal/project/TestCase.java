/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.duel.core.api.CaseExecution;
import org.leadpony.duel.core.api.Parameter;
import org.leadpony.duel.core.api.CaseNode;
import org.leadpony.duel.core.api.Node;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.MediaTypeParser;
import org.leadpony.duel.core.internal.common.UrlBuilder;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;
import org.opentest4j.IncompleteExecutionException;

/**
 * @author leadpony
 */
class TestCase extends AbstractNode implements CaseNode {

    static final String FILE_PATTERN = "*" + FILE_SUFFIX;
    static final int FILE_SUFFIX_LENGTH = FILE_SUFFIX.length();

    private final JsonObject request;

    TestCase(Path path,
            JsonObject json,
            JsonObject merged,
            JsonObject expanded) {
        super(path, json, merged, expanded);
        this.request = getParameterAsObject("request");
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return name;
        }
        String filename = getPath().getFileName().toString();
        return filename.substring(0, filename.length() - FILE_SUFFIX_LENGTH);
    }

    /* As a TestCase */

    @Override
    public URI getEndpointUrl() {
        return buildEndpointUri();
    }

    /* */

    CaseExecution createExecution(ExecutionContext context) {
        return new TestCaseExecution(context);
    }

    private Map<String, List<String>> getRequestParameter(String parameterName) {
        JsonValue parameterValue = this.request.getOrDefault(parameterName, JsonValue.NULL);
        if (parameterValue.getValueType() != ValueType.OBJECT) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new LinkedHashMap<>();
        parameterValue.asJsonObject().forEach((name, value) -> {
            List<String> values = new ArrayList<>();
            switch (value.getValueType()) {
            case ARRAY:
                value.asJsonArray().stream()
                    .map(item -> valueToString(item))
                    .forEach(values::add);
            default:
                values.add(valueToString(value));
                break;
            }
            map.put(name, values);
        });
        return map;
    }

    private Map<String, List<String>> getQuery() {
        return getRequestParameter("query");
    }

    private Map<String, List<String>> getRequestHeader() {
        return getRequestParameter("header");
    }

    private Optional<JsonValue> getRequestBody() {
        return Optional.ofNullable(this.request.get("body"));
    }

    private URI buildEndpointUri() {
        UrlBuilder builder = new UrlBuilder();
        builder.withScheme(getAsString(Parameter.SCHEME))
               .withHost(getAsString(Parameter.HOST));

        Object port = get(Parameter.PORT);
        if (port != null) {
            builder.withPort((int) port);
        }

        builder.withPath(getFullPath());
        builder.withQuery(getQuery());

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new IncompleteExecutionException(
                    Message.BAD_ENDPOINT_URL.asString(),
                    e);
        }
    }

    private String getFullPath() {
        return getAsString(Parameter.BASE_PATH) + getAsString(Parameter.PATH);
    }

    /**
     * @author leadpony
     */
    private class TestCaseExecution implements CaseExecution {

        private final ExecutionContext context;
        private final Assertion assertion;

        TestCaseExecution(ExecutionContext context) {
            this.context = context;
            this.assertion = context.getAssertionFactory().createAssertion(TestCase.this);
        }

        @Override
        public Node getNode() {
            return TestCase.this;
        }

        @Override
        public void run() {
            URI url = buildEndpointUri();
            HttpRequest request = buildRequest(url);
            HttpResponse<ResponseBody> response = sendRequest(request);
            validateResponse(response);
        }

        private HttpRequest buildRequest(URI url) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(url);
            getRequestHeader().forEach((name, values) -> {
                for (String value : values) {
                    builder.header(name, value);
                }
            });
            builder.method(getAsString(Parameter.METHOD), createRequestBodyPublisher(getRequestBody()));
            return builder.build();
        }

        private HttpResponse<ResponseBody> sendRequest(HttpRequest request) {
            HttpClient client = context.getHttpClient();
            try {
                return client.send(request, this::createBodySubscriber);
            } catch (IOException e) {
                throw new IncompleteExecutionException(
                        Message.NETWORK_FAILURE.format(request.uri()), e);
            } catch (InterruptedException e) {
                throw new IncompleteExecutionException(
                        Message.NETWORK_OPERATION_INTERRUPTED.asString(), e);
            }
        }

        private BodyPublisher createRequestBodyPublisher(Optional<JsonValue> body) {
            return body.map(value -> BodyPublishers.ofString(value.toString(), StandardCharsets.UTF_8))
                    .orElse(BodyPublishers.noBody());
        }

        private BodySubscriber<ResponseBody> createBodySubscriber(ResponseInfo responseInfo) {
            return BodySubscribers.mapping(
                    BodySubscribers.ofByteArray(),
                    byteArray -> createResponseBody(byteArray, responseInfo)
                    );
        }

        private ResponseBody createResponseBody(byte[] byteArray, ResponseInfo responseInfo) {
            return new ResponseBodyImpl(byteArray, parseMediaType(responseInfo));
        }

        private Optional<MediaType> parseMediaType(ResponseInfo responseInfo) {
            return responseInfo.headers().firstValue("content-type")
                .map(value -> new MediaTypeParser(value).parse());
        }

        private void validateResponse(HttpResponse<ResponseBody> response) {
            assertion.assertOn(response);
        }
    }
}
