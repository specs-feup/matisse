/*
 * Copyright 2013 SPeCS.
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

package org.specs.MatlabToCTester.MatisseVsCoder;

import java.io.File;
import java.util.List;

import org.specs.MatlabToCTester.MatlabToCTester;
import org.specs.MatlabToCTester.MatlabToCTesterData;
import org.specs.MatlabToCTester.MatlabToCTesterSetup;
import org.specs.MatlabToCTester.CGeneration.CGenerator;

import pt.up.fe.specs.guihelper.GuiHelperUtils;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * Generates C code from MATLAB using MATISSE and Coder.
 * 
 * @author Joao Bispo
 */
public class MatisseVsCoder {

    private static final String FOLDERNAME_TESTER_MATISSE = "matisse";
    private static final String FOLDERNAME_TESTER_CODER = "coder";
    private static final String FOLDERNAME_TESTER_MATISSE_C = FOLDERNAME_TESTER_MATISSE
	    + "/output/cSourceTestFiles";
    private static final String FOLDERNAME_TESTER_CODER_C = FOLDERNAME_TESTER_CODER
	    + "/output/cSourceTestFiles";

    private static final String FOLDERNAME_MATISSE = "c_matisse";
    private static final String FOLDERNAME_CODER = "c_coder";

    private static final String FILENAME_CODERMAIN = "main_test.c.coder";

    private final MatisseVsCoderData data;

    public MatisseVsCoder(MatisseVsCoderData data) {
	this.data = data;
    }

    public int execute() {

	SpecsProperty.ShowStackTrace.applyProperty("true");

	// Prepare output folder
	// File workFolder = IoUtils.safeFolder(data.outputFolderBase, OUTPUT_FOLDER);
	// IoUtils.deleteFolderContents(workFolder);

	SetupData setup = getTesterSetup();

	// Prepare options for MATISSE
	File matisseWorkspace = SpecsIo
		.mkdir(data.outputFolderBase, FOLDERNAME_TESTER_MATISSE);
	setup.put(MatlabToCTesterSetup.AspectFilesDirectory, data.matisseTypes.getAbsolutePath());
	setup.put(MatlabToCTesterSetup.MatlabToCCompiler, CGenerator.MATISSE);
	setup.put(MatlabToCTesterSetup.OutputFolder, matisseWorkspace.getAbsolutePath());

	// Create data
	MatlabToCTesterData matisseTesterData = MatlabToCTesterSetup.newData(setup, null);

	// Run Tester
	SpecsLogs.msgInfo("Generating C code using MATISSE:\n");
	MatlabToCTester matisseTester = new MatlabToCTester(matisseTesterData);
	int result = matisseTester.execute();

	if (result != 0) {
	    SpecsLogs.msgInfo("Problems while running Tester to generate C code with MATISSE.");
	    return -1;
	}

	processMatisseOutput();

	if (data.stopAfterMatisse) {
	    SpecsLogs.msgInfo("Stoping after MATISSE.");
	    return 0;
	}

	SpecsLogs.msgInfo("Generating C code using Coder:\n");

	// Prepare options for Coder
	File coderWorkspace = SpecsIo.mkdir(data.outputFolderBase, FOLDERNAME_TESTER_CODER);
	setup.put(MatlabToCTesterSetup.AspectFilesDirectory, data.coderTypes.getAbsolutePath());
	setup.put(MatlabToCTesterSetup.MatlabToCCompiler, CGenerator.MathworksCodegen);
	setup.put(MatlabToCTesterSetup.OutputFolder, coderWorkspace.getAbsolutePath());

	// Create data
	matisseTesterData = MatlabToCTesterSetup.newData(setup, null);

	// Run Tester
	matisseTester = new MatlabToCTester(matisseTesterData);
	result = matisseTester.execute();

	if (result != 0) {
	    SpecsLogs.msgInfo("Problems while running Tester to generate C code with Coder.");
	    return -1;
	}

	processCoderOutput();

	// Remove Tester files
	if (data.deleteTemporaryFiles) {
	    File testerOutput = SpecsIo.existingFolder(data.outputFolderBase,
		    FOLDERNAME_TESTER_MATISSE);
	    SpecsIo.deleteFolderContents(testerOutput);
	    testerOutput.delete();

	    testerOutput = SpecsIo.existingFolder(data.outputFolderBase, FOLDERNAME_TESTER_CODER);
	    SpecsIo.deleteFolderContents(testerOutput);
	    testerOutput.delete();
	}

	return 0;
    }

