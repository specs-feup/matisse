/*
 * Copyright 2015 SpecS.
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

package pt.up.fe.specs.matisse.weaver.gui;

import java.io.File;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Setup definition for program MatlabWeaver.
 *
 * @author Joao Bispo
 */
public enum MatlabWeaverSetup implements SetupFieldEnum, MultipleChoice, DefaultValue {

    LaraFile(FieldType.string),
    MainAspectArguments(FieldType.stringList),
    VerboseLevel(FieldType.multipleChoice),
    Debug(FieldType.bool),
    IncludesFolder(FieldType.folder),
    WeaverSystem(FieldType.multipleChoice),
    // ShowJavascriptOutput(FieldType.bool),
    LogOutput(FieldType.bool),
    LogFilename(FieldType.string),
    MainAspect(FieldType.string),
    Output(FieldType.folder),
    Workspace(FieldType.folder),
    ReportFilename(FieldType.string),
    ToolsXml(FieldType.string),
    Weaver(FieldType.string),
    XmlSpecs(FieldType.folder);

    public static DataStore newData(SetupData setupData) {
	SetupAccess setup = new SetupAccess(setupData);

	DataStore dataStore = DataStore.newInstance("matlab_weaver_data");

	File laraFile = setup.getExistingFile(LaraFile);
	if (laraFile == null) {
	    throw new RuntimeException("Could not find input file '" + setup.getString(LaraFile)
		    + "'. Terminating program.");
	}
	dataStore.set(MWeaverOption.INPUT_FILE, laraFile);

	// Set arguments, if present
	StringList mainAspectArg = setup.getStringList(MainAspectArguments);
	if (!mainAspectArg.getStringList().isEmpty()) {
	    dataStore.set(MWeaverOption.MAIN_ASPECT_ARGS, new pt.up.fe.specs.util.utilities.StringList(
		    mainAspectArg.getStringList()));
	}

	// Includes
	String includesFoldername = setup.getString(IncludesFolder);
	if (!includesFoldername.isEmpty()) {
	    File includesFolder = setup.getExistingFolder(IncludesFolder,
		    () -> "Could not find includes folder '" + includesFoldername + "'. Terminating program.");
	    dataStore.set(MWeaverOption.INCLUDES_FOLDER, includesFolder);
	}

	// Log file
	if (!setup.getString(LogFilename).isEmpty()) {
	    dataStore.set(MWeaverOption.LOG_FILENAME, setup.getString(LogFilename));
	}

	// Main aspect
	setString(MainAspect, MWeaverOption.MAIN_ASPECT, setup, dataStore);

	// Output folder
	if (!setup.getString(Output).isEmpty()) {
	    dataStore.set(MWeaverOption.OUTPUT, setup.getFolder(null, Output));
	}

	// Workspace
	if (!setup.getString(Workspace).isEmpty()) {
	    dataStore.set(MWeaverOption.WORKSPACE, setup.getExistingFolder(Workspace));
	}

	// Report file
	setString(ReportFilename, MWeaverOption.REPORT, setup, dataStore);
	// if (!setup.getString(ReportFilename).isEmpty()) {
	// dataStore.set(MWeaverOption.REPORT, setup.getString(ReportFilename));
	// }

	// Tools XML
	setString(ToolsXml, MWeaverOption.TOOLS, setup, dataStore);

	// Weaver class
	setString(Weaver, MWeaverOption.WEAVER, setup, dataStore);

	// Specification folder
	if (!setup.getString(XmlSpecs).isEmpty()) {
	    File xmlFolder = setup.getExistingFolder(XmlSpecs,
		    () -> "Could not find XML specification folder '" + setup.getString(XmlSpecs) + "'");
	    dataStore.set(MWeaverOption.XML_SPEC, xmlFolder);
	}

	dataStore.set(MWeaverOption.WEAVER_MODE, setup.getEnum(WeaverSystem, WeaverMode.class));
	dataStore.set(MWeaverOption.VERBOSE_LEVEL, setup.getEnum(VerboseLevel, VerboseLevel.class));
	dataStore.set(MWeaverOption.DEBUG, setup.getBoolean(Debug));
	// dataStore.set(MWeaverOption.SHOW_JS_OUTPUT, setup.getBoolean(ShowJavascriptOutput));
	dataStore.set(MWeaverOption.LOG, setup.getBoolean(LogOutput));

	return dataStore;
    }

    /**
     * Sets a string, if not empty.
     * 
     * @param setup
     * @param dataStore
     */
    private static void setString(SetupFieldEnum option, DataKey<String> key, SetupAccess setup, DataStore dataStore) {
	if (!setup.getString(option).isEmpty()) {
	    dataStore.set(key, setup.getString(option));
	}
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

    private MatlabWeaverSetup(FieldType fieldType) {
	this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
	return fieldType;
    }

    @Override
    public String getSetupName() {
	return "MatlabWeaver";
    }

    @Override
    public StringList getChoices() {
	if (this == VerboseLevel) {
	    return new StringList(VerboseLevel.class);
	}

	if (this == WeaverSystem) {
	    return new StringList(WeaverMode.class);
	}

	return null;
    }

    @Override
    public String toString() {
	if (this == ReportFilename) {
	    return "AspectOutputs (json)";
	}

	if (this == ToolsXml) {
	    return "Tools.xmlFile";
	}

	if (this == Weaver) {
	    return "Weaver (class file)";
	}

	if (this == XmlSpecs) {
	    return "XmlSpecificationFolder";
	}

	if (this == Workspace) {
	    return "M-filesFolder";
	}

	return super.toString();
    }

    @Override
    public FieldValue getDefaultValue() {
	if (this == WeaverSystem) {
	    return FieldValue.create(WeaverMode.TOM.name(), WeaverSystem);
	}

	if (this == IncludesFolder) {
	    return FieldValue.create("", IncludesFolder);
	}

	if (this == Output) {
	    return FieldValue.create("", Output);
	}

	if (this == Workspace) {
	    return FieldValue.create("", Workspace);
	}

	if (this == XmlSpecs) {
	    return FieldValue.create("", XmlSpecs);
	}
	return null;
    }
}
