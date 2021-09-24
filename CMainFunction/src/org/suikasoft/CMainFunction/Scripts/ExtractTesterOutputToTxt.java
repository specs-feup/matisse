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

package org.suikasoft.CMainFunction.Scripts;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.system.ProcessOutputAsString;

/**
 * @author Joao Bispo
 * 
 */
public class ExtractTesterOutputToTxt {

    /**
     * @param args
     */
    public static void main(String[] args) {
	SpecsSystem.programStandardInit();

	// Copy coder mains for printing outputs
	// String sourceFolder =
	// "C:\\Users\\Joao Bispo\\Work\\Code\\Outputs\\MatlabToCTester\\output\\cSourceTestFiles";
	String coderCFolder = "C:\\\\Users\\\\Joao Bispo\\\\Dropbox\\\\Research\\\\Work\\\\2013-12-11 Compare MATISSE and Coder V3\\\\c\\\\coder";

	// copyCoderMain(IoUtils.existingFolder(null, sourceFolder), IoUtils.safeFolder(coderCFolder), "prints");
	setCoderMain(SpecsIo.existingFolder(null, coderCFolder), "tester");
    }

    /**
     * @param exeFolder
     * @param baseOutputFolder
     */
    public static void execute(File exeFolder, File baseOutputFolder) {
	// Get all executables
	List<File> executables = SpecsIo.getFilesRecursive(exeFolder);

	// Run and save the output of each executable
	for (File executable : executables) {
	    ProcessOutputAsString output = SpecsSystem.runProcess(Arrays.asList(executable.getAbsolutePath()),
		    executable.getParentFile(), true, false);

	    // Output file
	    String exeOutput = SpecsIo.getRelativePath(executable.getParentFile(), exeFolder);
	    File outputFolder = new File(baseOutputFolder, exeOutput);

	    File outputFile = new File(outputFolder, "outputC.txt");

	    SpecsIo.write(outputFile, output.getOutput());

	}

    }

    /**
     * 
     * @param sourceFolder
     * @param baseOutputFolder
     * @param newExtension
     */
    public static void copyCoderMain(File sourceFolder, File baseOutputFolder, String newExtension) {
	// Get all mains for coder
	List<File> coderMains = SpecsIo.getFilesRecursive(sourceFolder, "coder");

	// Run and save the output of each executable
	for (File coderMain : coderMains) {

	    // Output file
	    String coderOutput = SpecsIo.getRelativePath(coderMain.getParentFile(), sourceFolder);
	    File outputFolder = new File(baseOutputFolder, coderOutput);

	    File outputFile = new File(outputFolder, SpecsIo.removeExtension(coderMain.getName()) + "." + newExtension);

	    SpecsIo.copy(coderMain, outputFile);

	}
    }

    /**
     * @param exeFolder
     * @param baseOutputFolder
     */
    public static void setCoderMain(File cFolder, String targetExtension) {
	// Get target mains
	List<File> coderMains = SpecsIo.getFilesRecursive(cFolder, targetExtension);

	// Create each main
	for (File coderMain : coderMains) {

	    // Output file
	    File outputFile = new File(coderMain.getParentFile(), SpecsIo.removeExtension(coderMain.getName()));
	    SpecsIo.copy(coderMain, outputFile);
	}

    }
}
