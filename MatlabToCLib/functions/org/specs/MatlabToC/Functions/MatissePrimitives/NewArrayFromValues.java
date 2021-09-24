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

package org.specs.MatlabToC.Functions.MatissePrimitives;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;

public class NewArrayFromValues extends AInstanceBuilder {

    private static final Checker CHECKER = new MatisseChecker()
	    .numOfInputsAtLeast(3)
	    .isMatrix(0)
	    .isScalar(1)
	    .range(2).areInteger();

    public NewArrayFromValues(ProviderData data) {
	super(data);
    }

    /*
        @Override
        protected Checker getCheckerPrivate() {
    	return CHECKER;
        }
    */
    @Override
    public FunctionInstance create() {
	// Get matrix type and dims
	MatrixType matrixType = getData().getInputType(MatrixType.class, 0);
	ScalarType elementType = getData().getInputType(ScalarType.class, 1);

	List<VariableType> dims = SpecsCollections.subList(getData().getInputTypes(), 2);
	TypeShape matrixShape = getMatrixShape(ScalarUtils.cast(dims));

	// Clean elementType
	elementType = elementType.scalar().setConstant(null);

	// Prepare matrix type
	MatrixType outputType = matrixType.copy();
	outputType = outputType.matrix().setElementType(elementType);
	outputType = outputType.matrix().setShape(matrixShape);

	// Set input types, 'create' function only uses 'dims
	ProviderData newData = getData().createWithContext(dims);

	// Set output type
	newData.setOutputType(outputType);

	return outputType.matrix().functions().create().newCInstance(newData);

    }

    public static InstanceProvider getProvider() {
	return new MatlabInstanceProviderHelper(CHECKER, data -> new NewArrayFromValues(data).create(), getFilter());
	// return new GenericInstanceProvider(CHECKER, data -> new NewArrayFromValues(data).create());
	// return new data -> new NewArrayFromValues(data).create();
    }

    private static InputsFilter getFilter() {
	return (data, args) -> {
	    // Remove first and second argument, only needs dimensions.
	    return args.subList(2, args.size());
	};
    }
}
