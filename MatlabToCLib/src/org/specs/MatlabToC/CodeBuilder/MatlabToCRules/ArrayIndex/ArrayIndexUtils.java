/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ColonNotationNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.TokenRules;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class ArrayIndexUtils {

    private static final String READ_MATRIX_NAME = "read_matrix";

    private final MatlabToCFunctionData m2cFunctionData;
    private final CNodeFactory cnodes;

    // private final GenericInstanceHelper helper;

    /**
     * @param data
     */
    public ArrayIndexUtils(MatlabToCFunctionData data) {
        // super(data.getProviderData());
        m2cFunctionData = data;
        cnodes = new CNodeFactory(m2cFunctionData.getSettings());
        // this.helper = new GenericInstanceHelper(data.getProviderData());
    }

    /**
     * @return the readMatrixName
     */
    public static String getReadMatrixName() {
        return ArrayIndexUtils.READ_MATRIX_NAME;
    }

    /**
     * @param mIndexes
     * @return
     */
    public List<ArrayIndex> getArrayIndexes(CNode arrayVar, List<MatlabNode> mIndexes) {

        List<ArrayIndex> indexes = SpecsFactory.newArrayList();
        for (int i = 0; i < mIndexes.size(); i++) {
            ArrayIndex index = getArrayIndex(i, arrayVar, mIndexes.get(i), mIndexes.size());

            indexes.add(index);
        }

        return indexes;
    }

    /**
     * @param i
     * @param index
     * @return
     */
    public ArrayIndex getArrayIndex(int position, CNode arrayVar, MatlabNode index, int numIndexes) {

        // Normalize
        index = index.normalize();

        // Check if identifier
        if (index instanceof IdentifierNode) {
            // If identifier, check if matrix
            String idName = ((IdentifierNode) index).getName();
            VariableType idType = m2cFunctionData.getVariableType(idName);
            if (MatrixUtils.isMatrix(idType)) {
                return newMatrixIndex(position, idName, idType);
            }

            return newExpressionIndex(position, index);
        }

        // Check if colon notation
        if (index instanceof ColonNotationNode) {
            boolean isOnlyIndex = false;
            if (numIndexes == 1) {
                isOnlyIndex = true;
            }
            return newColonNotationIndex(position, arrayVar, isOnlyIndex);
        }

        // Check if colon operator
        if (index instanceof OperatorNode) {
            OperatorNode opNode = (OperatorNode) index;
            if (opNode.getOp() == MatlabOperator.Colon) {
                return newColonIndex(position, opNode, arrayVar);
            }
        }

        // Check if there are any colon operators inside
        int colonOps = countColonOperators(index);
        if (colonOps == 0) {
            return newExpressionIndex(position, index);
        }

        // Check how many colon operations are inside
        // List<MatlabToken> operators = TokenUtils.getChildrenRecursively(matlabToken, MTokenType.Operator);
        throw new RuntimeException("Case not defined:" + index.getNodeName());

    }

    /**
     * @param position
     * @param idName
     * @param idType
     * @return
     */
    private ArrayIndex newMatrixIndex(int position, String idName, VariableType idType) {
        CNode matrix = CNodeFactory.newVariable(idName, idType);
        return new MatrixIndex(position, matrix, true, new ForNodes(m2cFunctionData.getProviderData()));
    }

    /**
     * @param index
     * @return
     */
    public static int countColonOperators(MatlabNode index) {
        List<OperatorNode> operators = index.getDescendantsAndSelf(OperatorNode.class);

        int colonOps = 0;
        for (OperatorNode operator : operators) {
            if (operator.getOp() == MatlabOperator.Colon) {
                colonOps++;
            }
        }

        return colonOps;

    }

    /**
     * @param position
     * @param index
     * @param arrayVar
     * @return
     */
    private ArrayIndex newColonIndex(int position, OperatorNode index, CNode arrayVar) {
        // throw new RuntimeException("TEST");

        // Check if only one colon operation
        int numColons = countColonOperators(index);
        if (numColons == 1) {
            List<MatlabNode> operands = index.getOperands();
            Preconditions.checkArgument(operands.size() == 2, "Colon should have 2 operands");

            CNode start = TokenRules.convertTokenExpr(operands.get(0), m2cFunctionData);
            CNode end = TokenRules.convertTokenExpr(operands.get(1), m2cFunctionData);

            // Subtract 1, to adjust from MATLAB index to C index
            CNode startIndex = cnodes.newFunctionCall(COperator.Subtraction, start, CNodeFactory.newCNumber(1));
            CNode endIndex = cnodes.newFunctionCall(COperator.Subtraction, end, CNodeFactory.newCNumber(1));

            return new RangeIndex(position, startIndex, endIndex, new ForNodes(m2cFunctionData.getProviderData()));
        }

        throw new RuntimeException("Not implemented yet when expression has " + numColons + " colon operators");

    }

    /**
     * @param position
     * @param arrayVar
     * @return
     */
    private ArrayIndex newColonNotationIndex(int position, CNode arrayVar, boolean isOnlyIndex) {
        return new DimIndex(position, arrayVar, isOnlyIndex, new ForNodes(m2cFunctionData.getProviderData()));
    }

    /**
     * @param matlabExpression
     * @return
     */
    private ArrayIndex newExpressionIndex(int position, MatlabNode matlabExpression) {
        // Convert the token to C
        CNode expression = TokenRules.convertTokenExpr(matlabExpression, m2cFunctionData);

        // Subtract 1, to adjust from MATLAB index to C index
        CNode index = cnodes.newFunctionCall(COperator.Subtraction, expression, CNodeFactory.newCNumber(1));

        return new ValueIndex(position, index);
    }

    /**
     * @param matrixType
     * @param arrayIndexes
     * @return
     */
    public static VariableType getOutputType(VariableType inputType, List<ArrayIndex> arrayIndexes) {
        // If input type implements package ArrayIndex (interface ArrayIndexType), cast and use function
        // Otherwise, get "equivalent" primitive type
        // System.out.println("ARRAY INDEXES:" + arrayIndexes);
        if (MatrixUtils.isStaticMatrix(inputType)) {
            SpecsLogs.warn("NOT IMPLEMENTED YET FOR DECLARED ARRAYS");
            // return inputType; // HACK
            return null;
        }

        if (MatrixUtils.usesDynamicAllocation(inputType)) {
            VariableType elementType = MatrixUtils.getElementType(inputType);
            // MultipleGet always return a row matrix (one dimension)
            TypeShape shape = TypeShape.newRow();
            return DynamicMatrixType.newInstance(elementType, shape);
        }

        throw new RuntimeException("Case not defined:" + inputType);
    }

    /**
     * @param callIndexes
     * @return
     */
    public static List<ArrayIndex> getFunctionIndexes(List<ArrayIndex> callIndexes) {
        List<ArrayIndex> functionIndexes = SpecsFactory.newArrayList();

        for (ArrayIndex index : callIndexes) {
            functionIndexes.add(index.convertToFunction());
        }

        return functionIndexes;
    }

    public CNode buildFors(List<ArrayIndex> indexes, List<CNode> innerLoopInstructions) {
        return buildFors(indexes, CNodeFactory.newBlock(innerLoopInstructions));
    }

    public CNode buildFors(List<ArrayIndex> indexes, CNode innerLoopInstructions) {

        // MemoryLayout memLayout = m2cFunctionData.getHelper().getMultipleChoice(CirOption.MEMORY_LAYOUT,
        // MemoryLayout.class);
        MemoryLayout memLayout = CirKeys.getMemoryLayout(m2cFunctionData.getSettings());

        // Identifier array has values store in the order of the memory layout. Have to access them respecting that
        // order
        if (memLayout == MemoryLayout.ROW_MAJOR) {
            Collections.reverse(indexes);
        }

        CNode currentInstruction = innerLoopInstructions;
        for (ArrayIndex arrayIndex : indexes) {
            // Get for
            CNode forToken = arrayIndex.getFor(Arrays.asList(currentInstruction));

            // If null, means that the index does not need a for
            if (forToken == null) {
                continue;
            }

            // Update current token
            currentInstruction = forToken;
        }

        return currentInstruction;
    }

    /**
     * @param arrayIndexes
     * @return
     */
    /*
    public static List<CToken> getIndexArguments(List<ArrayIndex> arrayIndexes) {
    List<CToken> arguments = FactoryUtils.newArrayList();
    
    for(ArrayIndex arrayIndex : arrayIndexes) {
        arguments.addAll(arrayIndex.getFunctionInputs());
    }
    
    return arguments;
    }
    */
}
