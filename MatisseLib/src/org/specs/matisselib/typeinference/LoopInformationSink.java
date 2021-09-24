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

package org.specs.matisselib.typeinference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassMessage;

public final class LoopInformationSink {
    private final Map<Integer, Map<String, VariableType>> mostGeneralContext = new HashMap<>();
    private final ProviderData providerData;
    private final boolean forLoop;

    private final Map<String, VariableType> endData = new HashMap<>();
    private final Set<Integer> pendingStartBlocks = new HashSet<>();
    private boolean isEndReachable = false;

    public LoopInformationSink(ProviderData providerData, boolean forLoop) {
        this.providerData = providerData;
        this.forLoop = forLoop;
    }

    public boolean hasPendingStartBlocks() {
        return !pendingStartBlocks.isEmpty();
    }

    public int nextPendingStartBlock() {
        Iterator<Integer> iterator = pendingStartBlocks.iterator();
        int blockId = iterator.next();
        iterator.remove();

        return blockId;
    }

    public boolean isEndReachable() {
        return isEndReachable;
    }

    public void doContinue(int blockId, Map<String, VariableType> variableTypes, boolean forceRevisit) {

        Map<String, VariableType> startInformation = mostGeneralContext.getOrDefault(blockId,
                Collections.emptyMap());
        Map<String, VariableType> newTypes = new HashMap<>(startInformation);

        boolean changed = false;
        for (String name : variableTypes.keySet()) {
            VariableType previousType = startInformation.get(name);
            VariableType newType = variableTypes.get(name);

            VariableType combinedType;
            if (previousType == null) {
                combinedType = newType;
                changed = true;
            } else {
                combinedType = TypeCombiner
                        .getCombinedVariableType(providerData.getSettings().get(CirKeys.DEFAULT_REAL),
                                Arrays.asList(previousType, newType))
                        .orElseThrow(() -> new RuntimeException("Could not infer type of " + name + ": Can't combine "
                                + previousType + " with " + newType));

                if (!combinedType.strictEquals(previousType)) {
                    changed = true;
                }
            }

            newTypes.put(name, combinedType);
        }

        if (changed || forceRevisit) {
            pendingStartBlocks.add(blockId);
        }

        mostGeneralContext.put(blockId, newTypes);

        if (forLoop) {
            // All continues can trigger a break in for loops.
            doBreak(variableTypes);
        }
    }

    public void doBreak(Map<String, VariableType> variableTypes) {
        isEndReachable = true;

        for (String var : variableTypes.keySet()) {
            VariableType previousType = endData.get(var);
            VariableType newType = variableTypes.get(var);

            VariableType combinedType;
            if (previousType == null) {
                combinedType = newType;
            } else {
                combinedType = TypeCombiner.getCombinedVariableType(
                        providerData.getSettings().get(CirKeys.DEFAULT_REAL),
                        Arrays.asList(previousType, newType))
                        .orElseThrow(() -> {
                            return providerData.getReportService().emitError(PassMessage.TYPE_INFERENCE_FAILURE,
                                    "Could not infer type of " + var + ": Conflict between types " + previousType
                                            + " and " + newType);
                        });
            }
            endData.put(var, combinedType);
        }
    }

    public void submitEndToContext(TypeInferenceContext endContext) {
        Map<String, VariableType> endTypes = endData;

        for (String variableName : endTypes.keySet()) {
            endContext.addVariable(variableName, endTypes.get(variableName));
        }
    }

    public Map<String, VariableType> getVariableTypesStartingFrom(int pendingBlock) {
        return mostGeneralContext.getOrDefault(pendingBlock, Collections.emptyMap());
    }
}
