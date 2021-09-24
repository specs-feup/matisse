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

import java.util.Collections;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

import pt.up.fe.specs.util.collections.MultiMap;

public final class GeneratedKernel extends GeneratedCodeSegment {
    private final FunctionInstance instance;
    private final List<KernelArgument> arguments;
    private final MultiMap<Integer, Reduction> localReductions;

    public GeneratedKernel(FunctionInstance instance,
            ParallelRegionSettings parallelSettings,
            List<KernelArgument> arguments,
            MultiMap<Integer, Reduction> localReductions,
            CLVersion requiredVersion) {
        super(parallelSettings, requiredVersion);

        this.instance = instance;
        this.arguments = arguments;
        this.localReductions = localReductions;
    }

    public FunctionInstance getInstance() {
        return this.instance;
    }

    public List<KernelArgument> getArguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    public String getInstanceName() {
        return this.instance.getCName();
    }

    public MultiMap<Integer, Reduction> getLocalReductions() {
        return localReductions;
    }
}
