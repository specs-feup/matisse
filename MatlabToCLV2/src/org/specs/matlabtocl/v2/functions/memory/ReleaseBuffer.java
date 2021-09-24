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

package org.specs.matlabtocl.v2.functions.memory;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.Variable;

public class ReleaseBuffer implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        Variable resource = new Variable("resource", data.getInputTypes().get(0));

        FunctionType functionType = FunctionTypeBuilder
                .newSimple()
                .addInput(resource)
                .returningVoid()
                .build();

        String code = "if (resource != 0) {\n   clReleaseMemObject(resource);\n}";

        LiteralInstance instance = new LiteralInstance(
                functionType,
                "release_cl_resource",
                "cl_utils",
                code);
        instance.setCustomImplementationIncludes("matisse-cl.h");

        return instance;
    }

}
