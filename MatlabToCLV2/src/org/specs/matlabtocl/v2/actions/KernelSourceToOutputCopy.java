/**
 * Copyright 2015 SPeCS.
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

package org.specs.matlabtocl.v2.actions;

import java.io.File;

import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;

import pt.up.fe.specs.util.SpecsIo;

public class KernelSourceToOutputCopy implements PostCodeGenAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void run(File outputFolder) {
        File kernelFile = new File(outputFolder, CLCodeGenUtils.PROGRAM_SOURCE_CODE_NAME);
        String inputName = outputFolder.getName();

        if (!kernelFile.exists()) {
            return;
        }

        File parentFolder = outputFolder.getParentFile();
        String testName = parentFolder.getName();

        File outputRootFolder = parentFolder.getParentFile().getParentFile();
        File executablesFolder = new File(outputRootFolder, "cExecutables");
        File testExecutableFolder = new File(executablesFolder, testName);
        File inputExecutableFolder = new File(testExecutableFolder, inputName);
        File outputFile = new File(inputExecutableFolder, kernelFile.getName());

        SpecsIo.copy(kernelFile, outputFile);
    }

}
