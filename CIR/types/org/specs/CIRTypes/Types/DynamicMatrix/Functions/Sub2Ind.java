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

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Input:<br>
 * - A matrix;<br>
 * - A variable number of integers representing the indexes;<br>
 * 
 * Returns a 0-based array index from a list of 0-based input indices. Effectively, it is a way to "flatten" a
 * multi-dimensional array access, though it can also be used for 1D accesses.
 * 
 * @author JoaoBispo
 *
 */
public class Sub2Ind extends AInstanceBuilder {

    public Sub2Ind(ProviderData data) {
        super(data);
    }

    private DynamicMatrixType getMatrixType() {
        List<VariableType> inputs = getData().getInputTypes();

        // Must have at least one argument, of type matrix
        Preconditions.checkArgument(inputs.size() > 0, "Must have at least one input");
        VariableType matrixType = inputs.get(0);

        // First argument must be a matrix
        Preconditions.checkArgument(MatrixUtils.isMatrix(matrixType), "First input must be a matrix");

        // Matrix must be of type DynamicMatrixType
        Preconditions.checkArgument(matrixType instanceof DynamicMatrixType, "Matrix must be of type '"
                + DynamicMatrixType.class + "'");

        return (DynamicMatrixType) matrixType;

    }

    private int getNumArgs() {
        List<VariableType> inputs = getData().getInputTypes();

        // Must have at least 1 argument after the matrix
        Preconditions.checkArgument(inputs.size() > 1, "Must have at least one input after the matrix");

        return inputs.size() - 1;

    }

    /**
     * Input:<br>
     * - A matrix;<br>
     * - A variable number of integers representing the indexes;<br>
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {

        DynamicMatrixType matrixType = getMatrixType();
        ScalarType elementType = matrixType.getElementType();

        int numArgs = getNumArgs();

        // Name of the function
        String functionName = "tensor_sub2ind_inline_" + elementType.getSmallId() + "_" + numArgs;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();

        String arrayName = "array";
        inputNames.add(arrayName);

        String indexesPrefix = "index_";
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexesPrefix, numArgs));

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // matrix
        VariableType inputMatrix = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(inputMatrix);

        // indexes
        for (int i = 0; i < numArgs; i++) {
            inputTypes.add(getNumerics().newInt());
        }

        VariableType returnType = getNumerics().newInt();

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, returnType);

        MemoryLayout memoryLayout = getData().getMemoryLayout();

        InlineCode inlineCode = createInlineCode(memoryLayout);

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    private static InlineCode createInlineCode(MemoryLayout memoryLayout) {
        return (List<CNode> arguments) -> {

            // VariableType intType = getNumerics().newInt();

            // First argument is the matrix
            CNode matrix = arguments.get(0);
            List<CNode> indexes = arguments.subList(1, arguments.size());

            CNode sub2ind = Sub2Ind.newSub2Ind(matrix, indexes, memoryLayout);

            return sub2ind.getCode();
        };
    }

    private static CNode newSub2Ind(CNode matrix, List<CNode> indexes, MemoryLayout memoryLayout) {

        if (memoryLayout == MemoryLayout.ROW_MAJOR) {
            return newSub2IndRowMajor(matrix, indexes);
        }

        if (memoryLayout == MemoryLayout.COLUMN_MAJOR) {
            return newSub2IndColumnMajor(matrix, indexes);
        }

        throw new RuntimeException("Case not implemented:" + memoryLayout);
    }

    /**
     * Creates an index from the given subscripts, assuming zero-based numbering and COLUMN-major ordering.
     * 
     * <p>
     * Creates a CToken with an expression that transforms the subscripts and the given matrix in an index that can be
     * used to access a linear array, row major ordering. The formula used is '[arg1 + (arg2)*matrix->shape[0] +
     * (arg3)*matrix->shape[0]*matrix->shape[1]+ ...]'
     * 
     * <p>
     * This function is to be applied on allocated matrixes, where the shape is defined in the matrix structure.
     * 
     * @param subscripts
     * @param shape
     * @param useSolver
     *            if true, evaluates constant expressions to a single value
     * @return
     */
    private static CNode newSub2IndColumnMajor(CNode matrix, List<CNode> indexes) {

        Preconditions.checkArgument(!indexes.isEmpty(), "Matrix access must have at least one index.");

        // TODO: Replace this part with getSize, when implemented
        String matrixCode = matrix.getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer);
        MatrixType matrixType = (MatrixType) matrix.getVariableType();
        TypeShape shape = matrixType.getTypeShape();

