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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.RelativeGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes extraneous indices (of constant value 1) in simple_get/set/relative_get instructions.
 * <p>
 * Relative get instructions with a single index are also converted to simple gets
 * 
 * @author Luís Reis
 *
 */
public class SimpleAccessSimplificationPass extends TypeTransparentSsaPass {

    private static final boolean ENABLE_LOGGING = false;

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof SimpleSetInstruction) {
                    log("Found: " + instruction);
                    handleSimpleSet(iterator, (SimpleSetInstruction) instruction, typeGetter);
                } else if (instruction instanceof SimpleGetInstruction) {
                    log("Found: " + instruction);
                    handleSimpleGet(iterator, (SimpleGetInstruction) instruction, typeGetter);
                } else if (instruction instanceof RelativeGetInstruction) {
                    log("Found: " + instruction);
                    handleRelativeGet(iterator, (RelativeGetInstruction) instruction, typeGetter);
                }
            }
        }
    }

    private static void handleSimpleGet(ListIterator<SsaInstruction> iterator,
            SimpleGetInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter) {

        List<String> indices = instruction.getIndices();
        getFlatIndex(instruction.getInputMatrix(), indices, typeGetter).ifPresent(flatIndices -> {
            SimpleGetInstruction newInstruction = new SimpleGetInstruction(
                    instruction.getOutput(),
                    instruction.getInputMatrix(),
                    flatIndices);

            log("Simplifying to " + newInstruction);
            iterator.set(newInstruction);
        });
    }

    private static void handleSimpleSet(ListIterator<SsaInstruction> iterator,
            SimpleSetInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter) {

        List<String> indices = instruction.getIndices();
        getFlatIndex(instruction.getInputMatrix(), indices, typeGetter).ifPresent(flatIndices -> {
            SimpleSetInstruction newInstruction = new SimpleSetInstruction(
                    instruction.getOutput(),
                    instruction.getInputMatrix(),
                    flatIndices,
                    instruction.getValue());

            log("Simplifying to " + newInstruction);
            iterator.set(newInstruction);
        });
    }

    private static void handleRelativeGet(ListIterator<SsaInstruction> iterator,
            RelativeGetInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter) {

        List<String> indices = instruction.getIndices();
        getFlatIndex(instruction.getInputMatrix(), indices, typeGetter).ifPresent(flatIndices -> {
            SsaInstruction newInstruction;
            if (flatIndices.size() == 1) {
                newInstruction = new GetOrFirstInstruction(
                        instruction.getOutput(),
                        instruction.getInputMatrix(),
                        flatIndices.get(0));
            } else {
                newInstruction = new RelativeGetInstruction(
                        instruction.getOutput(),
                        instruction.getInputMatrix(),
                        instruction.getSizeMatrix(),
                        flatIndices);
            }

            log("Simplifying to " + newInstruction);
            iterator.set(newInstruction);
        });
    }

    private static Optional<List<String>> getFlatIndex(String matrixName,
            List<String> indices,
            Function<String, Optional<VariableType>> typeGetter) {

        if (indices.size() == 0) {
            return Optional.empty();
        }

        TypeShape shape = typeGetter.apply(matrixName)
                .filter(MatrixType.class::isInstance)
                .map(MatrixUtils::getShape)
                .orElse(TypeShape.newUndefinedShape());

        List<String> proposedIndices = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            String index = indices.get(i);

            if (ConstantUtils.isConstantOne(typeGetter.apply(index).get())) {
                if (i < shape.getRawNumDims() && shape.getDim(i) == 1) {
                    continue;
                }
                if (shape.getRawNumDims() > 0 && i >= shape.getRawNumDims()) {
                    continue;
                }
            }

            proposedIndices.add(index);
        }

        // Remove trailing ones
        for (int i = proposedIndices.size() - 1; i >= 0; --i) {
            String index = proposedIndices.get(i);

            if (ConstantUtils.isConstantOne(typeGetter.apply(index).get())) {
                proposedIndices.remove(i);
            } else {
                break;
            }
        }

        if (proposedIndices.isEmpty()) {
            // All indices are 1
            proposedIndices.add(indices.get(0));
        }

        return Optional.of(proposedIndices);
    }

    private static void log(String message) {
        if (ENABLE_LOGGING) {
            System.out.print("[simple_access_simplification] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
