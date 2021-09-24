/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.MatlabToCTester.FileOperations;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 *
 */
public enum ScriptResource implements ResourceProvider {

    CODEGEN_RUN("codegen_run.m"),
    MAIN_SCRIPT_BODY("main_script_body.m"),
    MAIN_SCRIPT_TESTFUNCTION("main_script_testfunction.m"),
    MAIN_SCRIPT_RUNTEST("main_script_runtest.m"),
    TEST_SCRIPT_BODY("test_script_body.m"),
    TEST_SCRIPT_COMPARE("test_script_compare.m");
    
    
    private final static String RESOURCE_FOLDER = "matlabCode";
    
    private final String resource;

    /**
     * @param resource
     */
    private ScriptResource(String resource) {
	this.resource = resource;
    }
    
    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }
    
}
