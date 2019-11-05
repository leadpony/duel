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
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.json.bind.Jsonb;

import org.leadpony.duel.core.api.TestCase;
import org.leadpony.duel.core.api.TestContainer;
import org.leadpony.duel.core.api.TestException;
import org.leadpony.duel.core.internal.common.MediaTypeParser;
import org.leadpony.duel.core.internal.config.TestCaseConfig;
import org.leadpony.duel.core.spi.Assertion;
import org.leadpony.duel.core.spi.MediaType;
import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class BasicTestCase extends BasicTestNode implements TestCase {

    static final String FILE_PATTERN = "*.test.json";

    private final TestCaseConfig config;
    private final Assertion assertion;

    BasicTestCase(Path path, TestContext context, TestContainer parent) {
        super(path, context, parent);
        this.config = loadConfig(path);
        this.assertion = context.getAssertionFactory()
                .createAssertion(config.getResponse());

    }

    /* As a TestNode */

    @Override
    public String getName() {
        String name = config.getName();
        if (name == null) {
            name = super.getName();
        }
        return name;
    }

    /* As a TestCase */

    @Override
    public URI getEndpointUrl() {
        URI path = config.getPath();
        if (path.isAbsolute()) {
            return path;
        } else {
            URI baseUrl = getContext().getProject().getBaseUrl();
            return baseUrl.resolve(path);
        }
    }

    @Override
    public void run() {
        runTest();
    }

    private void runTest() {
        HttpRequest request = buildRequest();
        HttpResponse<ResponseBody> response = sendRequest(request);
        validateResponse(response);
    }

    private TestCaseConfig loadConfig(Path path) {
        Jsonb jsonb = getContext().getJsonBinder();
        try (InputStream in = Files.newInputStream(path)) {
            return jsonb.fromJson(in, TestCaseConfig.class);
        } catch (IOException e) {
            throw new TestException(e.getMessage(), e);
        }
    }

    private HttpRequest buildRequest() {
        HttpRequest.Builder builder = HttpRequest.newBuilder(getEndpointUrl());
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
                    byteArray -> {
                        return new ResponseBodyImpl(byteArray,
                                parseMediaType(responseInfo),
                                getContext());
                    });
    }

    private Optional<MediaType> parseMediaType(ResponseInfo responseInfo) {
        return responseInfo.headers().firstValue("content-type")
            .map(value -> new MediaTypeParser(value).parse());
    }

    private void validateResponse(HttpResponse<ResponseBody> response) {
        assertion.assertOn(response);
    }
}
