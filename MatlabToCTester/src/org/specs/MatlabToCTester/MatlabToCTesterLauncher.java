/*
 * Copyright 2012 SPeCS.
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

package org.specs.MatlabToCTester;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.specs.guihelper.App;
import pt.up.fe.specs.guihelper.AppDefaultConfig;
import pt.up.fe.specs.guihelper.AppSource;
import pt.up.fe.specs.guihelper.AppUsesGlobalOptions;
import pt.up.fe.specs.guihelper.GuiHelperUtils;
import pt.up.fe.specs.guihelper.Base.SetupDefinition;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.gui.SimpleGui;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * Launches the program MatlabToCTester.
 * 
 * @author Pedro Pinto
 */
public class MatlabToCTesterLauncher implements AppDefaultConfig, AppSource, AppUsesGlobalOptions {

    // private final static String FLAG_AUTO= "-auto";

    private final static boolean DEBUG_MODE = true;

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SpecsSystem.programStandardInit();
        // LoggingUtils.setPrintStackTrace(true);

        SpecsIo.resourceCopy(getResources());

        MatlabToCTesterLauncher app = new MatlabToCTesterLauncher();

        if (args.length > 0) {
            /*
            System.out.println("Pausing, waiting for input...");
            try {
                System.in.read();
            } catch (IOException e) {
                LoggingUtils.msgWarn("Error message:\n", e);
            }
            */

            // Check if 'auto' flag
            /*
            if(args[0].equals(FLAG_AUTO)) {
            // Create AutomatedTest
            AutomatedTest autoTest = AutomatedTest.build(args, app);
            
            // If could not create, return
            if(autoTest == null) {
                return;
            }
            
            // Run automated tests
            autoTest.run();
            return;
            }
            */

            GuiHelperUtils.trySingleConfigMode(args, app);
            return;
        }

        SimpleGui gui = new SimpleGui(app);

        gui.setTitle("Matisse v0.1");
        gui.execute();
    }

    public static List<String> getResources() {
        List<String> resources = new ArrayList<>();
        resources.addAll(MatlabToCTesterLauncher.baseResourceFiles);
        resources.addAll(SpecsProperty.getResources());
        return resources;
    }

    @Override
    public int execute(File setupFile) throws InterruptedException {
        MatlabToCTesterGlobalData globalData = MatlabToCTesterGlobalSetup.getData();
        SetupData setupData = GuiHelperUtils.loadData(setupFile);

        MatlabToCTesterData data = null;
        try {
            data = MatlabToCTesterSetup.newData(setupData, globalData);
        } catch (Exception e) {
            SpecsLogs.warn("Could not create configuration data, " + e.getMessage(), e);
            e.printStackTrace();
        }

        return execute(data);
    }

    public int execute(MatlabToCTesterData data) {
        if (data == null) {
            // LoggingUtils.msgInfo("Could not build configuration data.");
            return -1;
        }

        MatlabToCTester appBody = new MatlabToCTester(data);
        int result = -1;

        if (MatlabToCTesterLauncher.DEBUG_MODE) {
            result = appBody.execute();
        } else {
            try {
                result = appBody.execute();
            } catch (Exception e) {
                SpecsLogs.msgInfo(e.getMessage());
            }
        }

        SpecsLogs.msgInfo("Done.");
        return result;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.AppSource#newInstance()
     */
    @Override
    public App newInstance() {
        return new MatlabToCTesterLauncher();
    }

    @Override
    public SetupDefinition getEnumKeys() {
        return SetupDefinition.create(MatlabToCTesterSetup.class);
    }

    @Override
    public String defaultConfigFile() {
        return MatlabToCTesterLauncher.DEFAULT_CONFIG;
    }

    @Override
    public Class<? extends SetupFieldEnum> getGlobalOptions() {
        return MatlabToCTesterGlobalSetup.class;
    }

    private final static String DEFAULT_CONFIG = "MatlabToCTester.config";
    private final static List<String> baseResourceFiles = Arrays.asList();

}