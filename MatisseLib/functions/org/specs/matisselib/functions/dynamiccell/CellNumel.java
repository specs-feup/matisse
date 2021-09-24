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

package org.specs.matisselib.functions.dynamiccell;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.DynamicCellStruct;
import org.specs.matisselib.types.DynamicCellType;

public class CellNumel extends AInstanceBuilder {

    private CellNumel(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return new GenericInstanceProvider(new MatisseInputsChecker()
                .numOfInputs(1)
                .isDynamicCell(0), data -> new CellNumel(data).create());
    }

    @Override
    public FunctionInstance create() {
        DynamicCellType type = getData().getInputType(DynamicCellType.class, 0);

        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInput(type)
                .returning(getNumerics().newInt())
                .build();

        InlineCode code = tokens -> {
            CNode token = tokens.get(0);

            return token.getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer) + "->"
                    + DynamicCellStruct.CELL_LENGTH;
        };
        InlinedInstance instance = new InlinedInstance(functionType, "$cell_numel$" + type.getSmallId(), code);
        instance.setCallPrecedenceLevel(PrecedenceLevel.MemberAccessThroughPointer);
        return instance;
    }

}
