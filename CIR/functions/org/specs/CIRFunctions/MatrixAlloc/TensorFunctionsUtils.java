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

package org.specs.CIRFunctions.MatrixAlloc;

import java.util.List;

import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.CirFunctionsUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

/**
 * Utility methods for TensorFunctions class.
 * 
 * @author Joao Bispo
 * 
 */
public class TensorFunctionsUtils {

    private final static String VAR_NAME_DIM_PREFIX = "dim_";

    // private final static String VAR_NAME_INDEX_PREFIX = "index_";
    // private final static String C_FILENAME = "lib/tensor";

    /**
     * Returns the filename for functions related to allocated matrixes (currently 'lib/tensor').
     * 
     * @return
     */
    public static String getFilename() {
	return CirFilename.ALLOCATED.getFilename();
	// return C_FILENAME;
    }

    /**
     * Returns the .H file for functions related to allocated matrixes (currently 'lib/tensor.h').
     * 
     * @return
     */
    /*
    public static String getInclude() {
    return C_FILENAME + ".h";
    }
    */

    /**
     * Returns a string with the dimension variables.
     * 
     * <p>
     * E.g., if 'numDims' is 3, returns the string "dim_1, dim_2, dim_3".
     * 
     * @param numDims
     * @return
     */
    public static String getDimNames(int numDims) {
	return CirFunctionsUtils.getNameString(VAR_NAME_DIM_PREFIX, numDims);
	/*
	StringBuilder builder = new StringBuilder();
	
	if (numDims == 0) {
	    return "";
	}
	
	builder.append(getInputName(0));
	for (int i = 1; i < numDims; i++) {
	    builder.append(", ");
	    builder.append(getInputName(i));
	}
	
	return builder.toString();
	*/
    }

    /*
    static String getIndexesNames(int numIndexes) {
    return getNameString(numIndexes, VAR_NAME_INDEX_PREFIX);
    }
    */

    /**
     * Returns a string with the input name, based on the index.
     * 
     * <p>
     * E.g., if 'index' is 0, returns the string 'dim_1'.
     * 
     * @param index
     * @return
     */
    public static String getInputName(int index) {
	return getInputName(VAR_NAME_DIM_PREFIX, index);
    }

    public static String getInputName(String prefix, int index) {
	return prefix + (index + 1);
    }

    /**
     * Generates code that transforms the given subscripts into a single index. Assumes zero-based indexing.
     * 
     * @param subscripts
     * @return
     */
    /*
    static String getIndexCode(List<CToken> subscripts) {
    // If only one index, return code of the subscript itself
    if(subscripts.size() == 1) {
        return CodeGeneratorUtils.tokenCode(subscripts.get(0));
    }
    
    // TODO Auto-generated method stub
    
    return null;
    }
    */

    public static CNode newSub2Ind(CNode matrix, List<CNode> indexes, MemoryLayout memoryLayout) {
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
    public static CNode newSub2IndColumnMajor(CNode matrix, List<CNode> indexes) {

	// TODO: Replace this part with getSize, when implemented
	String matrixCode = matrix.getCodeForContent(PrecedenceLevel.MemberAccess);

	// First argument
	String accIndexString = CNodeFactory.newParenthesis(indexes.get(0)).getCode();
	// Simplify accIndex, if possible
	// Disabled, it does not work with casts
	// accIndexString = simplify(accIndexString);

	// simplification = SymjaPlusUtils.simplify(simplification, null);

	// Using the formula dim1 + dim2*size1 + dim3*size1*size2 +
	// dim4*size1*size2*size3 + ...

	// String currentMultiplier = "1";
	String currentMultiplier = null;
	for (int i = 1; i < indexes.size(); i++) {
	    // Get argument
	    CNode arg = indexes.get(i);

	    // Get size of previous dimension
	    String previousSizeString = matrixCode + "->" + DynamicMatrixStruct.TENSOR_SHAPE + "[" + (i - 1) + "]";

	    // Adjust multiplier, if different than one

	    currentMultiplier = updateMultiplier(currentMultiplier, previousSizeString);
	    // currentMultiplier += "*" + previousSizeString;

	    // Because arg could be an expression
	    arg = CNodeFactory.newParenthesis(arg);

	    // Build function for 'multiplication', if not null
	    String multToken = buildMultiplyToken(currentMultiplier, arg);
	    // String multToken = CodeGeneratorUtils.tokenCode(arg) + "*" + currentMultiplier;

	    // Add to accumulator
	    accIndexString += "+" + multToken;
	}

	return CNodeFactory.newLiteral(accIndexString);
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

	// String currentMultiplier = "1";
	String currentMultiplier = null;
	// for (int i = 1; i < indexes.size(); i++) {
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
	    // String multToken = CodeGeneratorUtils.tokenCode(arg) + "*" + currentMultiplier;

	    // Add to accumulator
	    accIndexString += "+" + multToken;
	}

	return CNodeFactory.newLiteral(accIndexString);
    }

