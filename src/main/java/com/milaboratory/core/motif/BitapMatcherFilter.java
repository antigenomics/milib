/*
 * Copyright 2015 MiLaboratory.com
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
package com.milaboratory.core.motif;

import java.util.Arrays;

import static java.lang.System.arraycopy;

public final class BitapMatcherFilter implements BitapMatcherWithScore, java.io.Serializable {
    final BitapMatcher nestedMatcher;
    final int[] positionsBuffer;
    final int[] errorsBuffer;
    final double[] bitScore;
    final double[] bitScoreCost;

    public BitapMatcherFilter(BitapMatcher nestedMatcher) {
        this.nestedMatcher = nestedMatcher;

        this.positionsBuffer = new int[3];
        Arrays.fill(this.positionsBuffer, -1);
        this.errorsBuffer = new int[3];
        Arrays.fill(this.errorsBuffer, -1);

        if (nestedMatcher instanceof BitapMatcherWithScore) {
            this.bitScore = new double[3];
            Arrays.fill(this.bitScore, Double.NaN);
            this.bitScoreCost = new double[3];
            Arrays.fill(this.bitScoreCost, Double.NaN);
        } else {
            this.bitScore = null;
            this.bitScoreCost = null;
        }

        next();
    }

    private void next() {
        for (int i = 0; i < 2; ++i) {
            positionsBuffer[i] = positionsBuffer[i + 1];
            errorsBuffer[i] = errorsBuffer[i + 1];
            if (bitScore != null) {
                bitScore[i] = bitScore[i + 1];
                bitScoreCost[i] = bitScoreCost[i + 1];
            }
        }

        int pos = nestedMatcher.findNext();
        if (pos == -1) {
            positionsBuffer[2] = -1;
            errorsBuffer[2] = -1;
            if (bitScore != null) {
                bitScore[2] = Double.NaN;
                bitScoreCost[2] = Double.NaN;
            }
        } else {
            positionsBuffer[2] = pos;
            errorsBuffer[2] = nestedMatcher.getNumberOfErrors();
            if (bitScore != null) {
                BitapMatcherWithScore nestedMatcher = (BitapMatcherWithScore) this.nestedMatcher;
                bitScore[2] = nestedMatcher.getBitScore();
                bitScoreCost[2] = nestedMatcher.getBitScoreCost();
            }
        }
    }

    @Override
    public int findNext() {
        while (true) {
            next();

            if (positionsBuffer[0] != -1 &&
                    Math.abs(positionsBuffer[0] - positionsBuffer[1]) == 1
                    && errorsBuffer[0] + 1 == errorsBuffer[1])
                continue;

            if (positionsBuffer[2] != -1 &&
                    Math.abs(positionsBuffer[1] - positionsBuffer[2]) == 1
                    && errorsBuffer[1] == errorsBuffer[2] + 1)
                continue;

            return positionsBuffer[1];
        }
    }

    @Override
    public int getNumberOfErrors() {
        return errorsBuffer[1];
    }

    @Override
    public double getBitScore() {
        if (bitScore == null)
            throw new IllegalStateException("Underlying matcher has no scoring information.");
        return bitScore[1];
    }

    @Override
    public double getBitScoreCost() {
        if (bitScore == null)
            throw new IllegalStateException("Underlying matcher has no scoring information.");
        return bitScoreCost[1];
    }
}
