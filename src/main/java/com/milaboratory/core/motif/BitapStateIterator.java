package com.milaboratory.core.motif;

import com.milaboratory.core.sequence.Sequence;

abstract class BitapStateIterator {
    final BitapData data;
    final Sequence sequence;
    int errors;
    final long[] R;
    final int to;
    int symbolsProcessed = 0;
    int current;
    boolean match;

    BitapStateIterator(BitapData data, Sequence sequence, int count, int from, int to) {
        if (sequence.getAlphabet().size() != data.patternMask.length)
            throw new IllegalArgumentException();
        this.data = data;
        this.sequence = sequence;
        this.R = new long[count];
        for (int i = 0; i < count; ++i)
            R[i] = (~0L) << i;
        this.to = to;
        this.current = from;
    }

    /**
     * Try to advance the state to the next position, returns false, if sequence boundary is reached.
     * {@link BitapStateIterator @match} shows whether tha match was found.
     */
    abstract boolean nextState();

    abstract int currentPosition();

    static final class ExactMatchStateIterator extends BitapStateIterator {
        public ExactMatchStateIterator(BitapData data, Sequence sequence, int from, int to) {
            super(data, sequence, 1, from, to);
            errors = 0;
        }

        @Override
        boolean nextState() {
            // Reset state
            match = false;

            // Check end of sequence
            if (current == to)
                return false;

            // Main part
            long matchingMask = (1L << (data.size - 1));
            R[0] <<= 1;
            R[0] |= data.patternMask[sequence.codeAt(current)];
            ++current;
            match = (0 == (R[0] & matchingMask));

            // Next state calculated
            return true;
        }

        @Override
        int currentPosition() {
            return current - data.size;
        }
    }

    static final class SubstitutionOnlyFirstStateIterator extends BitapStateIterator {
        public SubstitutionOnlyFirstStateIterator(BitapData data, Sequence sequence, int maxSubstitutions, int from, int to) {
            super(data, sequence, maxSubstitutions + 1, from, to);
        }

        @Override
        boolean nextState() {
            // Reset state
            match = false;

            // Check end of sequence
            if (current == to)
                return false;

            int d;
            long preMismatchTmp, mismatchTmp;

            // Main part
            long matchingMask = (1L << (data.size - 1));

            long currentPatternMask = data.patternMask[sequence.codeAt(current)];
            ++current;
            ++symbolsProcessed;

            // Exact match on the previous step == match with insertion on current step
            R[0] <<= 1;
            mismatchTmp = R[0];
            R[0] |= currentPatternMask;

            if (0 == (R[0] & matchingMask)) {
                errors = 0;
                match = true;
            }

            for (d = 1; d < R.length; ++d) {
                R[d] <<= 1;
                preMismatchTmp = R[d];
                R[d] |= currentPatternMask;
                R[d] &= mismatchTmp;
                if (!match && 0 == (R[d] & matchingMask) && symbolsProcessed >= data.size) {
                    errors = d;
                    match = true;
                }
                mismatchTmp = preMismatchTmp;
            }

            return true;
        }

        @Override
        int currentPosition() {
            assert current >= data.size;
            return current - data.size;
        }
    }

    static abstract class IndelStateIterator extends BitapStateIterator {
        public IndelStateIterator(BitapData data, Sequence sequence, int maxErrors, int from, int to) {
            super(data, sequence, maxErrors + 1, from, to);
        }

        void updateState(long currentPatternMask) {
            long matchingMask = (1L << (data.size - 1));

            long preInsertionTmp, preMismatchTmp,
                    insertionTmp, deletionTmp, mismatchTmp;

            // Exact match on the previous step == match with insertion on current step
            insertionTmp = R[0];
            R[0] <<= 1;
            mismatchTmp = R[0];
            R[0] |= currentPatternMask;
            deletionTmp = R[0];

            if (0 == (R[0] & matchingMask)) {
                errors = 0;
                match = true;
            }

            for (int d = 1; d < R.length; ++d) {
                preInsertionTmp = R[d];
                R[d] <<= 1;
                preMismatchTmp = R[d];
                R[d] |= currentPatternMask;
                R[d] &= insertionTmp & mismatchTmp & (deletionTmp << 1);
                if (!match && 0 == (R[d] & matchingMask) && symbolsProcessed >= data.size - R.length + 1) {
                    errors = d;
                    match = true;
                }
                deletionTmp = R[d];
                insertionTmp = preInsertionTmp;
                mismatchTmp = preMismatchTmp;
            }
        }
    }

    static final class SubstitutionAndIndelLastStateIterator extends IndelStateIterator {
        public SubstitutionAndIndelLastStateIterator(BitapData data, Sequence sequence, int maxErrors, int from, int to) {
            super(data, sequence, maxErrors, from, to);
        }

        @Override
        boolean nextState() {
            // Reset state
            match = false;

            // Check end of sequence
            if (current == to)
                return false;

            // Main part
            ++symbolsProcessed;
            updateState(data.patternMask[sequence.codeAt(current++)]);

            return true;
        }

        @Override
        int currentPosition() {
            return current - 1;
        }
    }

    static final class SubstitutionAndIndelFirstStateIterator extends IndelStateIterator {
        public SubstitutionAndIndelFirstStateIterator(BitapData data, Sequence sequence, int maxErrors, int from, int to) {
            super(data, sequence, maxErrors, to - 1, from);
        }

        @Override
        boolean nextState() {
            // Reset state
            match = false;

            // Check end of sequence
            if (current == to - 1)
                return false;

            // Main part
            ++symbolsProcessed;
            updateState(data.reversePatternMask[sequence.codeAt(current--)]);

            return true;
        }

        @Override
        int currentPosition() {
            return current + 1;
        }
    }
}
