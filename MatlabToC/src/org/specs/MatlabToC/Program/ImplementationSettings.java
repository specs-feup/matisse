/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Program;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Utilities.AvoidableFunctionsData;
import org.specs.CIR.Utilities.Inlining.InliningData;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Options.MultipleChoice;

import pt.up.fe.specs.util.SpecsEnums;

/**
 * Data object which contains settings related to function implementation options.
 * 
 * @author Joao Bispo
 * 
 */
public class ImplementationSettings {

    private final boolean allowDynamicAllocation;
    private final MemoryLayout memoryLayout;
    private final InliningData inliningData;
    private final AvoidableFunctionsData avoidData;

    public ImplementationSettings(boolean allowDynamicAllocation, MemoryLayout disposition, InliningData inliningData,
            AvoidableFunctionsData avoidData) {

        this.allowDynamicAllocation = allowDynamicAllocation;
        memoryLayout = disposition;
        this.inliningData = inliningData;
        this.avoidData = avoidData;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @param generalSetup
     */
    public void set(DataStore setup) {
        // Apply FunctionSettings to Setup
        setup.set(CirKeys.ALLOW_DYNAMIC_ALLOCATION, allowDynamicAllocation);

        MultipleChoice memoryLayoutChoice = MultipleChoice
                .newInstance(SpecsEnums.buildList(MemoryLayout.class.getEnumConstants()));
        memoryLayoutChoice.setChoice(memoryLayout.name());
        setup.set(CirKeys.MEMORY_LAYOUT, memoryLayoutChoice);

        setup.set(CirKeys.INLINE, inliningData);
        setup.set(CirKeys.AVOID, avoidData);
    }
}
