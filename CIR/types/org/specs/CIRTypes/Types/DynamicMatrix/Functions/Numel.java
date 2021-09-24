/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CodeGenerator.MatrixCode;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixTemplate;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class Numel extends AInstanceBuilder {

    /**
     * @param data
     */
    public Numel(ProviderData data) {
        super(data);
    }

    /**
     * 
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix;
     * 
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {
        boolean inline = getSettings().get(CirKeys.INLINE).inline(MatrixFunctionName.NUMEL);

        if (inline) {
            return newInlinedInstance();
        }

        return newLiteralInstance();
    }

    /**
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix;
     * 
     * @return
     */
    private FunctionInstance newLiteralInstance() {

        DynamicMatrixType matrixType = getTypeAtIndex(DynamicMatrixType.class, 0);
        VariableType elementType = matrixType.getElementType();
        // VariableType elementType = getMatrixType();

        // Name of the function
        String functionName = "numel_alloc_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String valueName = "value";

        List<String> inputNames = Arrays.asList(tensorName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = Arrays.asList(tensorType);

        // FunctionTypes
        VariableType returnType = getReturnType(matrixType);
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, valueName, returnType);

        String cBody = SpecsIo.getResource(DynamicMatrixResource.NUMEL_BODY.getResource());

        String tensorLengthTag = "<TENSOR_LENGTH>";
        cBody = cBody.replace(tensorLengthTag, DynamicMatrixStruct.TENSOR_LENGTH);

        LiteralInstance length = new LiteralInstance(fTypes, functionName, DynamicMatrixUtils.getFilename(), cBody);

        // Add comments
        String comments = SpecsIo.getResource(DynamicMatrixResource.NUMEL_COMMENTS.getResource());
        comments = DynamicMatrixTemplate.parseTemplate(comments);
        length.setComments(comments);

        return length;
    }

    private VariableType getReturnType(DynamicMatrixType matrixType) {
        VariableType returnType = matrixType.getShape().isFullyDefined()
                ? getNumerics().newInt(matrixType.getShape().getNumElements()) : getNumerics().newInt();
        return returnType;
    }

    /**
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix;
     * 
     * @param data
     * @return
     */
    private FunctionInstance newInlinedInstance() {

        // Should have only one argument, of type matrix
        DynamicMatrixType matrixType = getTypeAtIndex(DynamicMatrixType.class, 0);
        ScalarType elementType = matrixType.getElementType();

        // Name of the function
        String functionName = "numel_alloc_" + elementType.getSmallId();

        // Input types
        final DynamicMatrixType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = SpecsCollections.asListT(VariableType.class, tensorType);

        // FunctionTypes
        VariableType returnType = getReturnType(matrixType);
        FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputTypes, returnType);

        InlineCode inlineCode = newInlineCode();
        InlinedInstance instance = new InlinedInstance(fTypes, functionName, inlineCode);
        instance.setCallPrecedenceLevel(PrecedenceLevel.MemberAccess);

        return instance;

    }

    private static InlineCode newInlineCode() {
        return new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                if (arguments.size() != 1) {
                    SpecsLogs
                            .msgWarn("Calling this version of 'numel' with a number of arguments different than one. ");
                }

                CNode strut = arguments.get(0);

                return MatrixCode.getStructField(strut, DynamicMatrixStruct.TENSOR_LENGTH, null);

            }
        };
    }

}