        String currentMultiplier = null;
        List<String> components = new ArrayList<>();

        for (int i = 0; i < indexes.size(); ++i) {
            CNode index = indexes.get(i);
            String code = index.getCode();
            if (!code.equals("0") && !code.equals("0.0")) {
                components.add(buildMultiplyToken(currentMultiplier, index));
            }

            if (shape.getRawNumDims() <= i || shape.getDims().get(i) != 1) {
                String sizeString = matrixCode + "->" + DynamicMatrixStruct.TENSOR_SHAPE + "[" + i + "]";
                currentMultiplier = updateMultiplier(currentMultiplier, sizeString);
            }
        }

        String code;
        if (components.size() == 0) {
            code = "0";
        } else {
            code = String.join(" + ", components);
        }

        return CNodeFactory.newLiteral(code);
    }

    /**
     * Creates an index from the given subscripts, assuming zero-based numbering and ROW-major ordering.
     * 
     * <p>
     * Creates a CToken with an expression that transforms the subscripts and the given matrix in an index that can be
     * used to access a linear array, row major ordering. The formula used is '[argN + (argN-1)*matrix->shape[N] +
     * (argN-2)*matrix->shape[N]*matrix->shape[N-1]+ ...]'
     * 
     * <p>
     * This function is to be applied on allocated matrixes, where the shape is defined in the matrix structure.
     * 
     * @param matrix
     * @param indexes
     * 
     * @return
     */
    public static CNode newSub2IndRowMajor(CNode matrix, List<CNode> indexes) {

        // TODO: Replace this part with getSize, when implemented
        String matrixCode = matrix.getCodeForContent(PrecedenceLevel.MemberAccess);

        int lastIndex = indexes.size() - 1;

        // First argument
        String accIndexString = CNodeFactory.newParenthesis(indexes.get(lastIndex)).getCode();

        // Simplify accIndex, if possible
        // Disabled, it does not work with casts
        // accIndexString = simplify(accIndexString);
        // simplification = SymjaPlusUtils.simplify(simplification, null);

        // Using the formula dimN + dimN-1*sizeN + dimN-2*sizeN*sizeN-1 +
        // dimN-3*sizeN*sizeN-1*sizeN-2 + ...

        String currentMultiplier = null;
        for (int i = lastIndex - 1; i >= 0; i--) {
            // Get argument
            CNode arg = indexes.get(i);

            // Get size of previous dimension
            String previousSizeString = matrixCode + "->" + DynamicMatrixStruct.TENSOR_SHAPE + "[" + (i + 1) + "]";

            // Adjust multiplier, if different than one

            currentMultiplier = updateMultiplier(currentMultiplier, previousSizeString);
            // currentMultiplier += "*" + previousSizeString;

            // Because arg could be an expression
            arg = CNodeFactory.newParenthesis(arg);

            // Build function for 'multiplication', if not null
            String multToken = buildMultiplyToken(currentMultiplier, arg);

            // Add to accumulator
            accIndexString += "+" + multToken;
        }

        return CNodeFactory.newLiteral(accIndexString);
    }

    private static String updateMultiplier(String currentMultiplier, String multiplyValue) {

        // Check if value to multiply is different than 1
        if ("1".equals(multiplyValue)) {
            return currentMultiplier;
        }

        // If current multiplier is null, just return the multiply value
        if (currentMultiplier == null) {
            return multiplyValue;
        }

        // Multiply the value with the current multiplier
        currentMultiplier += "*" + multiplyValue;

        return currentMultiplier;
    }

    private static String buildMultiplyToken(String currentMultiplier, CNode token) {

        // If current multiplier is null, just remove the code for the token
        if (currentMultiplier == null) {
            return token.getCodeForContent(PrecedenceLevel.Addition);
        }

        // Multiply the code with the current multiplier
        return token.getCodeForLeftSideOf(PrecedenceLevel.Multiplication) + " * " + currentMultiplier;
    }

}
