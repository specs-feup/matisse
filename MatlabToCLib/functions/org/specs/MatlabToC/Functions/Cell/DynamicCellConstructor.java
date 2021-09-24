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

package org.specs.MatlabToC.Functions.Cell;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.DynamicCellType;

public class DynamicCellConstructor extends AInstanceBuilder {
    private DynamicCellConstructor(ProviderData data) {
	super(data);
    }

    public static InstanceProvider getProvider() {
	MatisseInputsChecker checker = new MatisseInputsChecker()
		.numOfOutputs(1)
		.areScalar()
		.outputOfType(DynamicCellType.class, 0);

	return new GenericInstanceProvider(checker, data -> new DynamicCellConstructor(data).create());

    }

    @Override
    public FunctionInstance create() {
	DynamicCellType outputType = (DynamicCellType) getData().getOutputType();

	return outputType.cell().functions().createFromDims().newCInstance(getData());
    }
}
