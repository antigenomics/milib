/*
 * Copyright 2020 MiLaboratory, LLC
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
package com.milaboratory.core.alignment;

/**
 * Letters from here https://samtools.github.io/hts-specs/SAMv1.pdf (table on page 8).
 * See also here: https://docs.airr-community.org/en/stable/datarep/rearrangements.html#cigar-specification
 */
public enum AlignmentElementType {
    /** Match */
    Match('='),
    /** Mismatch / substitution */
    Mismatch('X'),
    /** Insertion [in query] (the opposite meaning to what is used throughout this library) */
    Insertion('I'),
    /** Deletion [in query] (the opposite meaning to what is used throughout this library) */
    Deletion('D'),
    /** Basically says the 1-based position of the first aligned nucleotide in the query */
    SkippedQuery('S'),
    /** Basically says the 1-based position of the first aligned nucleotide in the reference */
    SkippedReference('N');

    public final char cigarLetter, cigarLetterUpper;

    AlignmentElementType(char cigarLetter) {
        this.cigarLetter = cigarLetter;
        this.cigarLetterUpper = Character.toUpperCase(cigarLetter);
    }
}
