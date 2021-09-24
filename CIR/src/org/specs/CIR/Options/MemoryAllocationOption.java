/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIR.Options;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.Interfaces.MemoryAllocation;
import org.suikasoft.jOptions.Interfaces.AliasProvider;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public enum MemoryAllocationOption implements MemoryAllocation, AliasProvider {
    Static(true, false, "static", "DECLARED"),
    //Dynamic(false, true, "dynamic", "ALLOCATED");
    Dynamic(false, true, "dynamic", "ALLOCATED");

    private static final Map<String, String> alias;
    static {
	alias = SpecsFactory.newLinkedHashMap();

	// Add alias from all enums
	for (MemoryAllocationOption value : values()) {
	    for (String singleAlias : value.enumAlias) {
		alias.put(singleAlias, value.toString());
	    }
	}
    }

    private final boolean useStatic;
    private final boolean useDynamic;
    private final List<String> enumAlias;

    /**
     * @param useStatic
     * @param useDynamic
     */
    private MemoryAllocationOption(boolean useStatic, boolean useDynamic, String... alias) {
	this.useStatic = useStatic;
	this.useDynamic = useDynamic;

	this.enumAlias = Arrays.asList(alias);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.Interfaces.MemoryAllocation#useStatic()
     */
    @Override
    public boolean useStatic() {
	return useStatic;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.Interfaces.MemoryAllocation#useDynamic()
     */
    @Override
    public boolean useDynamic() {
	return useDynamic;
    }

    /* (non-Javadoc)
     * @see org.suikasoft.jOptions.Interfaces.AliasProvider#getAlias()
     */
    @Override
    public Map<String, String> getAlias() {
	return alias;
    }

}
