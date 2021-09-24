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

package org.specs.CIR.Utilities;

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.suikasoft.MvelPlus.MvelSolver;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 *
 */
public class IndexUtils {

    private final DataStore setup;
    private final CNodeFactory cnodes;

    /**
     * @param data
     */
    public IndexUtils(DataStore setup) {
        this.setup = setup;
        cnodes = new CNodeFactory(setup);
    }

    /**
     * Receives a list of C, zero-based indexes and returns a single, linear C index.
     * 
     * @param decMatrixType
     * @param arguments
     * @return
     */
    public CNode getLinearCIndex(List<Integer> shape, List<CNode> arguments) {

        // If only one index, try to evaluate token
        if (arguments.size() == 1) {
            CNode arg = arguments.get(0);
            return arg;

            // Try to solve expression
            /*Integer result = MvelSolver.evaltoInteger(arg.getCode());
            
            // Return result, if solved, or argument otherwise
            if (result != null) {
                return CNodeFactory.newCNumber(result);
            }
            
            return arg;*/

            // return arguments.get(0);
        }

        // List<Integer> shape = decMatrixType.getMatrixShape().getDims();
        boolean useSolver = true;

        CNode linearSubscript = newSub2Ind(arguments, shape, useSolver);

        return linearSubscript;
    }

    private CNode newSub2Ind(List<CNode> indexes, List<Integer> shape, boolean useSolver) {
        // MemoryLayout memoryLayout = setup.get(CirKeys.MEMORY_LAYOUT);
        MemoryLayout memoryLayout = SpecsEnums.valueOf(MemoryLayout.class,
                setup.get(CirKeys.MEMORY_LAYOUT).getChoice());

        if (memoryLayout == MemoryLayout.ROW_MAJOR) {
            return newSub2IndRowMajor(indexes, shape, useSolver);
        }

        if (memoryLayout == MemoryLayout.COLUMN_MAJOR) {
            return newSub2IndColumnMajor(indexes, shape, useSolver);
        }

        throw new RuntimeException("Case not defined:" + memoryLayout);
    }

    /**
     * Creates an index from the given subscripts, assuming zero-based numbering and COLUMN-major ordering.
     * 
     * <p>
     * Creates a CToken with an expression that transforms the subscripts and the matrix sizes in an index that can be
     * used to access a linear array, row major ordering. The formula used is '[arg1 + (arg2)*dim1 + (arg3)*dim1*dim2 +
     * ...]'
     * 
     * <p>
     * This function is to be applied on declared matrixes, where the shape is completely known (i.e., the number of
     * dimensions and the size of each dimension).
     * 
     * <p>
     * If the sizes of 'subscripts' and 'shape' are not the same, throws an exception.
     * 
     * @param subscripts
     * @param shape
     * @param useSolver
     *            if true, evaluates constant expressions to a single value
     * @return
     */
    // public static CToken newSub2Ind(List<CToken> indexes, List<Integer> shape, boolean useSolver) {
    private CNode newSub2IndColumnMajor(List<CNode> indexes, List<Integer> shape, boolean useSolver) {

        // If subscripts less than shape, add 0 indexes until same size
        int diff = shape.size() - indexes.size();
        if (diff > 0) {
            // Create list to work with
            indexes = SpecsFactory.newArrayList(indexes);

            // Add '0' indexes
            for (int i = 0; i < diff; i++) {
                indexes.add(CNodeFactory.newCNumber(0));
            }
        }

        // Check if subscripts and shape agree
        if (indexes.size() != shape.size()) {
            throw new CodeGenerationException("The size of subscripts (" + indexes + ") and shape ("
                    + shape
                    + ") do not agree.");
        }

        // First argument
        CNode accIndex = indexes.get(0);

        // Using the formula dim1 + dim2*size1 + dim3*size1*size2 +
        // dim4*size1*size2*size3 + ...

        int multiplier = 1;
        for (int i = 1; i < indexes.size(); i++) {
            // Get argument
            CNode arg = indexes.get(i);

            // Get size of previous dimension
            Integer previousSize = shape.get(i - 1);

            // Adjust multiplier
            multiplier *= previousSize;

            // Build multiplication
            CNode accMultFactor = CNodeFactory.newCNumber(multiplier);

            // Because arg could be an expression
            arg = CNodeFactory.newParenthesis(arg);

            // Build function for 'multiplication'
            // CToken multToken = COperator.Multiplication.getFunctionCall(arg, accMultFactor);
            CNode multToken = cnodes.newFunctionCall(COperator.Multiplication, arg, accMultFactor);

            // CToken parenToken = CTokenFactory.newParenthesis(multToken);

            // Add to accumulator
            // accIndex = COperator.Addition.getFunctionCall(accIndex, multToken);
            accIndex = cnodes.newFunctionCall(COperator.Addition, accIndex, multToken);

        }

        // Try to solve expression
        Integer result = null;
        if (useSolver) {
            result = MvelSolver.evaltoInteger(accIndex.getCode());
        }

        // Return result, if solved, or accIndex otherwise
        if (result != null) {
            return CNodeFactory.newCNumber(result);
        }

        return accIndex;

    }

