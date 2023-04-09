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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.Wildcard;
import com.milaboratory.util.BitArray;

import java.util.Arrays;

/**
 * Data structure for efficient exact and fuzzy matching/searching of sequences (wildcard-aware).
 *
 * @param <S> base sequence type
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE
)
public final class Motif<S extends Sequence<S>> implements java.io.Serializable {
    final Alphabet<S> alphabet;
    private final int size;

    @JsonIgnore
    private final double matchBitScore;
    /**
     * data.get(code * size + position)
     */
    final BitArray data;

    @JsonIgnore
    final BitapPattern bitapPattern;

    Motif(Alphabet<S> alphabet, int size, BitArray data) {
        if (!dataConsistent(data, size))
            throw new IllegalArgumentException("Inconsistent data. Some positions in motif has no possible values.");
        this.alphabet = alphabet;
        this.size = size;
        this.data = data;
        this.bitapPattern = toBitapPattern(null);
        this.matchBitScore = calculateMaxBitScore();
    }

    /**
     * Creates motif from sequence.
     *
     * @param sequence sequence
     */
    public Motif(S sequence) {
        this.alphabet = sequence.getAlphabet();
        this.size = sequence.size();
        int alphabetSize = alphabet.size();
        this.data = new BitArray(alphabetSize * size);
        for (int i = 0; i < size; ++i) {
            Wildcard wildcard = this.alphabet.codeToWildcard(sequence.codeAt(i));
            for (int j = 0; j < wildcard.size(); j++)
                data.set(wildcard.getMatchingCode(j) * size + i);
        }
        this.bitapPattern = toBitapPattern(null);
        this.matchBitScore = calculateMaxBitScore();
    }

    /** Ln array for the corresponding alphabet */
    double[] getLnArray() {
        return BitScoreCalculator.getLnArray(alphabet);
    }

    /** Cost array for the corresponding alphabet */
    double[] getCostArray() {
        return BitScoreCalculator.getCostArray(alphabet);
    }

    private double calculateMaxBitScore() {
        double[] scores = getLnArray();
        double result = size * scores[scores.length - 1];
        for (int j = 0; j < size; ++j)
            result += -scores[allowedBasicCodes(j) - 1];
        return result;
    }

    public int allowedBasicCodes(int position) {
        int aSize = alphabet.basicSize();
        int allowed = 0;
        for (int i = 0; i < aSize; ++i)
            if (data.get(i * size + position))
                ++allowed;
        return allowed;
    }

    /**
     * Returns per-position or of two motifs.
     *
     * <p>e.g. ATGC or TTCC = WTSC</p>
     */
    public Motif<S> or(Motif<S> other) {
        if (other.size != size)
            throw new IllegalArgumentException("Supports only motifs with the same size as this.");

        BitArray result = data.clone();
        result.or(other.data);

        return new Motif<>(alphabet, size, result);
    }

    public BitapPattern getBitapPattern() {
        if (size >= 64)
            throw new RuntimeException("Supports motifs with length less then 64.");
        return bitapPattern;
    }

    public BitapPattern toBitapPattern(BitArray exactMask) {
        if (size >= 64)
            return null;
        int aSize = alphabet.size();
        long[] patternMask = new long[aSize],
                reversePatternMask = new long[aSize];
        Arrays.fill(patternMask, ~0);
        Arrays.fill(reversePatternMask, ~0);
        int p = 0;
        for (int i = 0; i < aSize; ++i)
            for (int j = 0; j < size; ++j)
                if (data.get(p++)) {
                    patternMask[i] &= ~(1L << j);
                    reversePatternMask[i] &= ~(1L << (size - j - 1));
                }
        BitapData mainData = new BitapData(size, patternMask, reversePatternMask);
        return new BitapPattern(this, mainData, exactMask);
    }

    public int size() {
        return size;
    }

    public boolean allows(byte code, int position) {
        return data.get(code * size + position);
    }

    public double matchBitScore() {
        return matchBitScore;
    }

    public boolean matches(S sequence, int from) {
        if (from < 0 || from + size > sequence.size())
            throw new IndexOutOfBoundsException();
        for (int i = 0; i < size; ++i)
            if (!allows(sequence.codeAt(from++), i))
                return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Motif<?> motif = (Motif<?>) o;

        if (size != motif.size) return false;
        if (!alphabet.equals(motif.alphabet)) return false;
        return data.equals(motif.data);
    }

    @Override
    public int hashCode() {
        int result = alphabet.hashCode();
        result = 31 * result + size;
        result = 31 * result + data.hashCode();
        return result;
    }

    private final static boolean dataConsistent(BitArray data, int size) {
        OUTER:
        for (int i = 0; i < size; i++) {
            for (int j = i; j < data.size(); j += size)
                if (data.get(j))
                    continue OUTER;
            return false;
        }
        return true;
    }

}
