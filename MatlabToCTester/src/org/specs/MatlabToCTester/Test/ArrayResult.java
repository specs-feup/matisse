/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToCTester.Test;

import java.util.List;

/**
 * Represents results from an execution.
 * 
 * @author JoaoBispo
 *
 */
public interface ArrayResult {

    /**
     * The shape of the results.
     * 
     * @return
     */
    List<Integer> getDimensions();

    /**
     * @return the name of the variable
     */
    String getName();

    /**
     * @return the number of elements
     */
    default int getNumElements() {
	int elements = 1;
	for (Integer dim : getDimensions()) {
	    elements *= dim;
	}

	return elements;
    }

    /**
     * 
     * @param index
     * @return the element at the given index
     */
    double getDouble(int index);

}
