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

package org.specs.matlabtocl.v2.launcher;

import java.io.File;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.GlobalOptionsUtils;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;

public enum MatlabToCLTesterGlobalSetup implements SetupFieldEnum {
    CLIncludeDirectory(FieldType.string),
    CLLibraryDirectory(FieldType.string),
    CLLibraryName(FieldType.string);

    private final FieldType fieldType;

    private MatlabToCLTesterGlobalSetup(FieldType fieldType) {
	this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
	return this.fieldType;
    }

    @Override
    public String getSetupName() {
	return "MatlabToCLTester Global";
    }

    public static MatlabToCLTesterGlobalData getData() {
	SetupData setupData = GlobalOptionsUtils.loadData(MatlabToCLTesterGlobalSetup.class);

	SetupAccess setup = new SetupAccess(setupData);

	File includeDirectory = new File(setup.getString(CLIncludeDirectory));
	if (!includeDirectory.isDirectory()) {
	    includeDirectory = null;
	}

	File libraryDirectory = new File(setup.getString(CLLibraryDirectory));
	if (!libraryDirectory.isDirectory()) {
	    libraryDirectory = null;
	}

	String libraryName = setup.getString(CLLibraryName);

	OpenCLSDKInformation clSdk = new OpenCLSDKInformation(includeDirectory, libraryDirectory, libraryName);

	return new MatlabToCLTesterGlobalData(clSdk);
    }
}
