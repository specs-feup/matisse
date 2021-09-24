/**
 * Copyright 2017 SPeCS.
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

package org.specs.CIR.FunctionInstance.Instances;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;

public class GlobalVariableInstance extends FunctionInstance {

    private final String globalName;
    private final VariableType variableType;
    public static final String FILENAME = "globals";

    private final Set<String> customDeclarationIncludes = new HashSet<>();

    public GlobalVariableInstance(String globalName, VariableType variableType) {
        super(null);

        this.globalName = globalName;
        this.variableType = variableType;
    }

    public VariableNode getGlobalNode() {
        return CNodeFactory.newVariable(globalName, variableType, true);
    }

    @Override
    public String getCName() {
        return globalName;
    }

    @Override
    public String getCFilename() {
        return FILENAME;
    }

    @Override
    public Set<String> getDeclarationIncludes() {
        Set<String> includes = new HashSet<>(variableType.code().getIncludes());
        includes.addAll(customDeclarationIncludes);
        return includes;
    }

    @Override
    public Set<FunctionInstance> getDeclarationInstances() {
        return variableType.code().getInstances();
    }

    @Override
    public String getDeclarationCode() {
        return "extern " + variableType.code().getDeclaration(globalName) + ";";
    }

    @Override
    public String getImplementationCode() {
        return variableType.code().getDeclaration(globalName) + ";";
    }

    public void addCustomDeclarationIncludes(String... includes) {
        customDeclarationIncludes.addAll(Arrays.asList(includes));
    }
}
