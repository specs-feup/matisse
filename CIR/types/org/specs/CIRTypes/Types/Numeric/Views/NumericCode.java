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

package org.specs.CIRTypes.Types.Numeric.Views;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

public class NumericCode extends ACode {

    private final NumericTypeV2 type;

    public NumericCode(NumericTypeV2 type) {
        super(type);
        this.type = type;
    }

    @Override
    public String getSimpleType() {
        return type.getCtype().getDeclaration();
    }

    @Override
    public CInstructionList getSafeDefaultDeclaration(CNode node, ProviderData providerData) {
        CInstructionList instructions = new CInstructionList();

        instructions.addComment("No initialization required for " + node.getCode());

        return instructions;
    }

}
