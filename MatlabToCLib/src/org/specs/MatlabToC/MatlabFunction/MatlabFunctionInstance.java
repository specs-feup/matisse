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

package org.specs.MatlabToC.MatlabFunction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.Variable;
import org.specs.MatlabToC.Utilities.InputsFilter;

public class MatlabFunctionInstance extends FunctionInstance {

    private final FunctionInstance instance;
    private final InputsFilter filter;
    private final ProviderData data;

    public static class Builder {

        private final FunctionInstance newInstance;
        private InputsFilter newFilter;
        private ProviderData newData;

        public Builder(FunctionInstance instance) {
            newInstance = instance;
            newFilter = InputsFilter.DEFAULT_FILTER;
            newData = null;
        }

        public Builder filter(InputsFilter filter) {
            newFilter = filter;
            return this;
        }

        public Builder providerData(ProviderData data) {
            newData = data;
            return this;
        }

        public MatlabFunctionInstance create() {
            return new MatlabFunctionInstance(newInstance, newFilter, newData);
        }
    }

    private MatlabFunctionInstance(FunctionInstance instance, InputsFilter filter, ProviderData data) {
        super(instance.getFunctionType());
        this.instance = instance;
        this.filter = filter;
        this.data = data;
    }

    @Override
    public String getCName() {
        return instance.getCName();
    }

    @Override
    public String getCFilename() {
        return instance.getCFilename();
    }

    /**
     * Filters input arguments using an InputsFilter before creating the function call.
     */
    @Override
    public FunctionCallNode newFunctionCall(List<CNode> cArguments) {
        // Set ProviderData
        ProviderData newData = data;
        if (newData == null) {
            // newData = ProviderData.newInstance(new StubSetup());
            newData = ProviderData.newInstance("StubSetup");
        }

        cArguments = filter.filterInputArguments(newData, cArguments);

        return instance.newFunctionCall(cArguments);
    }

    @Override
    public String getCallCode(List<CNode> cArguments) throws CodeGenerationException {
        return instance.getCallCode(cArguments);
    }

    @Override
    public Set<String> getCallIncludes() {
        return instance.getCallIncludes();
    }

    @Override
    public Set<FunctionInstance> getCallInstances() {
        return instance.getCallInstances();
    }

    @Override
    public Collection<Variable> getCallVars() {
        return instance.getCallVars();
    }

    @Override
    public List<String> getComments() {
        return instance.getComments();
    }

    @Override
    public String getDeclarationCode() {
        return instance.getDeclarationCode();
    }

    @Override
    public Set<String> getDeclarationIncludes() {
        return instance.getDeclarationIncludes();
    }

    @Override
    public Set<FunctionInstance> getDeclarationInstances() {
        return instance.getDeclarationInstances();
    }

    @Override
    public FunctionType getFunctionType() {
        return instance.getFunctionType();
    }

    @Override
    public String getHFilename() {
        return instance.getHFilename();
    }

    @Override
    public String getImplementationCode() {
        return instance.getImplementationCode();
    }

    @Override
    public Set<String> getImplementationIncludes() {
        return instance.getImplementationIncludes();
    }

    @Override
    public Set<FunctionInstance> getImplementationInstances() {
        return instance.getImplementationInstances();
    }

    @Override
    public boolean hasDeclaration() {
        return instance.hasDeclaration();
    }

    @Override
    public boolean hasImplementation() {
        return instance.hasImplementation();
    }

    @Override
    public boolean isConstantSpecialized() {
        return instance.isConstantSpecialized();
    }

    @Override
    public boolean isInlined() {
        return instance.isInlined();
    }

    @Override
    public boolean maintainLiteralTypes() {
        return instance.maintainLiteralTypes();
    }

    @Override
    public FunctionCallNode newFunctionCall(CNode... cArguments) {
        return instance.newFunctionCall(cArguments);
    }

    @Override
    public String toString() {
        return instance.toString();
    }

}
