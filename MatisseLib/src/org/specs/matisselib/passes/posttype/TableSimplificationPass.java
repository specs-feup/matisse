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

package org.specs.matisselib.passes.posttype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;

public class TableSimplificationPass implements PostTypeInferencePass {
    public static final String PASS_NAME = "table_simplification";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);
        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.logSkip(instance);
            return;
        }

        logger.logStart(instance);

        InstanceProvider allocatorProvider = passData
                .get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER)
                .getSystemFunction("matisse_new_array_from_dims")
                .get();

        Map<String, FunctionCallInstruction> trivialCats = new HashMap<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (!(instruction instanceof FunctionCallInstruction)) {
                continue;
            }

            FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;
            if (functionCall.getInputVariables().isEmpty()) {
                continue;
            }

            if (functionCall.getOutputs().size() != 1) {
                continue;
            }

            String output = functionCall.getOutputs().get(0);

            if (functionCall.getFunctionName().equals("horzcat") && functionCall.getInputVariables().stream()
                    .map(instance::getVariableType)
                    .allMatch(ScalarUtils::isScalar)) {

                logger.log("Found trivial horzcat: " + output);
                trivialCats.put(output, functionCall);
            }
            if (functionCall.getFunctionName().equals("vertcat")) {
                if (functionCall.getInputVariables().stream().map(instance::getVariableType)
                        .allMatch(ScalarUtils::isScalar)) {
                    logger.log("Found trivial vertcat: " + output);

                    trivialCats.put(output, functionCall);
                }

                if (!trivialCats.keySet().containsAll(functionCall.getInputVariables())) {
                    continue;
                }

                int size = -1;
                for (String cat : functionCall.getInputVariables()) {
                    FunctionCallInstruction sourceCat = trivialCats.get(cat);
                    if (!sourceCat.getFunctionName().equals("horzcat")) {
                        // Skip. We don't support nested vertcats here.
                        size = -1;
                        break;
                    }

                    int sourceSize = sourceCat.getInputVariables().size();
                    if (size == -1) {
                        size = sourceSize;
                    } else if (size != sourceSize) {
                        // Skip. Different sized horzcats.
                        size = -1;
                        break;
                    }
                }

                if (size < 0) {
                    continue;
                }

                trivialCats.put(output, functionCall);
            }
        }

        for (SsaBlock block : instance.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                String output = SpecsCollections.singleTry(instruction.getOutputs()).orElse(null);
                FunctionCallInstruction functionCall = trivialCats
                        .get(output);
                if (functionCall == null) {
                    continue;
                }

                iterator.remove();

                String name = NameUtils.getSuggestedName(output);

                String rows = makeRows(instance, iterator, trivialCats, name + "_rows", functionCall);
                String cols = makeCols(instance, iterator, trivialCats, name + "_cols", functionCall);

                VariableType rowsType = instance.getVariableType(rows).get();
                VariableType colsType = instance.getVariableType(cols).get();

                int rowsValue = ScalarUtils.getConstant(rowsType).intValue();
                int colsValue = ScalarUtils.getConstant(colsType).intValue();

                MatrixType matrixType = (MatrixType) instance.getVariableType(output).get();
                String initialAllocationMatrix = instance.makeTemporary(name, matrixType);

                ProviderData allocatorData = instance.getProviderData().create(rowsType, colsType);
                allocatorData.setOutputType(matrixType);
                FunctionType functionType = allocatorProvider.getType(allocatorData);
                iterator
                        .add(new TypedFunctionCallInstruction("matisse_new_array_from_dims", functionType,
                                Arrays.asList(initialAllocationMatrix), Arrays.asList(rows, cols)));

                String currentMatrix = initialAllocationMatrix;
                for (int col = 0; col < colsValue; ++col) {
                    for (int row = 0; row < rowsValue; ++row) {

                        int index = col * rowsValue + row + 1;
                        String value = getValueAt(functionCall, trivialCats, row, col);

                        String intermediateOutput;
                        if (row == rowsValue - 1 && col == colsValue - 1) {
                            intermediateOutput = output;
                        } else {
                            intermediateOutput = instance.makeTemporary(name, matrixType);
                        }

                        VariableType indexType = instance.getProviderData().getNumerics().newInt(index);
                        String indexVar = instance.makeTemporary("index", indexType);
                        iterator.add(AssignmentInstruction.fromInteger(indexVar, index));

                        iterator.add(
                                new SimpleSetInstruction(intermediateOutput, currentMatrix, Arrays.asList(indexVar),
                                        value));

                        currentMatrix = intermediateOutput;
                    }
                }
            }
        }
    }

    private String getValueAt(FunctionCallInstruction functionCall,
            Map<String, FunctionCallInstruction> trivialCats,
            int row,
            int col) {

        if (functionCall.getFunctionName().equals("horzcat")) {
            assert row == 0;

            return functionCall.getInputVariables().get(col);
        }

        assert functionCall.getFunctionName().equals("vertcat");
        String line = functionCall.getInputVariables().get(row);

        if (trivialCats.containsKey(line)) {
            return getValueAt(trivialCats.get(line), trivialCats, 0, col);
        }

        // Scalar vertcat.
        assert col == 0;
        return line;
    }

    private String makeRows(TypedInstance instance,
            ListIterator<SsaInstruction> iterator,
            Map<String, FunctionCallInstruction> trivialCats,
            String nameSemantics,
            FunctionCallInstruction functionCall) {

        if (functionCall.getFunctionName().equals("horzcat")) {
            return makeSize(instance, iterator, nameSemantics, 1);
        }

        assert functionCall.getFunctionName().equals("vertcat");

        List<String> inputs = functionCall.getInputVariables();
        assert inputs.size() > 0;

        return makeSize(instance, iterator, nameSemantics, inputs.size());
    }

    private String makeCols(TypedInstance instance,
            ListIterator<SsaInstruction> iterator,
            Map<String, FunctionCallInstruction> trivialCats,
            String nameSemantics,
            FunctionCallInstruction functionCall) {

        if (functionCall.getFunctionName().equals("horzcat")) {
            return makeSize(instance, iterator, nameSemantics, functionCall.getInputVariables().size());
        }

        assert functionCall.getFunctionName().equals("vertcat");

        List<String> inputs = functionCall.getInputVariables();
        assert inputs.size() > 0;

        String exampleInput = inputs.get(0);
        if (trivialCats.containsKey(exampleInput)) {
            return makeSize(instance, iterator, nameSemantics,
                    trivialCats.get(exampleInput).getInputVariables().size());
        } else {
            return makeSize(instance, iterator, nameSemantics, 1);
        }
    }

    private String makeSize(TypedInstance instance,
            ListIterator<SsaInstruction> iterator,
            String nameSemantics,
            int size) {

        VariableType type = instance.getProviderData().getNumerics().newInt(size);
        String var = instance.makeTemporary(nameSemantics, type);

        iterator.add(AssignmentInstruction.fromInteger(var, size));

        return var;
    }
}
