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

package org.specs.MatlabToCTester.Auxiliary;

import java.io.File;
import java.util.List;

import org.specs.MatlabToCTester.MatlabToCTesterUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class TestableFunction {

    private final File functionToTest;
    private final List<File> inputVectors;
    private final List<List<String>> testInputs;
    private final boolean hasAuxiliaryFiles;

    private List<String> inputVectorNames;

    public TestableFunction(File functionName, List<File> inputVectorNames,
            List<List<String>> inputNames, File auxiliaryFolder, boolean combinedAuxiliaryFolder) {
        this.functionToTest = functionName;
        this.inputVectors = inputVectorNames;
        this.testInputs = inputNames;

        // Check if this functions needs auxiliary files
        boolean hasAuxFiles = false;
        File testAuxFolder = MatlabToCTesterUtils.getAuxilaryFilesFolder(auxiliaryFolder, getFunctionName(),
                combinedAuxiliaryFolder);
        if (testAuxFolder.isDirectory()) {
            hasAuxFiles = true;
        }

        this.hasAuxiliaryFiles = hasAuxFiles;

        inputVectorNames = null;
    }

    /*
    public FunctionToTest(File functionName, List<File> inputVectors, File auxiliaryFolder) {
    this(functionName, inputVectors, getNullInputNames(inputVectors.size()), auxiliaryFolder);
    }
    */

    /**
     * @param size
     * @return
     */
    /*
    private static List<List<String>> getNullInputNames(int size) {
    List<List<String>> inputNames = FactoryUtils.newArrayList();
    for (int i = 0; i < size; i++) {
        inputNames.add(null);
    }
    
    return inputNames;
    }
    */

    /**
     * @return the testInputs
     */
    public List<List<String>> getTestInputs() {
        return testInputs;
    }

    /**
     * @return the inputVectorNames
     */
    public List<String> getInputVectorNames() {
        return inputVectorNames;
    }

    /**
     * @return the functionToTest
     */
    public File getFunctionToTest() {
        return functionToTest;
    }

    /**
     * @return the inputVectors
     */
    public List<File> getInputVectors() {
        return inputVectors;
    }

    public String getFunctionName() {
        return SpecsIo.removeExtension(functionToTest.getName());
    }

    public List<String> getInputNames() {
        if (inputVectorNames != null) {
            return inputVectorNames;
        }

        inputVectorNames = SpecsFactory.newArrayList();
        for (File inputVector : inputVectors) {
            inputVectorNames.add(SpecsIo.removeExtension(inputVector.getName()));
        }

        return inputVectorNames;
    }

    /**
     * @return the auxiliary
     */
    public boolean hasAuxiliaryFiles() {
        return hasAuxiliaryFiles;
    }

}
