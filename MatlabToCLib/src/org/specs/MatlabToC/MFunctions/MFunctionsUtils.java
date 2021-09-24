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

package org.specs.MatlabToC.MFunctions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Utility methods related to transforming Matlab trees into C trees.
 * 
 * @author Joao Bispo
 * 
 */
public class MFunctionsUtils extends CirBuilder {

    /**
     * @param data
     */
    public MFunctionsUtils(ProviderData data) {
        super(data);
    }

    /**
     * @param data
     * @return
     */
    public static FunctionType newFunctionTypes(MatlabToCFunctionData data) {
        List<String> inputNames = new ArrayList<>(data.getInputNames());
        List<VariableType> inputTypes = data.getInputTypes();

        List<VariableType> outputTypes = data.getOutputTypes();

        // Since types will be used to build FunctionTypes, use the types without pointer information
        outputTypes = ReferenceUtils.getType(outputTypes, false);

        // If outputs are greater than 1, use outputs as inputs
        if (outputTypes.size() > 1) {
            List<String> outputNames = new ArrayList<>(data.getOutputNames());

            return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputNames, outputTypes);
        }

        // If output type is matrix, and is a declared implementation, add to
        // the inputs
        if (outputTypes.size() == 1) {
            if (MatrixUtils.isMatrix(outputTypes.get(0))) {
                // return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, data.getOutputNames()
                // .get(0), outputTypes.get(0));
                return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, data.getOutputNames()
                        .iterator().next(), outputTypes.get(0));
            }
        }

        // If outputs is empty, return type is void
        VariableType cReturnType = null;
        if (outputTypes.isEmpty()) {
            cReturnType = VoidType.newInstance();
        } else {
            cReturnType = outputTypes.get(0);
        }

        // If type is not a valid C type, transform into output as input
        if (!cReturnType.isReturnType()) {
            List<VariableType> outputAsInput = SpecsFactory.newArrayList();
            outputAsInput.add(cReturnType);

            List<String> outputName = new ArrayList<>(data.getOutputNames());
            return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputAsInput);
        }

        // Build function with simple return type
        String outputName = null;
        if (!outputTypes.isEmpty()) {
            // outputName = data.getOutputNames().get(0);
            outputName = data.getOutputNames().iterator().next();
        }

        return FunctionType.newInstance(inputNames, inputTypes, outputName, cReturnType);
    }

    /**
     * Gets the string from a constant, to be used in the function name.
     * 
     * @param f
     * @param numericData
     * @return
     */
    public static String getConstantString(NumberFormat f, VariableType type) {
        if (ScalarUtils.isInteger(type)) {
            return Long.toString(ScalarUtils.getConstant(type).longValue());
        }

        Double doubleConst = ScalarUtils.getConstant(type).doubleValue();
        return f.format(doubleConst);
    }

    /**
     * Adds calls to free to variables that needed memory allocation and that will not escape the current function. The
     * calls are then added before every return statement.
     * 
     * @param instructions
     */
    public void addCallsToFree(CInstructionList instructions) {
        FunctionType types = instructions.getFunctionTypes();

        // Get the names of all input and output variables
        Set<String> inoutNames = SpecsFactory.newHashSet();
        inoutNames.addAll(types.getCInputNames());

        if (!VoidTypeUtils.isVoid(types.getCReturnType())) {
            inoutNames.add(types.getCOutputName());
        }

        Map<String, VariableType> allVars = instructions.getLocalVars();
        Map<String, VariableType> varsToFree = SpecsFactory.newHashMap();
        for (String variableName : allVars.keySet()) {
            // Check if variable is an input/output
            if (inoutNames.contains(variableName)) {
                continue;
            }

            VariableType variableType = allVars.get(variableName);

            // Check if variable should be freed
            if (!variableType.usesDynamicAllocation()) {
                continue;
            }

            // Add variable to table of variables to free
            varsToFree.put(variableName, variableType);
        }

        // If there are no variables to free, return
        if (varsToFree.isEmpty()) {
            return;
        }

        // Create list of free instructions
        List<CNode> freeInstructions = buildFreeInstructions(varsToFree);

        // Find all return nodes
        List<CNode> returnTokens = SpecsFactory.newArrayList();
        List<InstructionNode> insts = instructions.getRoot().getDescendantsAndSelf(InstructionNode.class);

        for (InstructionNode inst : insts) {
            // Check if is a return instruction
            if (inst.getInstructionType() != InstructionType.Return) {
                continue;
            }

            returnTokens.add(inst);
        }

        if (returnTokens.isEmpty()) {
            throw new RuntimeException("Could not find return tokens in function, there should be at least one");
        }

        // Add list of free instruction before the return token
        for (CNode returnToken : returnTokens) {
            for (CNode freeInst : freeInstructions) {
                NodeInsertUtils.insertBefore(returnToken, freeInst);
            }
        }
    }

    /**
     * @param varsToFree
     * @return
     */
    private List<CNode> buildFreeInstructions(Map<String, VariableType> varsToFree) {
        List<CNode> freeInsts = SpecsFactory.newArrayList();

        List<String> variableNames = SpecsFactory.newArrayList(varsToFree.keySet());
        Collections.sort(variableNames);

        for (String variableName : variableNames) {
            VariableType type = varsToFree.get(variableName);
            CNode freeInst = buildFreeInstruction(variableName, type);

            freeInsts.add(freeInst);
        }

        return freeInsts;
    }

    /**
     * @param variableName
     * @param type
     * @return
     */
    private CNode buildFreeInstruction(String variableName, VariableType type) {

        // Check if it is a type with allocated memory
        if (!type.usesDynamicAllocation()) {
            throw new RuntimeException("Given type '" + type + "' does not have memory allocation");
        }

        // Create Variable token
        CNode variableToken = CNodeFactory.newVariable(variableName, type);
        CNode functionCall = getFunctionCall(type.functions().free(), variableToken);

        return CNodeFactory.newInstruction(InstructionType.FunctionCall, functionCall);

    }
}
