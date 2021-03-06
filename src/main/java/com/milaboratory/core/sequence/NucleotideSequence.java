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
package com.milaboratory.core.sequence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.core.Range;

import java.util.Arrays;

/**
 * Representation of nucleotide sequence.
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @author Shugay Mikhail (mikhail.shugay@gmail.com)
 * @see com.milaboratory.core.sequence.Sequence
 * @see com.milaboratory.core.sequence.NucleotideAlphabet
 */
@JsonSerialize(using = IO.NSeqSerializer.class)
@JsonDeserialize(using = IO.NSeqDeserializer.class)
public final class NucleotideSequence extends AbstractArraySequence<NucleotideSequence>
        implements NSeq<NucleotideSequence>, java.io.Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Nucleotide alphabet
     */
    public static final NucleotideAlphabet ALPHABET = NucleotideAlphabet.INSTANCE;

    /**
     * Empty instance
     */
    public static final NucleotideSequence EMPTY = new NucleotideSequence("");

    /**
     * Single letter sequence A
     */
    public static final NucleotideSequence A = new NucleotideSequence(new byte[]{NucleotideAlphabet.A});

    /**
     * Single letter sequence T
     */
    public static final NucleotideSequence T = new NucleotideSequence(new byte[]{NucleotideAlphabet.T});

    /**
     * Single letter sequence G
     */
    public static final NucleotideSequence G = new NucleotideSequence(new byte[]{NucleotideAlphabet.G});

    /**
     * Single letter sequence C
     */
    public static final NucleotideSequence C = new NucleotideSequence(new byte[]{NucleotideAlphabet.C});

    /**
     * Single letter sequence R
     */
    public static final NucleotideSequence R = new NucleotideSequence(new byte[]{NucleotideAlphabet.R});

    /**
     * Single letter sequence Y
     */
    public static final NucleotideSequence Y = new NucleotideSequence(new byte[]{NucleotideAlphabet.Y});

    /**
     * Single letter sequence S
     */
    public static final NucleotideSequence S = new NucleotideSequence(new byte[]{NucleotideAlphabet.S});

    /**
     * Single letter sequence W
     */
    public static final NucleotideSequence W = new NucleotideSequence(new byte[]{NucleotideAlphabet.W});

    /**
     * Single letter sequence K
     */
    public static final NucleotideSequence K = new NucleotideSequence(new byte[]{NucleotideAlphabet.K});

    /**
     * Single letter sequence M
     */
    public static final NucleotideSequence M = new NucleotideSequence(new byte[]{NucleotideAlphabet.M});

    /**
     * Single letter sequence B
     */
    public static final NucleotideSequence B = new NucleotideSequence(new byte[]{NucleotideAlphabet.B});

    /**
     * Single letter sequence D
     */
    public static final NucleotideSequence D = new NucleotideSequence(new byte[]{NucleotideAlphabet.D});

    /**
     * Single letter sequence H
     */
    public static final NucleotideSequence H = new NucleotideSequence(new byte[]{NucleotideAlphabet.H});

    /**
     * Single letter sequence V
     */
    public static final NucleotideSequence V = new NucleotideSequence(new byte[]{NucleotideAlphabet.V});

    /**
     * Single letter sequence N
     */
    public static final NucleotideSequence N = new NucleotideSequence(new byte[]{NucleotideAlphabet.N});

    static final NucleotideSequence[] ONE_LETTER_SEQUENCES = {A, G, C, T, N, R, Y, S, W, K, M, B, D, H, V};

    static NucleotideSequence getOneLetterSequence(byte letter) {
        return ONE_LETTER_SEQUENCES[letter];
    }

    /**
     * Creates nucleotide sequence from its string representation (e.g. "ATCGG" or "atcgg").
     *
     * @param sequence string representation of sequence (case insensitive)
     * @throws java.lang.IllegalArgumentException if sequence contains unknown nucleotide symbol
     */
    public NucleotideSequence(String sequence) {
        super(sequence);
    }

    /**
     * Creates nucleotide sequence from char array of nucleotides (e.g. ['A','T','C','G','G']).
     *
     * @param sequence char array of nucleotides
     * @throws java.lang.IllegalArgumentException if sequence contains unknown nucleotide symbol
     */
    public NucleotideSequence(char[] sequence) {
        super(sequence);
    }

    /**
     * Creates nucleotide sequence from specified {@code Bit2Array} (will be copied in constructor).
     *
     * @param data Bit2Array
     */
    public NucleotideSequence(byte[] data) {
        super(data.clone());
    }

    NucleotideSequence(byte[] data, boolean unsafe) {
        super(data);
        assert unsafe;
    }

    @Override
    public NucleotideSequence getRange(Range range) {
        if (range.getLower() < 0 || range.getUpper() < 0
                || range.getLower() > size() || range.getUpper() > size())
            throw new IndexOutOfBoundsException("Range = " + range + "; size = " + size());

        if (range.length() == 0)
            return EMPTY;

        if (range.length() == 1)
            return range.isReverse()
                    ? getOneLetterSequence(NucleotideAlphabet.complementCode(data[range.getLower()]))
                    : getOneLetterSequence(data[range.getLower()]);

        if (range.isReverse())
            return new NucleotideSequence(
                    transformToRC(data, range.getLower(), range.getUpper()), true);
        else
            return super.getRange(range);
    }

    /**
     * Returns reverse complement of this sequence.
     *
     * @return reverse complement sequence
     */
    @Override
    public NucleotideSequence getReverseComplement() {
        return new NucleotideSequence(transformToRC(data, 0, data.length), true);
    }

    /**
     * Returns {@literal true} if sequence contains wildcards in specified region.
     *
     * @return {@literal true} if sequence contains wildcards in specified region
     */
    public boolean containsWildcards(int from, int to) {
        for (int i = from; i < to; i++)
            if (isWildcard(codeAt(i)))
                return true;
        return false;
    }

    /**
     * Returns {@literal true} if sequence contains wildcards.
     *
     * @return {@literal true} if sequence contains wildcards
     */
    public boolean containsWildcards() {
        return containsWildcards(0, size());
    }

    @Override
    public NucleotideAlphabet getAlphabet() {
        return ALPHABET;
    }

    /**
     * Creates nucleotide sequence from specified byte array.
     *
     * @param sequence byte array
     * @param offset   offset in {@code sequence}
     * @param length   length of resulting sequence
     * @return nucleotide sequence
     */
    public static NucleotideSequence fromSequence(byte[] sequence, int offset, int length) {
        byte[] storage = new byte[length];
        for (int i = 0; i < length; ++i)
            storage[i] = ALPHABET.symbolToCode((char) sequence[offset + i]);
        return new NucleotideSequence(storage, true);
    }

    private static byte[] transformToRC(byte[] data, int from, int to) {
        byte[] newData = new byte[to - from];
        int reverseCord;
        for (int coord = 0, s = to - from; coord < s; ++coord) {
            reverseCord = to - 1 - coord;
            newData[coord] = NucleotideAlphabet.complementCode(data[reverseCord]);
        }
        return newData;
    }

    private static boolean isWildcard(byte nucleotide) {
        return nucleotide >= 4;
    }


    public static boolean equals(NucleotideSequence seq1, int from1, int to1,
                                 NucleotideSequence seq2, int from2) {
        if (seq2.size() < from2 + to1 - from1)
            return false;
        for (; from1 < to1; ++from1, ++from2)
            if (seq1.data[from1] != seq2.data[from2])
                return false;
        return true;
    }

}
