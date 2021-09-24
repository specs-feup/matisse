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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.specs.guihelper.App;
import pt.up.fe.specs.guihelper.AppDefaultConfig;
import pt.up.fe.specs.guihelper.AppSource;
import pt.up.fe.specs.guihelper.GuiHelperUtils;
import pt.up.fe.specs.guihelper.Base.SetupDefinition;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.gui.SimpleGui;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * Launches the program MatisseVsCoder.
 *
 * @author Joao Bispo
 */
public class MatisseVsCoderLauncher implements AppDefaultConfig, AppSource {

    private final static String DEFAULT_CONFIG = "default.config";
    private final static List<String> baseResourceFiles = Arrays.asList();

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
	SpecsSystem.programStandardInit();

	SpecsIo.resourceCopy(getResources());

	MatisseVsCoderLauncher app = new MatisseVsCoderLauncher();

	if (args.length > 0) {
	    GuiHelperUtils.trySingleConfigMode(args, app);
	    return;
	}

	SimpleGui gui = new SimpleGui(app);

	gui.setTitle("MatisseVsCoder v0.1");
	gui.execute();
    }

    public static List<String> getResources() {
	List<String> resources = new ArrayList<>();
	resources.addAll(baseResourceFiles);
	resources.addAll(SpecsProperty.getResources());
	return resources;
    }

    @Override
    public int execute(File setupFile) throws InterruptedException {
	SetupData setupData = GuiHelperUtils.loadData(setupFile);

	// MatisseVsCoderGlobalData globalData = MatisseVsCoderGlobalSetup.getData();

	MatisseVsCoderData data = null;
	try {
	    data = MatisseVsCoderSetup.newData(setupData);
	} catch (Exception e) {
	    SpecsLogs.warn("Exception while building configuration data.", e);
	    return -1;
	}

	if (data == null) {
	    SpecsLogs.warn("Configuration data is null.");
	    return -1;
	}

	MatisseVsCoder appBody = new MatisseVsCoder(data);

	int result = appBody.execute();
	SpecsLogs.msgInfo("Done");

	return result;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.AppSource#newInstance()
     */
    @Override
    public App newInstance() {
	return new MatisseVsCoderLauncher();
    }

    @Override
    public SetupDefinition getEnumKeys() {
	return SetupDefinition.create(MatisseVsCoderSetup.class);
    }

    @Override
    public String defaultConfigFile() {
	return DEFAULT_CONFIG;
    }

}