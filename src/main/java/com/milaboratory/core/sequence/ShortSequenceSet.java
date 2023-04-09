package com.milaboratory.core.sequence;

import gnu.trove.set.hash.TLongHashSet;

import java.util.Objects;

public class ShortSequenceSet {
    private final TLongHashSet set = new TLongHashSet();

    public boolean add(NucleotideSequence seq) {
        return set.add(toLong(seq));
    }

    public boolean contains(NucleotideSequence seq) {
        return !seq.containsWildcards() && set.contains(toLong(seq));
    }

    public int size() {
        return set.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortSequenceSet)) return false;
        ShortSequenceSet that = (ShortSequenceSet) o;
        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return Objects.hash(set);
    }

    public static long toLong(NucleotideSequence seq) {
        if (seq.size() > 29)
            throw new IllegalArgumentException("Only sequences shorter than 30 nucleotides are supported.");
        if (seq.containsWildcards())
            throw new IllegalArgumentException("Sequences containing wildcards are not supported.");
        long ret = 0;
        for (int i = 0; i < seq.size(); i++) {
            ret <<= 2;
            ret |= seq.codeAt(i);
        }
        ret |= ((long) seq.size()) << 58;
        return ret;
    }
}
