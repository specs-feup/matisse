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

package org.specs.CIR.Types.Views.Code;

import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

/**
 * The C code of a VariableType.
 * 
 * @author Joao Bispo
 * 
 */
public interface Code {

    /**
     * The base type, without pointer information.
     * 
     * @return
     */
    String getSimpleType();

    /**
     * The C code corresponding to the specified type. For instance, suffixes pointer types with "*".
     * 
     * 
     * @return the C declaration name corresponding to this variable.
     * 
     */
    String getType();

    /**
     * The C code corresponding to the type that is returned from a function.
     * 
     * <p>
     * For most cases, this is equal to getType().
     */
    String getReturnType();

    /**
     * Builds the code for declaring a variable, as an input or to be used in the function.
     * 
     * @param variableName
     * @return
     */
    String getDeclaration(String variableName);

    /**
     * Builds the code for declaring a variable to be used in the function, assigning input values to it.
     * 
     * @param variableName
     * @param values
     * @return
     */
    String getDeclarationWithInputs(String variableName, List<String> values);

    default boolean requiresExplicitInitialization() {
        return false;
    }

    default CInstructionList getSafeDefaultDeclaration(CNode node, ProviderData providerData) {
        throw new NotImplementedException("getSafeDefaultDeclaration for " + getType());
    }

    /**
     * Returns function instances that might be needed by the current type.
     * <p>
     * E.g., the dynamic matrix returns a FunctionInstance representing a structure needed by the type.
     * 
     * @param type
     * @return
     */
    Set<FunctionInstance> getInstances();

    /**
     * Returns the includes needed by the current type.
     * 
     * @return
     */
    Set<String> getIncludes();

}
