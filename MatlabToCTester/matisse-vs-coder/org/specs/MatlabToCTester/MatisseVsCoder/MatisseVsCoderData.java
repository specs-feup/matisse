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

import pt.up.fe.specs.guihelper.BaseTypes.SetupData;

/**
 * Data fields for MatisseVsCoder.
 * 
 * @author Joao Bispo
 */
public class MatisseVsCoderData {

    public final File srcFolder;
    public final File inputsFolder;
    public final File auxiFolder;
    public final File matisseTypes;
    public final File coderTypes;
    public final boolean stopAfterMatisse;
    public final File outputFolderBase;
    public final boolean deleteTemporaryFiles;
    public final String testToGenerate;
    public final SetupData implementationData;

    /**
     * @param srcFolder
     * @param inputsFolder
     * @param auxiFolder
     * @param matisseTypes
     * @param coderTypes
     * @param stopAfterMatisse
     * @param deleteTemporaryFiles 
     * @param outputFolderBase
     * @param testToGenerate
     * @param implementationData
     */
    public MatisseVsCoderData(File srcFolder, File inputsFolder, File auxiFolder,
	    File matisseTypes, File coderTypes, boolean stopAfterMatisse, File outputFolder,
	    boolean deleteTemporaryFiles, String testToGenerate, SetupData implementationData) {

	this.srcFolder = srcFolder;
	this.inputsFolder = inputsFolder;
	this.auxiFolder = auxiFolder;
	this.matisseTypes = matisseTypes;
	this.coderTypes = coderTypes;
	this.stopAfterMatisse = stopAfterMatisse;
	this.outputFolderBase = outputFolder;
	this.deleteTemporaryFiles = deleteTemporaryFiles;
	this.testToGenerate = testToGenerate;
	this.implementationData = implementationData;
    }

}
