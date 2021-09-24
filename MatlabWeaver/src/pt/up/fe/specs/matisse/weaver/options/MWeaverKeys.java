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

package pt.up.fe.specs.matisse.weaver.options;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

public interface MWeaverKeys {
    DataKey<LanguageMode> LANGUAGE = KeyFactory.enumeration("language_mode", LanguageMode.class);
    // static DataKey<Boolean> MATLABTYPES = KeyFactory.bool("MATLAB_TYPES").setLabel("Use Matlab Types");
    DataKey<Boolean> CHECK_SYNTAX = KeyFactory.bool("CHECK_SYNTAX").setLabel("Check weaved MATLAB Syntax");
    // static DataKey<Boolean> TOM = KeyFactory.bool("TOM").setLabel("Use TOM (deprecated!)");

    DataKey<Boolean> AUTOMATIC_CODE_GENERATION = KeyFactory.bool("Automatic Code Generation")
            .setLabel("Generates code from AST to output folder")
            .setDefault(() -> true);

    DataKey<Boolean> DISABLE_CODE_PARSING = KeyFactory.bool("Disable Code Parsing")
            .setLabel("Does not parse input code (starts with empty AST)");

}
