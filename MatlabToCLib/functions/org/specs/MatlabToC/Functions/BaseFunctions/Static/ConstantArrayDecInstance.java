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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * Creates a declared matrix initialized to a single value.
 * 
 * @author Joao Bispo
 * 
 */
public class ConstantArrayDecInstance extends AInstanceBuilder {

    private final ProviderData pdata;

    private final String functionBaseName;
    private final int setValue;

    public ConstantArrayDecInstance(ProviderData data, String functionBaseName, int setValue) {
	super(data);
	this.pdata = data;

	this.functionBaseName = functionBaseName;
	this.setValue = setValue;
    }

    /**
     * Creates a new FunctionImplementation for a function that creates declared matrixes initialized to a single value.
     * 
     * <p>
     * Rules for 'inputTypes': <br>
     * - All elements of 'inputTypes' but the last must be NumericTypes with a constant value, from which an integer is
     * extracted;<br>
     * - The last type must be a constant String with a value as defined in 'NumericClassName';<br>
     * 
     * <p>
     * The FunctionCall receives:<br>
     * - Variable with type representing a declared matrix of NumericTypes with the size and shape of the return type of
     * this function.
     * 
     * <p>
     * TODO: make the last 'String' argument optional, use 'double' as default.
     * 
     * @param function
     * @param inputTypes
     * @param useLinearArrays
     * @return
     */
    /*
    public static FunctionInstance newInstance(ProviderData data, String functionBaseName,
        int setValue) {

    return new ConstantArrayDecInstance(data, functionBaseName, setValue).newInstance();
    }
     */
    /*
        public static InstanceProvider newProvider(final String functionBaseName, final int setValue) {
    	return new InstanceProvider() {

    	    @Override
    	    public FunctionInstance getInstance(ProviderData data) {
    		return new ConstantArrayDecInstance(data, data.getInputTypes(), functionBaseName,
    			setValue).newInstance();
    	    }
    	};
        }*/

    public static InstanceProvider newProvider(final String functionBaseName, final int setValue) {

	return data -> new ConstantArrayDecInstance(data, functionBaseName, setValue).create();
    }

    @Override
    public FunctionInstance create() {

	// List<VariableType> inputTypes = pdata.getInputTypes();

	// Build FunctionTypes
	FunctionType fTypes = buildFunctionTypes();

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(fTypes.getCInputTypes());
	String cFunctionName = this.functionBaseName + typesSuffix;

	String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();

	CInstructionList cBody = buildInstructions(fTypes, this.setValue);

	InstructionsInstance constantArrayInstance = new InstructionsInstance(fTypes, cFunctionName, cFilename, cBody);

	constantArrayInstance.setIsConstantSpecialized(true);

	return constantArrayInstance;
    }

    /**
     * Function Call inputs: a list of constant numeric types.
     * 
     * <p>
     * - All elements of 'inputTypes' but the last must be NumericTypes with a constant value, from which an integer is
     * extracted;<br>
     * - The last type must be a constant String as defined in 'NumericClassName';<br>
     * 
     * @param argumentTypes
     * @return
     */
    private FunctionType buildFunctionTypes() {

	// Get element type
	VariableType type = MatlabToCTypesUtils.getElementType(this.pdata);

	// Get scalar types
	// List<ScalarType> scalarTypes = getData().getInputTypes(ScalarType.class);
	List<VariableType> inputTypes = new ArrayList<>(this.pdata.getInputTypes());

	if (StringTypeUtils.isString(SpecsCollections.last(this.pdata.getInputTypes()))) {
	    // Remove last type
	    inputTypes.remove(inputTypes.size() - 1);
	}

	List<ScalarType> scalarTypes = inputTypes
		.stream()
		.map(ScalarType.class::cast)
		.collect(Collectors.toList());

	// If only one type, duplicate it to make a square matrix shape
	if (scalarTypes.size() == 1) {
	    scalarTypes.add(scalarTypes.get(0));
	}

	TypeShape shape = getShape(scalarTypes);

	// Input types are in the same number as argumentTypes and are integers
	String returnName = "zero_temp_var";
	VariableType returnType = StaticMatrixTypeBuilder
		.fromElementTypeAndShape((ScalarType) type, shape)
		.build();

	FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(
		Collections.emptyList(),
		Collections.emptyList(),
		returnName,
		returnType);

	return fTypes;
    }

