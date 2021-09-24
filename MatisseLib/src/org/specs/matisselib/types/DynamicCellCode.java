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

package org.specs.matisselib.types;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.Views.Code.ACode;

import pt.up.fe.specs.util.SpecsFactory;

public class DynamicCellCode extends ACode {
    private final DynamicCellType type;

    public DynamicCellCode(DynamicCellType type) {
        super(type);

        this.type = type;
    }

    @Override
    public String getSimpleType() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.type.getStructInstance().getCName());
        buffer.append("*");

        return buffer.toString();
    }

    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {
        StringBuilder builder = new StringBuilder();
        builder.append(getDeclaration(variableName));
        builder.append(" = NULL");
        return builder.toString();
    }

    @Override
    public Set<FunctionInstance> getInstances() {
        Set<FunctionInstance> instances = SpecsFactory.newHashSet();

        // Add tensor instance of the type
        instances.add(this.type.getStructInstance());

        return instances;
    }

    @Override
    public Set<String> getIncludes() {
        Set<String> includes = new HashSet<>();

        includes.addAll(super.getIncludes());
        includes.addAll(this.type.getStructInstance().getCallIncludes());
        includes.add(SystemInclude.Stdlib.getIncludeName());

        return includes;
    }
}
