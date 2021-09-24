/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.heuristics.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.services.TypedInstanceProviderService;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

import pt.up.fe.specs.util.collections.MultiMap;

public class SchedulePredictorContext {
    public ProviderData providerData;
    public ParallelRegionSettings settings;
    public FunctionBody body;
    public Function<String, Optional<VariableType>> typeGetter;
    public ParallelLoopInformation parallelLoop;
    public int blockId;
    public WideScopeService wideScope;
    public TypedInstanceProviderService typedInstanceProvider;
    public MultiMap<Integer, Reduction> localReductions;

    public SchedulePredictorContext copy() {
        SchedulePredictorContext copy = new SchedulePredictorContext();

        copy.providerData = providerData;
        copy.settings = settings;
        copy.body = body;
        copy.typeGetter = typeGetter;
        copy.parallelLoop = parallelLoop;
        copy.blockId = blockId;
        copy.wideScope = wideScope;
        copy.typedInstanceProvider = typedInstanceProvider;
        copy.localReductions = new MultiMap<>(localReductions);

        return copy;
    }

    public List<Integer> getLocalSize() {
        List<Integer> localSize = new ArrayList<>();

        System.out.println(settings.localSizes);
        for (String localSizeVar : settings.localSizes) {
            Number number = ScalarUtils.getConstant(typeGetter.apply(localSizeVar).get());
            System.out.println("> " + localSizeVar + ": " + number);
            if (number == null) {
                localSize.add(-1);
            } else {
                localSize.add(number.intValue());
            }
        }

        return localSize;
    }
}
