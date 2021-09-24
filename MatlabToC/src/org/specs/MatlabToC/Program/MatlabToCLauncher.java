/*
 * Copyright 2012 Specs.
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

package org.specs.MatlabToC.Program;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabToC.Program.Global.MatlabToCGlobalData;
import org.specs.MatlabToC.Program.Global.MatlabToCGlobalSetup;

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

/**
 * Launches the program MatlabToC.
 * 
 * @author Joao Bispo
 */
public class MatlabToCLauncher implements AppDefaultConfig, AppSource, AppUsesGlobalOptions {

    public static boolean execute(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsIo.resourceCopy(getResources());

        MatlabToCLauncher app = new MatlabToCLauncher();

        return GuiHelperUtils.trySingleConfigMode(args, app);
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsIo.resourceCopy(getResources());

        MatlabToCLauncher app = new MatlabToCLauncher();

        if (args.length > 0) {
            boolean success = GuiHelperUtils.trySingleConfigMode(args, app);

            if (!success) {
                SpecsLogs.warn("Matlab-to-C did not exit properly");
                System.exit(1);
            }

            return;
        }
        /*
        if (GuiHelperUtils.trySingleConfigMode(args, app)) {
            return;
        }
        */

        SimpleGui gui = new SimpleGui(app);

        gui.setTitle("Matisse - MatlabToC v0.4");
        gui.execute();

        return;
    }

    public static List<String> getResources() {
        List<String> resources = new ArrayList<>();
        resources.addAll(MatlabToCLauncher.baseResourceFiles);
        // resources.addAll(SuikaProperty.getResources());
        return resources;
    }

    @Override
    public int execute(File setupFile) throws InterruptedException {

        SetupData setupData = GuiHelperUtils.loadData(setupFile);

        MatlabToCGlobalData globalData = MatlabToCGlobalSetup.getData();
        MatlabToCData data = MatlabToCSetup.newData(setupData, globalData);
        if (data == null) {
            SpecsLogs.getLogger().warning("Could not get application setup.");
            return -1;
        }

        int result = execute(data);

        // Return with error if result is not zero
        if (result != 0) {
            return 1;
        }

        SpecsLogs.msgInfo("Done");

        return 0;
    }

    public int execute(MatlabToCData data) {
        MatlabToCOldExecute appBody = new MatlabToCOldExecute(data);

        int result = appBody.execute();
        return result;
    }

    @Override
    public SetupDefinition getEnumKeys() {
        return SetupDefinition.create(MatlabToCSetup.class);
    }

    @Override
    public String defaultConfigFile() {
        return MatlabToCLauncher.DEFAULT_CONFIG;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.AppSource#newInstance()
     */
    @Override
    public App newInstance() {
        return new MatlabToCLauncher();
    }

    private final static String DEFAULT_CONFIG = "./configs/csum.config";

    private final static List<String> baseResourceFiles = Arrays.asList();

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.AppUsesGlobalOptions#getGlobalOptions()
     */
    @Override
    public Class<? extends SetupFieldEnum> getGlobalOptions() {
        return MatlabToCGlobalSetup.class;
    }

}