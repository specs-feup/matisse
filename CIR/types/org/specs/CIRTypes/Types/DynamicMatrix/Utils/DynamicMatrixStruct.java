/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIRTypes.Types.DynamicMatrix.Utils;

import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.StructInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * @author Joao Bispo
 */
public class DynamicMatrixStruct {

    private final static String TENSOR_NAME_PREFIX = "tensor_";
    private final static String TENSOR_STRUCT_RESOURCE = "cirlib/tensor_struct.c";

    public final static String TENSOR_OWNS_DATA = "owns_data";
    public final static String TENSOR_LENGTH = "length";
    public final static String TENSOR_DIMS = "dims";
    public final static String TENSOR_SHAPE = "shape";
    public final static String TENSOR_DATA = "data";

    private static ThreadSafeLazy<String> structResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TENSOR_STRUCT_RESOURCE));

    public static FunctionInstance newInstance(VariableType varType) {

        String structName = TENSOR_NAME_PREFIX + varType.getSmallId();
        String cFilename = "lib/tensor_struct";
        String declarationCode = getDeclarationCode(structName, varType);

        StructInstance struct = new StructInstance(structName, cFilename, declarationCode);

        struct.setDeclarationIncludes(CodeUtils.getIncludes(varType));

        // Add stdlib in use includes because of NULL macro during declaration
        Set<String> customUseIncludes = SpecsFactory.newHashSet();
        customUseIncludes.add(SystemInclude.Stdlib.getIncludeName());
        customUseIncludes.addAll(varType.code().getIncludes());
        struct.setCustomUseIncludes(customUseIncludes);

        return struct;
    }

    private static String getDeclarationCode(String structName, VariableType varType) {

        String text = structResource.getValue();
        // Get template
        Replacer body = new Replacer(text);
        // String body = IoUtils.getResourceString(TENSOR_STRUCT_RESOURCE);

        // Replace fields
        body.replace("<SMALL_ID>", varType.getSmallId());
        body.replace("<DATA_TYPE>", CodeUtils.getType(varType));

        replaceFields(null, body);
        /*
        body.replace("<TENSOR_DATA>", TENSOR_DATA);
        body.replace("<TENSOR_LENGTH>", TENSOR_LENGTH);
        body.replace("<TENSOR_SHAPE>", TENSOR_SHAPE);
        body.replace("<TENSOR_DIMS>", TENSOR_DIMS);
        body.replace("<TENSOR_OWNS_DATA>", TENSOR_OWNS_DATA);
         */

        body.replace("<TENSOR_NAME>", structName);

        return body.toString();
    }

    public static String replaceFields(ProviderData providerData, String code) {
        Replacer replacer = new Replacer(code);

        return replaceFields(providerData, replacer).toString();
    }

    public static Replacer replaceFields(ProviderData providerData, Replacer code) {

        code.replace("<TENSOR_DATA>", TENSOR_DATA);
        code.replace("<TENSOR_LENGTH>", TENSOR_LENGTH);
        code.replace("<TENSOR_SHAPE>", TENSOR_SHAPE);
        code.replace("<TENSOR_DIMS>", TENSOR_DIMS);
        code.replace("<TENSOR_OWNS_DATA>", TENSOR_OWNS_DATA);
        if (providerData != null) {
            code.replaceRegex("<FREE_DATA_FUNCTION>\\[\\[(.*)\\]\\]",
                    providerData.getSettings().get(CirKeys.CUSTOM_FREE_DATA_CODE));
            code.replaceRegex("<CUSTOM_DATA_ALLOCATOR>\\[\\[(.*)\\]\\]",
                    providerData.getSettings().get(CirKeys.CUSTOM_DATA_ALLOCATOR));
        }

        return code;
    }

    public static String getDataCode(String tensorName, VariableType matrixType) {
        return getFieldCode(tensorName, matrixType, TENSOR_DATA);
    }

    /**
     * @param tensorName
     * @param matrixType
     * @return
     */
    public static String getDimsCode(String tensorName, VariableType matrixType) {
        return getFieldCode(tensorName, matrixType, TENSOR_DIMS);
    }

    /**
     * @param tensorName
     * @param matrixType
     * @return
     */
    public static String getShapeCode(String tensorName, VariableType matrixType) {
        /*
        StringBuilder builder = new StringBuilder();
        if (PointerUtils.isPointer(matrixType)) {
        builder.append("(*");
        builder.append(tensorName);
        builder.append(")");
        } else {
        builder.append(tensorName);
        }
        
        builder.append("->");
        builder.append(TENSOR_SHAPE);
        
        return builder.toString();
        */
        return getFieldCode(tensorName, matrixType, TENSOR_SHAPE);
    }

    private static String getFieldCode(String tensorName, VariableType matrixType, String field) {
        StringBuilder builder = new StringBuilder();
        if (ReferenceUtils.isPointer(matrixType)) {
            builder.append("(*");
            builder.append(tensorName);
            builder.append(")");
        } else {
            builder.append(tensorName);
        }

        builder.append("->");
        builder.append(field);

        return builder.toString();
    }

}
