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

package org.specs.matisselib.tests.passmanager;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum ProjectPassCompilationManagerOutputResources implements ResourceProvider {
    DUMMY_NO_COMMENTS("dummy_no_comments.m"),
    DUMMY_INSERTED_COMMENTS("dummy_inserted_comments.m"),
    DUMMYX_INSERTED_COMMENTS("dummyx_inserted_comments.m"),
    TEST1_LOG("test1.log"),
    TEST2_LOG("test2.log"),
    TESTRESOURCE_LOG("testresource.log"),
    TESTRESOURCE_LOG2("testresource2.log");

    private static final String path = "passmanager";
    private final String fileName;

    private ProjectPassCompilationManagerOutputResources(String fileName) {
	assert fileName != null;

	this.fileName = fileName;
    }

    @Override
    public String getResource() {
	return path + "/" + fileName;
    }

}
