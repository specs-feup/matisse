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

package org.specs.matlabtocl.v2.functions.builtins;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;

public class CastOperator {

    public static FunctionInstance build(VariableType input, VariableType output) {
        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInput(input)
                .returning(output)
                .build();
        InlineCode code = tokens -> "(" + output.code().getSimpleType() + ")"
                + tokens.get(0).getCodeForContent(PrecedenceLevel.Cast);
        return new InlinedInstance(functionType, "cast$" + input.getSmallId() + output.getSmallId(), code);
    }

}
