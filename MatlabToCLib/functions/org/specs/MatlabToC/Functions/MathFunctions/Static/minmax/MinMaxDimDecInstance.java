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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

public class MinMaxDimDecInstance extends AInstanceBuilder {

    // The names
    private final static String MM_INPUT_MATRIX_NAME = "matrix";
    private final static String MM_OUTPUT_STRING_NAME = "max";
    private final static String MM_BASE_FUNCTION_NAME = "_dec";

    private final MinMax minOrMax;

    public MinMaxDimDecInstance(ProviderData data, MinMax minOrMax) {
	super(data);

	this.minOrMax = minOrMax;
    }

    public static FunctionInstance newInstance(ProviderData data, MinMax minOrMax) {
	return new MinMaxDimDecInstance(data, minOrMax).create();
    }

    /**
     * Creates and returns a new instance of {@link MinMaxDimDecInstance}.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @param useSolver
     * @param minOrMax
     * @return
     */
    @Override
    public FunctionInstance create() {

	List<VariableType> originalTypes = getData().getInputTypes();

	// FIX [ MinMaxDimDecInstance.newInstance() ] : javadoc this function with the correct inputs (matrix, DIM

	// Build the function types
	MinMaxData output = buildFunctionTypes(originalTypes);

	// return new MinMaxDimDecInstance(output.getFunctionTypes(), minOrMax,
	// output.getInputShape(), useSolver, output.getDim());
	String cFunctionName = getFunctionName(output.getFunctionTypes(), minOrMax, output.getDim());
	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(output, minOrMax);

	return new InstructionsInstance(output.getFunctionTypes(), cFunctionName, cFilename, cBody);
    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @return
     */
    private static MinMaxData buildFunctionTypes(List<VariableType> originalTypes) {

	// Get data about the inputs
	VariableType inputMatrix = originalTypes.get(0);
	List<Integer> inputShape = MatrixUtils.getShapeDims(inputMatrix);

	VariableType inputDim = originalTypes.get(1);
	// int dim = VariableTypeContent.getNumeric(inputDim).getIntValue();
	int dim = ScalarUtils.getConstant(inputDim).intValue();

	// The input types and names
	List<VariableType> inputTypes = Arrays.asList(inputMatrix);
	List<String> inputNames = Arrays.asList(MM_INPUT_MATRIX_NAME);

	// The output type and name
	List<Integer> outputShape = SpecsFactory.newArrayList(inputShape);
	outputShape.set(dim - 1, 1);
	VariableType outputInnerType = MatrixUtils.getElementType(inputMatrix);

	VariableType outputType = StaticMatrixType.newInstance(outputInnerType, outputShape);

	String outputName = MM_OUTPUT_STRING_NAME;

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	return new MinMaxData(functionTypes, inputShape, outputShape, dim);
    }

    /**
     * Builds the instructions needed for this instance.
     * 
     * @param data
     * 
     * @param minOrMax
     *            - whether this instance is for the min or max functions
     * @param useSolver
     * @return an instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions(MinMaxData data, MinMax minOrMax) {

	FunctionType functionTypes = data.getFunctionTypes();

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The input and output arrays
	Variable inputArray = functionTypes.getInputVar(MM_INPUT_MATRIX_NAME);
	Variable outputArray = functionTypes.getInputVar(MM_OUTPUT_STRING_NAME);

	instructions.addComment(" assign the first slice of the input to the output");

	// Build the first FOR loop nest
	CNode firstNestT = buildFirstNest(data, inputArray, outputArray);
	instructions.addInstruction(firstNestT, InstructionType.Block);

	instructions.addComment(" test the rest of the values against these ones ");

	// Build the second FOR loop nest
	CNode secondNestT = buildSecondNest(data, minOrMax, inputArray, outputArray);
	instructions.addInstruction(secondNestT, InstructionType.Block);

	// Build the return instruction
	CNode returnT = CNodeFactory.newReturn(outputArray);
	instructions.addInstruction(returnT, InstructionType.Return);

	return instructions;
    }

    /**
     * Builds the second nest of FOR loops, the one responsible for assigning the first slice of the input to the
     * output.
     * 
     * @param data
     * 
     * @param inputArray
     * @param outputArray
     * @param useSolver
     * @return
     */
    private CNode buildSecondNest(MinMaxData data, MinMax minOrMax, Variable inputArray, Variable outputArray) {

	/** Build the FOR loops **/

	// Two lists for the induction variables tokens and for the tokens with the FOR loops
	List<String> indexNames = SpecsFactory.newArrayList();
	List<CNode> inductionVariablesT = SpecsFactory.newArrayList();
	List<CNode> forLoopsT = SpecsFactory.newArrayList();

	int dimToSkip = data.getDim() - 1;

	// For each dimension of the input
	for (int i = 0; i < data.getInputShape().size(); i++) {

	    // Create the induction variable
	    String indexName = SpecsStrings.getAlphaId(i).toLowerCase();
	    Variable inductionVariable = new Variable(indexName,
		    // VariableTypeFactoryOld.newNumeric(NumericType.Cint));
		    getNumerics().newInt());

	    // Build a new FOR loop
	    CNode forLoopT = buildForLoop(data.getInputShape(), i, inductionVariable, dimToSkip);
	    forLoopsT.add(forLoopT);

	    // Build a new CToken with the induction variable
	    inductionVariablesT.add(CNodeFactory.newVariable(inductionVariable));
	    indexNames.add(indexName);
	}

	// MatrixNodes matrixNodes = new MatrixNodes(getSetup());

	/** Build the instructions of the inner loop **/

	// Create a subscript of the type [i][j][0][l] (if DIM was 3)
	// List<CNode> outputAccessSubscript = FactoryUtils.newArrayList(inductionVariablesT);
	// outputAccessSubscript.set(data.getDim() - 1, CNodeFactory.newCNumber(0));
	List<String> outputIndexNames = new ArrayList<>(indexNames);
	outputIndexNames.set(data.getDim() - 1, "0");

	// Create a subscript of the type [i][j][k][l]
	// List<CNode> inputAccessSubscript = inductionVariablesT;

	// Create the array accesses
	// CNode inputArrayAccessT = matrixUtils.newGet(inputArray, inputAccessSubscript);
	CNode inputArrayAccessT = getNodes().matrix().get(inputArray, indexNames);
	// CNode outputArrayAccessT = matrixUtils.newGet(outputArray, outputAccessSubscript);
	CNode outputArrayAccessT = getNodes().matrix().get(outputArray, outputIndexNames);

	// The output 'set'
	// CNode outputArraySet = matrixUtils.newSet(outputArray, outputAccessSubscript, inputArrayAccessT);
	CNode outputArraySet = getNodes().matrix().set(outputArray, inputArrayAccessT, outputIndexNames);
	CNode setInst = CNodeFactory.newInstruction(InstructionType.FunctionCall, outputArraySet);

	// The condition
	// CToken conditionT = minOrMax.getOperator().getFunctionCall(inputArrayAccessT,
	// outputArrayAccessT);
	CNode conditionT = getFunctionCall(minOrMax.getOperator(), inputArrayAccessT, outputArrayAccessT);

	// The IF block
	CNode innerInstruction = IfNodes.newIfThen(conditionT, Arrays.asList(setInst));

	/** Build the loop blocks **/

	// Build the first ( inner ) block
	List<CNode> innerBlockT = Arrays.asList(forLoopsT.get(forLoopsT.size() - 1), innerInstruction);
	CNode blockToken = CNodeFactory.newBlock(innerBlockT);

	// Build the rest
	for (int i = forLoopsT.size() - 2; i >= 0; i--) {

	    List<CNode> blockTokens = Arrays.asList(forLoopsT.get(i), blockToken);
	    blockToken = CNodeFactory.newBlock(blockTokens);
	}

	// Return the outer most block
	return blockToken;
    }

    /**
     * Builds the first nest of FOR loops, the one responsible for assigning the first slice of the input to the output.
     * 
     * @param data
     * 
     * @param inputArray
     * @param outputArray
     * @param useSolver
     * @return
     */
    private CNode buildFirstNest(MinMaxData data, Variable inputArray, Variable outputArray) {

	/** Build the FOR loops **/

	// Two lists for the induction variables tokens and for the tokens with the FOR loops
	List<String> inductionVariablesNames = SpecsFactory.newArrayList();
	List<CNode> inductionVariablesT = SpecsFactory.newArrayList();
	List<CNode> forLoopsT = SpecsFactory.newArrayList();

	int dimToSkip = data.getDim() - 1;

	// For each dimension of the input
	for (int i = 0; i < data.getInputShape().size(); i++) {

	    // No FOR loop for this dimension
	    if (i == dimToSkip) {
		continue;
	    }

	    // Create the induction variable
	    String variableName = SpecsStrings.getAlphaId(i).toLowerCase();
	    Variable inductionVariable = new Variable(variableName, getNumerics().newInt());
	    // VariableTypeFactoryOld.newNumeric(NumericType.Cint));

	    // Build a new FOR loop
	    CNode forLoopT = buildForLoop(data.getInputShape(), i, inductionVariable, null);
	    forLoopsT.add(forLoopT);

	    // Build a new CToken with the induction variable
	    inductionVariablesT.add(CNodeFactory.newVariable(inductionVariable));
	    inductionVariablesNames.add(variableName);
	}

	/** Build the instructions of the inner loop **/

	// Create a subscript of the type [i][j][0][l] (if DIM was 3)
	// List<CNode> accessSubscript = FactoryUtils.newArrayList(inductionVariablesT);
	// accessSubscript.add(dimToSkip, CNodeFactory.newCNumber(0));
	inductionVariablesNames.add(dimToSkip, "0");

	// Create the array accesses
	// CNode inputArrayAccessT = matrixUtils.newGet(inputArray, accessSubscript);
	CNode inputArrayAccessT = getNodes().matrix().get(inputArray, inductionVariablesNames);

	// The assignment
	// CNode outputSet = matrixUtils.newSet(outputArray, accessSubscript, inputArrayAccessT);
	CNode outputSet = getNodes().matrix().set(outputArray, inputArrayAccessT, inductionVariablesNames);

	CNode innerInstruction = CNodeFactory.newInstruction(InstructionType.FunctionCall, outputSet);

	/** Build the loop blocks **/

	// Build the first ( inner ) block
	List<CNode> innerBlockT = Arrays.asList(forLoopsT.get(forLoopsT.size() - 1), innerInstruction);
	CNode blockToken = CNodeFactory.newBlock(innerBlockT);

	// Build the rest
	for (int i = forLoopsT.size() - 2; i >= 0; i--) {

	    List<CNode> blockTokens = Arrays.asList(forLoopsT.get(i), blockToken);
	    blockToken = CNodeFactory.newBlock(blockTokens);
	}

	// Return the outer most block
	return blockToken;
    }

    /**
     * Creates a new FOR loop.
     * 
     * @param i
     * @return
     */
    CNode buildForLoop(List<Integer> inputShape, int i, Variable inductionVariable, Integer dimToSkip) {

	// The data for the loop
	CNode startValue = CNodeFactory.newCNumber(0);

	// If dimToSkip == null we want to start at 0, if != we might want to start at 1
	if (dimToSkip != null) {
	    if (i == dimToSkip) {
		startValue = CNodeFactory.newCNumber(1);
	    }
	}

	COperator stopOp = COperator.LessThan;
	CNode endValue = CNodeFactory.newCNumber(inputShape.get(i));
	COperator incrementOp = COperator.Addition;

	// Create a new FOR loop
	return new ForNodes(getData()).newForInstruction(inductionVariable, startValue, stopOp, endValue,
		incrementOp, CNodeFactory.newCNumber(1));
    }

    static String getFunctionName(FunctionType functionTypes, MinMax minOrMax, int dim) {

	StringBuilder builder = new StringBuilder();

	builder.append(minOrMax.getName());

	builder.append(MM_BASE_FUNCTION_NAME);

	builder.append("_dim");
	builder.append(dim);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	builder.append(typesSuffix);

	return builder.toString();
    }

}
