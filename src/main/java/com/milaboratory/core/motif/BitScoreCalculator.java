package com.milaboratory.core.motif;

import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BitScoreCalculator {
    private BitScoreCalculator() {
    }

    public static final double LN2 = 0.6931471805599453D;

    private static Map<Alphabet, double[]> lnArrays = new HashMap<>();
    private static Map<Alphabet, double[]> costArrays = new HashMap<>();

    static {
        for (Alphabet alphabet : new Alphabet[]{NucleotideSequence.ALPHABET, AminoAcidSequence.ALPHABET}) {
            double[] lnArray = new double[alphabet.basicSize()];
            for (int i = 0; i < lnArray.length; i++)
                lnArray[i] = Math.log(i + 1) / LN2;
            lnArrays.put(alphabet, lnArray);
            double[] costArray = new double[alphabet.basicSize()];
            for (int i = 0; i < costArray.length; i++)
                costArray[i] = lnArray[lnArray.length - 1] - lnArray[i];
            costArrays.put(alphabet, costArray);
        }
    }

    /**
     * array[i-1] = log2(i)
     * array.length = alphabet.basicSize()
     * array[array.length - 1] == maxCost
     */
    public static double[] getLnArray(Alphabet alphabet) {
        return Objects.requireNonNull(lnArrays.get(alphabet));
    }

    /**
     * array[i-1] = log2(alphabet.basicSize()) - log2(i)
     * array.length = alphabet.basicSize()
     * array[0] == maxCost
     */
    public static double[] getCostArray(Alphabet alphabet) {
        return Objects.requireNonNull(costArrays.get(alphabet));
    }

    public static double maxCost(Alphabet alphabet) {
        return Objects.requireNonNull(costArrays.get(alphabet))[0];
    }

    public static <S extends Sequence<S>> double cost(Alphabet<S> alphabet, int allowedBasicCodes) {
        double[] costArray = getCostArray(alphabet);
        return costArray[allowedBasicCodes - 1];
    }
}
