/**
 * Copyright 2016 SPeCS.
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

package org.specs.MatlabToC.CodeBuilder.SsaToC;

import java.util.Optional;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.CodeBuilder.VariableManager;

public class MockVariableManager extends VariableManager {

    @Override
    public Optional<VariableType> getUnprocessedVariableTypeFromFinalName(String finalName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<VariableType> getVariableTypeFromFinalName(String finalName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String convertSsaToFinalName(String variableName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Variable generateTemporary(String proposedName, VariableType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<VariableType> getGlobalType(String global) {
        return Optional.empty();
    }

    @Override
    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects, boolean inlineOnlyLiterals) {

        return generateVariableNodeForSsaName(ssaName);
    }

    @Override
    public boolean isGlobal(String finalName) {
        return false;
    }
}
