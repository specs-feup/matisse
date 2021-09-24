/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.DynamicMatrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Create;

import pt.up.fe.specs.util.SpecsFactory;

public class DynamicMatrixCode extends ACode {

    private final DynamicMatrixType matrix;

    public DynamicMatrixCode(DynamicMatrixType matrix) {
        super(matrix);
        this.matrix = matrix;
    }

    /*
        @Override
        public String getType() {
    
    	StringBuilder buffer = new StringBuilder();
    
    	buffer.append(matrix.getStructInstance().getCName());
    	buffer.append("*");
    
    	// If pointer, add a '*'
    	if (matrix.isPointer()) {
    	    buffer.append("*");
    	}
    
    	return buffer.toString();
    
        }
    */
    @Override
    public String getSimpleType() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(matrix.getStructInstance().getCName());
        buffer.append("*");

        return buffer.toString();
    }

    /**
     * Always initializes the dynamic matrix to NULL when declaring it.
     * 
     * <p>
     * Example: tensor_d* temp_m0 = NULL
     */
    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {

        StringBuilder builder = new StringBuilder();

        // Append name
        builder.append(getDeclaration(variableName));

        // Append NULL initialization
        builder.append(" = NULL");

        return builder.toString();
    }

    /**
     * Returns an instance of the tensor structure of this Dynamic Matrix.
     */
    @Override
    public Set<FunctionInstance> getInstances() {
        Set<FunctionInstance> instances = SpecsFactory.newHashSet();

        instances.addAll(super.getInstances());

        // Add tensor instance of the type
        instances.add(matrix.getStructInstance());
        instances.addAll(matrix.getElementType().code().getInstances());

        return instances;
    }

    @Override
    public Set<String> getIncludes() {
        Set<String> includes = new HashSet<>();

        includes.addAll(super.getIncludes());

        // Add includes for the tensor
        includes.addAll(matrix.getStructInstance().getCallIncludes());

        // Include STDLIB for NULL, for tensor declaration
        includes.add(SystemInclude.Stdlib.getIncludeName());

        return includes;
    }

    @Override
    public CInstructionList getSafeDefaultDeclaration(CNode variableNode, ProviderData providerData) {
        CInstructionList instructions = new CInstructionList();

        CNode zeroNode = CNodeFactory.newCNumber(0);
        CNode functionCall = FunctionInstanceUtils.getFunctionCall(data -> new Create(data).create(),
                providerData,
                Arrays.asList(zeroNode, zeroNode),
                Arrays.asList(variableNode));
        instructions.addInstruction(functionCall);

        return instructions;
    }

}
