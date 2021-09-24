package org.specs.MatlabToC.Functions.BaseFunctions.Static;

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
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Implementation of function 'col' for declared matrices.
 * 
 * @author Pedro Pinto
 * 
 */
public class ColDecNumericInstance extends AInstanceBuilder {

    /**
     * @param data
     */
    public ColDecNumericInstance(ProviderData data) {
	super(data);

    }

    private static final String BASE_NAME = "new_col";

    /**
     * Creates a new instance of function "new_col", which creates a column-array from the input values of its function
     * call.
     * 
     * @param numericType
     *            the numericType of the inputs of the function
     * @param numElements
     *            the number of inputs values in the function call
     * @param useLinearArrays
     * @return
     */
    public static InstanceProvider newProvider() {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new ColDecNumericInstance(data).create();
	    }
	};
    }

    @Override
    public FunctionInstance create() {

	// Get input types
	List<VariableType> argumentTypes = getData().getInputTypes();

	// Return type is a Matrix with of the numeric type with higher
	// priority,
	// VariableType numericType = VariableTypeUtilsG.getMaximalFit(argumentTypes);
	ScalarType numericType;
	if (argumentTypes.isEmpty()) {
	    numericType = getNumerics().newDouble();
	} else {
	    numericType = ScalarUtils.getMaxRank(ScalarUtils.cast(argumentTypes));
	}
	int numElements = argumentTypes.size();

	// The output matrix will be passed as input
	FunctionType functionTypes = buildFunctionTypes(numericType, numElements);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = ColDecNumericInstance.BASE_NAME + typesSuffix;

	String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
	CInstructionList cBody = buildBodyInstructions(functionTypes);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
    }

    private static FunctionType buildFunctionTypes(VariableType numericType, int numElements) {

	// Return type is a Matrix with of the numeric type with higher
	// priority,
	// VariableType type = VariableTypeUtils.getNumericHighestPriority(argumentTypes);

	// Dimensions are #narguments rows x 1 column
	List<Integer> shape = SpecsFactory.newArrayList();
	shape.add(numElements);
	shape.add(1);

	// Input types are in the same order as argumentTypes and equal to the
	// numeric type with the highest priority
	List<VariableType> inputTypes = SpecsFactory.newArrayList();
	List<String> inputNames = SpecsFactory.newArrayList();
	for (int i = 0; i < numElements; i++) {
	    inputTypes.add(numericType);
	    inputNames.add("arg" + i);
	}

	// The return variable
	String returnName = "output_array";
	VariableType returnType = StaticMatrixType.newInstance(numericType, shape);

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, returnName, returnType);
    }

    private CInstructionList buildBodyInstructions(FunctionType functionTypes) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// Build array variable
	String arrayname = functionTypes.getOutputAsInputNames().get(0);
	VariableType arrayType = functionTypes.getOutputAsInputTypesNormalized().get(0);
	Variable arrayVar = new Variable(arrayname, arrayType);

	// Build assignment instructions
	for (int index = 0; index < functionTypes.getArgumentsNames().size(); index++) {

	    // Build right hand side variable token
	    String varName = functionTypes.getArgumentsNames().get(index);
	    VariableType varType = functionTypes.getArgumentsTypes().get(index);
	    CNode rhsVar = CNodeFactory.newVariable(varName, varType);

	    // Array access index
	    // CNode arrayIndex = CNodeFactory.newCNumber(index);

	    // Build array access
	    // CNode arrayAccess = matrixUtils.newGet(arrayVar, arrayIndex);
	    CNode arrayAccess = getNodes().matrix().get(arrayVar, Integer.toString(index));

	    // Build assignment
	    CNode assignment = CNodeFactory.newAssignment(arrayAccess, rhsVar);

	    // Add instruction
	    instructions.addInstruction(assignment, InstructionType.Assignment);
	}

	// Add return
	CNode returnVar = CNodeFactory.newVariable(arrayVar);
	instructions.addInstruction(CNodeFactory.newReturn(returnVar), InstructionType.Return);

	return instructions;
    }

}
