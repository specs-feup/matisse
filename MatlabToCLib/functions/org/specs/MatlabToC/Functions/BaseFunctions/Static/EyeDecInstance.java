package org.specs.MatlabToC.Functions.BaseFunctions.Static;

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
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import pt.up.fe.specs.util.SpecsFactory;

public class EyeDecInstance extends AInstanceBuilder {

    // The names used in this implementation
    private final static String BASE_FUNCTION_NAME = "eye_dec";

    private static final String OUTPUT_NAME = "identity";

    private final ProviderData pdata;

    public EyeDecInstance(ProviderData data) {
	super(data);
	this.pdata = data;
    }

    /**
     * Creates a new instance of this implementation. The inputs need to conform to a standart which is explained below.
     * <p>
     * 
     * @param pdata
     *            TODO
     * @param useLinearArrays
     *            - whether the implementation will use linear arrays
     * 
     * @return a new instance of {@link EyeDecInstance}, specialized for the given inputs
     */
    public static FunctionInstance newInstance(ProviderData data) {
	EyeDecInstance instance = new EyeDecInstance(data);
	return instance.create();
    }

    @Override
    public FunctionInstance create() {

	// Get the numeric type of the matrix that will be created
	// VariableType lastArg = CollectionUtils.last(pdata.getInputTypes());

	// VariableType outputType = MatlabToCTypes.getNumericFromString(lastArg, numerics());
	VariableType outputType = MatlabToCTypesUtils.getElementType(pdata);

	// m and n values
	int m = ScalarUtils.getConstant(pdata.getInputTypes().get(0)).intValue();
	int n = ScalarUtils.getConstant(pdata.getInputTypes().get(1)).intValue();

	// Build the function types
	FunctionType functionTypes = newFunctionTypes(pdata.getInputTypes(), m, n, outputType);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = BASE_FUNCTION_NAME + typesSuffix;
	String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, m, n, outputType);

	InstructionsInstance instance = new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);

	instance.setIsConstantSpecialized(true);

	return instance;
    }

    // private FunctionTypes newFunctionTypes(List<VariableType> inputTypes, int m, int n, NumericType numericType) {
    private static FunctionType newFunctionTypes(List<VariableType> inputTypes, int m, int n,
	    VariableType numericType) {

	// The output is a matrix of the provided numeric type and with shape (m
	// x n)
	List<Integer> shape = Arrays.asList(m, n);
	// VariableType outputType =
	// VariableTypeFactory.newDeclaredMatrix(VariableTypeFactoryOld.newNumeric(numericType),
	// shape);
	VariableType outputType = StaticMatrixType.newInstance(numericType, shape);

	String outputname = OUTPUT_NAME;

	return FunctionType.newInstanceWithOutputsAsInputs(null, null, outputname, outputType);
    }

    // private CInstructionList buildInstructions(FunctionTypes functionTypes, int m, int n, NumericType numericType) {
    private CInstructionList buildInstructions(FunctionType functionTypes, int m, int n, VariableType numericType) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(m)));
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(n)));
	zeroTypes.add(getNumerics().newInt(m));
	zeroTypes.add(getNumerics().newInt(n));
	// zeroTypes.add(VariableTypeFactory.newString(MatlabToCTypes.getNumericClass(numericType).getMatlabString()));
	zeroTypes
		.add(VariableTypeFactory.newString(MatlabToCTypesUtils.getNumericClass(numericType).getMatlabString()));

	// Get the implementation of the 'zeros' function
	InstanceProvider zerosProvider = ConstantArrayDecInstance.newProvider("zeros", 0);
	// FunctionInstance zerosFunc = ConstantArrayDecInstance.newInstance(zeroTypes, "zeros", 0);
	FunctionInstance zerosFunc = getInstance(zerosProvider, zeroTypes);

	// The inputs of the function 'zeros'
	CNode outputToken = CNodeFactory.newVariable(functionTypes.getInputVar(OUTPUT_NAME));

	// The function call to 'zeros'
	CNode zerosCallToken = zerosFunc.newFunctionCall(Arrays.asList(outputToken));
	instructions.addInstruction(zerosCallToken, InstructionType.FunctionCall);

	// The index used to set the matrix positions to 1 inside the loop
	VariableType intType = getNumerics().newInt();
	// CNode indexToken = CNodeFactory.newVariable(new Variable("set_index", getNumerics().newInt()));
	String indexName = "set_index";
	CNode indexInitializationToken = CNodeFactory.newInstruction(InstructionType.Assignment,
		CNodeFactory.newAssignment(new Variable(indexName, intType), CNodeFactory.newCNumber(0)));
	instructions.addInstruction(indexInitializationToken, InstructionType.Assignment);

	// The loop that sets the diagonal values to 1
	CNode forBlockToken = buildForBlock(functionTypes, indexName, m, n);
	instructions.addInstruction(forBlockToken, InstructionType.Block);

	// The return instruction
	CNode returnToken = CNodeFactory.newReturn(outputToken);
	instructions.addInstruction(returnToken, InstructionType.Return);

	return instructions;
    }

    /**
     * Generates the code block with the FOR loops that sets the diagonal values of the matrix to 1.
     * 
     * @return
     */
    private CNode buildForBlock(FunctionType functionTypes, String index, int m, int n) {

	// The induction variable and its representing token
	// Variable inductionVariable = new Variable("i", VariableTypeFactoryOld.newNumeric(NumericType.Cint));
	Variable inductionVariable = new Variable("i", getNumerics().newInt());

	// The update value of this index
	CNode updateValueToken = CNodeFactory.newCNumber(m + 1);

	// Start value
	CNode startValueToken = CNodeFactory.newCNumber(0);

	// The stop operator
	COperator stopOperator = COperator.LessThan;

	// The end value ( the smallest between m and n )
	CNode endValueToken = CNodeFactory.newCNumber(m < n ? m : n);

	// The increment operator
	COperator incrementOperator = COperator.Addition;

	// The FOR loop
	CNode forToken = new ForNodes(pdata.newInstance()).newForInstruction(inductionVariable, startValueToken,
		stopOperator, endValueToken, incrementOperator, CNodeFactory.newCNumber(1));

	// The set to one instruction

	Variable outputVar = functionTypes.getInputVar(OUTPUT_NAME);
	CNode oneToken = CNodeFactory.newCNumber(1);
	// CNode setToken = new MatrixUtils(pdata.getSetupData()).newSet(outputVar, indexToken, oneToken);
	CNode setToken = getNodes().matrix().set(outputVar, oneToken, index);

	CNode setInst = CNodeFactory.newInstruction(InstructionType.FunctionCall, setToken);

	CNode indexToken = CNodeFactory.newVariable(index, getNumerics().newInt());
	// The index update instruction
	// CToken additionToken = COperator.Addition.getFunctionCall(indexToken, updateValueToken);
	CNode additionToken = getFunctionCall(COperator.Addition, indexToken, updateValueToken);
	CNode updateAssignmentToken = CNodeFactory.newAssignment(indexToken, additionToken);
	CNode update = CNodeFactory.newInstruction(InstructionType.Assignment, updateAssignmentToken);

	// Create the block and return
	// CToken blockToken = CTokenFactory.newBlock(Arrays.asList(forToken, setToOne, update));
	CNode blockToken = CNodeFactory.newBlock(Arrays.asList(forToken, setInst, update));

	return blockToken;
    }
}
