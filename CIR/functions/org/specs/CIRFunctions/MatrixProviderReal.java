/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIRFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.TypesOld.MatrixUtils.MatrixImplementation;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Joao Bispo
 * 
 */
public class MatrixProviderReal implements InstanceProvider {

    private final MatrixFunction function;
    private final MatrixImplementation impl;

    public MatrixProviderReal(MatrixFunction function) {
	this(function, null);
    }

    public MatrixProviderReal(MatrixFunction function, MatrixImplementation impl) {
	this.function = function;
	this.impl = impl;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.InstanceProvider#getInstance(org.specs.CIR.Functions.ProviderData)
     */
    @Override
    public FunctionInstance newCInstance(ProviderData data) {

	MatrixImplementation implementation = impl;
	if (implementation == null) {
	    implementation = getImplementation(data);
	}

	if (implementation == MatrixImplementation.DECLARED) {
	    InstanceProvider provider = function.getDeclaredProvider();

	    return provider.newCInstance(data);
	}

	if (implementation == MatrixImplementation.ALLOCATED) {
	    InstanceProvider provider = function.getTensorProvider();

	    return provider.newCInstance(data);
	}

	throw new RuntimeException("Matrix implementation '" + impl + "' not supported.");

    }

    private static MatrixImplementation getImplementation(ProviderData data) {
	MatrixType matrixType = SpecsCollections.getFirst(data.getInputTypes(), MatrixType.class);
	// VariableType matrixType = MatrixUtils.getFirstMatrix(data.getInputTypes());
	if (matrixType == null) {
	    throw new RuntimeException("Could not find a matrix type in " + data.getInputTypes());
	}

	MatrixImplementation impl = MatrixUtils.getImplementation(matrixType);
	return impl;
    }

}
