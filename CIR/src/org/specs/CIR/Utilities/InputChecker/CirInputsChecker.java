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

package org.specs.CIR.Utilities.InputChecker;

import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;

import com.google.common.collect.Lists;

/**
 * @author Joao Bispo
 * 
 */
public class CirInputsChecker extends AInputsChecker<CirInputsChecker> {

    private CirInputsChecker(ProviderData data, List<Check> checks) {
	super(data, checks);
    }

    public CirInputsChecker(List<Check> checks) {
	this(null, checks);
    }

    public CirInputsChecker() {
	this(null, Lists.newArrayList());
    }

    public CirInputsChecker(ProviderData data) {
	this(data, Lists.newArrayList());
	// this.data = data;
	// this.checks = Lists.newArrayList();
    }

    /**
     * Creates a shallow copy of the InputsChecker with the given ProviderData.
     * 
     * <p>
     * Changes in the rules of the created checker will reflect on the original checker.
     * 
     * @param data
     * @return
     */
    @Override
    public CirInputsChecker create(ProviderData data) {
	return new CirInputsChecker(data, this.getChecks());
    }

}
