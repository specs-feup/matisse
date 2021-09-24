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

package org.specs.CIR.CodeGenerator;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

/**
 * @author Joao Bispo
 * 
 */
public class MatrixCode {

    /**
     * Builds code for accessing a position of an allocated array.
     * 
     * <p>
     * E.g., <tensorName>->data[<index>]<br>
     * E.g., (*<tensorName>)->data[<index>] (if pointer)
     * 
     * @param allocType
     * @param subscripts
     * @return
     */
    public static String getAllocAccess(CNode alloc, String index) {
        return getStructField(alloc, DynamicMatrixStruct.TENSOR_DATA, index);
    }

    /**
     * Builds code for accessing a field of an allocated array.
     * 
     * <p>
     * (If index != null) <br>
     * E.g., <tensorName>-><field>[<index>]<br>
     * E.g., (*<tensorName>)-><field>[<index>] (if pointer)
     * 
     * <p>
     * (If index == null) <br>
     * E.g., <tensorName>-><field><br>
     * E.g., (*<tensorName>)-><field> (if pointer)
     * 
     * TODO: This should be generalized to structures
     * 
     * @param allocName
     * @param allocType
     * @param field
     * @param index
     * @return
     */
    public static String getStructField(CNode alloc, String field, String index) {

        VariableType allocType = alloc.getVariableType();

        if (!MatrixUtils.usesDynamicAllocation(allocType)) {
            throw new RuntimeException("This method only supports allocated matrix types. Given type: " + allocType);
        }

        // boolean isPointer = ReferenceUtils.isPointer(allocType);

        StringBuilder builder = new StringBuilder();
        builder.append(alloc.getCodeForContent(PrecedenceLevel.MemberAccess));

        // Simplify index
        index = VariableCode.simplifyIndex(index);

        // Access field
        builder.append("->");

        // If pointer, access field with '.'
        /*
        if(isPointer) {
        builder.append(".");
        } else {
        builder.append("->");
        }
        */

        // builder.append(DynamicMatrixStruct.TENSOR_DATA);
        builder.append(field);
        if (index != null) {
            builder.append("[");
            builder.append(index);
            builder.append("]");
        }

        return builder.toString();
    }

    /**
     * Builds code for storing a value in a position of an allocated array.
     * 
     * <p>
     * E.g., <tensorName>->data[<index>] = <value><br>
     * E.g., (*<tensorName>)->data[<index>] = <value> (if pointer)
     * 
     * @param allocType
     * @param subscripts
     * @return
     */
    public static String getAllocStore(CNode alloc, String index, CNode valueToken, ProviderData providerData) {

        if (!MatrixUtils.usesDynamicAllocation(alloc.getVariableType())) {
            throw new RuntimeException("This method only supports allocated matrix types. Given type: "
                    + alloc.getVariableType());
        }

        StringBuilder builder = new StringBuilder();

        builder.append(alloc.getCodeForContent(PrecedenceLevel.MemberAccess));

        // Access field
        builder.append("->");

        // If pointer, access field with '.'
        /*
        if(isPointer) {
        builder.append(".");
        } else {
        builder.append("->");
        }
        */

        // Simplify index
        index = VariableCode.simplifyIndex(index);

        builder.append(DynamicMatrixStruct.TENSOR_DATA);
        builder.append("[");
        builder.append(index);
        builder.append("]");
        String rightHandCode = builder.toString();

        return AssignmentUtils.buildAssignmentNode(
                CNodeFactory.newLiteral(rightHandCode, valueToken.getVariableType(), PrecedenceLevel.ArrayAccess),
                valueToken,
                providerData).getCode();
    }

    /**
     * Creates a function call to a 'get' function for tensor.
     * 
     * @param var
     * @param indexes
     * @return
     */
    /*
    public static String buildArrayAccessAlloc(Variable var, List<CToken> indexes) {
    
    if (indexes.size() != 1) {
        throw new RuntimeException("Method not supported when indexes size (" + indexes.size()
    	    + ") is different than one.");
    }
    
    List<CToken> inputs = Arrays.asList(CTokenFactory.newVariable(var), indexes.get(0));
    List<VariableType> inputTypes = CTokenUtils.getVariableTypes(inputs);
    
    //FunctionInstance getInstance = TensorFunctions.newGetLinear(var.getType());
    FunctionInstance getInstance = TensorProvider.GET_LINEAR.getInstance(inputTypes);
    CToken functionCall = getInstance.newFunctionCall(inputs);
    
    return CodeGeneratorUtils.tokenCode(functionCall);
    }
    */

}
