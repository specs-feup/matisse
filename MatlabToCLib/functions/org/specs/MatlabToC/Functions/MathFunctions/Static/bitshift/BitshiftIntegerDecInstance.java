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

package org.specs.MatlabToC.Functions.MathFunctions.Static.bitshift;

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

public class BitshiftIntegerDecInstance extends AInstanceBuilder {

    /**
     * @param data
     */
    public BitshiftIntegerDecInstance(ProviderData data) {
	super(data);
    }

    // The names of the inputs and outputs and the function
    private final static String FIRST_INPUT_NAME = "input";
    private final static String SECOND_INPUT_NAME = "bits_to_shift";
    private final static String OUTPUT_NAME = "output";
    private final static String BASE_FUNCTION_NAME = "bitshift_integer";
    private final static String ABS_BTS_NAME = "abs_bits_to_shift";

    /**
     * The private constructor used by the static method <i>newInstance()</i>.
     * 
     * @param functionTypes
     */
    /*
    private BitshiftIntegerDecInstance(FunctionTypes functionTypes, boolean isScalar,
        List<Integer> shape) {
    super(functionTypes);
    
    this.instructions = buildInstructions(isScalar, shape);
    }
    */

    /**
     * Creates a new instance of {@link BitshiftIntegerDecInstance}.
     * 
     * @param inputTypes
     * @param useLinearArrays
     * @return
     */
    /*
    public static BitshiftIntegerDecInstance newInstance(List<VariableType> inputTypes,
        boolean useLinearArrays) {
    
    // FIX [ BitshiftInteger2Instance.newInstance() ] : javadoc this
    
    // Build the function types
    FunctionTypesData data = buildFunctionTypes(inputTypes, useLinearArrays);
    
    return new BitshiftIntegerDecInstance(data.functionTypes, data.isScalar, data.shape);
    }
    */

    /**
     * Creates a new instance of {@link BitshiftIntegerDecInstance}.
     * 
     * @param inputTypes
     * @param useLinearArrays
     * @return
     */
    // public static FunctionInstance newInstance(List<VariableType> inputTypes) {
    public static FunctionInstance newInstance(ProviderData data) {

	return new BitshiftIntegerDecInstance(data).create();
    }

    @Override
    public FunctionInstance create() {

	// FIX [ BitshiftInteger2Instance.newInstance() ] : javadoc this

	// Build the function types and other data
	FunctionTypesData data = buildFunctionTypes(getData().getInputTypes());

	// Get FunctionTypes
	FunctionType functionTypes = data.functionTypes;

	// Build function name
	String nameSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = BASE_FUNCTION_NAME + nameSuffix;

	// Build filename
	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();

	// Build instructions
	CInstructionList cBody = buildInstructions(data.functionTypes, data.isScalar, data.shape);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     *            - the inputs of the original function call
     * @param useLinearArrays
     *            - whether this instance uses linear arrays to represent matrices
     * @return an instance of {@link FunctionTypesData}
     */
    private static FunctionTypesData buildFunctionTypes(List<VariableType> originalTypes) {

	FunctionType functionTypes = null;
	List<Integer> shape = null;

	// See if the input is a scalar ( it can only be a numeric or a matrix )
	boolean isScalar = false;
	VariableType firstInput = originalTypes.get(0);
	// if (firstInput.getType() == CType.Numeric) {
	if (ScalarUtils.isScalar(firstInput)) {
	    isScalar = true;
	}

	// The input types
	List<VariableType> inputTypes = Arrays.asList(originalTypes.get(0), originalTypes.get(1));

	// The input names
	List<String> inputNames = Arrays.asList(FIRST_INPUT_NAME, SECOND_INPUT_NAME);

	// The output name
	String outputName = OUTPUT_NAME;

	// The output type
	VariableType outputInnerType = ScalarUtils.toScalar(firstInput);

	// If the input is scalar we return a scalar of the same type
	if (isScalar) {

	    // The output type
	    VariableType outputType = outputInnerType;

	    functionTypes = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);

	    // If it is not, we return a matrix with the same dimensions and the same inner type
	} else {

	    shape = MatrixUtils.getShapeDims(firstInput);

	    // The output type
	    VariableType outputType = StaticMatrixType.newInstance(outputInnerType, shape);

	    functionTypes = FunctionType
		    .newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
	}

	// Return the function types
	return new FunctionTypesData(functionTypes, isScalar, shape);

    }

