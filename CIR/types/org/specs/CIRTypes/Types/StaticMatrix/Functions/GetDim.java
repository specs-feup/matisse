/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.StaticMatrix.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

public class GetDim extends AInstanceBuilder {

    public GetDim(ProviderData data) {
	super(data);
    }

    public static InstanceProvider getProvider() {
	CirInputsChecker checker = new CirInputsChecker()
		.numOfInputs(2)
		.ofType(StaticMatrixType.class, 0)
		.isScalar(1);

	return new GenericInstanceProvider(checker, data -> new GetDim(data).create());
    }

    /**
     * Creates a new instance of the function 'dimSize', which returns the size of the indicated dimension.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - The index of the dimension;
     * 
     * @param matrixType
     * @param useLinearArrays
     * @return
     */
    @Override
    public FunctionInstance create() {

	List<VariableType> inputTypes = getData().getInputTypes();

	VariableType matrixType = inputTypes.get(0);
	Number constant = ScalarUtils.getConstant(inputTypes.get(1));
	if (constant == null) {
	    return getFullInstance();
	}

	return getInlineInstance(matrixType, constant);
    }

    private FunctionInstance getFullInstance() {
	StaticMatrixType matrixType = getData().getInputType(StaticMatrixType.class, 0);
	TypeShape shape = matrixType.getShape();
	String functionName = "get_dim_" + matrixType.getSmallId();

	FunctionType functionType = FunctionTypeBuilder.newSimple()
		.addInput("in", matrixType)
		.addInput("dim", getData().getInputType(ScalarType.class, 1))
		.returning(getNumerics().newInt())
		.build();

	StringBuilder bodyBuilder = new StringBuilder();
	bodyBuilder.append("switch (dim) {\n");

	for (int i = 0; i < shape.getRawNumDims(); ++i) {
	    bodyBuilder.append("case " + (i + 1) + ":\n");
	    bodyBuilder.append("\treturn ");
	    bodyBuilder.append(shape.getDim(i));
	    bodyBuilder.append(";\n");
	}

	bodyBuilder.append("default:\tabort();\n");

	bodyBuilder.append("}\n");

	String body = bodyBuilder.toString();
	LiteralInstance instance = new LiteralInstance(functionType, functionName, CirFilename.DECLARED.getFilename(),
		body);
	instance.addInstance(new StdlibFunctions(getNumerics()).abort().getCheckedInstance(getData().create()));

	return instance;
    }

    private FunctionInstance getInlineInstance(VariableType matrixType, Number constant) {
	// Name of the function
	String functionName = "dim_size_dec";

	FunctionType functionTypes = FunctionType.newInstanceNotImplementable(
		Arrays.asList(matrixType, getNumerics().newInt()), getNumerics().newInt());

	InlineCode inlineCode = new InlineCode() {

	    @Override
	    public String getInlineCode(List<CNode> arguments) {
		// First argument is a variable with the matrix
		CNode varToken = arguments.get(0);
		assert varToken instanceof VariableNode;
		Variable var = ((VariableNode) varToken).getVariable();

		List<Integer> shape = MatrixUtils.getShapeDims(var.getType());

		int dim = constant.intValue();

		if (shape.size() <= dim) {
		    return "1";
		}

		return shape.get(dim).toString();
	    }
	};

	return new InlinedInstance(functionTypes, functionName, inlineCode);
    }
}
