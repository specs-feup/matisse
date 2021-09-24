package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRFunctions.Utilities.TransformationUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class SumDecHigherDimInstance extends AInstanceBuilder {

    /**
     * @param data
     */
    public SumDecHigherDimInstance(ProviderData data) {
	super(data);
    }

    // The names of the inputs and outputs and the function
    private final static String INPUT_NAME = "input_matrix";
    private final static String OUTPUT_NAME = "output_matrix";
    private final static String BASE_FUNCTION_NAME = "sum_dec_higher_dim";

    public static FunctionInstance newInstance(ProviderData data) {
	return new SumDecHigherDimInstance(data).create();
    }

    @Override
    public FunctionInstance create() {

	// FIX [ SumDecHigherDimInstance.newInstance() ] : javadoc this

	List<VariableType> inputTypes = getData().getInputTypes();

	// Get the shape of the input
	List<Integer> inputShape = MatrixUtils.getShapeDims(inputTypes.get(0));

	// Build the function types and return a new instance
	FunctionType functionTypes = buildFunctionTypes(inputTypes, inputShape);

	// return new SumDecHigherDimInstance(functionTypes, inputShape, settings);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = BASE_FUNCTION_NAME + typesSuffix;

	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, inputShape);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);

    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     *            - the original inputs
     * @param inputShape
     *            - the shape of the input matrix
     * @param useLinearArrays
     *            - whether this instance uses linear arrays to represent matrices
     * 
     * @return an instance of {@link FunctionType}
     */
    private FunctionType buildFunctionTypes(List<VariableType> originalTypes, List<Integer> inputShape) {

	VariableType firstInput = originalTypes.get(0);

	// The input type
	List<VariableType> inputTypes = Arrays.asList(firstInput);

	// The input name
	List<String> inputNames = Arrays.asList(INPUT_NAME);

	// The output type is a matrix of either Double or the same as the input matrix and the same shape as the input
	// matrix
	// VariableType outputInnerType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
	VariableType outputInnerType = getNumerics().newDouble();

	// If the class string is passed as an argument ( the last one )
	if (originalTypes.size() == 3) {

	    // See if it is 'native' and change the output inner type
	    String classString = StringTypeUtils.getString(originalTypes.get(2));

	    if (classString.equals("native")) {
		outputInnerType = ScalarUtils.toScalar(firstInput);
	    }
	}

	// The output type
	VariableType outputType = StaticMatrixType.newInstance(outputInnerType, inputShape);

	// The output name
	String outputName = OUTPUT_NAME;

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
    }

    /**
     * Builds the instructions for this instance.
     * 
     * @return an instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, List<Integer> inputShape) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The input and output arrays and their tokens
	Variable inputArray = functionTypes.getInputVar(INPUT_NAME);
	Variable outputArray = functionTypes.getInputVar(OUTPUT_NAME);
	CNode inputVariable = CNodeFactory.newVariable(inputArray);
	CNode outputVariable = CNodeFactory.newVariable(outputArray);

	// Build the function call to zeros and add it to the instructions ( output = zeros(output_shape); )
	CNode zerosFunctionCall = buildZerosCall(functionTypes, inputShape, outputArray);
	instructions.addInstruction(zerosFunctionCall, InstructionType.FunctionCall);

	// Copy the contents of the input array to the output array
	CNode copyFunctionCall = buildCopyCall(inputVariable, outputVariable);
	instructions.addInstruction(copyFunctionCall, InstructionType.FunctionCall);

	// Build and add the return instruction
	CNode returnToken = CNodeFactory.newReturn(inputArray);
	instructions.addInstruction(returnToken, InstructionType.Return);

	return instructions;
    }

    /**
     * Builds and returns the copy function call.
     * 
     * @param inputVariable
     *            - the input array variable
     * @param outputVariable
     *            - the output array variable
     * @return a {@link CNode} with the function call
     */
    private CNode buildCopyCall(CNode inputVariable, CNode outputVariable) {

	CNode copyCall = new TransformationUtils(getData()).parseMatrixCopy(inputVariable, outputVariable);

	return copyCall;
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosCall(FunctionType functionTypes, List<Integer> inputShape, Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	for (Integer dim : inputShape) {
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

}
