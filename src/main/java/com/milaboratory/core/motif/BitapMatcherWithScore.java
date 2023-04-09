package com.milaboratory.core.motif;

public interface BitapMatcherWithScore extends BitapMatcher {
    /**
     * Match scoring in bits. 2^(-score) roughly approximates the probability of random match (i.e. E-value without
     * corrections for the size of the target sequence, and number of sequences), taking
     * the number of mismatches, wildcard matches etc. into account.
     */
    double getBitScore();

    /** maximal possible bit score - actual bit score */
    double getBitScoreCost();
}
