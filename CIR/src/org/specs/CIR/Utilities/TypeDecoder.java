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

import java.util.Collection;

import org.specs.CIR.Types.VariableType;

/**
 * Transforms strings representing type names (float, double, int...) into variable types.
 * 
 * @author Joao Bispo
 * 
 */
public interface TypeDecoder {

    /**
     * 
     * @param typeString
     * @return the variable type corresponding to the given string, or null if could not decode
     */
    VariableType decode(String typeString);

    Collection<String> supportedTypes();

}
