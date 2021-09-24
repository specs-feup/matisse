/*
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

package org.suikasoft.CMainFunction.Tester.App;

import java.io.File;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.io.InputFiles;

/**
 * Setup definition for program EclipseDeployment.
 * 
 * @author Joao Bispo
 */
public enum AppTesterSetup implements SetupFieldEnum, DefaultValue {

    Path(FieldType.string),
    Output(FieldType.string),
    Executions(FieldType.integer),
    Min(FieldType.doublefloat),
    Repeat(FieldType.integer);

    public static AppTesterData newData(SetupData setupData) {
	SetupAccess setup = new SetupAccess(setupData);

	// File input = setup.getFile(null, Path, true);
	// File input = setup.getExistingPath(null, Path);
	// InputFiles inputs = InputFiles.newInstance(IoUtils.getPath(input));
	// File baseInputFolder = setup.getExistingFolder(Path);
	InputFiles inputs = setup.getInputFilesV2(Path);

	// Remove all files that do not have .exe extension
	for (int i = inputs.inputFiles.size() - 1; i >= 0; i--) {
	    // If "exe", do nothing
	    if (SpecsIo.getExtension(inputs.inputFiles.get(i)).toLowerCase().equals("exe")) {
		continue;
	    }

	    inputs.inputFiles.remove(i);
	}

	// File output = setup.getFolder(Output);
	File output = setup.getFolderV2(Output, false);

	Integer executions = setup.getInteger(Executions);
	Double minimumTimeSec = setup.getDouble(Min);
	Integer numRepeats = setup.getInteger(Repeat);

	return new AppTesterData(inputs, output, executions, minimumTimeSec, numRepeats);
    }

    private AppTesterSetup(FieldType fieldType) {
	this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
	return fieldType;
    }

    @Override
    public String getSetupName() {
	return "AppTester";
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {

	if (this == Min) {
	    return "MinimumExecutionTimePerKernel(seconds)";
	}

	if (this == Executions) {
	    return "Number of executions per measure(if >0, option below is ignored)";
	}

	if (this == Repeat) {
	    return "Number of Measures";
	}
	if (this == Output) {
	    return "OutputFolder";
	}

	return super.toString();
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {
	if (this == Min) {
	    return FieldValue.create((new Double(0.1)).toString(), Min);
	}

	if (this == Repeat) {
	    return FieldValue.create((new Integer(5)).toString(), Min);
	}

	if (this == Output) {
	    return FieldValue.create((SpecsIo.getWorkingDir()).getPath(), Output);
	}

	return null;
    }
}
