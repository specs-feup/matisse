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

import org.specs.MatlabToC.Program.ImplementationSetup;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupDefinition;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.SingleSetup;

/**
 * Setup definition for program MatisseVsCoder.
 * 
 * @author Joao Bispo
 */
public enum MatisseVsCoderSetup implements SetupFieldEnum, SingleSetup, DefaultValue {

    SourceFilesFolder(FieldType.string),
    InputVectorsFolder(FieldType.string),
    AuxiliaryFiles(FieldType.string),
    MatisseTypes(FieldType.string),
    CoderTypes(FieldType.string),
    StopAfterMatisse(FieldType.bool),
    OutputFolder(FieldType.string),
    DeleteTemporaryFiles(FieldType.bool),
    GenerateOnlyOneTest(FieldType.string),
    ImplementationSettings(FieldType.integratedSetup);

    public static MatisseVsCoderData newData(SetupData setupData) {
	SetupAccess setup = new SetupAccess(setupData);

	File srcFolder = setup.getFolderV2(SourceFilesFolder, true);
	File inputsFolder = setup.getFolderV2(InputVectorsFolder, true);
	File auxiFolder = setup.getFolderV2(AuxiliaryFiles, true);

	File matisseTypes = setup.getFile(null, MatisseTypes, true);
	File coderTypes = setup.getFile(null, CoderTypes, true);
	boolean stopAfterMatisse = setup.getBoolean(StopAfterMatisse);

	File outputFolder = setup.getFolderV2(null, OutputFolder, false);
	boolean deleteTemporaryFiles = setup.getBoolean(DeleteTemporaryFiles);

	String testToGenerate = setup.getString(GenerateOnlyOneTest);
	if (testToGenerate.trim().isEmpty()) {
	    testToGenerate = null;
	}

	// Implementation settings
	SetupData implementationData = setup.getSetup(ImplementationSettings);

	return new MatisseVsCoderData(srcFolder, inputsFolder, auxiFolder, matisseTypes,
		coderTypes, stopAfterMatisse, outputFolder, deleteTemporaryFiles, testToGenerate,
		implementationData);
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

    private MatisseVsCoderSetup(FieldType fieldType) {
	this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
	return fieldType;
    }

    @Override
    public String getSetupName() {
	return "MatisseVsCoder";
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.SingleSetup#getSetupOptions()
     */
    @Override
    public SetupDefinition getSetupOptions() {
	if (this == ImplementationSettings) {
	    return SetupDefinition.create(ImplementationSetup.class);
	}

	return null;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {
	if (this == DeleteTemporaryFiles) {
	    return FieldValue.create(Boolean.TRUE, DeleteTemporaryFiles);
	}

	return null;
    }

}
