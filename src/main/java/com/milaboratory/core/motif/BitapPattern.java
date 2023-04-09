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

import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.util.BitArray;

/**
 * Use {@link Motif#getBitapPattern()} to create bitap pattern.
 */
public final class BitapPattern implements java.io.Serializable {
    final Motif motif;
    final BitapData
            mainData,
            secondaryData;
    /** Masks positions which must match the underlying sequence even for fuzzy searches */
    final BitArray exactMask;

    /** Approximate bit scoring */
    final double[] matchScore, mismatchScore;
    final double averageMismatchPenalty;

    /**
     * Use {@link Motif#getBitapPattern()} to create bitap pattern.
     */
    BitapPattern(Motif motif, BitapData mainData, BitArray exactMask) {
        this.motif = motif;
        this.mainData = mainData;
        this.exactMask = exactMask;
        if (exactMask == null) {
            this.secondaryData = null;
            exactMask = new BitArray(motif.size());
        } else
            this.secondaryData = mainData.toSecondary(exactMask);

        MotifWithExactMask me = new MotifWithExactMask(motif, exactMask);
        this.matchScore = me.defaultMatchScore;
        this.mismatchScore = me.mismatchScore;
        this.averageMismatchPenalty = me.averageMismatchPenalty;
    }

    /** Positive value. Average difference between exact match bit-scoring and match with a single substitution. */
    public double getAverageMismatchPenalty() {
        return averageMismatchPenalty;
    }

    public int exactSearch(Sequence sequence) {
        return exactSearch(sequence, 0, sequence.size());
    }

    public int exactSearch(Sequence sequence, int from) {
        return exactSearch(sequence, from, sequence.size());
    }

    public int exactSearch(Sequence sequence, int from, int to) {
        return exactMatcher(sequence, from, to).findNext();
    }

    private final class BitapMatcherWithoutIndels extends BitapMatcherImpl implements BitapMatcherWithScore {
        final Sequence sequence;

        public BitapMatcherWithoutIndels(Sequence sequence, BitapStateIterator mainState, BitapStateIterator secondaryState) {
            super(mainState, secondaryState);
            this.sequence = sequence;
        }

        public BitapMatcherWithoutIndels(Sequence sequence, BitapStateIterator mainState) {
            super(mainState);
            this.sequence = sequence;
        }

        @Override
        public double getBitScore() {
            if (!mainState.match)
                throw new IllegalStateException("Bit-score available only when match is found.");
            double score = 0.0;
            double[] costArray = motif.getCostArray();
            int basicLetters = motif.alphabet.basicSize();
            for (int i = mainState.currentPosition(), j = 0; j < mainState.data.size; i++, j++) {
                byte code = sequence.codeAt(i);
                if (motif.allows(code, j)) {
                    if (code < basicLetters) {
                        // Normal match case (non-wildcard letter in target sequence)
                        score += matchScore[j];
                    } else {
                        // Wildcard letter in target sequence,
                        // counting scoring based on the widest wildcard
                        int widestWildcard = Math.max(motif.alphabet.codeToWildcard(code).basicSize(), motif.allowedBasicCodes(j));
                        score -= costArray[widestWildcard - 1];
                    }
                } else
                    score += mismatchScore[j];
            }
            return score;
        }

        @Override
        public double getBitScoreCost() {
            return motif.matchBitScore() - getBitScore();
        }
    }

    public BitapMatcherWithScore exactMatcher(final Sequence sequence, final int from, final int to) {
        return new BitapMatcherWithoutIndels(
                sequence,
                new BitapStateIterator.ExactMatchStateIterator(mainData, sequence, from, to)
        );
    }

