/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToCTester.MatisseVsCoder;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum MatisseVsCoderResource implements ResourceProvider {

    DEFAULT_MATLAB_TO_C_TESTER("default.matlabToCTester");

    private final static String RESOURCE_FOLDER = "matisse_vs_coder";

    private final String resource;

    /**
     * @param resource
     */
    private MatisseVsCoderResource(String resource) {
	this.resource = resource;
    }

    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }

}
