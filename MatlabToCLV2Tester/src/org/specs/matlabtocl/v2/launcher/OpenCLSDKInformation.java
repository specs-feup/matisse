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

public class OpenCLSDKInformation {
    private final File includeDirectory;
    private final File libraryDirectory;
    private final String libraryName;

    public OpenCLSDKInformation(File includeDirectory, File libraryDirectory, String libraryName) {
	this.includeDirectory = includeDirectory;
	this.libraryDirectory = libraryDirectory;
	this.libraryName = libraryName;
    }

    public File getIncludeDirectory() {
	return this.includeDirectory;
    }

    public File getLibraryDirectory() {
	return this.libraryDirectory;
    }

    public String getLibraryName() {
	return this.libraryName;
    }
}
