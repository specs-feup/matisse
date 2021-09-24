/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.StdInd.Views;

import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.CIRTypes.Types.StdInd.StdIntTypeUtils;

// public class NumericCode implements TypeCodeWorker {
public class StdIntCode extends ACode {

    private final StdIntType type;

    public StdIntCode(StdIntType type) {
        super(type);
        this.type = type;
    }

    @Override
    public String getSimpleType() {
        return StdIntTypeUtils.getSimpleType(type);
    }

    @Override
    public Set<String> getIncludes() {
        Set<String> includes = new HashSet<>();

        includes.addAll(super.getIncludes());

        includes.add(SystemInclude.IntTypes.getIncludeName());

        return includes;
    }

    @Override
    public CInstructionList getSafeDefaultDeclaration(CNode node, ProviderData providerData) {
        CInstructionList instructions = new CInstructionList();

        instructions.addComment("No initialization required for " + node.getCode());

        return instructions;
    }
}
