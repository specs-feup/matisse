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

package org.specs.CIR;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Utilities.AvoidableFunctionsData;
import org.specs.CIR.Utilities.Inlining.InliningData;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Options.MultipleChoice;

import pt.up.fe.specs.util.SpecsEnums;

public interface CirKeys {

    /**
     * Indicates which functions should be inlined.
     * 
     */
    DataKey<InliningData> INLINE = KeyFactory.object("inline", InliningData.class)
            .setDefault(() -> InliningData.newInstanceWithChecks(true, CirKeys.SUPPORTED_INLINES));

    /**
     * Indicates if dynamic memory allocation (e.g., with malloc) is allowed.
     * 
     * <p>
     * As default, returns 'true'.
     * 
     */
    DataKey<Boolean> ALLOW_DYNAMIC_ALLOCATION = KeyFactory.bool("allow_dynamic_allocation")
            .setDefault(() -> true);

    /**
     * Indicates the sizes in bits of the base types in C.
     * 
     * <p>
     * As default, returns the default values of CTypeSizes.
     * 
     */
    DataKey<CTypeSizes> C_BIT_SIZES = KeyFactory.object("c_bit_sizes", CTypeSizes.class)
            .setDefault(() -> CTypeSizes.newInstance());

    /**
     * The disposition of the values in memory. Returns a MemoryLayout instance.
     * 
     * TODO: Type should be MemoryLayout, initialized with a KeyFactory.enumeration(), but it would not be compatible
     * with corresponding CirOption. Change when CirOption.MEMORY_LAYOUT is no longer used
     */
    // DataKey<MemoryLayout> MEMORY_LAYOUT = KeyFactory.enumeration("memory_layout", MemoryLayout.class)
    // .setDefault(() -> MemoryLayout.COLUMN_MAJOR);
    DataKey<MultipleChoice> MEMORY_LAYOUT = KeyFactory.object("memory_layout", MultipleChoice.class)
            .setDefault(() -> MultipleChoice.newInstance(SpecsEnums.buildList(MemoryLayout.values()))
                    .setChoice(MemoryLayout.COLUMN_MAJOR.name()));

    /**
     * Indicates C library functions (e.g., memcpy) which should not be used.
     * 
     */
    DataKey<AvoidableFunctionsData> AVOID = KeyFactory.object("avoid", AvoidableFunctionsData.class)
            .setDefault(() -> AvoidableFunctionsData.newInstanceWithChecks(false));

    /**
     * 
     * The default floating point type to be used (e.g., single, double...).
     * 
     */
    DataKey<VariableType> DEFAULT_REAL = KeyFactory.object("default_float", VariableType.class)
            .setDefault(() -> CirUtils.getDefaultRealType());

    DataKey<String> CUSTOM_FREE_DATA_CODE = KeyFactory.string("custom_free_data_code", "free($1)");
    DataKey<String> CUSTOM_DATA_ALLOCATOR = KeyFactory.string("custom_data_allocator", "malloc($1)");
    DataKey<String> CUSTOM_ALLOCATION_HEADER = KeyFactory.string("custom_allocation_header", "");

    /**
     * TODO: Method will be unneeded after MemoryLayout is replaced with an Enumeration
     * 
     * @param data
     * @return
     */
    static MemoryLayout getMemoryLayout(DataStore data) {
        return SpecsEnums.valueOf(MemoryLayout.class, data.get(CirKeys.MEMORY_LAYOUT).getChoice());
    }

    /**
     * Supported functions that we can inline.
     */
    public static final List<Class<?>> SUPPORTED_INLINES = Arrays.asList(MatrixFunctionName.class);

    /**
     * @param name
     * @return
     */
    static DataStore newDefaultInitialization(String name) {
        DataStore defaultSetup = DataStore.newInstance(name);

        // Default inlinings
        Class<?>[] classes = CirKeys.SUPPORTED_INLINES.toArray(new Class<?>[CirKeys.SUPPORTED_INLINES.size()]);
        defaultSetup.set(CirKeys.INLINE, InliningData.newInstanceWithChecks(true, classes));

        return defaultSetup;
    }

}
