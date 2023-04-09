package com.milaboratory.core.motif;

import com.milaboratory.core.sequence.NucleotideSequence;
import junit.framework.TestCase;

public class BitScoreCalculatorTest extends TestCase {
    public void test1() {
        assertEquals(0.0, BitScoreCalculator.cost(NucleotideSequence.ALPHABET, 4), 0.001);
        assertEquals(1.0, BitScoreCalculator.cost(NucleotideSequence.ALPHABET, 2), 0.001);
        assertEquals(2.0, BitScoreCalculator.cost(NucleotideSequence.ALPHABET, 1), 0.001);
    }
}
