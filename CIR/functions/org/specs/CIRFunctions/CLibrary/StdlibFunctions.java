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

package org.specs.CIRFunctions.CLibrary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider;
import org.specs.CIR.Utilities.CLibraryProvider.GenericCLibrary;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Void.VoidType;

/**
 * @author Joao Bispo
 *
 */
public class StdlibFunctions {

    private final NumericFactory numerics;

    public StdlibFunctions(NumericFactory numerics) {
	this.numerics = numerics;
    }

    private static final SystemInclude include = SystemInclude.Stdlib;

    private static CLibraryProvider build(String name, VariableType outputType, List<VariableType> inputTypes,
	    boolean hasSideEffects) {
	return new GenericCLibrary(name, include, outputType, inputTypes, hasSideEffects);
    }

    public CLibraryProvider free() {
	return build("free", VoidType.newInstance(), Arrays.asList(VoidType.newInstance()), true);
    }

    public CLibraryProvider rand() {
	// FIXME: Maybe rand() should have hasSideEffects=true?
	return build("rand", numerics.newInt(), Arrays.asList(VoidType.newInstance()), false);
    }

    public CLibraryProvider abort() {
	return build("abort", VoidType.newInstance(), Collections.emptyList(), true);
    }
}