    /**
     * TODO: Does not work when there are casts present
     * 
     * @param accIndexString
     * @return
     */
    /*
    private static String simplify(String accIndexString) {
    // If '->' inside, do not simplify, it already has an array access
    if (accIndexString.contains("->")) {
        return accIndexString;
    }
    // If only contains alphanumeric and '_', return
    boolean variableName = true;
    for (int i = 0; i < accIndexString.length(); i++) {
        char c = accIndexString.charAt(i);
        if (!Character.isLetterOrDigit(c) && c != '_' && c != '(' && c != ')') {
    	variableName = false;
    	break;
        }
    }

    if (variableName) {
        return accIndexString;
    }

    String simplification = SymjaPlusUtils.simplify(accIndexString, null);
    if (simplification == null) {
        return accIndexString;
    }

    // simplification = beautifyExpression(simplification);

    return simplification;

    }
    */

    /**
     * Currently, library to simplify expressions put the numbers first and variables later (e.g., -2+i).
     * 
     * <p>
     * Change the String for this simple case so that it inverts the order (e.g., i-2). Onyl works when the expression
     * starts with '-'
     * 
     * @param simplification
     * @return
     */
    /*
    private static String beautifyExpression(String simplification) {
    // If first character is not a '-', do nothing
    if (simplification.charAt(0) != '-') {
        return simplification;
    }

    List<String> positiveMembers = FactoryUtils.newArrayList();
    List<String> negativeMembers = FactoryUtils.newArrayList();

    StringBuilder currentVar = new StringBuilder();
    Boolean isNegative = null;
    for (int i = 0; i < simplification.length(); i++) {
        char currentChar = simplification.charAt(i);

        // If signal, empty current buffer and set flag
        if (currentChar == '+' || currentChar == '-') {
    	// Add member, empty buffer
    	if (isNegative != null) {
    	    if (isNegative) {
    		negativeMembers.add(currentVar.toString());
    	    } else {
    		positiveMembers.add(currentVar.toString());
    	    }
    	    currentVar = new StringBuilder();
    	}

    	// Set flag
    	if (currentChar == '-') {
    	    isNegative = true;
    	} else {
    	    isNegative = false;
    	}

    	continue;
        }

        // Otherwise, add character to buffer
        currentVar.append(currentChar);
    }

    // Add last member
    if (isNegative) {
        negativeMembers.add(currentVar.toString());
    } else {
        positiveMembers.add(currentVar.toString());
    }

    // Build expression
    StringBuilder expression = new StringBuilder();
    if (!positiveMembers.isEmpty()) {
        expression.append(positiveMembers.get(0));
    }

    for (int i = 1; i < positiveMembers.size(); i++) {
        expression.append("+").append(positiveMembers.get(i));
    }

    for (int i = 0; i < negativeMembers.size(); i++) {
        expression.append("-").append(negativeMembers.get(i));
    }

    return expression.toString();
    }
    */

    private static String buildMultiplyToken(String currentMultiplier, CNode token) {

	String tokeCode = token.getCode();

	// If current multiplier is null, just remove the code for the token
	if (currentMultiplier == null) {
	    return tokeCode;
	}

	// Multiply the code with the current multiplier
	return tokeCode + "*" + currentMultiplier;
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
}
