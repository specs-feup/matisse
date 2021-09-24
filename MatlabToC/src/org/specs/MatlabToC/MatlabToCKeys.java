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

import org.suikasoft.jOptions.storedefinition.StoreDefinition;
import org.suikasoft.jOptions.storedefinition.StoreDefinitionBuilder;

public interface MatlabToCKeys {

    /** KEYS **/

    // DataKey<File> OUTPUT_FOLDER = KeyFactory.folder("Output Folder", true);
    // DataKey<Boolean> CLEAR_OUTPUT_FOLDER = KeyFactory.bool("Clear Output Folder");
    // DataKey<Integer> TARGET_LINES_OF_CODE = KeyFactory.integer("Target Lines-of-Code", 1000);
    // DataKey<Integer> MIN_STATEMENTS_PER_FUNCTION = KeyFactory.integer("Min. Statements per user function", 2);
    // DataKey<Integer> MAX_STATEMENTS_PER_FUNCTION = KeyFactory.integer("Max. Statements per user function", 300);
    // DataKey<Double> USER_MATLAB_CALLS_RATIO = KeyFactory.doubl("User/MATLAB called functions ratio", 0.5);

    /** DEFINITION **/

    static StoreDefinition getStoreDefinition() {
        StoreDefinitionBuilder builder = new StoreDefinitionBuilder("matlab_stress_generator");

        // builder.addKey(OUTPUT_FOLDER);
        // builder.addKey(CLEAR_OUTPUT_FOLDER);
        // builder.addKey(TARGET_LINES_OF_CODE);
        // builder.addKey(MIN_STATEMENTS_PER_FUNCTION);
        // builder.addKey(MAX_STATEMENTS_PER_FUNCTION);
        // builder.addKey(USER_MATLAB_CALLS_RATIO);

        return builder.build();
    }
}
