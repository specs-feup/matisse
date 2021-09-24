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

package org.specs.CIR.Utilities.InputChecker;

import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;

import com.google.common.collect.Lists;

/**
 * List of checks, returns true on the first true check, otherwise tries the next one. Only returns false if all checks
 * fail.
 * 
 * @author Joao Bispo
 *
 */
public class CheckChain implements Check {

    private final List<Check> checks;

    public CheckChain(List<Check> checks) {
	this.checks = checks;
    }

    public CheckChain(Check... checks) {
	this(Lists.newArrayList());

	for (Check check : checks) {
	    addCheck(check);
	}
    }

    /**
     * Adds a check to the end of the list.
     * 
     * @param check
     * @return
     */
    public CheckChain addCheck(Check check) {
	checks.add(check);

	return this;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.InputChecker.Check#check(org.specs.CIR.FunctionInstance.ProviderData)
     */
    @Override
    public boolean check(ProviderData data) {
	// Iterate over all checks, if any of the returns true, return true.
	// If none of the checks returns true, return false
	for (Check check : checks) {
	    if (check.check(data)) {
		return true;
	    }
	}

	return false;
    }

}