    /**
     * @param argumentTypes
     * @return
     */
    /*
    private static List<Integer> getShapePrivate(List<ScalarType> argumentTypes) {
    List<Integer> shape = FactoryUtils.newArrayList();

    // If only one integer argument N, represents the case where a NxN matrix is created
    // if (argumentTypes.size() - 1 == 1) {
    if (argumentTypes.size() == 1) {
        ScalarType constantType = argumentTypes.get(0);
        int constant = constantType.scalar().getConstant().intValue();

        shape.add(constant);
        shape.add(constant);
        return shape;
    }

    // Dimension is arg1 x arg2 x arg3 x ...
    // for (int i = 0; i < argumentTypes.size() - 1; i++) {
    for (int i = 0; i < argumentTypes.size(); i++) {
        ScalarType constantType = argumentTypes.get(i);

        int constant = constantType.scalar().getConstant().intValue();
        shape.add(constant);
    }

    return shape;
    }
     */

    /**
     * @return
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, int setValue) {

	// FunctionTypes functionTypes = getFunctionTypes();

	// Instruction list
	CInstructionList instructions = new CInstructionList(functionTypes);

	// Array variable
	String arrayName = functionTypes.getOutputAsInputNames().get(0);
	VariableType arrayType = functionTypes.getOutputAsInputTypesNormalized().get(0);
	Variable arrayVar = new Variable(arrayName, arrayType);

	// Build For
	List<CNode> forInstructions = buildFor(arrayVar, functionTypes.getCReturnType(), setValue);

	// Build block for For
	CNode forBlock = CNodeFactory.newBlock(forInstructions);

	instructions.addInstruction(forBlock, InstructionType.Block);

	// Add return
	CNode returnVariable = CNodeFactory.newVariable(arrayVar);
	CNode returnInst = CNodeFactory.newReturn(returnVariable);
	instructions.addInstruction(returnInst, InstructionType.Return);

	return instructions;
    }

    /**
     * @param arrayVar
     * @param setValue
     * @return
     */
    private List<CNode> buildFor(Variable arrayVar, VariableType declaredMatrix, int setValue) {
	List<CNode> forInstructions = SpecsFactory.newArrayList();

	CNode startValue = CNodeFactory.newCNumber(0);

	// Get matrix shape
	List<Integer> matrixShape = MatrixUtils.getShapeDims(declaredMatrix);

	// Get array size
	Integer multAcc = matrixShape.get(0);
	for (int i = 1; i < matrixShape.size(); i++) {
	    multAcc *= matrixShape.get(i);
	}

	CNode endValue = CNodeFactory.newCNumber(multAcc);

	CNode increment = CNodeFactory.newCNumber(1);

	// Get induction variable type
	String inductionVarName = "i";
	VariableType inductionVarType = getNumerics().newInt();
	Variable inductionVar = new Variable(inductionVarName, inductionVarType);

	// Get addition
	// FunctionPrototype addition = COperator.Addition.getPrototype();

	CNode forToken = new ForNodes(this.pdata).newForInstruction(inductionVar, startValue, COperator.LessThan,
		endValue, COperator.Addition, increment);

	forInstructions.add(forToken);

	// Build set
	CNode expression = CNodeFactory.newCNumber(setValue);
	// List<CNode> indexes = Arrays.asList(CNodeFactory.newVariable(inductionVar));
	// CNode arraySet = new MatrixUtils(pdata.getSetupData()).newSet(arrayVar, indexes, expression);
	CNode arraySet = getNodes().matrix().set(arrayVar, expression, inductionVarName);

	forInstructions.add(CNodeFactory.newInstruction(InstructionType.FunctionCall, arraySet));

	return forInstructions;
    }
}
