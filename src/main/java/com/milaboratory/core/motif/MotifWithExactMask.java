package com.milaboratory.core.motif;

import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.util.BitArray;

public final class MotifWithExactMask<S extends Sequence<S>> {
    private final Motif<S> motif;
    private final BitArray exactMask;

    /** Approximate bit scoring */
    final double[] defaultMatchScore, mismatchScore;

    final double averageMismatchPenalty;

    public MotifWithExactMask(Motif<S> motif, BitArray exactMask) {
        this.motif = motif;
        this.exactMask = exactMask;

        double[] costArray = motif.getCostArray();
        defaultMatchScore = new double[motif.size()];
        double averageMMPenalty = 0;
        for (int i = 0; i < motif.size(); i++)
            averageMMPenalty += defaultMatchScore[i] = costArray[motif.allowedBasicCodes(i) - 1];
        mismatchScore = defaultMatchScore.clone();
        double lengthPenalty = Math.log(Math.max(1, 1 + motif.size() - 1 - (exactMask == null ? 0 : exactMask.bitCount())));
        for (int i = 0; i < motif.size(); i++) {
            mismatchScore[i] -= lengthPenalty;
            averageMMPenalty -= mismatchScore[i] -= costArray[0];
        }
        this.averageMismatchPenalty = averageMMPenalty / motif.size();
    }

    public double getDefaultMatchBitScore(int position) {
        return defaultMatchScore[position];
    }

    public double getMatchBitScore(int position, byte matchingCode) {
        if (matchingCode < motif.alphabet.basicSize())
            return defaultMatchScore[position];
        else {
            double[] costArray = motif.getCostArray();
            return costArray[Math.max(motif.allowedBasicCodes(position), motif.alphabet.codeToWildcard(matchingCode).basicSize()) - 1];
        }
    }

    public double getMismatchBitScore(int position) {
        return exactMask.get(position) ? Double.NEGATIVE_INFINITY : mismatchScore[position];
    }

    public double getMismatchBitScoreCost(int position) {
        return getDefaultMatchBitScore(position) - getMismatchBitScore(position);
    }

    public int size() {
        return motif.size();
    }

    public Motif<S> getMotif() {
        return motif;
    }

    public BitArray getExactMask() {
        return exactMask.clone();
    }

    /**
     * No errors (mismatches / indels) will be allowed in upper case letters in fuzzy searches implemented in
     * the bitap pattern (only exact match allowed), in contrast lower case letters from the motif string will be fuzzy
     * matched.
     */
    public BitapPattern toBitapPattern() {
        return motif.toBitapPattern(exactMask);
    }

    public static <S extends Sequence<S>> MotifWithExactMask<S> from(Alphabet<S> alphabet, String motifString) {
        Motif<S> motif = alphabet.parse(motifString).toMotif();
        if (motif.size() != motifString.length())
            throw new IllegalArgumentException();
        BitArray exactMask = new BitArray(motifString.length());
        for (int i = 0; i < motifString.length(); i++)
            exactMask.set(i, Character.isUpperCase(motifString.charAt(i)));
        return new MotifWithExactMask<>(motif, exactMask);
    }

    public static MotifWithExactMask<NucleotideSequence> fromNucleotide(String motifString) {
        return from(NucleotideSequence.ALPHABET, motifString);
    }

    public static MotifWithExactMask<AminoAcidSequence> fromAminoAcid(String motifString) {
        return from(AminoAcidSequence.ALPHABET, motifString);
    }
}
