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
import java.net.URLEncoder;
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

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestGroup;
import org.leadpony.duel.core.api.TestNode;
import org.leadpony.duel.core.api.TestException;
import org.leadpony.duel.core.internal.common.MediaTypeParser;
import org.leadpony.duel.core.internal.config.TestCaseConfig;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class TestCaseImpl extends AbstractTestNode implements TestCase {

    static final String FILE_PATTERN = "*.test.json";

    private final TestCaseConfig config;
    private final TestGroup parent;
    private final Assertion assertion;

    TestCaseImpl(Path path, TestCaseConfig config, TestContext context, TestGroup parent) {
        super(path, context);
        this.config = config;
        this.parent = parent;
        this.assertion = context.getAssertionFactory()
                .createAssertion(getConfig().getResponse());
    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = getConfig().getName();
        if (name == null) {
            name = super.getName();
        }
        return name;
    }

    @Override
    public Optional<TestNode> getParent() {
        return Optional.of(parent);
    }

    /* As a TestCase */

    @Override
    public URI getEndpointUrl() {
        URI path = URI.create(getConfig().getPath());
        return getAbsoluteUrl(path);
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

    private HttpRequest buildRequest() {
        TestCaseConfig.Request request = config.getRequest();
        RequestBuilder builder = new RequestBuilder(getEndpointUrl(), config.getMethod());
        builder.addQuery(request.getQuery())
               .addHeader(request.getHeader());
        request.getBody().ifPresent(builder::addBody);
        return builder.build();
    }

    private HttpResponse<ResponseBody> sendRequest(HttpRequest request) {
        HttpClient client = getContext().getHttpClient();
        try {
            return client.send(request, this::createBodySubscriber);
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new TestException(e.getMessage(), e);
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

    private URI getAbsoluteUrl(URI url) {
        if (url.isAbsolute()) {
            return url;
        }
        URI base = getBaseUrl();
        String path = base.getPath();
        if (path != null && !path.endsWith("/")) {
            path = path + "/";
        }
        try {
            base = new URI(base.getScheme(),
                    base.getUserInfo(),
                    base.getHost(),
                    base.getPort(),
                    path,
                    null,
                    null);
            return base.resolve(url);
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * @author leadpony
     */
    private class RequestBuilder {

        private final HttpRequest.Builder underlying = HttpRequest.newBuilder();
        private final URI url;
        private final String method;
        private BodyPublisher bodyPublisher = BodyPublishers.noBody();

        RequestBuilder(URI url, String method) {
            this.url = url;
            this.method = method;
        }

        RequestBuilder addQuery(Map<String, List<String>> query) {
            underlying.uri(composeUrl(query));
            return this;
        }

        RequestBuilder addHeader(Map<String, List<String>> header) {
            header.forEach((name, values) -> {
                for (String value : values) {
                    underlying.header(name, value);
                }
            });
            return this;
        }

        RequestBuilder addBody(JsonValue body) {
            bodyPublisher = BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
            return this;
        }

        HttpRequest build() {
            underlying.method(method, bodyPublisher);
            return underlying.build();
        }

        private URI composeUrl(Map<String, List<String>> query) {
            if (query.isEmpty()) {
                return this.url;
            }
            StringBuilder builder = new StringBuilder(this.url.toString());
            char seprator = '?';
            for (var entry : query.entrySet()) {
                String key = entry.getKey();
                for (String value : entry.getValue()) {
                    builder.append(seprator)
                        .append(encode(key))
                        .append('=')
                        .append(encode(value));
                }
                seprator = '&';
            }
            return URI.create(builder.toString());
        }
    }

    private static String encode(String original) {
        return URLEncoder.encode(original, StandardCharsets.UTF_8);
    }
}