    /**
     * Builds the instructions of the body of this instance.
     * 
     * @param isScalar
     *            - is the input scalar?
     * @return an instance of {@link CInstructionList} with the instructions
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, boolean isScalar, List<Integer> shape) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The input variables and their tokens
	Variable firstVar = functionTypes.getInputVar(FIRST_INPUT_NAME);
	Variable secondVar = functionTypes.getInputVar(SECOND_INPUT_NAME);
	CNode firstVarToken = CNodeFactory.newVariable(firstVar);
	CNode secondVarToken = CNodeFactory.newVariable(secondVar);

	if (isScalar) {
	    buildInstructionsForScalar(instructions, firstVarToken, secondVarToken);
	} else {
	    buildInstructionsForMatrix(instructions, shape, firstVar, firstVarToken, secondVarToken);
	}

	return instructions;
    }

    /**
     * Builds the instructions of the body for the case where the input is a matrix.
     * 
     * @param instructions
     *            - the list of instructions
     * @param shape
     *            - the shape of the output matrix
     */
    private void buildInstructionsForMatrix(CInstructionList instructions, List<Integer> shape, Variable firstVar,
	    CNode input, CNode bitsToShift) {

	// The length of the linear version of the matrix
	int length = calculateLength(shape);

	// The output variable and its token
	FunctionType functionTypes = instructions.getFunctionTypes();
	Variable outputVar = functionTypes.getInputVar(OUTPUT_NAME);
	CNode outputVarToken = CNodeFactory.newVariable(outputVar);

	// The new bits to shift variable ( with the absolute value ) and its token
	// Variable absBitsToShiftVar = new Variable(ABS_BTS_NAME, VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable absBitsToShiftVar = new Variable(ABS_BTS_NAME, getNumerics().newInt());
	CNode absBitsToShift = CNodeFactory.newVariable(absBitsToShiftVar);

	// Test if the K input is positive or negative
	CNode ifCondition = getFunctionCall(COperator.LessThan, bitsToShift, CNodeFactory.newCNumber(0));

	// If true ( K < 0 )
	CNode multiplication = getFunctionCall(COperator.Multiplication, bitsToShift, CNodeFactory.newCNumber(-1));
	CNode abs = CNodeFactory.newAssignment(absBitsToShift, multiplication);

	// The FOR loop that iterates the matrix and does a right shift
	CNode thenInstruction = buildLoop(firstVar, input, bitsToShift, absBitsToShift, outputVar, true, length);

	// The FOR loop that iterates the matrix and does a left shift
	CNode elseInstruction = buildLoop(firstVar, input, bitsToShift, bitsToShift, outputVar, false, length);

	// Build the block and add it to the instructions
	CNode ifBlock = IfNodes.newIfThenElse(ifCondition, Arrays.asList(abs, thenInstruction),
		Arrays.asList(elseInstruction));
	instructions.addInstruction(ifBlock, InstructionType.Block);

	// The return statement
	CNode returnT = CNodeFactory.newReturn(outputVarToken);
	instructions.addInstruction(returnT, InstructionType.Return);

    }

    /**
     * Calculates the length of the linear version of the matrix with the shape passed as input.
     * 
     * @param shape
     *            - the shape of the matrix
     * @return an {@link Integer} with the length
     */
    private static int calculateLength(List<Integer> shape) {

	int prod = 1;

	for (Integer integer : shape) {
	    prod *= integer;
	}

	return prod;
    }

    /**
     * Builds the FOR loop that iterates the matrix linearly and bitshifts each element.
     * 
     * @param inputVar
     *            - the variable with the first input
     * @param inputToken
     *            - the token that represents the first input
     * @param bitsToShift
     *            - the token that represents the second input
     * @param absBitsToShiftToken
     *            - the token that represents the variable that has the absolute value of the number of bits to shift
     * @param outputVar
     *            - the output variable
     * @param rightShift
     *            - a boolean that is true if this is a right shift, false if it is left
     * @param length
     *            - the length of the linear version of the matrix
     * 
     * @return a {@link CNode} with the FOR loop block
     */
    private CNode buildLoop(Variable inputVar, CNode inputToken, CNode bitsToShift, CNode absBitsToShiftToken,
	    Variable outputVar, boolean rightShift, int length) {

	// The induction variable and its token
	String indexName = "i";
	// Variable inductionVar = new Variable("i", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable inductionVar = new Variable("i", getNumerics().newInt());
	// CNode inductionToken = CNodeFactory.newVariable(inductionVar);

	// The access to the input and the output
	// CNode inputAccess = matrixUtils.newGet(inputVar, inductionToken);
	CNode inputAccess = getNodes().matrix().get(inputVar, indexName);

	// The instruction inside the loop
	CNode shiftCall = null;
	if (rightShift) {
	    shiftCall = getFunctionCall(COperator.BitwiseRightShit, inputAccess, absBitsToShiftToken);
	} else {
	    shiftCall = getFunctionCall(COperator.BitwiseLeftShit, inputAccess, absBitsToShiftToken);
	}

	// CNode outputSet = matrixUtils.newSet(outputVar, inductionToken, shiftCall);
	CNode outputSet = getNodes().matrix().set(outputVar, shiftCall, indexName);

	// The loop instruction
	CNode startValue = CNodeFactory.newCNumber(0);
	CNode endValue = CNodeFactory.newCNumber(length);
	COperator stopOp = COperator.LessThan;
	COperator incrementOp = COperator.Addition;

	return new ForNodes(getData()).newForLoopBlock(inductionVar, startValue, stopOp, endValue, incrementOp,
		CNodeFactory.newCNumber(1),
		outputSet);
    }

