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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Instance returns a new matrix, with the shape indicated by the inputs. No guarantees are made about the contents of
 * the matrix (it might not be initialized). The type of the matrix is determined by the value in
 * ProviderData.getOutputType().
 * 
 * Inputs:<br>
 * - As many integers as the number of dimensions, specifying the size of each dimension. If only one integer is passed,
 * the function creates a row-vector (shape 1xN);
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ChangeShape extends AInstanceBuilder {

    private static final String MATRIX_NAME = "t";
    private static final String INPUT_NAME = "shape";

    public ChangeShape(ProviderData data) {
	super(data);
    }

    public static InstanceProvider getProvider() {

	CirInputsChecker checker = new CirInputsChecker()
		// All inputs should be matrix
		.ofType(MatrixType.class);

	return new GenericInstanceProvider(checker, data -> new ChangeShape(data).create());
    }

    @Override
    public FunctionInstance create() {

	// Build FunctionType
	List<String> inputNames = Arrays.asList(MATRIX_NAME, INPUT_NAME);
	List<VariableType> inputTypes = getData().getInputTypes();
	FunctionType type = FunctionType.newInstance(inputNames, inputTypes, "dummy", VoidType.newInstance());
	String functionName = "change_shape_" + inputTypes.get(0).getSmallId() + "_" + inputTypes.get(1).getSmallId();
	String filename = DynamicMatrixUtils.getFilename();

	Replacer body = new Replacer(DynamicMatrixResource.CHANGE_SHAPE_BODY);

	MatrixType shapeType = getData().getInputType(MatrixType.class, 1);

	// MatrixNodes matrixNodes = new MatrixNodes(getSetup());
	Variable matrixVar = new Variable(INPUT_NAME, shapeType);

	CNode shapeNumel = getNodes().matrix().numel(matrixVar);
	CNode shapeGet = getNodes().matrix().get(matrixVar, "i");

	body.replace("<SHAPE_LENGTH>", shapeNumel.getCode());
	body.replace("<SHAPE_GET>", shapeGet.getCode());

	LiteralInstance instance = new LiteralInstance(type, functionName, filename, body.toString());

	// Complete instance
	instance.setCustomImplementationIncludes(SystemInclude.Stdlib);
	instance.getCustomImplementationInstances().add(shapeNumel, shapeGet);

	return instance;
    }

}
