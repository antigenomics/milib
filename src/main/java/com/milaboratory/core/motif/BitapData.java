package com.milaboratory.core.motif;

import com.milaboratory.util.BitArray;

import java.io.Serializable;

final class BitapData implements Serializable {
    final int size;
    final long[] patternMask;
    final long[] reversePatternMask;

    BitapData(int size, long[] patternMask, long[] reversePatternMask) {
        if (patternMask.length != reversePatternMask.length)
            throw new IllegalArgumentException();

        this.size = size;
        this.patternMask = patternMask;
        this.reversePatternMask = reversePatternMask;
    }

    BitapData toSecondary(BitArray exactMask) {
        if (exactMask.size() != size)
            throw new IllegalArgumentException();

        long[] newPatternMask = patternMask.clone();
        long[] newReversePatternMask = reversePatternMask.clone();

        for (int j = 0; j < size; ++j)
            if (!exactMask.get(j))
                for (int i = 0; i < patternMask.length; ++i) {
                    newPatternMask[i] &= ~(1L << j);
                    newReversePatternMask[i] &= ~(1L << (size - j - 1));
                }
        return new BitapData(size, newPatternMask, newReversePatternMask);
    }
}
