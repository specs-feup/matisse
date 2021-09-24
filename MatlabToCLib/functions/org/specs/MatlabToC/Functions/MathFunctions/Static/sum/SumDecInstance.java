package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

public class SumDecInstance extends AInstanceBuilder {

    // The names of the inputs and outputs and the function
    private final static String INPUT_NAME = "input_matrix";
    private final static String OUTPUT_NAME = "output_matrix";
    private final static String BASE_FUNCTION_NAME = "sum_dec";

    public SumDecInstance(ProviderData data) {
	super(data);
    }

    public static FunctionInstance newInstance(ProviderData data) {
	return new SumDecInstance(data).create();
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> inputTypes = getData().getInputTypes();

	// FIX [ SumDecInstance.newInstance() ] : javadoc this method

	int dimToSum = findDimToSum();

	// The input and output shapes; The output shape is equal to the input but has a 1 on the dimension along which
	// we will sum
	List<Integer> inputShape = MatrixUtils.getShapeDims(inputTypes.get(0));
	List<Integer> outputShape = SpecsFactory.newArrayList(inputShape);
	outputShape.set(dimToSum - 1, 1);

	// The function types
	FunctionType functionTypes = buildFunctionTypes(inputTypes, outputShape);

	String cFunctionName = buildFunctionName(functionTypes, dimToSum);
	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, inputShape, outputShape, dimToSum);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);

	// return new SumDecInstance(functionTypes, dimToSum, inputShape, outputShape, useSolver);
    }

    /**
     * Finds the dimension along which we will sum. Defaults to the first non-singleton dimension.
     * 
     * @param inputTypes
     *            - the original inputs
     * @return the DIM input, or 1 if no input was provided
     */
    private int findDimToSum() {

	List<VariableType> inputTypes = getData().getInputTypes();

	if (inputTypes.size() == 3) {
	    return ScalarUtils.getConstant(inputTypes.get(1)).intValue();
	}

	if (inputTypes.size() == 2) {
	    if (!StringTypeUtils.isString(inputTypes.get(1))) {
		return ScalarUtils.getConstant(inputTypes.get(1)).intValue();
	    }
	}

	MatrixType matrixType = getData().getInputType(MatrixType.class, 0);

	// return MatrixUtils.firstNonSingletonDimension(inputTypes.get(0)) + 1;
	return matrixType.getTypeShape().getFirstNonSingletonDimension() + 1;
    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     *            - the original arguments
     * @param outputShape
     *            - the shape of the output matrix
     * @param useLinearArrays
     *            - whether matrices are represented by linear arrays
     * @return an instance of {@link FunctionType}
     */
    private static FunctionType buildFunctionTypes(List<VariableType> originalTypes, List<Integer> outputShape) {

	// The input type
	List<VariableType> inputTypes = Arrays.asList(originalTypes.get(0));

	// The input name
	List<String> inputNames = Arrays.asList(INPUT_NAME);

	// The output is matrix of either a Double (default) or the same as the input matrix
	// VariableType outputInnerType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
	// VariableType outputInnerType = numerics().newDouble();
	VariableType outputInnerType = MatrixUtils.getElementType(originalTypes.get(0));

	// If the class string is passed as an argument ( the last one )
	if (inputTypes.size() == 3) {

	    // See if it is native
	    String numericTypeString = StringTypeUtils.getString(inputTypes.get(2));

	    if (numericTypeString.equals("native")) {
		outputInnerType = ScalarUtils.toScalar(originalTypes.get(0));
	    }
	}

	// The output type
	VariableType outputType = StaticMatrixType.newInstance(outputInnerType, outputShape);

	// The output name
	String outputName = OUTPUT_NAME;

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
    }

    /**
     * Builds the instructions of this instance's body.
     * 
     * @param useSolver
     * 
     * @return an instance of {@link CInstructionList} with the instructions
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, List<Integer> inputShape,
	    List<Integer> outputShape, int dimToSum) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The input and output arrays
	Variable inputArray = functionTypes.getInputVar(INPUT_NAME);
	Variable outputArray = functionTypes.getInputVar(OUTPUT_NAME);

	// Build the function call to zeros and add it to the instructions ( output = zeros(output_shape); )
	CNode zerosFunctionCall = buildZerosInstruction(functionTypes, outputShape, outputArray);
	instructions.addInstruction(zerosFunctionCall, InstructionType.FunctionCall);

	// Build the FOR loops block and add it to the instructions
	CNode blockToken = buildForLoopsBlock(inputShape, dimToSum, inputArray, outputArray);
	instructions.addInstruction(blockToken, InstructionType.Block);

	// Build and add the return instruction
	CNode returnToken = CNodeFactory.newReturn(outputArray);
	instructions.addInstruction(returnToken, InstructionType.Return);

	return instructions;
    }

    /**
     * Builds the blocks of FOR loops. The inner loop has the sum instruction.
     * 
     * @param inputArray
     *            - the input array {@link Variable}
     * @param outputArray
     *            - the output array {@link Variable}
     * @param useSolver
     * @return an instance of {@link CNode} with the block
     */
    private CNode buildForLoopsBlock(List<Integer> inputShape, int dimToSum, Variable inputArray,
	    Variable outputArray) {

	/** Build the FOR loops **/

	// The list of induction variables
	List<CNode> inductionTokens = SpecsFactory.newArrayList();
	List<String> indexNames = new ArrayList<>();

	// The list of tokens with the FOR loop instructions
	List<CNode> forTokens = SpecsFactory.newArrayList();

	// For each dimension of the input
	for (int i = 0; i < inputShape.size(); i++) {

	    // The data for the loop
	    String indexName = SpecsStrings.getAlphaId(i);
	    Variable inductionVariable = new Variable(indexName, getNumerics().newInt());
	    // VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	    CNode startValue = CNodeFactory.newCNumber(0);
	    COperator stopOp = COperator.LessThan;
	    CNode endValue = CNodeFactory.newCNumber(inputShape.get(i));
	    COperator incrementOp = COperator.Addition;

	    // Create a new FOR loop and add it to the list of FOR loops
	    CNode forToken = new ForNodes(getData()).newForInstruction(inductionVariable, startValue, stopOp,
		    endValue, incrementOp, CNodeFactory.newCNumber(1));

	    forTokens.add(forToken);

	    // Build a CToken with the induction variable and add it to the list of induction variables
	    inductionTokens.add(CNodeFactory.newVariable(inductionVariable));
	    indexNames.add(indexName);
	}

	/** Build the instruction of the inner loop **/

	// MatrixNodes matrixNodes = new MatrixNodes(getSetup());

	// Build token for input 'get'
	// CNode inputGet = matrixUtils.newGet(inputArray, inductionTokens);
	CNode inputGet = getNodes().matrix().get(inputArray, indexNames);

	// Build token for output 'get'
	// The accesses to the input and output arrays, the output subscript has 1 on the dimension along which we sum
	List<CNode> outputIndexes = SpecsFactory.newArrayList(inductionTokens);
	outputIndexes.set(dimToSum - 1, CNodeFactory.newCNumber(0));
	List<String> outputIndexNames = new ArrayList<>(indexNames);
	outputIndexNames.set(dimToSum - 1, "0");

	// CNode outputGet = matrixUtils.newGet(outputArray, outputIndexes);
	CNode outputGet = getNodes().matrix().get(outputArray, outputIndexNames);

	// The sum
	CNode sum = getFunctionCall(COperator.Addition, outputGet, inputGet);

	// The set
	// CNode outputSet = matrixUtils.newSet(outputArray, outputIndexes, sum);
	CNode outputSet = getNodes().matrix().set(outputArray, sum, outputIndexNames);
	CNode innerInstruction = CNodeFactory.newInstruction(InstructionType.FunctionCall, outputSet);

	/** Build the blocks of blocks **/

	// Build the first block (inner)
	List<CNode> innerBlockTokens = Arrays.asList(forTokens.get(forTokens.size() - 1), innerInstruction);
	CNode block = CNodeFactory.newBlock(innerBlockTokens);

	// Build all the other blocks
	for (int i = forTokens.size() - 2; i >= 0; i--) {

	    List<CNode> blockTokens = Arrays.asList(forTokens.get(i), block);
	    block = CNodeFactory.newBlock(blockTokens);
	}

	// Return the outer block
	return block;
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the funcitno call
     */
    private CNode buildZerosInstruction(FunctionType functionTypes, List<Integer> outputShape,
	    Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	for (Integer dim : outputShape) {
	    // zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(dim)));
	    zeroTypes.add(getNumerics().newInt(dim));
	}

	// zeroTypes.add(VariableTypeFactory.newString(MatlabToCTypes.getNumericClass(
	// VariableTypeUtilsOld.getNumericType(functionTypes.getCReturnType())).getMatlabString()));
	zeroTypes.add(VariableTypeFactory.newString(MatlabToCTypesUtils.getNumericClass(functionTypes.getCReturnType())
		.getMatlabString()));

	// Get the implementation of 'zeros'
	InstanceProvider zerosProvider = ConstantArrayDecInstance.newProvider("zeros", 0);
	FunctionInstance zerosImplementation = getInstance(zerosProvider, zeroTypes);
	// FunctionInstance zerosImplementation = ConstantArrayDecInstance.newInstance(zeroTypes,
	// "zeros", 0);

	// The output token
	CNode outputToken = CNodeFactory.newVariable(outputVariable);

	// Get the function call to 'zeros'
	CNode zerosCallToken = zerosImplementation.newFunctionCall(Arrays.asList(outputToken));

	return zerosCallToken;
    }

    private static String buildFunctionName(FunctionType functionTypes, int dimToSum) {
	StringBuilder builder = new StringBuilder();

	builder.append(BASE_FUNCTION_NAME);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	builder.append(typesSuffix);

	builder.append("_dim");
	builder.append(dimToSum);

	return builder.toString();
    }

}
