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
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.matisselib.types.DynamicCellStruct;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

public class CreateDynamicCellHelper extends AInstanceBuilder {

    private static final ThreadSafeLazy<String> newCellHelperBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(CellCreationResource.NEW_CELL_HELPER_BODY));

    private final DynamicCellType type;

    private CreateDynamicCellHelper(DynamicCellType type, ProviderData data) {
        super(data);

        this.type = type;
    }

    public static InstanceProvider getProvider(DynamicCellType type) {
        return data -> new CreateDynamicCellHelper(type, data).create();
    }

    @Override
    public FunctionInstance create() {
        String functionName = "new_cell_helper_" + type.getSmallId();

        // Input names
        String shapeName = "shape";
        String dimsName = "dims";
        String outName = "result";

        String body = new Replacer(newCellHelperBodyResource.getValue())
                .replace("<UNDERLYING_DATA_TYPE>", type.getUnderlyingType().code().getReturnType())
                .replace("<CELL_DATA>", DynamicCellStruct.CELL_DATA)
                .replace("<CELL_DIMS>", DynamicCellStruct.CELL_DIMS)
                .replace("<CELL_LENGTH>", DynamicCellStruct.CELL_LENGTH)
                .replace("<CELL_SHAPE>", DynamicCellStruct.CELL_SHAPE)
                .replace("<CELL_STRUCT_NAME>", type.getStructInstance().getCName())
                .toString();

        FunctionType functionType = FunctionTypeBuilder.newSimple()
                .addInput(shapeName, new PointerType(getNumerics().newInt()))
                .addInput(dimsName, getNumerics().newInt())
                .addInput(outName, type)
                .returningVoid()
                .build();

        LiteralInstance instance = new LiteralInstance(functionType, functionName, CellGet.FILE_NAME, body);

        instance.setCustomImplementationIncludes(SystemInclude.Stdlib, SystemInclude.Stdio);

        return instance;
    }

}
