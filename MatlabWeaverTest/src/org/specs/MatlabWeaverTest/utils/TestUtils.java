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

package org.specs.MatlabWeaverTest.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.matisse.weaver.gui.MWeaverOption;
import pt.up.fe.specs.matisse.weaver.gui.MatlabWeaver;
import pt.up.fe.specs.matisse.weaver.gui.VerboseLevel;
import pt.up.fe.specs.matisse.weaver.gui.WeaverMode;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * @author Joao Bispo
 * 
 */
public class TestUtils {

    private static final String WORK_FOLDER = "output";
    private static final String TEST_FOLDER = "test";
    private static final String LARA_FILE = "test.lara";

    // private final File workspace;

    /**
     * @param subband
     * @param typesSubbandDouble
     * @return
     */
    public boolean runMatlabToMatlab(ResourceProvider matlab, ResourceProvider aspect) {

        SpecsLogs.msgInfo("Running '" + matlab.getResource() + "' + " + aspect.getResource());

        // Get folder
        File testFolder = SpecsIo.mkdir(TestUtils.TEST_FOLDER);

        // Clean it
        SpecsIo.deleteFolderContents(testFolder);

        // Create MATLAB file
        String matlabContents = SpecsIo.getResource(matlab);
        // String functionName = MatlabParseUtils.parseFunctionName(matlabContents);
        String functionName = MatlabProcessorUtils.getFunctionName(matlabContents, LanguageMode.MATLAB).get();
        File matlabFile = new File(testFolder, functionName + ".m");
        SpecsIo.write(matlabFile, matlabContents);

        // Create LARA file
        String laraContents = SpecsIo.getResource(aspect);
        File laraFile = new File(testFolder, TestUtils.LARA_FILE);
        SpecsIo.write(laraFile, laraContents);

        boolean verbose = false;

        MWeaverUtils.runMWeaver(LanguageMode.MATLAB, Arrays.asList(matlabFile), laraFile, new ArrayList<File>(),
                testFolder,
                MatlabToCOptionUtils.newDefaultSettings(), verbose);

        return true;

    }

    /*
    public static boolean runMWeaverGui(ResourceProvider lara, ResourceProvider matlab) {
    return runMWeaverGui(lara, Arrays.asList(matlab));
    }
    */

    public static void test(MWeaverResource resource) {
        SpecsLogs.msgInfo("Running '" + resource + "'");
        File log = TestUtils.runMWeaverGui(resource.getLara(), resource.getMatlab());

        assertEquals(StringLines.getLines(resource.getResult()), StringLines.getLines(log));
    }

    /**
     * 
     * @param lara
     * @param matlab
     * @return the log file
     */
    public static File runMWeaverGui(ResourceProvider lara, List<ResourceProvider> matlab) {
        // Prepare folder
        File workFolder = SpecsIo.mkdir(TestUtils.WORK_FOLDER);
        SpecsIo.deleteFolderContents(workFolder);

        // Prepare files
        matlab.forEach(resource -> resource.write(workFolder));
        File laraFile = lara.write(workFolder);

        DataStore data = DataStore.newInstance("MatlabWeaverTest");
        data.add(MWeaverOption.INPUT_FILE, laraFile);
        data.add(MWeaverOption.OUTPUT, workFolder);
        data.add(MWeaverOption.WORKSPACE, workFolder);
        data.add(MWeaverOption.WEAVER_MODE, WeaverMode.JAVA);
        data.add(MWeaverOption.VERBOSE_LEVEL, VerboseLevel.WARNING);
        data.add(MWeaverOption.LOG, Boolean.TRUE);
        data.add(MWeaverOption.LOG_FILENAME, getWeaverLog().getPath());

        MatlabWeaver matlabWeaver = new MatlabWeaver(data);
        matlabWeaver.execute();

        // Return true if result is 0
        // return result == 0 ? true : false;

        return getWeaverLog();
    }

    public static File getWorkFolder() {
        return SpecsIo.mkdir(TestUtils.WORK_FOLDER);
    }

    public static File getWeaverLog() {
        return new File(getWorkFolder(), "test.log");
    }

    public static void deleteTestFolder() {
        File testFolder = new File(TestUtils.TEST_FOLDER);

        SpecsIo.deleteFolderContents(testFolder);
        testFolder.delete();
    }

}
