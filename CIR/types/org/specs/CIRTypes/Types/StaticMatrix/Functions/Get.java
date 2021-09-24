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

package org.specs.CIRTypes.Types.StaticMatrix.Functions;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.IndexUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Creates an inlined version of the function 'get', which returns the contents of a declared matrix in the specified
 * index. The index can be represented with a variable number of integers.
 * 
 * <p>
 * FunctionCall receives:<br>
 * - A static matrix, whose values will be read;<br>
 * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single index.
 * Assumes zero-based indexing;<br>
 * 
 * @author Joao Bispo
 *
 */
public class Get extends AInstanceBuilder {

    /**
     * @param data
     */
    public Get(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        // First argument is the matrix to access
        StaticMatrixType matrixType = getTypeAtIndex(StaticMatrixType.class, 0);

        // Number of dimensions is size of inputs minus 1
        int numDims = getData().getInputTypes().size() - 1;
        Preconditions.checkArgument(numDims > 0, "Must have at least one index as input, found '" + numDims + "'");

        return newGetInline(matrixType, numDims);
    }

    private FunctionInstance newGetInline(StaticMatrixType matrixType, int numArgs) {

        // Name of the function
        ScalarType elementType = matrixType.getElementType();
        String functionName = "declared_get_" + elementType.getSmallId() + "_" + numArgs;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();

        String arrayName = "array";
        inputNames.add(arrayName);

        String indexesPrefix = "index_";
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexesPrefix, numArgs));

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // matrix
        inputTypes.add(matrixType);

        // indexes
        for (int i = 0; i < numArgs; i++) {
            inputTypes.add(getNumerics().newInt());
        }

        // If matrix type has values, put value in output type
        ScalarType outputType = getOutputType(elementType, matrixType);

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, outputType);

        InlineCode inlineCode = getInlineCode();

        return new InlinedInstance(functionTypes, functionName, inlineCode);

    }

    private ScalarType getOutputType(ScalarType elementType, StaticMatrixType matrixType) {
        if (!getData().isPropagateConstants()) {
            return elementType;
        }

        if (!matrixType.matrix().getShape().hasValues()) {
            return elementType;
        }

        List<VariableType> inputTypes = getData().getInputTypes();

        // Only supporting linear access
        if (inputTypes.size() != 2) {
            SpecsLogs.warn("Not supporting constant propagation when get has more than one input");
            return elementType;
        }

        // Get literal value, if present
        if (!ScalarUtils.hasScalarType(inputTypes.get(1))) {
            return elementType;
        }

        ScalarType inputType = ScalarUtils.toScalar(inputTypes.get(1));
        if (!inputType.scalar().hasConstant()) {
            return elementType;
        }

        Number value = matrixType.matrix().getShape().getValues().get(inputType.scalar().getConstant().intValue());

        return elementType.scalar().setConstant(value);
    }

    private InlineCode getInlineCode() {
        return (List<CNode> arguments) -> {
            StringBuilder builder = new StringBuilder();

            // First argument is a variable with the matrix
            CNode varToken = arguments.get(0);
            if (!(varToken instanceof VariableNode)) {
                throw new RuntimeException("Not supported: " + varToken.getCode());
            }

            Variable var = ((VariableNode) varToken).getVariable();
            builder.append(var.getName());

            // Remaining arguments are the indexes
            List<CNode> indexes = arguments.subList(1, arguments.size());

            MatrixType matrixType = (MatrixType) var.getType();
            List<Integer> shape = matrixType.matrix().getShape().getDims();

            CNode linearSubscript;
            linearSubscript = new IndexUtils(getSettings()).getLinearCIndex(shape, indexes);
            builder.append("[");
            builder.append(linearSubscript.getCode());
            builder.append("]");

            return builder.toString();
        };
    }
}
