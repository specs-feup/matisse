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

package org.specs.matisselib;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import pt.up.fe.specs.util.utilities.StringList;

public class MatisseLibOption {
    private MatisseLibOption() {
    }
 
    public static final DataKey<Boolean> ASSUME_ALL_MATRIX_ACCESSES_ARE_IN_RANGE = KeyFactory
            .bool("assume_all_matrix_accesses_are_in_range");
    public static final DataKey<Boolean> ASSUME_ALL_MATRIX_SIZES_MATCH = KeyFactory
            .bool("assume_all_matrix_sizes_match");
    public static final DataKey<Boolean> SUPPRESS_PRINTING = KeyFactory.bool("suppress_printing");

    public static final DataKey<Boolean> DUMP_SSA_INSTRUCTIONS = KeyFactory.bool("print_ssa_instructions");
    public static final DataKey<Boolean> DUMP_OUTPUT_TYPES = KeyFactory.bool("dump_output_types");

    public static final DataKey<StringList> PASSES_TO_LOG = KeyFactory.stringList("passes_to_log");
}
