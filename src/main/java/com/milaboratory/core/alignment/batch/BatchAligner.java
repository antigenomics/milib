package com.milaboratory.core.alignment.batch;

import com.milaboratory.core.sequence.Sequence;

/**
 * Represents aligner that can align a sequence against a set of other sequences.
 *
 * @param <S> sequence type
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface BatchAligner<S extends Sequence<S>, H extends AlignmentHit<S, ?>> {
    AlignmentResult<H> align(S sequence);

    AlignmentResult<H> align(S sequence, int from, int to);
}
