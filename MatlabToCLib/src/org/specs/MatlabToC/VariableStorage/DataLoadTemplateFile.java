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

package org.specs.MatlabToC.VariableStorage;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum DataLoadTemplateFile implements ResourceProvider {
    LOAD_FROM_FILE("load_from_file.c"),
    GET_ABSOLUTE_FILENAME("get_absolute_filename.c"),
    GET_ABSOLUTE_FILENAME_INCLUDES("get_absolute_filename_includes.c");
    private final String resource;

    private DataLoadTemplateFile(String resource) {
	this.resource = DataLoadTemplateFile.class.getPackage().getName().replace('.', '/') + "/" + resource;
    }

    @Override
    public String getResource() {
	return resource;
    }

}
