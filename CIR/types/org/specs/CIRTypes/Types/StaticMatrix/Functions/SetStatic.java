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
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.CIR.Utilities.IndexUtils;
import org.specs.CIRFunctions.CirFunctionsUtils;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Creates a new instance of the function 'set', which sets a single value in a static matrix.
 * 
 * <p>
 * FunctionCall receives:<br>
 * - A matrix, whose values will be set;<br>
 * - A variable number of integers with the indexes of the matrix that will be set. Function assumes zero-based
 * indexing;<br>
 * - A scalar, whose value will be set in the matrix at the specified index;<br>
 * 
 * @author Joao Bispo
 *
 */
public class SetStatic extends AInstanceBuilder {

    public SetStatic(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {

        // Number of dimensions is size of inputs minus 2
        int numDims = getData().getInputTypes().size() - 2;

        // return DeclaredFunctions.newSetInline(matrixType, numDims);

        // return new DeclaredFunctions(data).newSetInline();
        // InstanceProvider provider = data -> newSetInlineInstance(numDims);
        // return data -> newSetInlineInstance(numDims));
        return newProvider(numDims).newCInstance(getData());
    }

    private InstanceProvider newProvider(final int numIndexes) {
        return data -> newSetInlineInstance(numIndexes);
    }

    private FunctionInstance newSetInlineInstance(int numIndexes) {

        // First argument is the matrix to access
        VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex("SetInline", getData().getInputTypes(), 0);

        VariableType elementType = MatrixUtils.getElementType(matrixType);

        // Name of the function
        String functionName = "dec_set_inline_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String indexPrefix = "index_";
        String valueName = "value";

        // List<String> inputNames = Arrays.asList(tensorName, indexName, valueName);
        List<String> inputNames = SpecsFactory.newArrayList();
        inputNames.add(tensorName);
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexPrefix, numIndexes));
        inputNames.add(valueName);

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // Matrix
        inputTypes.add(matrixType);

        // Indexes
        for (int i = 0; i < numIndexes; i++) {
            inputTypes.add(getNumerics().newInt());
        }

        // value
        inputTypes.add(elementType);

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                StringBuilder builder = new StringBuilder();

                // First argument is a variable with the matrix
                CNode varToken = arguments.get(0);
                assert varToken instanceof VariableNode;
                Variable var = ((VariableNode) varToken).getVariable();

                // Second argument to before last are indexes
                List<CNode> indexes = arguments.subList(1, arguments.size() - 1);

                // CToken linearIndex = new DeclaredFunctionsUtils(getData()).getLinearCIndex(var.getType(), indexes);
                List<Integer> shape = MatrixUtils.getShape(var.getType()).getDims();
                CNode linearIndex = new IndexUtils(getSettings()).getLinearCIndex(shape, indexes);

                // Last argument is the value
                CNode value = arguments.get(arguments.size() - 1);

                builder.append(var.getName());
                builder.append("[");
                builder.append(linearIndex.getCode());
                builder.append("]");
                String leftHandCode = builder.toString();

                return AssignmentUtils.buildAssignmentNode(
                        CNodeFactory.newLiteral(leftHandCode, elementType, PrecedenceLevel.ArrayAccess),
                        value,
                        getData()).getCode();
            }
        };

        InlinedInstance inlinedInstance = new InlinedInstance(functionTypes, functionName, inlineCode);
        inlinedInstance.setCallInstances(
                AssignmentUtils.getAssignmentInstances(elementType, elementType, getData()));
        return inlinedInstance;

    }
}