    private BitapStateIterator secondaryIterator(Sequence sequence, int from, int to) {
        return secondaryData == null ? null : new BitapStateIterator.ExactMatchStateIterator(secondaryData, sequence, from, to);
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a whole {@code sequence}. Search allows no more than {@code
     * substitutions} number of substitutions. Matcher will return positions of first matched letter in the motif in
     * ascending order.
     *
     * @param maxSubstitutions maximal number of allowed substitutions
     * @param sequence         target sequence
     * @return matcher which will return positions of first matched letter in the motif in ascending order
     */
    public BitapMatcherWithScore substitutionOnlyMatcherFirst(int maxSubstitutions, Sequence sequence) {
        return substitutionOnlyMatcherFirst(maxSubstitutions, sequence, 0, sequence.size());
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a subsequence of {@code sequence}. Search range starts from
     * {@code from} (inclusive) and ends at {@code to} (exclusive). Search allows no more than {@code maxSubstitutions}
     * number of substitutions. Matcher will return positions of first matched letter in the motif in ascending order.
     *
     * @param maxSubstitutions maximal number of allowed substitutions
     * @param sequence         target sequence
     * @param from             left boundary of search range (inclusive)
     * @param to               right boundary of search range (exclusive)
     * @return matcher which will return positions of first matched letter in the motif in ascending order
     */
    public BitapMatcherWithScore substitutionOnlyMatcherFirst(int maxSubstitutions, Sequence sequence, int from, int to) {
        return new BitapMatcherWithoutIndels(
                sequence,
                new BitapStateIterator.SubstitutionOnlyFirstStateIterator(mainData, sequence, maxSubstitutions, from, to),
                secondaryIterator(sequence, from, to)
        );
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a whole {@code sequence}.  Search allows no more than {@code
     * maxNumberOfErrors} number of substitutions/insertions/deletions. Matcher will return positions of last matched
     * letter in the motif in ascending order.
     *
     * @param maxNumberOfErrors maximal number of allowed substitutions/insertions/deletions
     * @param sequence          target sequence
     * @return matcher which will return positions of last matched letter in the motif
     */
    public BitapMatcher substitutionAndIndelMatcherLast(int maxNumberOfErrors, final Sequence sequence) {
        return substitutionAndIndelMatcherLast(maxNumberOfErrors, sequence, 0, sequence.size());
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a subsequence of {@code sequence}. Search range starts from
     * {@code from} (inclusive) and ends at {@code to} (exclusive). Search allows no more than {@code
     * maxNumberOfErrors}
     * number of substitutions/insertions/deletions. Matcher will return positions of last matched letter in the motif
     * in ascending order.
     *
     * @param maxNumberOfErrors maximal number of allowed substitutions/insertions/deletions
     * @param sequence          target sequence
     * @param from              left boundary of search range (inclusive)
     * @param to                right boundary of search range (exclusive)
     * @return matcher which will return positions of last matched letter in the motif in ascending order
     */
    public BitapMatcher substitutionAndIndelMatcherLast(int maxNumberOfErrors, final Sequence sequence, int from, int to) {
        if (secondaryData != null)
            throw new IllegalStateException("Not implemented");
        return new BitapMatcherImpl(
                new BitapStateIterator.SubstitutionAndIndelLastStateIterator(mainData, sequence, maxNumberOfErrors, from, to),
                secondaryIterator(sequence, from, to)
        );
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a whole {@code sequence}. Search allows no more than {@code
     * maxNumberOfErrors} number of substitutions/insertions/deletions. Matcher will return positions of first matched
     * letter in the motif in descending order.
     *
     * @param maxNumberOfErrors maximal number of allowed substitutions/insertions/deletions
     * @param sequence          target sequence
     * @return matcher which will return positions of first matched letter in the motif in descending order
     */
    public BitapMatcher substitutionAndIndelMatcherFirst(int maxNumberOfErrors, final Sequence sequence) {
        return substitutionAndIndelMatcherFirst(maxNumberOfErrors, sequence, 0, sequence.size());
    }

    /**
     * Returns a BitapMatcher preforming a fuzzy search in a subsequence of {@code sequence}. Search range starts from
     * {@code from} (inclusive) and ends at {@code to} (exclusive). Search allows no more than {@code
     * maxNumberOfErrors} number of substitutions/insertions/deletions. Matcher will return positions of first matched
     * letter in the motif in descending order.
     *
     * @param maxNumberOfErrors maximal number of allowed substitutions/insertions/deletions
     * @param sequence          target sequence
     * @param from              left boundary of search range (inclusive)
     * @param to                right boundary of search range (exclusive)
     * @return matcher which will return positions of first matched letter in the motif in descending order
     */
    public BitapMatcher substitutionAndIndelMatcherFirst(int maxNumberOfErrors, final Sequence sequence, int from, int to) {
        if (secondaryData != null)
            throw new IllegalStateException("Not implemented");
        return new BitapMatcherImpl(
                new BitapStateIterator.SubstitutionAndIndelFirstStateIterator(mainData, sequence, maxNumberOfErrors, from, to)
        );
    }
}
