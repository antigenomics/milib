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
package com.milaboratory.core.alignment.kaligner2;

import com.milaboratory.core.alignment.kaligner1.KMapper;
import com.milaboratory.util.IntArrayList;

import java.util.Collections;
import java.util.List;

/**
 * KMappingResult - class which is result of {@link KMapper#align(com.milaboratory.core.sequence.NucleotideSequence)}
 * methods. <p>It contains seeds used for aligning by {@link KMapper} and list
 * of hits found in target sequence.</p>
 */
public final class KMappingResult2 implements java.io.Serializable {
    /**
     * Seeds used to align target sequence
     */
    IntArrayList seeds;
    /**
     * List of hits (potential candidates) for target sequence
     */
    List<KMappingHit2> hits;

    /**
     * Creates new KMappingResult
     *
     * @param seeds seeds used for alignment
     * @param hits  hits obtained by {@link KMapper}
     */
    public KMappingResult2(IntArrayList seeds, List<KMappingHit2> hits) {
        this.seeds = seeds;
        this.hits = hits;
        for (KMappingHit2 hit : hits)
            hit.result = this;
    }

    public int getSeedsCount() {
        return seeds.size();
    }

    public int getSeedPosition(int i) {
        return seeds.get(i);
    }

    public List<KMappingHit2> getHits() {
        return Collections.unmodifiableList(hits);
    }
}
