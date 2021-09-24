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
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

public class MinMaxDimIndexDecInstance extends AInstanceBuilder {

    // The dimension along which we work
    /*
    private final int dim;

    private final MinMax minOrMax;
    private final CInstructionList instructions;
    private final List<Integer> inputShape;
    */
    // The names
    private final static String MM_INPUT_MATRIX_NAME = "matrix";
    private final static String MM_OUTPUT_MATRIX_STRING_NAME = "max";
    private final static String MM_OUTPUT_INDEXES_STRING_NAME = "indexes";

    private final MinMax minOrMax;

    public MinMaxDimIndexDecInstance(ProviderData data, MinMax minOrMax) {
	super(data);

	this.minOrMax = minOrMax;
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
    public static FunctionInstance newInstance(ProviderData data, MinMax minOrMax) {
	return new MinMaxDimIndexDecInstance(data, minOrMax).create();
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> originalTypes = getData().getInputTypes();

	// FIX [ MinMaxDimDecInstance.newInstance() ] : javadoc this function with the correct inputs (matrix, DIM

	// Build the function types
	MinMaxData output = buildFunctionTypes(originalTypes);

	// return new MinMaxDimIndexDecInstance(output.getFunctionTypes(), minOrMax,
	// output.getInputShape(), useSolver, output.getDim());
	String cFunctionName = MinMaxDimDecInstance.getFunctionName(output.getFunctionTypes(), minOrMax,
		output.getDim());
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
    private MinMaxData buildFunctionTypes(List<VariableType> originalTypes) {

	// Get data about the inputs
	VariableType inputMatrix = originalTypes.get(0);
	List<Integer> inputShape = MatrixUtils.getShapeDims(inputMatrix);

	VariableType inputDim = originalTypes.get(1);
	// int dim = VariableTypeContent.getNumeric(inputDim).getIntValue();
	int dim = ScalarUtils.getConstant(inputDim).intValue();

	// The input types and names
	List<VariableType> inputTypes = Arrays.asList(inputMatrix);
	List<String> inputNames = Arrays.asList(MM_INPUT_MATRIX_NAME);

	// The output types and names
	List<Integer> matrixOutputShape = SpecsFactory.newArrayList(inputShape);
	matrixOutputShape.set(dim - 1, 1);
	VariableType matrixOutputInnerType = MatrixUtils.getElementType(inputMatrix);

	VariableType matrixOutputType = StaticMatrixType.newInstance(matrixOutputInnerType, matrixOutputShape);
	String matrixOutputName = MM_OUTPUT_MATRIX_STRING_NAME;

	VariableType indexesOutputType = StaticMatrixType.newInstance(getNumerics().newInt(), matrixOutputShape);
	// VariableTypeFactoryOld.newNumeric(NumericType.Cint), matrixOutputShape);
	String indexesOutputName = MM_OUTPUT_INDEXES_STRING_NAME;

	List<VariableType> outputTypes = Arrays.asList(matrixOutputType, indexesOutputType);
	List<String> outputNames = Arrays.asList(matrixOutputName, indexesOutputName);

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputNames,
		outputTypes);

	return new MinMaxData(functionTypes, inputShape, matrixOutputShape, dim);
    }

    /**
     * Builds the instructions needed for this instance.
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
	Variable outputArray = functionTypes.getInputVar(MM_OUTPUT_MATRIX_STRING_NAME);
	Variable outputIndexesArray = functionTypes.getInputVar(MM_OUTPUT_INDEXES_STRING_NAME);

	instructions.addComment(" assign the first slice of the input to the output");

	// Build the first FOR loop nest
	CNode firstNestT = buildFirstNest(data, inputArray, outputArray, outputIndexesArray);
	instructions.addInstruction(firstNestT, InstructionType.Block);

	instructions.addComment(" test the rest of the values against these ones ");

	// Build the second FOR loop nest
	CNode secondNestT = buildSecondNest(data, minOrMax, inputArray, outputArray, outputIndexesArray);
	instructions.addInstruction(secondNestT, InstructionType.Block);

	// Build the return instruction
	// CToken returnT = CTokenFactory.newReturn(outputArray);
	// instructions.addInstruction(returnT, InstructionType.Return);

	return instructions;
    }

    /**
     * Builds the second nest of FOR loops, the one responsible for assigning the first slice of the input to the
     * output.
     * 
     * @param minOrMax
     * 
     * @param inputArray
     * @param outputArray
     * @param outputIndexesArray
     * @param useSolver
     * @return
     */
    private CNode buildSecondNest(MinMaxData data, MinMax minOrMax, Variable inputArray, Variable outputArray,
	    Variable outputIndexesArray) {

	/** Build the FOR loops **/

	// Two lists for the induction variables tokens and for the tokens with the FOR loops
	List<CNode> inductionVariablesT = SpecsFactory.newArrayList();
	List<String> indexNames = new ArrayList<>();
	List<CNode> forLoopsT = SpecsFactory.newArrayList();

	int dimToSkip = data.getDim() - 1;

	// For each dimension of the input
	for (int i = 0; i < data.getInputShape().size(); i++) {

	    // Create the induction variable
	    String indexName = SpecsStrings.getAlphaId(i).toLowerCase();
	    Variable inductionVariable = new Variable(indexName, getNumerics().newInt());
	    // VariableTypeFactoryOld.newNumeric(NumericType.Cint));

	    // Build a new FOR loop
	    CNode forLoopT = new MinMaxDimDecInstance(getData(), minOrMax).buildForLoop(data.getInputShape(), i,
		    inductionVariable, dimToSkip);

	    forLoopsT.add(forLoopT);

	    // Build a new CToken with the induction variable
	    inductionVariablesT.add(CNodeFactory.newVariable(inductionVariable));
	    indexNames.add(indexName);
	}

	/** Build the instructions of the inner loop **/

	// Create a subscript of the type [i][j][0][l] (if DIM was 3)
	// List<CNode> outputAccessSubscript = FactoryUtils.newArrayList(inductionVariablesT);
	// outputAccessSubscript.set(dimToSkip, CNodeFactory.newCNumber(0));
	List<String> outputIndexNames = new ArrayList<>(indexNames);
	outputIndexNames.set(dimToSkip, "0");

	// Create a subscript of the type [i][j][k][l]
	// List<CNode> inputAccessSubscript = inductionVariablesT;

	// Create the array accesses
	// CNode inputArrayAccessT = matrixUtils.newGet(inputArray, inputAccessSubscript);
	CNode inputArrayAccessT = getNodes().matrix().get(inputArray, indexNames);
	// CNode outputArrayAccessT = matrixUtils.newGet(outputArray, outputAccessSubscript);
	CNode outputArrayAccessT = getNodes().matrix().get(outputArray, outputIndexNames);

	// CNode firstSet = matrixUtils.newSet(outputArray, outputAccessSubscript, inputArrayAccessT);
	CNode firstSet = getNodes().matrix().set(outputArray, inputArrayAccessT, outputIndexNames);
	CNode firstSetInst = CNodeFactory.newInstruction(InstructionType.FunctionCall, firstSet);

	// The index assignment
	CNode commentT = CNodeFactory.newComment(" index adjusted to match Matlab ");
	CNode oneToken = CNodeFactory.newCNumber(1);
	CNode dimToSkipToken = inductionVariablesT.get(dimToSkip);
	CNode sumT = getFunctionCall(COperator.Addition, dimToSkipToken, oneToken);

	// CNode secondSet = matrixUtils.newSet(outputIndexesArray, outputAccessSubscript, sumT);
	CNode secondSet = getNodes().matrix().set(outputIndexesArray, sumT, outputIndexNames);
	CNode secondSetInst = CInstructionList.newInstruction(secondSet);

	// The condition
	CNode conditionT = getFunctionCall(minOrMax.getOperator(), inputArrayAccessT, outputArrayAccessT);

	// The IF block
	CNode innerInstruction = IfNodes.newIfThen(conditionT, Arrays.asList(firstSetInst, commentT, secondSetInst));

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
     * @param inputArray
     * @param outputArray
     * @param outputIndexesArray
     * @param useSolver
     * @return
     */
    private CNode buildFirstNest(MinMaxData data, Variable inputArray, Variable outputArray,
	    Variable outputIndexesArray) {

	/** Build the FOR loops **/

	// Two lists for the induction variables tokens and for the tokens with the FOR loops
	// List<CNode> inductionVariablesT = FactoryUtils.newArrayList();
	List<String> indexNames = new ArrayList<>();
	List<CNode> forLoopsT = SpecsFactory.newArrayList();

	int dimToSkip = data.getDim() - 1;

	// For each dimension of the input
	for (int i = 0; i < data.getInputShape().size(); i++) {

	    // No FOR loop for this dimension
	    if (i == dimToSkip) {
		continue;
	    }

	    // Create the induction variable
	    String indexName = SpecsStrings.getAlphaId(i).toLowerCase();
	    Variable inductionVariable = new Variable(indexName, getNumerics().newInt());
	    // VariableTypeFactoryOld.newNumeric(NumericType.Cint));

	    // Build a new FOR loop
	    CNode forLoopT = new MinMaxDimDecInstance(getData(), minOrMax).buildForLoop(data.getInputShape(), i,
		    inductionVariable, null);
	    forLoopsT.add(forLoopT);

	    // Build a new CToken with the induction variable
	    // inductionVariablesT.add(CNodeFactory.newVariable(inductionVariable));
	    indexNames.add(indexName);
	}

	/** Build the instructions of the inner loop **/

	// Create a subscript of the type [i][j][0][l] (if DIM was 3)
	// List<CNode> accessSubscript = FactoryUtils.newArrayList(inductionVariablesT);
	// accessSubscript.add(dimToSkip, CNodeFactory.newCNumber(0));
	indexNames.add(dimToSkip, "0");

	// MatrixNodes matrixNodes = new MatrixNodes(getSetup());

	// Create the array accesses
	// CNode inputArrayAccessT = matrixUtils.newGet(inputArray, accessSubscript);
	CNode inputArrayAccessT = getNodes().matrix().get(inputArray, indexNames);

	// The assignments
	// CNode firstSet = matrixUtils.newSet(outputArray, accessSubscript, inputArrayAccessT);
	CNode firstSet = getNodes().matrix().set(outputArray, inputArrayAccessT, indexNames);

	CNode firstInnerInstruction = CNodeFactory.newInstruction(InstructionType.FunctionCall, firstSet);

	CNode oneToken = CNodeFactory.newCNumber(1);
	// CNode secondSet = matrixUtils.newSet(outputIndexesArray, accessSubscript, oneToken);
	CNode secondSet = getNodes().matrix().set(outputIndexesArray, oneToken, indexNames);

	CNode secondInnerInstruction = CNodeFactory.newInstruction(InstructionType.FunctionCall, secondSet);

	/** Build the loop blocks **/

	// Build the first ( inner ) block
	List<CNode> innerBlockT = Arrays.asList(forLoopsT.get(forLoopsT.size() - 1), firstInnerInstruction,
		secondInnerInstruction);
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
     * @param dimToSkip
     * @return
     */
    /*
    private CToken buildForLoop(int i, Variable inductionVariable, Integer dimToSkip) {

    // The data for the loop
    CToken startValue = CTokenFactory.newCNumber(0);

    // If dimToSkip == null we want to start at 0, if != we might want to start at 1
    if (dimToSkip != null) {
        if (i == dimToSkip) {
    	startValue = CTokenFactory.newCNumber(1);
        }
    }

    COperator stopOp = COperator.LessThan;
    CToken endValue = CTokenFactory.newCNumber(inputShape.get(i));
    COperator incrementOp = COperator.IncrementSuffix;

    // Create a new FOR loop
    return ForFactory.newForInstruction(inductionVariable, startValue, stopOp, endValue,
    	incrementOp, null);
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getInstructions()
     */
    /*
    @Override
    protected CInstructionList getInstructions() {
    return instructions;
    }

    @Override
    public String getCName() {

    StringBuilder builder = new StringBuilder();

    builder.append(minOrMax.getName());

    builder.append(MM_BASE_FUNCTION_NAME);

    builder.append("_dim");
    builder.append(dim);

    String typesSuffix = FunctionUtils.getTypesSuffix(getFunctionTypes().getCInputTypes());
    builder.append(typesSuffix);

    return builder.toString();
    }

    @Override
    public String getCFileName() {
    return MatlabCFilename.MatlabGeneral.getCFilename();
    }
    */
}
