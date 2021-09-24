package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRFunctions.MatrixDec.DeclaredProvider;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class MatrixMultiplicationDecInstance extends AInstanceBuilder {

    private final static String BASE_FUNCTION_NAME = "dec_matrix_multiplication";

    private final static String FIRST_INPUT_NAME = "A";
    private final static String SECOND_INPUT_NAME = "B";
    private final static String OUTPUT_NAME = "C";

    public MatrixMultiplicationDecInstance(ProviderData data) {
	super(data);
    }

    /**
     * Creates the following code:
     * 
     * <pre>
     * {@code
     *   for( i=0 ; i<m ; i++ )
     *   for( j=0 ; j<n ; j++ )
     *     for( k=0 ; k<o ; k++ )
     *       C[i + j*m] += A[i + k*m] * B[k + j*o];
     * }
     * </pre>
     * 
     * @param useLinearArrays
     *            - whether this instance uses linearized versions of the matrices
     * 
     * @return an instance of {@link CInstructionList} with the code
     */
    private CInstructionList buildInstructions(NewFunctionTypesReturn data) {

	FunctionType functionTypes = data.functionTypes;

	CInstructionList instructions = new CInstructionList(functionTypes);

	// Create a new temporary variable, used to store the output during execution, with the same type as the output
	// variable
	Variable temp = new Variable("temp", functionTypes.getInputVar(OUTPUT_NAME).getType());

	// Call the function zeros to clear the temp variable
	CNode zerosCall = buildZerosInstruction(functionTypes, temp, Arrays.asList(data.m, data.n));
	instructions.addInstruction(zerosCall, InstructionType.FunctionCall);

	// The output variable
	Variable c = functionTypes.getInputVar(OUTPUT_NAME);

	// Create the loop nest used to perform matrix multiplication
	CNode lastForBlock = createNestedForLoop(data, temp);
	instructions.addInstruction(lastForBlock, InstructionType.Block);

	// Call the copy function
	CNode copyCall = buildCopyInstruction(temp, c);
	instructions.addInstruction(copyCall, InstructionType.FunctionCall);

	// Add the return instruction
	instructions.addInstruction(CNodeFactory.newReturn(c), InstructionType.Return);

	return instructions;
    }

    /**
     * Creates the nested for loops used in the matrix multiplication algorithm.
     * 
     * @return a {@link CNode} with a block containing the nested loops
     */
    private CNode createNestedForLoop(NewFunctionTypesReturn data, Variable temp) {

	// The induction variables
	// Variable inductionI = new Variable("i", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	// Variable inductionJ = new Variable("j", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	// Variable inductionK = new Variable("k", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable inductionI = new Variable("i", getNumerics().newInt());
	Variable inductionJ = new Variable("j", getNumerics().newInt());
	Variable inductionK = new Variable("k", getNumerics().newInt());
	CNode inductionTokenI = CNodeFactory.newVariable(inductionI);
	CNode inductionTokenJ = CNodeFactory.newVariable(inductionJ);
	CNode inductionTokenK = CNodeFactory.newVariable(inductionK);

	// The array values
	Variable a = data.functionTypes.getInputVar(FIRST_INPUT_NAME);
	Variable b = data.functionTypes.getInputVar(SECOND_INPUT_NAME);

	// The m, n, o values
	CNode mToken = CNodeFactory.newCNumber(data.m);
	CNode nToken = CNodeFactory.newCNumber(data.n);
	CNode oToken = CNodeFactory.newCNumber(data.o);

	// The start value for the main FOR loops
	CNode startValueToken = CNodeFactory.newCNumber(0);

	// The end values for the main FOR loops
	CNode endValueIToken = mToken;
	CNode endValueJToken = nToken;
	CNode endValueKToken = oToken;

	// The stop operator for the main FOR loops
	COperator stopOp = COperator.LessThan;

	// The increment operator for the main FOR loops
	COperator incrementOp = COperator.Addition;

	ForNodes forFactory = new ForNodes(getData());

	// The for tokens
	CNode forIToken = forFactory.newForInstruction(inductionI, startValueToken, stopOp, endValueIToken,
		incrementOp, CNodeFactory.newCNumber(1));
	CNode forJToken = forFactory.newForInstruction(inductionJ, startValueToken, stopOp, endValueJToken,
		incrementOp, CNodeFactory.newCNumber(1));
	CNode forKToken = forFactory.newForInstruction(inductionK, startValueToken, stopOp, endValueKToken,
		incrementOp, CNodeFactory.newCNumber(1));

	// The array accesses
	CNode tempAccess = createArrayAccess(temp, inductionTokenI, inductionTokenJ, mToken);
	CNode aAccess = createArrayAccess(a, inductionTokenI, inductionTokenK, mToken);
	CNode bAccess = createArrayAccess(b, inductionTokenK, inductionTokenJ, oToken);

	// The multiplication
	CNode multiplication = getFunctionCall(COperator.Multiplication, aAccess, bAccess);

	// The addition
	CNode addition = getFunctionCall(COperator.Addition, tempAccess, multiplication);

	// The assignment
	CNode assignment = CNodeFactory.newInstruction(InstructionType.Assignment,
		CNodeFactory.newAssignment(tempAccess, addition));

	// The first FOR block
	CNode firstForBlock = CNodeFactory.newBlock(Arrays.asList(forKToken, assignment));

	// The second FOR block
	CNode secondForBlock = CNodeFactory.newBlock(Arrays.asList(forJToken, firstForBlock));

	// The last FOR block
	CNode lastForBlock = CNodeFactory.newBlock(Arrays.asList(forIToken, secondForBlock));
	return lastForBlock;
    }

    /**
     * Creates and returns a CToken that represents the following array access:
     * 
     * <pre>
     * array[x + y * z]
     * </pre>
     * 
     * @param array
     *            - the array that is being accessed
     * @param x
     *            - the x parameter
     * @param y
     *            - the y parameter
     * @param z
     *            - the z parameter
     * @return - a {@link CNode} with the correct array access
     */
    private CNode createArrayAccess(Variable array, CNode x, CNode y, CNode z) {

	CNode partialSubscript = getFunctionCall(COperator.Multiplication, y, z);
	CNode subscript = getFunctionCall(COperator.Addition, x, partialSubscript);

	// return matrixUtils.newGet(array, subscript);
	return getNodes().matrix().get(array, subscript);
    }

    /**
     * Creates and returns a new instance of {@link MatrixMultiplicationDecInstance}.
     * 
     * @param functionSettings
     *            - the function settings
     * @param inputTypes
     *            - the input types for this implementation
     * @param useUnrolledMatrices
     *            - whether this implementation will use unrolled matrices
     * @return a new instance of {@link MatrixMultiplicationDecInstance}
     */
    public static FunctionInstance newInstance(ProviderData data) {
	return new MatrixMultiplicationDecInstance(data).create();
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> inputTypes = getData().getInputTypes();

	// FunctionSettings functionSettings = builderData.getFunctionSettings();
	// List<VariableType> inputTypes = builderData.getInputTypes();

	NewFunctionTypesReturn data = newFunctionTypes(inputTypes);

	// return new MatrixMultiplicationDecInstance(data.functionTypes, data.m, data.n, data.o,
	// functionSettings);

	String cFunctionName = getFunctionName(data);
	String cFilename = MatlabCFilename.MatrixMath.getCFilename();
	CInstructionList cBody = buildInstructions(data);

	return new InstructionsInstance(data.functionTypes, cFunctionName, cFilename, cBody);
    }

    private static NewFunctionTypesReturn newFunctionTypes(List<VariableType> inputTypes) {

	// The input names
	List<String> inputNames = Arrays.asList(FIRST_INPUT_NAME, SECOND_INPUT_NAME);

	// The output name
	String outputName = OUTPUT_NAME;

	// The output shape
	List<Integer> aShape = MatrixUtils.getShapeDims(inputTypes.get(0));
	List<Integer> bShape = MatrixUtils.getShapeDims(inputTypes.get(1));
	List<Integer> outputShape = Arrays.asList(aShape.get(0), bShape.get(1));

	List<ScalarType> elementTypes = MatrixUtils.getElementTypes(inputTypes);

	// The output inner type
	ScalarType highestPriorityType = ScalarUtils.getMaxRank(elementTypes);

	// The output type
	VariableType outputType = StaticMatrixType.newInstance(highestPriorityType, outputShape);

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	// The matrices dimensions
	int m = aShape.get(0);
	int n = bShape.get(1);
	int o = aShape.get(1);

	return new NewFunctionTypesReturn(functionTypes, m, n, o);
    }

    private static String getFunctionName(NewFunctionTypesReturn data) {

	FunctionType functionTypes = data.functionTypes;

	StringBuilder builder = new StringBuilder();

	builder.append(BASE_FUNCTION_NAME);

	VariableType a = functionTypes.getArgumentsTypes().get(0);
	VariableType b = functionTypes.getArgumentsTypes().get(1);

	// The inner type of matrix A
	builder.append("_");
	builder.append(ScalarUtils.toScalar(a).getSmallId());

	// The shape of matrix A
	builder.append("_");
	builder.append(data.m).append("_").append(data.o);

	// The inner type of matrix B
	builder.append("_");
	builder.append(ScalarUtils.toScalar(b).getSmallId());

	// The shape of matrix B
	builder.append("_");
	builder.append(data.o).append("_").append(data.n);

	return builder.toString();
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosInstruction(FunctionType functionTypes, Variable outputVariable,
	    List<Integer> outputShape) {

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
	// FunctionInstance zerosImplementation = ConstantArrayDecInstance.newInstance(zeroTypes,
	// "zeros", 0);
	InstanceProvider zerosProvider = ConstantArrayDecInstance.newProvider("zeros", 0);
	FunctionInstance zerosImplementation = getInstance(zerosProvider, zeroTypes);
	// ProviderData zerosData = ProviderData.newInstance(pdata, zeroTypes);
	// FunctionInstance zerosImplementation = ConstantArrayDecInstance.newProvider("zeros", 0)
	// .getInstance(zerosData);

	// The output token
	CNode outputToken = CNodeFactory.newVariable(outputVariable);

	// Get the function call to 'zeros'
	CNode zerosCallToken = zerosImplementation.newFunctionCall(Arrays.asList(outputToken));

	return zerosCallToken;
    }

    /**
     * Builds and returns the copy function call.
     * 
     * @param functionSettings
     * @param temp
     * @param c
     * @param length
     *            TODO
     * @param functionTypes
     * @return
     */
    private CNode buildCopyInstruction(Variable temp, Variable c) {

	// The arguments for the function call (source, destination, size)
	List<CNode> cArguments = SpecsFactory.newArrayList();
	cArguments.add(CNodeFactory.newVariable(temp));
	cArguments.add(CNodeFactory.newVariable(c));

	List<VariableType> copyInputs = CNodeUtils.getVariableTypes(cArguments);

	ProviderData copyData = ProviderData.newInstance(getData(), copyInputs);
	FunctionInstance copy = DeclaredProvider.COPY.newCInstance(copyData);

	return CNodeFactory.newFunctionCall(copy, cArguments);
    }

    /**
     * Class used as return for the method newFunctionTypes.
     * 
     * @author Pedro Pinto
     * 
     */
    static class NewFunctionTypesReturn {
	public final FunctionType functionTypes;
	int m;
	int n;
	int o;

	public NewFunctionTypesReturn(FunctionType functionTypes, int m, int n, int o) {
	    this.functionTypes = functionTypes;
	    this.m = m;
	    this.n = n;
	    this.o = o;
	}
    }

}
