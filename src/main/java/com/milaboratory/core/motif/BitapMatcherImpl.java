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
package com.milaboratory.core.motif;

public class BitapMatcherImpl implements BitapMatcher, java.io.Serializable {
    final BitapStateIterator mainState;
    final BitapStateIterator secondaryState;

    public BitapMatcherImpl(BitapStateIterator mainState, BitapStateIterator secondaryState) {
        this.mainState = mainState;
        this.secondaryState = secondaryState;
    }

    public BitapMatcherImpl(BitapStateIterator mainState) {
        this(mainState, null);
    }

    public final int findNext() {
        while (mainState.nextState()) {
            if (secondaryState != null) {
                secondaryState.nextState();
                if (!secondaryState.match)
                    continue;
            }
            if (mainState.match)
                return mainState.currentPosition();
        }
        return -1;
    }

    public final int getNumberOfErrors() {
        return mainState.errors;
    }
}
