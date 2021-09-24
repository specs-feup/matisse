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

package org.specs.Matisse;

import org.specs.Matisse.Matlab.TypesMap;
import org.specs.Matisse.Matlab.VariableTable;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

/**
 * TODO: Move options to other Key container (maybe MatlabToCFunctionKeys? Rename to MatlabToCKeys? MatisseKeys?)
 * 
 * @author João Bispo
 *
 */
public interface MatisseKeys {

    /**
     * A TypesMap with the variable definitions given to the program.
     */
    DataKey<TypesMap> TYPE_DEFINITION = KeyFactory.object("TypesDefinition", TypesMap.class)
            .setDefault(() -> new TypesMap());

    /**
     * Maps variable names to the corresponding MATLAB function. If they are in the table, it means they have been
     * marked as constant.
     * 
     */
    DataKey<VariableTable> CONSTANT_VARIABLES = KeyFactory.object("constant_variables", VariableTable.class)
            .setDefault(() -> new VariableTable());
}
