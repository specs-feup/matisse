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

package org.suikasoft.CMainFunction.Scripts;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import pt.up.fe.specs.util.SpecsIo;

public class DeleteAllButMain {

    private static final Set<String> EXCLUDE_FILES = new HashSet<>(Arrays.asList("main_test.c",
	    "array_creators_alloc.c", "array_creators_alloc.h", "load.c", "load.h", "matrix.c", "matrix.c", "tensor.c",
	    "tensor.h", "tensor_struct.h"));
    private static final String DATA_EXTENSION = "dat";

    // private static final String MAIN_FILE = "main_test.c";

    // private static final String INPUTS_FOLDER = "data";

    public static void main(String[] args) {
	String inputFolderStr = "C:\\Users\\JoaoBispo\\Work\\Workspaces\\specs-java-all\\MatlabToCTester\\output\\cSourceTestFiles";

	File inputFolder = SpecsIo.existingFolder(inputFolderStr);
	SpecsIo.getFilesRecursive(inputFolder).stream()
		.filter(file -> !EXCLUDE_FILES.contains(file.getName()))
		.filter(file -> !SpecsIo.getExtension(file).equals(DATA_EXTENSION))
		.forEach(file -> file.delete());
	/*
	IoUtils.getFilesRecursive(inputFolder).stream()
		.filter(file -> !file.getName().equals(MAIN_FILE))
		.filter(file -> !(file.isDirectory() && file.getName().equals(INPUTS_FOLDER)))
		.forEach(file -> file.delete());
		*/
    }
}
