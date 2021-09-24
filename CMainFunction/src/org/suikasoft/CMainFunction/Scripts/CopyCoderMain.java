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

package org.suikasoft.CMainFunction.Scripts;

import java.io.File;
import java.util.List;
import java.util.Set;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

public class CopyCoderMain {

    public static void main(String[] args) {
	String matisseCFoldername = "C:\\Users\\Joao Bispo\\Work\\Code\\Outputs\\MatlabToCTester\\output\\output\\cSourceTestFiles\\editdist";
	String coderCFoldername = "C:\\Users\\Joao Bispo\\Dropbox\\Research\\Work\\2014-02-13 Compare MATISSE and Coder V4\\c\\editdist\\Coder_2012b_int_out";

	String coderMainName = "main_test.c.coder";
	String mainName = "main_test.c";

	File matisseFolder = new File(matisseCFoldername);
	File coderFolder = new File(coderCFoldername);

	// Get all folders in MATISSE
	List<File> matisseTestFolders = SpecsIo.getFolders(matisseFolder);

	// Copy folder names
	Set<String> coderAllFolders = SpecsFactory.newHashSet();
	for (File folder : SpecsIo.getFolders(coderFolder)) {
	    coderAllFolders.add(folder.getName());
	}

	// Get all folders in Coder that are also in MATISSE
	// Go in reverse order so that items can be removed

	List<File> coderTestFolders = SpecsFactory.newArrayList();
	for (int i = matisseTestFolders.size() - 1; i >= 0; i--) {
	    // Check if name exists in both folders
	    File currentMatisseFolder = matisseTestFolders.get(i);

	    // If file is not in coder folder, remove it from list
	    if (!coderAllFolders.contains(currentMatisseFolder.getName())) {
		matisseTestFolders.remove(i);
		continue;
	    }

	    coderTestFolders.add(0, new File(coderFolder, currentMatisseFolder.getName()));
	}

	// Copy 'main_test.c.coder' to coder folder and rename
	// for (File matisseTestFolder : matisseTestFolders) {
	for (int i = 0; i < matisseTestFolders.size(); i++) {
	    File matisseTestFolder = matisseTestFolders.get(i);

	    File matisseMain = new File(matisseTestFolder, coderMainName);
	    File coderMain = new File(coderTestFolders.get(i), mainName);

	    // Check
	    if (!matisseMain.getParentFile().getName().equals(coderMain.getParentFile().getName())) {
		SpecsLogs.warn("Name of folders");
		continue;
	    }

	    System.out.println("COPYING " + matisseMain + " to " + coderMain);
	    SpecsIo.copy(matisseMain, coderMain);
	}
    }
}
