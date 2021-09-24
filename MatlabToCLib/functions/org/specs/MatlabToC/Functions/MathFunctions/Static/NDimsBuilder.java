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

package org.specs.MatlabToC.Functions.MathFunctions.Static;

import java.util.Arrays;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.Misc.MatlabCheckers;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;

/**
 * Creates a NDims function for static matrices. In MATLAB minimum number of dimensions is 2.
 * 
 * @author JoaoBispo
 *
 */
public class NDimsBuilder extends AInstanceBuilder {

    private final static Checker CHECKER = MatlabCheckers.oneStaticMatrix();

    public static MatlabInstanceProvider getProvider() {
	return new MatlabInstanceProviderHelper(CHECKER, data -> new NDimsBuilder(data).create());
    }

    public NDimsBuilder(ProviderData data) {
	super(data);
    }

    @Override
    protected Checker getCheckerPrivate() {
	return CHECKER;
    }

    @Override
    public FunctionInstance create() {
	MatrixType declaredMatrix = getData().getInputType(MatrixType.class, 0);

	int numDims = MatlabToCTypesUtils.getMatlabNumDims(declaredMatrix);

	String functionName = "ndims_dec_" + numDims;

	FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(declaredMatrix),
		getNumerics().newInt());

	InlineCode inlineCode = args -> Integer.toString(numDims);

	return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    /*
    private static int getNumDims(MatrixType declaredMatrix) {
    // Use the size of the dimension list as the number of dimensions, so that a vector returns 2.
    int numDims = declaredMatrix.getMatrixShape().getNumDims();

    // Adjust number of dimensions
    if (numDims < 2) {
        numDims = 2;
    }
    return numDims;
    }
    */

}