    /**
     * Creates an index from the given subscripts, assuming zero-based numbering and ROW-major ordering.
     * 
     * <p>
     * Creates a CToken with an expression that transforms the subscripts and the matrix sizes in an index that can be
     * used to access a linear array, row major ordering. The formula used is '[argN + (argN-1)*matrix->shape[N] +
     * (argN-2)*matrix->shape[N]*matrix->shape[N-1]+ ...]'
     * 
     * <p>
     * This function is to be applied on declared matrixes, where the shape is completely known (i.e., the number of
     * dimensions and the size of each dimension).
     * 
     * <p>
     * If the sizes of 'subscripts' and 'shape' are not the same, throws an exception.
     * 
     * @param subscripts
     * @param shape
     * @param useSolver
     *            if true, evaluates constant expressions to a single value
     * @return
     */
    // public static CToken newSub2Ind(List<CToken> indexes, List<Integer> shape, boolean useSolver) {
    private CNode newSub2IndRowMajor(List<CNode> indexes, List<Integer> shape, boolean useSolver) {

        // If subscripts less than shape, add 0 indexes until same size
        int diff = shape.size() - indexes.size();
        if (diff > 0) {
            // Create list to work with
            indexes = SpecsFactory.newArrayList(indexes);

            // Add '0' indexes
            for (int i = 0; i < diff; i++) {
                indexes.add(CNodeFactory.newCNumber(0));
            }
        }

        // Check if subscripts and shape agree
        if (indexes.size() != shape.size()) {
            throw new RuntimeException("The size of subscripts (" + indexes.size() + ") and shape (" + shape.size()
                    + ") do not agree.");
        }

        int lastIndex = indexes.size() - 1;

        // First argument
        CNode accIndex = indexes.get(lastIndex);

        // Using the formula dimN + dimN-1*sizeN + dimN-2*sizeN*sizeN-1 +
        // dimN-3*sizeN*sizeN-1*sizeN-2 + ...

        int multiplier = 1;
        for (int i = lastIndex - 1; i >= 0; i--) {
            // Get argument
            CNode arg = indexes.get(i);

            // Get size of previous dimension
            Integer previousSize = shape.get(i + 1);

            // Adjust multiplier
            multiplier *= previousSize;

            // Build multiplication
            CNode accMultFactor = CNodeFactory.newCNumber(multiplier);

            // Because arg could be an expression
            arg = CNodeFactory.newParenthesis(arg);

            // Build function for 'multiplication'
            // CToken multToken = COperator.Multiplication.getFunctionCall(arg, accMultFactor);
            CNode multToken = cnodes.newFunctionCall(COperator.Multiplication, arg, accMultFactor);

            // CToken parenToken = CTokenFactory.newParenthesis(multToken);

            // Add to accumulator
            // accIndex = COperator.Addition.getFunctionCall(accIndex, multToken);
            accIndex = cnodes.newFunctionCall(COperator.Addition, accIndex, multToken);

        }

        // Try to solve expression
        Integer result = null;
        if (useSolver) {
            result = MvelSolver.evaltoInteger(accIndex.getCode());
        }

        // Return result, if solved, or accIndex otherwise
        if (result != null) {
            return CNodeFactory.newCNumber(result);
        }
        return accIndex;

    }

}
