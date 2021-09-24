/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndexUtils;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class FunctionMultipleGet extends CirBuilder {

    private final MatlabToCFunctionData data;

    /**
     * @param data
     */
    public FunctionMultipleGet(MatlabToCFunctionData data) {
        super(data.getSettings());
        this.data = data;
    }

    /**
     * @param getVar
     * @param mIndexes
     * @return
     */
    public CNode newFunctionCall(CNode getVar, List<MatlabNode> mIndexes) {

        // Convert mIndexes from MATLAB to C
        List<ArrayIndex> callIndexes = new ArrayIndexUtils(data).getArrayIndexes(getVar, mIndexes);

        // Get type. Since it will be used to build a function, clean it from pointer information
        // VariableType matrixType = DiscoveryUtils.getVarTypeClean(getVar);
        VariableType matrixType = getVar.getVariableType().normalize();

        // Convert ArrayIndex to be used inside the function
        List<ArrayIndex> functionIndexes = ArrayIndexUtils.getFunctionIndexes(callIndexes);

        FunctionInstance instance = newFunctionInstance(matrixType, functionIndexes);

        List<CNode> arguments = SpecsFactory.newArrayList();
        arguments.add(getVar);

        for (ArrayIndex index : callIndexes) {
            arguments.addAll(index.getFunctionInputs());
        }
        // List<ArrayIndex> callIndexes = new ArrayIndexUtils(data).getArrayIndexes(getVar, mIndexes);
        // arguments.addAll(ArrayIndexUtils.getIndexArguments(callIndexes));

        return FunctionInstanceUtils.getFunctionCall(instance, arguments);
        // return null;
        // return new InstructionsInstance(functionName, cFilename, instructions);
    }

    public FunctionInstance newFunctionInstance(VariableType matrixType, List<ArrayIndex> functionIndexes) {

        String functionName = getName(matrixType, functionIndexes);
        String cFilename = "lib/arrays";

        // FunctionTypes functionTypes = getFunctionTypes(readMatrix, functionIndexes, outputVar);
        FunctionType functionTypes = getFunctionTypes(matrixType, functionIndexes);
        // System.out.println("Matrix Type: " + matrixType);
        // System.out.println("F Indexes:" + functionIndexes);
        // System.out.println("FTYPES:" + functionTypes);
        CInstructionList instructions = getInstructions(functionTypes, functionIndexes);

        return new InstructionsInstance(functionName, cFilename, instructions);
    }

    /**
     * @param functionTypes
     * @param functionIndexes
     * @return
     */
    private CInstructionList getInstructions(FunctionType functionTypes, List<ArrayIndex> functionIndexes) {
        CInstructionList instructions = new CInstructionList(functionTypes);

        Variable inputVar = functionTypes.getInputVar(ArrayIndexUtils.getReadMatrixName());
        Variable outputVar = functionTypes.getReturnVar();

        // Allocate output
        // TODO: Should be replaced by primitive function supported by all matrix types
        if (MatrixUtils.usesDynamicAllocation(outputVar.getType())) {
            List<CNode> newArrayArgs = SpecsFactory.newArrayList();
            for (ArrayIndex arrayIndex : functionIndexes) {
                newArrayArgs.add(arrayIndex.getSize());
            }

            newArrayArgs.add(CNodeFactory.newVariable(outputVar));
            CNode newCall = getFunctionCall(TensorProvider.NEW_ARRAY, newArrayArgs);
            instructions.addInstruction(newCall);
        }

        // "counter = 0;"
        CNode counterVar = newCounterVar();
        instructions.addAssignment(counterVar, CNodeFactory.newCNumber(0));

        // / Inner loop assignment
        // Matrix variable Get
        List<CNode> getArguments = SpecsFactory.newArrayList();
        getArguments.add(CNodeFactory.newVariable(inputVar));

        for (ArrayIndex arrayIndex : functionIndexes) {
            getArguments.add(arrayIndex.getIndex());
        }

        CNode matrixGet = getFunctionCall(MatrixFunction.GET, getArguments);

        // Output set (simple counter)
        CNode outToken = CNodeFactory.newVariable(outputVar);
        CNode outSet = getFunctionCall(MatrixFunction.SET, outToken, counterVar, matrixGet);

        // Increment counter
        CNode incrementCounter = getFunctionCall(COperator.Addition, counterVar, CNodeFactory.newCNumber(1));

        CNode innerLoop = CNodeFactory.newBlock(outSet, incrementCounter);
        CNode fors = new ArrayIndexUtils(data).buildFors(functionIndexes, innerLoop);
        /*
        	MemoryLayout memLayout = data.getSetup().getMultipleChoice(CirOption.MEMORY_LAYOUT, MemoryLayout.class);
        
        	// Identifier array has values store in the order of the memory layout. Have to access them respecting that
        	// order
        	if (memLayout == MemoryLayout.ROW_MAJOR) {
        	    Collections.reverse(functionIndexes);
        	}
        
        	for (ArrayIndex arrayIndex : functionIndexes) {
        	    // Get for
        	    CToken forToken = arrayIndex.getFor(Arrays.asList(currentInstruction));
        
        	    // If null, means that the index does not need a for
        	    if (forToken == null) {
        		continue;
        	    }
        
        	    // Update current token
        	    currentInstruction = forToken;
        	}
        */
        instructions.addInstruction(fors);

        // Add return
        instructions.addReturn(CNodeFactory.newVariable(outputVar));

        return instructions;
    }

    /**
     * @param matrixType
     * @param functionIndexes
     * @return
     */
    private static FunctionType getFunctionTypes(VariableType matrixType, List<ArrayIndex> functionIndexes) {
        String matrixName = ArrayIndexUtils.getReadMatrixName();

        List<String> inputNames = SpecsFactory.newArrayList();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        inputNames.add(matrixName);
        inputTypes.add(matrixType);

        for (ArrayIndex index : functionIndexes) {
            for (CNode input : index.getFunctionInputs()) {
                assert input instanceof VariableNode;
                Variable inputIndex = ((VariableNode) input).getVariable();
                inputNames.add(inputIndex.getName());
                inputTypes.add(inputIndex.getType());
            }

        }

        // Get output variable
        String outputName = "write_matrix";
        VariableType outputType = ArrayIndexUtils.getOutputType(matrixType, functionIndexes);

        if (outputType == null) {
            return null;
        }

        return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
    }

    /**
     * @param readMatrix
     * @param functionIndexes
     * @param outputVar
     * @return
     */
    /*
    private FunctionTypes getFunctionTypes(CToken readMatrix, List<ArrayIndex> functionIndexes, CToken writeMatrix) {
    List<String> inputNames = FactoryUtils.newArrayList();
    List<VariableType> inputTypes = FactoryUtils.newArrayList();
    
    Variable inputVar = CTokenContent.getVariable(readMatrix);
    inputNames.add(inputVar.getName());
    inputTypes.add(inputVar.getType());
    
    for (ArrayIndex index : functionIndexes) {
        for (CToken input : index.getFunctionInputs()) {
    	Variable inputIndex = CTokenContent.getVariable(input);
    	inputNames.add(inputIndex.getName());
    	inputTypes.add(inputIndex.getType());
        }
    
    }
    
    Variable outputVar = CTokenContent.getVariable(writeMatrix);
    String outName = outputVar.getName();
    VariableType outType = outputVar.getType();
    
    return FunctionTypes.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outName, outType);
    }
    */

    /**
     * @param matrixType
     * @param functionIndexes
     * @return
     */
    private static String getName(VariableType matrixType, List<ArrayIndex> functionIndexes) {
        StringBuilder builder = new StringBuilder();

        // Append prefix
        builder.append("set_mult_");

        // Append read matrix type
        builder.append(matrixType.getSmallId());

        // For each input of the index, append its small id
        for (ArrayIndex arrayIndex : functionIndexes) {
            builder.append("_");
            builder.append(arrayIndex.getSmallId());
        }

        return builder.toString();

    }

    /**
     * @return
     */
    private CNode newCounterVar() {
        String counterName = "counter";
        CNode counterVar = CNodeFactory.newVariable(counterName, getNumerics().newInt());
        return counterVar;
    }
}
