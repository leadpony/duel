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

package org.leadpony.duel.assertion.basic;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonValue;

import org.leadpony.duel.core.spi.ResponseBody;

/**
 * @author leadpony
 */
class JsonBodyAssertion extends AbstractAssertion {

    private final JsonValue expected;

    JsonBodyAssertion(JsonValue expected) {
        this.expected = expected;
    }

    @Override
    public void assertOn(HttpResponse<ResponseBody> response) {
        ReportingJsonMatcher validator = new ReportingJsonMatcher();
        JsonValue actual = response.body().asJson();
        if (!validator.match(this.expected, actual)) {
            fail(buildErrorMessage(validator.getProblems()), expected, actual);
        }
    }

    private static String buildErrorMessage(List<JsonProblem> problems) {
        String detail = problems.stream()
                .map(JsonBodyAssertion::renderProblem)
                .collect(Collectors.joining("\n"));
        return Message.thatJsonBodyDoesNotMatch(detail);
    }

    private static String renderProblem(JsonProblem problem) {
        return new StringBuilder()
                .append('[')
                .append(problem.getPointer())
                .append(']')
                .append(' ')
                .append(problem.getDescription())
                .toString();
    }
}
