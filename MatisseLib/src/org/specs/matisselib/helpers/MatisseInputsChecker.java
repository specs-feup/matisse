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

package org.specs.matisselib.helpers;

import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Utilities.InputChecker.AInputsChecker;
import org.specs.CIR.Utilities.InputChecker.Check;
import org.specs.matisselib.types.DynamicCellType;

public class MatisseInputsChecker extends AInputsChecker<MatisseInputsChecker> {

    public MatisseInputsChecker() {
    }

    public MatisseInputsChecker(ProviderData data, List<Check> checks) {
	super(data, checks);
    }

    @Override
    public MatisseInputsChecker create(ProviderData data) {
	return new MatisseInputsChecker(data, this.getChecks());
    }

    public MatisseInputsChecker isDynamicCell(int index) {
	return ofType(DynamicCellType.class, index);
    }
}
