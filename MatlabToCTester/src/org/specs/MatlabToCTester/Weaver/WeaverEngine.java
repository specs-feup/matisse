/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToCTester.Weaver;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.MatlabToCTester.MatlabToCTesterData;
import org.specs.MatlabToCTester.MatlabToCTesterUtils;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pt.up.fe.specs.matisse.weaver.MWeaverRun;
import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 *
 */
public class WeaverEngine {

    private final MatlabToCTesterData data;

    public WeaverEngine(MatlabToCTesterData data) {
        this.data = data;
    }

    /**
     * @return
     *
     */
    public InputFolders execute() {
        // If no LARA file specified, return original InputFolders
        Optional<File> laraFile = getLaraFile();
        if (!laraFile.isPresent()) {
            return data.inputFolders;
        }

        File workspace = data.outputFolders.getWeaverWorkspace();

        List<MJob> matlabJobs = getMatlabJobs();

        // Use folder of aspect file as an include folder
        List<File> includeFolders = Lists.newArrayList(laraFile.get().getParentFile());
        includeFolders.addAll(data.inputFolders.getExtraLaraFolders());

        File weavedSrc = new File(workspace, "srcWeaved");
        File weavedAuxi = new File(workspace, "auxiWeaved");

        // TODO: MWeaver now supports the joinpoint app, try to run it only once
        // Run an instance of MWeaver for each test
        for (MJob mjob : matlabJobs) {
            SpecsLogs.msgInfo("Weaving '" + SpecsIo.removeExtension(mjob.getTestFile().getName()) + "'");

            List<File> matlabFiles = Lists.newArrayList();
            matlabFiles.add(mjob.getTestFile());
            if (mjob.auxFolder.isPresent()) {
                matlabFiles.addAll(SpecsIo.getFilesRecursive(mjob.getAuxFolder().get(), "m"));
            }

            MWeaverRun run = MWeaverUtils.runMWeaver(data.languageMode, matlabFiles, laraFile.get(), includeFolders,
                    workspace, data.getSettings(), false);

            // Merge options
            // data.generalSetup.getOptionTable().setOptions(run.getSetup().getOptionTable());
            data.getSettings().set(run.getSettings());

            // Recreate folder structure
            String testName = mjob.testFile.getName();
            String simpleName = SpecsIo.removeExtension(testName);
            File auxiFolder = new File(weavedAuxi, simpleName);

            for (File mFile : run.getMatlabFiles()) {
                // Get weaved file
                File weavedFile = new File(run.getOutputFolder(), mFile.getName());

                // If test file, put in src folder
                if (mFile.getName().equals(testName)) {
                    // Copy to source
                    // IoUtils.copy(mFile, new File(weavedSrc, mFile.getName()));
                    // Copy weaved file
                    SpecsIo.copy(weavedFile, new File(weavedSrc, mFile.getName()));
                }
                // Otherwise, goes to respective auxiliary folder
                else {
                    // TODO: Copying the original files, check how to copy the weaved files (where does the weaver put
                    // the auxiliary files? Does it change them?)
                    // TODO: Check if it is possible to support relative paths (e.g., auxi folder having more than one
                    // level of folders
                    // Copy weaved file

                    // IoUtils.copy(mFile, new File(auxiFolder, mFile.getName()));
                    SpecsIo.copy(weavedFile, new File(auxiFolder, mFile.getName()));
                }

            }
        }

        // Recreate folder structure
        return new InputFolders(weavedSrc, data.inputFolders.getInputVectorsFolder(),
                data.inputFolders.getResourceFolder(),
                data.inputFolders.getAspectsFolder(), data.inputFolders.getExtraLaraFolders(), weavedAuxi, null);

    }

