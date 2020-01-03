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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.params.ParameterizedTest;

/**
 * @author leadpony
 */
public class ReportingJsonMatcherTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @JsonSource("ReportingJsonMatcherTest.json")
    public void validateShouldFindProblemsAsExpected(String title,
            JsonValue expected, JsonValue actual, List<JsonObject> problems) {
        ReportingJsonMatcher matcher = new ReportingJsonMatcher("@");
        boolean result = matcher.match(expected, actual);
        if (result) {
            assertThat(problems).isEmpty();
        } else {
            List<JsonObject> actualProblems = matcher.getProblems()
                    .stream()
                    .map(JsonProblem::toJson)
                    .collect(Collectors.toList());
            assertThat(actualProblems).isEqualTo(problems);

            printProblems(matcher.getProblems());
        }
    }

    private static void printProblems(List<JsonProblem> problems) {
        for (JsonProblem problem : problems) {
            System.out.println(problem.toString());
        }
    }
}
