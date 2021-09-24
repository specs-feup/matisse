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

package org.specs.MatlabToC.Functions.Builtin;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Utilities.ConstantInstance;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 *
 */
public class BuiltinScalarBuilders {

    /**
     * @return
     */
    public static InstanceProvider newScalarBuilder() {
        MatisseChecker checker = new MatisseChecker().numOfInputs(1).areScalar();

        InstanceProvider provider = data -> ConstantInstance.newInstanceInternal(data.getInputTypes(),
                data.getNumerics().newInt(), 1);

        return new GenericInstanceProvider(checker, provider);
    }

}
