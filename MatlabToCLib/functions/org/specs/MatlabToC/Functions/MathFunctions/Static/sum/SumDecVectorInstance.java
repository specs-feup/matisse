package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

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
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;

public class SumDecVectorInstance extends AInstanceBuilder {

    // The base names
    private static final String BASE_FUNCTION_NAME = "sum_dec_vec";
    private static final String OUTPUT_NAME = "sum";
    private static final String INPUT_NAME = "input_vector";

    public SumDecVectorInstance(ProviderData data) {
	super(data);
    }

    /**
     * Creates and returns a new instance of {@link SumDecVectorInstance}. The inputs need to conform to a standart
     * which is explained below.
     * 
     * <p>
     * The FunctionCall receives:<br>
     * - An input matrix, which will be summed according to the instance characteristics;<br>
     * - An output-as-input matrix, which will contain the result. <br>
     * 
     * <p>
     * TODO: Specify the rules, how does the output matrix size changes with the input?
     * 
     * @param inputTypes
     *            - a list of {@link VariableType} that <b>MUST</b> contain the following:
     *            <ul>
     *            <li><i>input_vector</i>: a vector of numerics (dimensions Mx1 or 1xN)</li>
     *            <li><i>class</i>: a string representing the type of the output matrix ('native' or 'double')
     *            [optional, defaults to double]</li>
     *            </ul>
     * @param useLinearArrays
     *            - whether the matrices are represented by linear arrays
     * @return a new instance of {@link SumDecVectorInstance}
     */
    public static FunctionInstance newInstance(ProviderData data) {
	return new SumDecVectorInstance(data).create();
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> inputTypes = getData().getInputTypes();

	MatrixType matrixType = getTypeAtIndex(MatrixType.class, 0);
	// The length of the input vector
	// int vectorLength = MatrixUtils.getVectorLength(inputTypes.get(0));
	int vectorLength = matrixType.getTypeShape().getNumElements();

	FunctionType functionTypes = buildFunctionTypes(inputTypes);

	String cFunctionName = buildFunctionName(functionTypes);

	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, vectorLength);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
	// return new SumDecVectorInstance(functionTypes, vectorLength);
	// return new InlinedInstance(functionTypes, cFunctionName, args -> "test");
    }

    /**
     * Builds the function types for this implementation.
     * 
     * @param inputTypes
     *            - the input types of the original function call
     * @param useLinearArrays
     *            - whether the matrices are represented by linear arrays
     * @return the {@link FunctionType} for this implementation
     */
    private FunctionType buildFunctionTypes(List<VariableType> inputTypes) {

	// The input vector
	VariableType inputVector = inputTypes.get(0);

	// The output is either a Double (default) or it is the same as the vector
	// VariableType outputType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
	VariableType outputType = getNumerics().newDouble();

	// If the class string is passed as an argument
	if (inputTypes.size() == 2) {

	    // See if it is native
	    String numericTypeString = StringTypeUtils.getString(inputTypes.get(1));

	    if (numericTypeString.equals("native")) {
		outputType = ScalarUtils.toScalar(inputVector);
	    }
	}

	String outputName = SumDecVectorInstance.OUTPUT_NAME;

	return FunctionType.newInstance(Arrays.asList(SumDecVectorInstance.INPUT_NAME), Arrays.asList(inputVector),
		outputName, outputType);
    }

    /**
     * Builds the instructions of this implementation and adds them to an instructions list.
     * 
     * @return - an instance of {@link CInstructionList} with the instructions
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, int vectorLength) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The output token, initialize it to 0
	CNode outputToken = CNodeFactory.newVariable(SumDecVectorInstance.OUTPUT_NAME, functionTypes.getCReturnType());
	CNode outputInitToken = CNodeFactory.newAssignment(outputToken, CNodeFactory.newCNumber(0));
	instructions.addAssignment(outputInitToken);

	// The input vectors
	Variable inputVector = functionTypes.getInputVar(SumDecVectorInstance.INPUT_NAME);

	// The induction variable and token
	// Variable inductionVariable = new Variable("i", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable inductionVariable = new Variable("i", getNumerics().newInt());
	// CNode inductionToken = CNodeFactory.newVariable(inductionVariable);

	// The tokens and operators needed for the FOR loop
	CNode startToken = CNodeFactory.newCNumber(0);
	COperator stopOperator = COperator.LessThan;
	CNode endToken = CNodeFactory.newCNumber(vectorLength);
	COperator incrementOperator = COperator.Addition;

	// The FOR loop token
	CNode forToken = new ForNodes(getData()).newForInstruction(inductionVariable, startToken, stopOperator,
		endToken, incrementOperator, CNodeFactory.newCNumber(1));

	// The input vector access token
	// CNode vectorAccessToken = matrixUtils.newGet(inputVector, inductionToken);
	CNode vectorAccessToken = getNodes().matrix().get(inputVector, "i");

	// The sum
	CNode sumToken = getFunctionCall(COperator.Addition, outputToken, vectorAccessToken);

	// The assignment
	CNode assignmentToken = CNodeFactory.newAssignment(outputToken, sumToken);

	// The instruction inside the FOR loop
	// CToken instructionToken = CTokenFactory.newInstruction(InstructionType.Assignment,
	// assignmentToken);

	// The block with the FOR loop
	// CToken blockToken = CTokenFactory.newBlock(Arrays.asList(forToken, instructionToken));
	CNode blockToken = CNodeFactory.newBlock(Arrays.asList(forToken, assignmentToken));
	instructions.addInstruction(blockToken, InstructionType.Block);

	// The return instruction
	CNode returnToken = CNodeFactory.newReturn(outputToken);
	instructions.addInstruction(returnToken, InstructionType.Return);

	return instructions;
    }

    private static String buildFunctionName(FunctionType functionTypes) {
	StringBuilder builder = new StringBuilder();

	builder.append(SumDecVectorInstance.BASE_FUNCTION_NAME);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());

	builder.append(typesSuffix);

	// Appends the type of the return ( same as the sum ) to the end, used to differentiate 'native' calls that use
	// different types
	builder.append("_");
	builder.append(functionTypes.getReturnVar().getType().getSmallId());

	return builder.toString();
    }

}