    /**
     * 
     */
    private void processCoderOutput() {

	// Get folder with C output
	File cOutput = SpecsIo.existingFolder(data.outputFolderBase, FOLDERNAME_TESTER_CODER_C);

	// Each folder is benchmark
	List<File> testerTests = SpecsIo.getFolders(cOutput);

	SpecsLogs.msgInfo("\nCopying Coder sources...");
	// Copy each Matisse test to the corresponding folder
	// File matisseFolder = IoUtils.safeFolder(data.outputFolderBase, FOLDERNAME_MATISSE);
	File coderFolder = SpecsIo.mkdir(data.outputFolderBase, FOLDERNAME_CODER);

	// IoUtils.deleteFolderContents(matisseFolder);
	// IoUtils.deleteFolderContents(coderFolder);

	for (File testerTestFolder : testerTests) {
	    // Get name
	    String testName = testerTestFolder.getName();

	    // Get folder with source code
	    File singleTestFolder = getSingleTest(testerTestFolder);
	    if (singleTestFolder == null) {
		SpecsLogs.msgInfo("No tests generated for '" + testerTestFolder.getName()
			+ "'. Skipping test.");
		continue;
	    }

	    // Get destination folder
	    File destinationTestFolder = SpecsIo.mkdir(coderFolder, testName);

	    SpecsIo.copyFolder(singleTestFolder, destinationTestFolder, true);

	}

    }

    /**
     * 
     */
    private void processMatisseOutput() {
	// Get folder with C output
	File cOutput = SpecsIo.existingFolder(data.outputFolderBase, FOLDERNAME_TESTER_MATISSE_C);

	// Each folder is benchmark
	List<File> matisseTests = SpecsIo.getFolders(cOutput);

	SpecsLogs.msgInfo("\nCopying MATISSE sources...");
	// Copy each Matisse test to the corresponding folder
	File matisseFolder = SpecsIo.mkdir(data.outputFolderBase, FOLDERNAME_MATISSE);
	File coderFolder = SpecsIo.mkdir(data.outputFolderBase, FOLDERNAME_CODER);

	SpecsIo.deleteFolderContents(matisseFolder);
	SpecsIo.deleteFolderContents(coderFolder);

	for (File matisseTestFolder : matisseTests) {
	    // Get name
	    String testName = matisseTestFolder.getName();

	    // Get folder with source code
	    File singleTestFolder = getSingleTest(matisseTestFolder);
	    if (singleTestFolder == null) {
		SpecsLogs.msgInfo("No tests generated for '" + matisseTestFolder.getName()
			+ "'. Skipping test.");
		continue;
	    }

	    // Get destination folder
	    File destinationTestFolder = SpecsIo.mkdir(matisseFolder, testName);

	    SpecsIo.copyFolder(singleTestFolder, destinationTestFolder, true);

	    // Copy main for Coder
	    File coderMain = SpecsIo.existingFile(destinationTestFolder, FILENAME_CODERMAIN);

	    File coderOutputFolder = SpecsIo.mkdir(coderFolder, testName);
	    File coderOutputFile = new File(coderOutputFolder, SpecsIo.removeExtension(coderMain
		    .getName()));

	    SpecsIo.copy(coderMain, coderOutputFile, true);
	}
    }

    /**
     * @param matisseTestFolder
     */
    private static File getSingleTest(File matisseTestFolder) {
	List<File> testInstances = SpecsIo.getFolders(matisseTestFolder);
	if (testInstances.isEmpty()) {
	    return null;
	}

	File testInstance = testInstances.get(0);
	if (testInstances.size() > 1) {
	    SpecsLogs.msgInfo("Found more than one test instance, returning the first one, '"
		    + testInstance.getName()
		    + "'. This happens when there are multiple input vectors for the same source");
	}

	return testInstance;
    }

    /**
     * @return
     */
    private SetupData getTesterSetup() {
	// Prepare MatlabToCTester setup
	SetupData setup = GuiHelperUtils.loadData(SpecsIo
		.getResource(MatisseVsCoderResource.DEFAULT_MATLAB_TO_C_TESTER));

	setup.put(MatlabToCTesterSetup.MatlabSourceFilesDirectory, data.srcFolder.getAbsolutePath());
	setup.put(MatlabToCTesterSetup.MatlabInputFilesDirectory,
		data.inputsFolder.getAbsolutePath());
	setup.put(MatlabToCTesterSetup.MatlabAuxiliaryFiles, data.auxiFolder.getAbsolutePath());

	// setup.put(MatlabToCTesterSetup.OutputFolder, workFolder.getAbsolutePath());

	if (data.testToGenerate != null) {
	    setup.put(MatlabToCTesterSetup.RunOnlyOneTest, data.testToGenerate);
	}

	setup.put(MatlabToCTesterSetup.ImplementationSettings, data.implementationData);

	setup.put(MatlabToCTesterSetup.DeleteOutputContents, data.deleteTemporaryFiles);

	return setup;
    }

}
