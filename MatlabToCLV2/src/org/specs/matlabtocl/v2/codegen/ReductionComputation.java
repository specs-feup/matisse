/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matlabtocl.v2.codegen;

import org.specs.CIR.Tree.CNode;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;

public class ReductionComputation {
    private final CNode localBuffer;
    /**
     * The buffer containing the result of the reduction that is transferred to the host. Null for local reductions.
     */
    private final CNode globalBuffer;
    private final String startValueName;
    private final String innerLoopEndName;
    private final CNode defaultValue;
    private final CNode accumulatorVariable;
    private final ReductionStrategy strategy;

    public ReductionComputation(CNode localBuffer,
            CNode globalBuffer,
            String startValueName,
            String innerLoopName,
            CNode defaultValue,
            CNode accumulatorVariable,
            ReductionStrategy strategy) {

        this.localBuffer = localBuffer;
        this.globalBuffer = globalBuffer;
        this.startValueName = startValueName;
        this.innerLoopEndName = innerLoopName;
        this.defaultValue = defaultValue;
        this.accumulatorVariable = accumulatorVariable;
        this.strategy = strategy;
    }

    public CNode getLocalBuffer() {
        return this.localBuffer;
    }

    public CNode getGlobalBuffer() {
        return this.globalBuffer;
    }

    public String getStartValueName() {
        return this.startValueName;
    }

    public String getInnerLoopEndName() {
        return this.innerLoopEndName;
    }

    public CNode getDefaultValue() {
        return this.defaultValue;
    }

    public CNode getAccumulatorVariable() {
        return this.accumulatorVariable;
    }

    public ReductionStrategy getStrategy() {
        return strategy;
    }
}
