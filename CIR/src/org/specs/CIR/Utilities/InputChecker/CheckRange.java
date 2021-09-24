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

import org.specs.CIR.FunctionInstance.ProviderData;

/**
 * Check that is to be applied on a given range of the input types.
 * 
 * @author Joao Bispo
 *
 */
public class CheckRange implements Check {

    private final Check check;
    private final int startPosition;
    // If -1, means the end of the list
    private final int endPosition;

    public CheckRange(Check check, int startPosition, int endPosition) {
	this.check = check;
	this.startPosition = startPosition;
	this.endPosition = endPosition;
    }

    public CheckRange(Check check, int startPosition) {
	this(check, startPosition, -1);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.InputChecker.Check#check(org.specs.CIR.FunctionInstance.ProviderData)
     */
    @Override
    public boolean check(ProviderData data) {
	int parsedEnd = getEndPosition(data);

	ProviderData subData = data.create(data.getInputTypes().subList(startPosition, parsedEnd));

	return check.check(subData);
    }

    /**
     * @param data
     * @return
     */
    private int getEndPosition(ProviderData data) {
	int numInputs = data.getInputTypes().size();

	// If end position is negative, return end of the list
	if (endPosition < 0) {
	    return numInputs;
	}

	// If end position is greater than input list, return end of the list
	if (endPosition > numInputs) {
	    return numInputs;
	}

	return endPosition;
    }

}
