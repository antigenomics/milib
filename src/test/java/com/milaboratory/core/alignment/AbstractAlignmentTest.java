package com.milaboratory.core.alignment;

import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.RandomUtil;
import org.junit.Before;

public class AbstractAlignmentTest {
    public static NucleotideSequence mutate(NucleotideSequence seq, int[] mut) {
        return new Mutations<NucleotideSequence>(NucleotideSequence.ALPHABET, mut).mutate(seq);
    }

    @Before
    public void setUp() throws Exception {
        // 0123456L bad
        RandomUtil.getThreadLocalRandom().setSeed(3262235L);
    }
}
