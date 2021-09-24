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

package org.specs.MatlabToC.MFunctions;

import org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsAlloc;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Contains the resources needed by {@link MatlabOperatorsAlloc}.
 * 
 * @author Pedro Pinto
 * 
 */
public enum RulesResource implements ResourceProvider {

    // For allocated scalar colon
    MULTIPLE_SET_TO_FOR("multiple_set_to_for.m");

    private static final String MATISSE_INDEX_NAME = "matisse_index";

    private final static String RESOURCE_FOLDER = "templates/transform";
    private final String resource;

    public static String getMatisseIndexName() {
	return RulesResource.MATISSE_INDEX_NAME;
    }

    private RulesResource(String resource) {
	this.resource = resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return RulesResource.RESOURCE_FOLDER + "/" + this.resource;
    }
}
