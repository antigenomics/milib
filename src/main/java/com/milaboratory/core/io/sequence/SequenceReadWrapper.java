package com.milaboratory.core.io.sequence;

import com.milaboratory.core.sequence.quality.FunctionWithIndex;

import java.util.Iterator;
import java.util.function.Function;

public class SequenceReadWrapper<P> implements SequenceRead {
    final SequenceRead read;
    final P payload;

    public SequenceReadWrapper(SequenceRead read, P payload) {
        this.read = read;
        this.payload = payload;
    }

    public SequenceRead unwrap() {
        return read;
    }

    public P getPayload() {
        return payload;
    }

    @Override
    public int numberOfReads() {
        return read.numberOfReads();
    }

    @Override
    public SingleRead getRead(int i) {
        return read.getRead(i);
    }

    @Override
    public long getId() {
        return read.getId();
    }

    @Override
    public Iterator<SingleRead> iterator() {
        return read.iterator();
    }

    @Override
    public SequenceReadWrapper<P> mapReads(Function<SingleRead, SingleRead> mapping) {
        return new SequenceReadWrapper<P>(read.mapReads(mapping), payload);
    }

    @Override
    public SequenceReadWrapper<P> mapReadsWithIndex(FunctionWithIndex<SingleRead, SingleRead> mapping) {
        return new SequenceReadWrapper<>(read.mapReadsWithIndex(mapping), payload);
    }
}
