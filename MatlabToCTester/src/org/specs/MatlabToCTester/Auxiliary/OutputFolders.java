/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToCTester.Auxiliary;

import java.io.File;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class OutputFolders {

    private final static String MATLAB_SCRIPTS_FOLDERNAME = "matlabScripts";
    private final static String C_OUTPUTS_FOLDERNAME = "cOutputs";
    private final static String C_SOURCES_FOLDERNAME = "cSourceTestFiles";
    private final static String C_EXECUTABLES_FOLDERNAME = "cExecutables";
    private final static String WEAVER_WORKSPACE = "matlabWeaver";

    // If defined (not an empty string), indicates the single file where the code should be.
    private final String singleCFile;
    // General output directory
    private final File baseOutputFolder;
    // The directory of the matlab scripts
    private final File matlabScripts;
    // The directory of the output files
    private final File cOutputs;
    private final File cSources;
    // The directory of the executable files (compiled tests)
    private final File cExecutables;
    // The directory of the woven MATLAB files
    private final File weaverWorkspace;

    /**
     * @param matlabScripts
     * @param cOutputs
     * @param cSources
     * @param cExecutables
     */
    private OutputFolders(String singleCFile, File baseOutputFolder, File matlabScripts, File cOutputs, File cSources,
            File cExecutables, File weaverWorkspace) {

        this.singleCFile = singleCFile;
        this.baseOutputFolder = baseOutputFolder;
        this.matlabScripts = matlabScripts;
        this.cOutputs = cOutputs;
        this.cSources = cSources;
        this.cExecutables = cExecutables;
        this.weaverWorkspace = weaverWorkspace;
    }

    public static OutputFolders newInstance(File outputFolder, String singleCFile) {

        File matlabScripts = getFolder(outputFolder, MATLAB_SCRIPTS_FOLDERNAME);
        File cOutputs = getFolder(outputFolder, C_OUTPUTS_FOLDERNAME);
        File cSources = getFolder(outputFolder, C_SOURCES_FOLDERNAME);
        File cExecutables = getFolder(outputFolder, C_EXECUTABLES_FOLDERNAME);
        File weaverWorkspace = getFolder(outputFolder, WEAVER_WORKSPACE);

        return new OutputFolders(singleCFile, outputFolder, matlabScripts, cOutputs, cSources, cExecutables,
                weaverWorkspace);
    }

    /**
     * Tries to create a subfolder, if fails throws an exception.
     * 
     * @param outputFolderBase
     * @param subfoldername
     * @return
     */
    private static File getFolder(File outputFolder, String subfoldername) {
        File subfolder = SpecsIo.mkdir(outputFolder, subfoldername);
        if (subfolder != null) {
            return subfolder;
        }

        throw new RuntimeException("Could not create folder.");
    }

    private static File getFolder(File folder) {
        if (folder.isDirectory()) {
            return folder;
        }

        // If not folder, try to create it
        File newFolder = SpecsIo.mkdir(folder);
        if (newFolder != null) {
            return newFolder;
        }

        throw new RuntimeException("Could not create folder '" + folder.getPath() + "'.");
    }

    /**
     * @return the baseOutputFolder
     */
    public File getBaseOutputFolder() {
        return getFolder(baseOutputFolder);
    }

    /**
     * @return the cExecutables
     */
    public File getcExecutablesFolder() {
        return getFolder(cExecutables);
    }

    /**
     * @return the cOutputs
     */
    public File getcOutputsFolder() {
        return getFolder(cOutputs);
    }

    /**
     * @return the cSources
     */
    public File getcSourcesFolder() {
        return getFolder(cSources);
    }

    /**
     * @return the weaverWorkspace
     */
    public File getWeaverWorkspace() {
        return getFolder(weaverWorkspace);
    }

    /**
     * @return the matlabScripts
     */
    public File getMatlabScriptsFolder() {
        return getFolder(matlabScripts);
    }

    /**
     * @return the matlabScriptsFoldername
     */
    public static String getMatlabScriptsFoldername() {
        return MATLAB_SCRIPTS_FOLDERNAME;
    }

    /**
     * @return the singleCFile
     */
    public String getSingleCFile() {
        return singleCFile;
    }
}
