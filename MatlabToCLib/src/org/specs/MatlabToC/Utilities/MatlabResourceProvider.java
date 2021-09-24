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

package org.specs.MatlabToC.Utilities;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Resource provider for M-files.
 * 
 * @author Joao Bispo
 *
 */
public interface MatlabResourceProvider extends ResourceProvider {

    default String getFunctionName() {

	// Remove extension
	String resourceName = SpecsIo.removeExtension(getResource());

	// Remove path
	int slashIndex = resourceName.lastIndexOf('/');
	if (slashIndex == -1) {
	    return resourceName;
	}

	return resourceName.substring(slashIndex + 1, resourceName.length());

    }

}
