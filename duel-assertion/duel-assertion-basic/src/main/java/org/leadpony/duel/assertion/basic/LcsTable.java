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

import javax.json.JsonArray;

/**
 * @author leadpony
 */
class LcsTable {

    private final int[][] lengths;
    private final int sourceSize;
    private final int targetSize;

    static LcsTable build(
            JsonArray source, JsonArray target, int sourceSize, int targetSize,
            JsonMatcher matcher) {

        int[][] lengths = new int[sourceSize + 1][targetSize + 1];

        for (int i = 1; i <= sourceSize; i++) {
            for (int j = 1; j <= targetSize; j++) {
                if (matcher.match(source.get(i - 1), target.get(j - 1))) {
                    // negative value for matched pair
                    lengths[i][j] = -(Math.abs(lengths[i - 1][j - 1]) + 1);
                } else {
                    int a = Math.abs(lengths[i - 1][j]);
                    int b = Math.abs(lengths[i][j - 1]);
                    lengths[i][j] = Math.max(a, b);
                }
            }
        }
        return new LcsTable(lengths, sourceSize, targetSize);
    }

    private LcsTable(int[][] lengths, int sourceSize, int targetSize) {
        this.lengths = lengths;
        this.sourceSize = sourceSize;
        this.targetSize = targetSize;
    }

    final int getSourceSize() {
        return sourceSize;
    }

    final int getTargetSize() {
        return targetSize;
    }

    final boolean getMatchResult(int i, int j) {
        return lengths[i][j] < 0;
    }

    final int getLength(int i, int j) {
        return Math.abs(lengths[i][j]);
    }
}
