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
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class SumDecScalarInstance extends AInstanceBuilder {

    /**
     * @param data
     */
    public SumDecScalarInstance(ProviderData data) {
	super(data);
    }

    // The names of the inputs and outputs and the function
    private final static String INPUT_NAME = "input_scalar";
    private final static String OUTPUT_NAME_MATRIX = "output_matrix";
    private final static String OUTPUT_NAME_SCALAR = "output_scalar";
    private final static String BASE_FUNCTION_NAME = "sum_dec_scalar";

    /**
     * Creates and returns a new instance of {@link SumDecScalarInstance}. The inputs need to conform to a standart
     * which is explained below.
     * 
     * @param inputTypes
     *            - a list of {@link VariableType} that <b>MUST</b> contain the following:
     *            <ul>
     *            <li><i>input_scalar</i>: a numeric scalar)</li>
     *            <li><i>dim</i>: a constant numeric scalar [optional]</li>
     *            <li><i>class</i>: a string representing the type of the output scalar ('native' or 'double')
     *            [optional, defaults to 'double']</li>
     *            </ul>
     * @param hasDim
     *            - whether the <i>'DIM'</i> parameter was specified ( its value is not necessary )
     * @param useLinearArrays
     *            - whether this implementation uses linear arrays for matrices
     * @return a new instance of {@link SumDecScalarInstance}
     */
    public static InstanceProvider newProvider() {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new SumDecScalarInstance(data).create();
	    }
	};
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> inputTypes = getData().getInputTypes();

	boolean hasDim = hasDim(inputTypes);

	FunctionType functionTypes = buildFunctiontypes(inputTypes, hasDim);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = SumDecScalarInstance.BASE_FUNCTION_NAME + typesSuffix;

	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, hasDim);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
    }

    /**
     * @param inputTypes
     * @return
     */
    private static boolean hasDim(List<VariableType> inputTypes) {
	// Indicates if the DIM parameter was specified
	boolean hasDim = false;
	// Check it
	if (inputTypes.size() == 3) {
	    hasDim = true;
	} else {
	    if (inputTypes.size() == 2) {
		if (!StringTypeUtils.isString(inputTypes.get(1))) {
		    hasDim = true;
		}
	    }
	}
	return hasDim;
    }

    /**
     * Builds the function types for this implementation.
     * 
     * @param useLinearArrays
     *            - whether this implementation uses linear arrays for matrices
     * @param inputTypes
     *            - the input types of the original call
     * 
     * @return an instance of {@link FunctionType}
     */
    private FunctionType buildFunctiontypes(List<VariableType> originalTypes, boolean hasDim) {

	// The input type
	VariableType inputType = originalTypes.get(0);
	List<VariableType> inputTypes = Arrays.asList(inputType);

	// The input name
	List<String> inputNames = Arrays.asList(SumDecScalarInstance.INPUT_NAME);

	// The inner type of the output
	// VariableType outputInnerType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
	VariableType outputInnerType = getNumerics().newDouble();

	// Get the class string if there is one
	String numericTypeString = null;
	if (hasDim) {
	    if (originalTypes.size() == 3) {

		numericTypeString = StringTypeUtils.getString(originalTypes.get(2));
	    }
	} else {
	    if (originalTypes.size() == 2) {

		numericTypeString = StringTypeUtils.getString(originalTypes.get(1));
	    }
	}

	// See if it is native and make the output inner type the same as the input
	if (numericTypeString != null) {
	    if (numericTypeString.equals("native")) {
		outputInnerType = inputType;
	    }
	}

	// The output type is a matrix if DIM was specified or a scalar other wise
	// VariableType outputType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
	VariableType outputType = getNumerics().newDouble();

	// The output name
	String outputName = SumDecScalarInstance.OUTPUT_NAME_SCALAR;

	// If DIM was specified change the output type and name;
	// The function types have an output as input
	if (hasDim) {

	    List<Integer> outputShape = Arrays.asList(1, 1);
	    outputType = StaticMatrixType.newInstance(outputInnerType, outputShape);

	    outputName = SumDecScalarInstance.OUTPUT_NAME_MATRIX;

	    return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
	}

	return FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);
    }

    /**
     * Builds the instructions for this implementation's body.
     * 
     * @return an instance of {@link CInstructionList} with the instructions
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, boolean hasDim) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	if (hasDim) {
	    buildInstructionsForMatrix(instructions);
	} else {
	    buildInstructionsForScalar(instructions);
	}

	return instructions;
    }

    /**
     * Builds the instructions for the case where the output is a scalar. <br>
     * <br>
     * <b>Code:</b>
     * 
     * <pre>
     * {@code
     * output_scalar = input_scalar;
     * return output_scalar;
     * }
     * </pre>
     * 
     * @param instructions
     *            the {@link CInstructionList} with the instructions
     */
    private static void buildInstructionsForScalar(CInstructionList instructions) {

	FunctionType functionTypes = instructions.getFunctionTypes();

	// The output and input variables and their tokens
	Variable inputVar = functionTypes.getInputVar(SumDecScalarInstance.INPUT_NAME);
	Variable outputVar = functionTypes.getReturnVar();

	CNode inputToken = CNodeFactory.newVariable(inputVar);
	CNode outputToken = CNodeFactory.newVariable(outputVar);

	// Create the assignment
	CNode assignment = CNodeFactory.newAssignment(outputToken, inputToken);
	instructions.addAssignment(assignment);

	// Create the return
	instructions.addReturn(outputToken);

    }

    /**
     * Builds the instructions for the case where the output is a matrix. <br>
     * <br>
     * <b>Code:</b>
     * 
     * <pre>
     * {@code
     * zeros(1, 1, output_matrix);
     * output_matrix[0] = input_scalar;
     * return output_matrix;
     * }
     * </pre>
     * 
     * @param instructions
     *            the {@link CInstructionList} with the instructions
     */
    private void buildInstructionsForMatrix(CInstructionList instructions) {

	FunctionType functionTypes = instructions.getFunctionTypes();

	// The output and input variables and their tokens
	Variable inputVar = functionTypes.getInputVar(SumDecScalarInstance.INPUT_NAME);
	Variable outputVar = functionTypes.getInputVar(SumDecScalarInstance.OUTPUT_NAME_MATRIX);

	CNode inputToken = CNodeFactory.newVariable(inputVar);
	CNode outputToken = CNodeFactory.newVariable(outputVar);

	// Build the function call to zeros
	CNode zerosFunctionCall = buildZerosInstruction(functionTypes, outputVar);
	instructions.addInstruction(zerosFunctionCall, InstructionType.FunctionCall);

	// CNode zeroToken = CNodeFactory.newCNumber(0);
	CNode set = getNodes().matrix().set(outputVar, inputToken, "0");
	instructions.addInstruction(set);

	// Create the return
	CNode returnT = CNodeFactory.newReturn(outputToken);
	instructions.addInstruction(returnT, InstructionType.Return);
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosInstruction(FunctionType functionTypes, Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(1)));
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(1)));
	zeroTypes.add(getNumerics().newInt(1));
	zeroTypes.add(getNumerics().newInt(1));
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
