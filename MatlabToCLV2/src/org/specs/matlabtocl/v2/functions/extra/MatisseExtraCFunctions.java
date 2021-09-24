/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.functions.extra;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

public enum MatisseExtraCFunctions implements MatlabFunctionProviderEnum {
    MATISSE_cl_global_id("MATISSE_cl_global_id") {
        @Override
        public List<InstanceProvider> getProviders() {
            return Arrays.asList(new SequentialMatisseCLGlobalId());
        }
    };
    private final String functionName;

    private MatisseExtraCFunctions(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public String getName() {
        return this.functionName;
    }
}
