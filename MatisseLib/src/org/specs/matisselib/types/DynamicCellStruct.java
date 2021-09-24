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

import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.Instances.StructInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.CodeUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

public class DynamicCellStruct {

    private static final String CELL_NAME_PREFIX = "dynamic_cell_";
    private final static String CELL_STRUCT_RESOURCE = "matisselib/dynamic_cell_struct.c";

    public static final String FILE_NAME = "lib/dynamic_cell_struct";
    public final static String CELL_LENGTH = "length";
    public final static String CELL_DIMS = "dims";
    public final static String CELL_SHAPE = "shape";
    public final static String CELL_DATA = "data";

    private static ThreadSafeLazy<String> structResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(DynamicCellStruct.CELL_STRUCT_RESOURCE));

    public static FunctionInstance newInstance(VariableType elementType) {
        String structName = DynamicCellStruct.CELL_NAME_PREFIX + elementType.getSmallId();
        String declarationCode = getDeclarationCode(structName, elementType);

        StructInstance instance = new StructInstance(structName, DynamicCellStruct.FILE_NAME, declarationCode);
        instance.setDeclarationIncludes(CodeUtils.getIncludes(elementType));

        // Add stdlib in use includes because of NULL macro during declaration
        Set<String> customUseIncludes = SpecsFactory.newHashSet();
        customUseIncludes.add(SystemInclude.Stdlib.getIncludeName());
        instance.setCustomUseIncludes(customUseIncludes);
        return instance;
    }

    private static String getDeclarationCode(String structName, VariableType elementType) {
        String text = DynamicCellStruct.structResource.getValue();
        // Get template
        Replacer body = new Replacer(text);

        // Replace fields
        body.replace("<SMALL_ID>", elementType.getSmallId());
        body.replace("<DATA_TYPE>", CodeUtils.getReturnType(elementType));

        replaceFields(body);

        body.replace("<CELL_NAME>", structName);

        return body.toString();
    }

    private static Replacer replaceFields(Replacer code) {

        code.replace("<CELL_DATA>", DynamicCellStruct.CELL_DATA);
        code.replace("<CELL_LENGTH>", DynamicCellStruct.CELL_LENGTH);
        code.replace("<CELL_SHAPE>", DynamicCellStruct.CELL_SHAPE);
        code.replace("<CELL_DIMS>", DynamicCellStruct.CELL_DIMS);

        return code;
    }

    public static String getShapeCode(String name, DynamicCellType type) {
        return name + "->" + DynamicCellStruct.CELL_SHAPE;
    }

}
