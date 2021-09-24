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

package org.specs.Matisse.Matlab;

import java.util.List;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.collections.ScopedMap;

/**
 * Keeps track of the types of variables used in the functions.
 * 
 * @author Joao Bispo
 * 
 */
public class TypesMap extends ScopedMap<VariableType> {

    /**
     * Structure with String of names, as a helper when using recursive algorithms, to avoid calling the method twice
     * for the same case when unnecessary.
     * <p>
     * The method which adds an element before a recursive call should immediately remove the element after the call.
     */
    // private Set<String> calledNames;

    /**
     * 
     */
    public TypesMap() {
    }

    /**
     * Builds a new TypesMap with the types of the specified scope, but without preserving the original scope.
     * 
     * <p>
     * For instance, if a scope 'x' is asked, the scopes in the returned TypesMap will start after 'x'.
     * 
     * @param scope
     * @return
     */
    public TypesMap getTypesMap(List<String> scope) {
        ScopedMap<VariableType> symbolMap = getSymbolMap(scope);
        TypesMap newTypesMap = new TypesMap();
        newTypesMap.addSymbols(symbolMap);
        return newTypesMap;
    }

}
