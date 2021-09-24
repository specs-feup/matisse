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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabToC.MatlabToCUtils;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class InputFolders {

    // The directory where the matlab input files are located
    private final File inputVectorsFolder;
    /* Files that are copied to the output directory */
    public final File resourceFolder;
    // The directory of the matlab source files
    private final File testsFolder;
    // The directory where the auxiliary files for each function test are
    private final File auxiliaryFolder;
    // The directories with the additional aspect files.
    private final List<File> extraLaraFolders;

    // The directory with output vectors (optional)
    private final File outputVectorsFolder;

    // The directory where the aspect files are
    private final File aspectsFolder;

    public InputFolders(File testsFolder,
            File inputVectorsFolder,
            File resourceFolder,
            File aspectsFolder,
            List<File> extraLaraFolders,
            File auxiliaryFolder, File outputVectorsFolder) {

        this.inputVectorsFolder = inputVectorsFolder;
        this.resourceFolder = resourceFolder;
        this.testsFolder = testsFolder;
        this.auxiliaryFolder = auxiliaryFolder;
        this.extraLaraFolders = new ArrayList<>(extraLaraFolders);
        this.aspectsFolder = aspectsFolder;
        this.outputVectorsFolder = outputVectorsFolder;
    }

    /**
     * @return the aspectsFolder
     */
    // public File getAspectsFiles() {
    public List<File> getAspectsFiles() {
        if (aspectsFolder.isFile()) {
            return SpecsFactory.newArrayList(Arrays.asList(aspectsFolder));
        }

        return MatlabToCUtils.getAspectFiles(aspectsFolder);

        // return aspectsFolder;
    }

    /**
     * @return the aspectsFolder
     */
    public File getAspectsFolder() {
        return aspectsFolder;
    }

    /**
     * @return the auxiliaryFolder
     */
    public File getAuxiliaryFolder() {
        return auxiliaryFolder;
    }

    /**
     * @return the inputVectorsFolder
     */
    public File getInputVectorsFolder() {
        return inputVectorsFolder;
    }

    public File getResourceFolder() {
        return resourceFolder;
    }

    public File getOutputVectorsFolder() {
        return outputVectorsFolder;
    }

    /**
     * @return the testsFolder
     */
    public File getTestsFolder() {
        return testsFolder;
    }

    public List<File> getExtraLaraFolders() {
        return extraLaraFolders;
    }

}
