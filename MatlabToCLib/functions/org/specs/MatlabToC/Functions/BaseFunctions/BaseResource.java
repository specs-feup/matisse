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

package org.specs.MatlabToC.Functions.BaseFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BaseResource implements ResourceProvider {

    ACCESS_SINGLE_COLON("access_single_colon.m"),
    FIND_DYNAMIC_1("find_dynamic1.m"),
    FIND_DYNAMIC_2("find_dynamic2.m"),
    ISEMPTY("isempty_general.m"),
    ISMEMBER("ismember_general.m"),
    SIZE_TWO_ARGS("size_two_args.m"),
    SORT1("sort1.m"),
    SORT2("sort2.m"),
    PADARRAY("padarray.m"),
    FLIP1D("flip1d.m");
    // FIND_STATIC("find_static.m");

    private final static String RESOURCE_FOLDER = "mfiles/base/";

    private final String resourceFilename;

    private BaseResource(String resource) {
        this.resourceFilename = BaseResource.RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
        return this.resourceFilename;
    }

    public String getFunctionName() {
        return SpecsIo.removeExtension(this.resourceFilename);
    }

    /**
     * Returns the value of 'getResource()'
     */
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getResource();
    }

}
