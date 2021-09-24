/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.passes.posttype;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.AssumeMatrixIndicesInRangeProperty;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * On functions with the AssumeMatrixIndicesInRangeProperty, converts scalar gets into simple_gets and sets into
 * simple_sets.
 */
public class ConvertMatrixAccessesPass extends TypeTransparentSsaPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        if (!body
                .getPropertyStream(AssumeMatrixIndicesInRangeProperty.class)
                .findAny()
                .isPresent()) {

            log("Function " + body.getName() + " does not have 'assume matrix indices in range' property");
            return;
        }

        boolean performedOptimization = false;

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();

            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();
                if (instruction instanceof MatrixGetInstruction) {
                    MatrixGetInstruction matrixGet = (MatrixGetInstruction) instruction;

                    List<String> indices = matrixGet.getIndices();
                    Optional<String> nonScalarIndex = indices
                            .stream()
                            .filter(name -> !(typeGetter.apply(name).get() instanceof ScalarType))
                            .findFirst();

                    if (nonScalarIndex.isPresent()) {
                        log("Index " + nonScalarIndex.get() + " isn't a scalar in " + matrixGet);
                        continue;
                    }
                    String output = matrixGet.getOutput();
                    if (!(typeGetter.apply(output).get() instanceof ScalarType)) {
                        log("Output isn't a scalar in " + matrixGet);
                    }

                    SimpleGetInstruction simpleGet = new SimpleGetInstruction(output, matrixGet.getInputMatrix(),
                            indices);
                    iterator.set(simpleGet);

                    continue;
                }

                if (instruction instanceof MatrixSetInstruction) {
                    MatrixSetInstruction matrixSet = (MatrixSetInstruction) instruction;

                    List<String> indices = matrixSet.getIndices();
                    Optional<String> nonScalarIndex = indices
                            .stream()
                            .filter(name -> !(typeGetter.apply(name).get() instanceof ScalarType))
                            .findFirst();

                    if (nonScalarIndex.isPresent()) {
                        log("Index " + nonScalarIndex.get() + " isn't a scalar in " + matrixSet);
                        continue;
                    }
                    String value = matrixSet.getValue();
                    if (!(typeGetter.apply(value).get() instanceof ScalarType)) {
                        log("Value isn't a scalar in " + matrixSet);
                    }
                    String output = matrixSet.getOutput();

                    SimpleSetInstruction simpleGet = new SimpleSetInstruction(output, matrixSet.getInputMatrix(),
                            indices, value);
                    iterator.set(simpleGet);
                    performedOptimization = true;

                    continue;
                }
            }
        }

        if (performedOptimization) {
            // Due to matrix_set -> simple_set conversions,
            // some information that previously could not be proven
            // may now be exposed.
            // This is mostly due to matrices that were previously believed to be resized in loops.
            passData.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    public static void log(String message) {
        if (ConvertMatrixAccessesPass.ENABLE_DIAGNOSTICS) {
            System.err.print("[convert_matrix_accesses] ");
            System.err.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