    /**
     * Builds the instructions of the body for the case where the input is a scalar.
     * 
     * @param instructions
     *            - the list of instructions
     * @param input
     *            - the token that represents the first input
     * @param bitsToShift
     *            - the token that represents the second input
     */
    private void buildInstructionsForScalar(CInstructionList instructions, CNode input, CNode bitsToShift) {

	// The output variable and its token
	FunctionType functionTypes = instructions.getFunctionTypes();
	Variable outputVar = functionTypes.getReturnVar();
	CNode outputVarToken = CNodeFactory.newVariable(outputVar);

	// The new bits to shift variable ( with the absolute value ) and its token
	// Variable absBitsToShiftVar = new Variable(ABS_BTS_NAME, VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable absBitsToShiftVar = new Variable(ABS_BTS_NAME, getNumerics().newInt());
	CNode absBitsToShiftToken = CNodeFactory.newVariable(absBitsToShiftVar);

	// Test if the K input is positive or negative
	CNode ifCondition = getFunctionCall(COperator.LessThan, bitsToShift, CNodeFactory.newCNumber(0));

	// If true ( K < 0 ) we do a right shift ( after we make the number of bits to shift positive )
	CNode multiplication = getFunctionCall(COperator.Multiplication, bitsToShift, CNodeFactory.newCNumber(-1));
	CNode abs = CNodeFactory.newAssignment(absBitsToShiftToken, multiplication);
	CNode rightShift = getFunctionCall(COperator.BitwiseRightShit, input, absBitsToShiftToken);
	CNode thenInstruction = CNodeFactory.newAssignment(outputVarToken, rightShift);

	// If false we do a left shit
	CNode leftShift = getFunctionCall(COperator.BitwiseLeftShit, input, bitsToShift);
	CNode elseInstruction = CNodeFactory.newAssignment(outputVarToken, leftShift);

	// Build the block and add it to the instructions
	CNode ifBlock = IfNodes.newIfThenElse(ifCondition, Arrays.asList(abs, thenInstruction),
		Arrays.asList(elseInstruction));
	instructions.addInstruction(ifBlock, InstructionType.Block);

	// The return statement
	CNode returnT = CNodeFactory.newReturn(outputVarToken);
	instructions.addInstruction(returnT, InstructionType.Return);

    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getInstructions()
     */
    /*
    @Override
    protected CInstructionList getInstructions() {
    return instructions;
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCName()
     */
    /*
    @Override
    public String getCName() {
    
    StringBuilder builder = new StringBuilder();
    
    builder.append(BASE_FUNCTION_NAME);
    
    String typesSuffix = FunctionUtils.getTypesSuffix(getFunctionTypes().getCInputTypes());
    builder.append(typesSuffix);
    
    return builder.toString();
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCFileName()
     */
    /*
    @Override
    public String getCFileName() {
    return MatlabCFilename.MatlabGeneral.getCFilename();
    }
    */
    /**
     * A class used as the return type for the method <i>buildFunctionTypes()</i>.
     * 
     * @author Pedro Pinto
     * 
     */
    private static class FunctionTypesData {

	public final FunctionType functionTypes;
	public final boolean isScalar;
	public final List<Integer> shape;

	public FunctionTypesData(FunctionType functionTypes, boolean isScalar, List<Integer> shape) {

	    this.functionTypes = functionTypes;
	    this.isScalar = isScalar;
	    this.shape = shape;
	}

    }
}
