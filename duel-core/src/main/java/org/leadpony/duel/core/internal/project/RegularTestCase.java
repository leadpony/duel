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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonValue;

import org.leadpony.duel.core.api.Parameter;
import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;
import org.leadpony.duel.core.api.TestNode;
import org.leadpony.duel.core.internal.Message;
import org.leadpony.duel.core.internal.common.MediaTypeParser;
import org.leadpony.duel.core.internal.common.UrlBuilder;
import org.leadpony.duel.core.internal.config.TestCaseConfig;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;
import org.opentest4j.IncompleteExecutionException;

/**
 * @author leadpony
 */
class RegularTestCase extends AbstractRegularTestNode implements TestCase {

    static final String FILE_PATTERN = "*" + FILE_SUFFIX;

    private final TestCaseConfig config;
    private final TestGroup parent;
    private final Assertion assertion;

    RegularTestCase(Path path, TestCaseConfig config, TestContext context, TestGroup parent) {
        super(path, context);
        this.config = config;
        this.parent = parent;
        this.assertion = context.getAssertionFactory()
                .createAssertion(getConfig().getResponse());
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return name;
        }
        return TestCase.super.getName();
    }

    @Override
    public Optional<TestNode> getParent() {
        return Optional.of(parent);
    }

    /* As a TestCase */

    @Override
    public URI getEndpointUrl() {
        return buildEndpointUri();
    }

    @Override
    public void run() {
        runTest();
    }

    /* As a AbstractTestNode */

    @Override
    TestCaseConfig getConfig() {
        return config;
    }

    private void runTest() {
        HttpRequest request = buildRequest();
        HttpResponse<ResponseBody> response = sendRequest(request);
        validateResponse(response);
    }

    private TestCaseConfig.Request getRequestConfig() {
        return getConfig().getRequest();
    }

    private HttpRequest buildRequest() {
        TestCaseConfig.Request request = getRequestConfig();
        RequestBuilder builder = new RequestBuilder(getEndpointUrl(), config.getMethod());
        builder.addHeader(request.getHeader());
        request.getBody().ifPresent(builder::addBody);
        return builder.build();
    }

    private HttpResponse<ResponseBody> sendRequest(HttpRequest request) {
        HttpClient client = getContext().getHttpClient();
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

    private URI buildEndpointUri() {
        UrlBuilder builder = new UrlBuilder();
        builder.withScheme(getAsString(Parameter.SCHEME))
               .withHost(getAsString(Parameter.HOST));

        Object port = get(Parameter.PORT);
        if (port != null) {
            builder.withPort((int) port);
        }

        builder.withPath(getFullPath());
        builder.withQuery(getRequestConfig().getQuery());

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new IncompleteExecutionException(
                    Message.BAD_ENDPOINT_URL.asString(),
                    e);
        }
    }

    private String getFullPath() {
        return getAsString(Parameter.BASE_PATH) + getConfig().getPath();
    }

    /**
     * @author leadpony
     */
    private class RequestBuilder {

        private final HttpRequest.Builder builder = HttpRequest.newBuilder();
        private final String method;
        private BodyPublisher bodyPublisher = BodyPublishers.noBody();

        RequestBuilder(URI url, String method) {
            this.builder.uri(url);
            this.method = method;
        }

        RequestBuilder addHeader(Map<String, List<String>> header) {
            header.forEach((name, values) -> {
                for (String value : values) {
                    builder.header(name, value);
                }
            });
            return this;
        }

        RequestBuilder addBody(JsonValue body) {
            bodyPublisher = BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
            return this;
        }

        HttpRequest build() {
            builder.method(method, bodyPublisher);
            return builder.build();
        }
    }
}
