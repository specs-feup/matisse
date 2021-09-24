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

package org.specs.MatlabToC;

import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules.SpecialStatementRule;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Keys related with MATLAB-to-C conversion of functions.
 * 
 * @author João Bispo
 *
 */
public interface MatlabToCFunctionKeys {

    /**
     * TODO: Replace type with a generic EnumList<E extends Enum<E>> and use GenericKey
     */
    DataKey<StringList> SPECIAL_STATEMENT_RULES = KeyFactory.stringList("SPECIAL_STATEMENT_RULES",
            SpecsEnums.buildListToString(SpecialStatementRule.class));

    /**
     * A TypesMap with all variable definitions used during the program
     */
    DataKey<TypesMap> FINAL_TYPE_DEFINITION = KeyFactory.object("FinalTypesDefinition", TypesMap.class)
            .setDefault(() -> new TypesMap());

}