    private List<MJob> getMatlabJobs() {
        String singleTest = data.testerOptions.getRunOnlyOneTest();

        List<MJob> matlabJobs = Lists.newArrayList();

        // If no single test defined, get all M-files
        if (singleTest == null || singleTest.isEmpty()) {

            List<File> matlabTests = SpecsIo.getFilesRecursive(data.inputFolders.getTestsFolder(), "m");

            // Build a map with the auxiliary folder names
            List<File> auxiFolders = SpecsIo.getFolders(data.inputFolders.getAuxiliaryFolder());
            Map<String, File> auxiMap = Maps.newHashMap();
            auxiFolders.forEach(folder -> auxiMap.put(folder.getName(), folder));

            // For each test, build a job
            for (File matlabTest : matlabTests) {
                // Check if there is an auxiliary folder
                File auxiFolder = auxiMap.get(SpecsIo.removeExtension(matlabTest.getName()));

                if (auxiFolder == null) {
                    matlabJobs.add(new MJob(matlabTest));
                } else {
                    matlabJobs.add(new MJob(matlabTest, auxiFolder));
                }

            }
        }

        // Return M-files related to the test
        File testFile = new File(data.inputFolders.getTestsFolder(), singleTest + ".m");
        if (!testFile.isFile()) {
            SpecsLogs.msgInfo(" - Could not find test with name '" + singleTest + "'");
            return matlabJobs;
        }

        // Add auxiliary files, if present
        File auxiFolder = MatlabToCTesterUtils.getAuxilaryFilesFolder(data.inputFolders.getAuxiliaryFolder(),
                singleTest, data.testerOptions.isCombinedAuxiliaryFolders());
        if (auxiFolder.isDirectory()) {
            matlabJobs.add(new MJob(testFile, auxiFolder));
        } else {
            matlabJobs.add(new MJob(testFile));
        }

        return matlabJobs;
    }

    /**
     * @return
     */
    /*
    private List<File> getMatlabFiles() {
    String singleTest = data.testerOptions.getRunOnlyOneTest();
    
    List<File> matlabFiles = Lists.newArrayList();
    
    // If no single test defined, get all M-files
    if (singleTest.isEmpty()) {
    
        matlabFiles.addAll(IoUtils.getFilesRecursive(data.inputFolders.getTestsFolder(), "m"));
        matlabFiles.addAll(IoUtils.getFilesRecursive(data.inputFolders.getAuxiliaryFolder(), "m"));
    
        return matlabFiles;
    }
    
    // Return M-files related to the test
    File testFile = new File(data.inputFolders.getTestsFolder(), singleTest + ".m");
    if (!testFile.isFile()) {
        LoggingUtils.msgInfo(" - Could not find test with name '" + singleTest + "'");
        return matlabFiles;
    }
    
    matlabFiles.add(testFile);
    
    // Add auxiliary files, if present
    File auxiFolder = new File(data.inputFolders.getAuxiliaryFolder(), singleTest);
    if (auxiFolder.isDirectory()) {
        matlabFiles.addAll(IoUtils.getFilesRecursive(auxiFolder, "m"));
    }
    
    return matlabFiles;
    }
    */
    /**
     * @return
     */
    private Optional<File> getLaraFile() {
        if (data.inputFolders.getAspectsFiles().size() != 1) {
            return Optional.empty();
        }

        File laraFile = data.inputFolders.getAspectsFiles().get(0);
        if (!SpecsIo.getExtension(laraFile).toLowerCase().equals("lara")) {
            return Optional.empty();
        }

        // List<File> matlabFiles, File aspectFile, List<File> includeFolders,
        // File outputFolder, boolean verbos

        return Optional.of(laraFile);
    }

    class MJob {
        private final File testFile;
        private final Optional<File> auxFolder;

        public MJob(File testFile, File auxFolder) {
            this.testFile = testFile;
            this.auxFolder = Optional.of(auxFolder);
        }

        public MJob(File testFile) {
            this.testFile = testFile;
            auxFolder = Optional.empty();
        }

        /**
         * @return the auxFolder
         */
        public Optional<File> getAuxFolder() {
            return auxFolder;
        }

        /**
         * @return the testFile
         */
        public File getTestFile() {
            return testFile;
        }

    }
}
