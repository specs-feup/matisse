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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.LoopVariable;

public final class Reduction {
    private final List<LoopVariable> loopVariables;
    private final ReductionType reductionType;
    private final VariableType underlyingType;
    private final List<String> names;

    public Reduction(List<LoopVariable> loopVariables,
            ReductionType reductionType,
            VariableType underlyingType,
            List<String> names) {
        this.loopVariables = new ArrayList<>(loopVariables);
        this.reductionType = reductionType;
        this.underlyingType = underlyingType;
        this.names = new ArrayList<>(names);
    }

    public List<LoopVariable> getLoopVariables() {
        return Collections.unmodifiableList(this.loopVariables);
    }

    public ReductionType getReductionType() {
        return this.reductionType;
    }

    public VariableType getUnderlyingType() {
        return this.underlyingType;
    }

    @Override
    public String toString() {
        return this.reductionType + "(" + this.loopVariables + ")";
    }

    public String getInitialName() {
        return loopVariables.get(0).beforeLoop;
    }

    public String getFinalName() {
        return loopVariables.get(0).getAfterLoop().get();
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public void setInitialName(String newName) {
        LoopVariable original = loopVariables.get(0);
        loopVariables.set(0,
                new LoopVariable(newName, original.loopStart, original.loopEnd, original.getAfterLoop().orElse(null)));
    }

    public void setFinalName(String newName) {
        LoopVariable original = loopVariables.get(0);
        loopVariables.set(0,
                new LoopVariable(original.beforeLoop, original.loopStart, original.loopEnd, newName));
    }
}
