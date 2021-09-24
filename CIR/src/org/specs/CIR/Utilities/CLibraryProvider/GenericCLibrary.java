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

package org.specs.CIR.Utilities.CLibraryProvider;

import java.util.List;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;

/**
 * @author Joao Bispo
 *
 */
public class GenericCLibrary implements CLibraryProvider {

    private final String functionName;
    private final SystemInclude library;
    private final VariableType outputType;
    private final List<VariableType> inputTypes;
    private final boolean hasSideEffects;

    public GenericCLibrary(String functionName, SystemInclude library, VariableType outputType,
	    List<VariableType> inputTypes, boolean hasSideEffects) {

	this.functionName = functionName;
	this.library = library;
	this.outputType = outputType;
	this.inputTypes = inputTypes;
	this.hasSideEffects = hasSideEffects;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider#getFunctionName()
     */
    @Override
    public String getFunctionName() {
	return functionName;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider#getLibrary()
     */
    @Override
    public SystemInclude getLibrary() {
	return library;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider#getInputTypes()
     */
    @Override
    public List<VariableType> getInputTypes() {
	return inputTypes;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider#getOutputType()
     */
    @Override
    public VariableType getOutputType() {
	return outputType;
    }

    @Override
    public boolean canHaveSideEffects() {
	return hasSideEffects;
    }

}
