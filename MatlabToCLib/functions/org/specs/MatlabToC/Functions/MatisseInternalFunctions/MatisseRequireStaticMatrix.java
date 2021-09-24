/**
 * Copyright 2016 SPeCS.
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

package org.specs.MatlabToC.Functions.MatisseInternalFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.matisselib.PassMessage;

public class MatisseRequireStaticMatrix {

    public static InstanceProvider create() {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new InlinedInstance(getType(data), "MATISSE_require_static_matrix$", tokens -> "");
	    }

	    @Override
	    public FunctionType getType(ProviderData data) {
		for (VariableType type : data.getInputTypes()) {
		    if (!(type instanceof StaticMatrixType)) {
			throw data.getReportService().emitError(PassMessage.INTERNAL_ERROR,
				"Expected static matrix, got " + type);
		    }
		}

		return FunctionTypeBuilder.newInline()
			.addInputs(data.getInputTypes())
			.withSideEffects()
			.returningVoid()
			.build();
	    }
	};
    }

}
