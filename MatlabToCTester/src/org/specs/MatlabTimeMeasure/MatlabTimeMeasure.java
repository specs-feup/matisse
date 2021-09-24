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

package org.specs.MatlabTimeMeasure;

import static org.specs.MatlabIR.MatlabCodeUtils.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToCTester.MatlabToCTesterUtils;
import org.specs.MatlabToCTester.Auxiliary.MatlabCodeBuilder;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabTimeMeasure {

    private static final String OUTPUT_FILE = "times.csv";

    private final LanguageMode languageMode;
    private final List<File> functionsToTest;
    private final Map<String, File> inputVectorsMap;
    private final File auxiliaryBaseFolder;
    private final int executions;

    private MatlabTimeMeasure(LanguageMode languageMode, List<File> functionsToTest, Map<String, File> inputVectorsMap,
            File auxiliaryBaseFolder) {

        this.languageMode = languageMode;
        this.functionsToTest = functionsToTest;
        this.inputVectorsMap = inputVectorsMap;
        this.auxiliaryBaseFolder = auxiliaryBaseFolder;
        this.executions = 5;
    }

    public static MatlabTimeMeasure newInstance(LanguageMode languageMode, File sourceFolder, File inputVectors,
            File auxiliaryFolder) {
        // Get M-files to test
        List<File> functionsToTest = MatlabToCTesterUtils.getTests(sourceFolder, null);

        // Build input vectors map
        Map<String, File> inputVectorsMap = MatlabToCTesterUtils.getInputVectorsMap(inputVectors);

        return new MatlabTimeMeasure(languageMode, functionsToTest, inputVectorsMap, auxiliaryFolder);
    }

    public String build(File outputFolder) {

        String tests = buildTests(outputFolder);

        Replacer main = new Replacer(SpecsIo.getResource(MatisseMeasureResource.MAIN));

        main.replace("<OUTPUT_FILENAME>", OUTPUT_FILE);
        main.replace("<TESTS>", tests);
        main.replace("<EXECUTIONS>", executions);

        return main.toString();
    }

    private String buildTests(File outputFolder) {
        StringBuilder builder = new StringBuilder();

        for (File testFunction : functionsToTest) {

            // Build function call
            String functionCall = MatlabCodeBuilder.generateFunctionCall(testFunction, languageMode);

            /*
            String functionFilename = functionToTest.getName();
            String functionName = IoUtils.removeExtension(functionFilename);
            
            // Get input vectors folder
            File inputVectorsFolder = inputVectorsMap.get(functionName);
            if (inputVectorsFolder == null && runOnlyOneTest == null) {
            LoggingUtils.msgInfo("!Skipping test file '" + functionName
            	+ "': could not find corresponding input vectors folder.");
            continue;
            }
            */
            // Get input vectors for given test file
            List<File> inputVectors = MatlabToCTesterUtils.getInputVectors(testFunction, inputVectorsMap);

            for (File inputVector : inputVectors) {
                Replacer test = new Replacer(SpecsIo.getResource(MatisseMeasureResource.TEST));

                String functionName = SpecsIo.removeExtension(testFunction.getName());
                String inputVectorName = SpecsIo.removeExtension(inputVector.getName());

                String testName = functionName + "_" + inputVectorName;
                test.replace("<TEST_NAME>", testName);

                String addPaths = buildPaths(testFunction, inputVector, outputFolder, true);
                test.replace("<ADD_PATHS>", addPaths);

                String loadInput = buildLoadInput(inputVector);
                test.replace("<INPUT_VECTOR_FUNCTION>", loadInput);
                test.replace("<FUNCTION_CALL>", functionCall);

                String removePaths = buildPaths(testFunction, inputVector, outputFolder, false);
                test.replace("<REMOVE_PATHS>", removePaths);

                test.replace("<EXECUTIONS>", executions);

                builder.append(test.toString());
            }
        }

        return builder.toString();
    }

    private static String buildLoadInput(File inputVector) {
        // Check extension
        String extension = SpecsIo.getExtension(inputVector);

        // Just call .m script
        if (extension.equals("m")) {
            return SpecsIo.removeExtension(inputVector.getName());
        }

        // Load mat
        if (extension.equals("mat")) {
            return "load('" + inputVector.getName() + "')";
        }

        throw new RuntimeException("Extension not supported for input vectors: '" + extension + "'");
    }

    /*
    private String getSourcePath() {
    if (functionsToTest.isEmpty()) {
        throw new RuntimeException("There are no functions to test.");
    }
    
    // Get parent of first file
    File baseFolder = functionsToTest.get(0).getParentFile();
    
    // Check if all files are 
    
    // TODO Auto-generated method stub
    return null;
    }
    */

    private String buildPaths(File testFile, File inputVector, File outputFolder, boolean isAdd) {
        StringBuilder builder = new StringBuilder();

        String src = SpecsIo.getRelativePath(testFile.getParentFile(), outputFolder);
        String input = SpecsIo.getRelativePath(inputVector.getParentFile(), outputFolder);
        String aux = null;
        File testAuxFolder = new File(auxiliaryBaseFolder, SpecsIo.removeExtension(testFile.getName()));
        if (testAuxFolder.isDirectory()) {
            aux = SpecsIo.getRelativePath(testAuxFolder, outputFolder);
        }

        String srcCode = "";
        String inputCode = "";
        String auxCode = "";

        if (isAdd) {
            srcCode = addPath(src, "-begin");
            inputCode = addPath(input, "-begin");
            if (aux != null) {
                auxCode = addPath(aux, "-begin");
            }
        } else {
            srcCode = rmPath(src);
            inputCode = rmPath(input);
            if (aux != null) {
                auxCode = rmPath(aux);
            }
        }

        builder.append(srcCode);
        builder.append(inputCode);
        builder.append(auxCode);
        /*
        	// Source path
        	builder.append(addPath(IoUtils.getRelativePath(testFile.getParentFile(), outputFolder), "-begin"));
        	// Input vectors path
        	builder.append(addPath(IoUtils.getRelativePath(inputVector.getParentFile(), outputFolder), "-begin"));
        	// Auxiliary path
        
        	File testAuxFolder = new File(auxiliaryBaseFolder, IoUtils.removeExtension(testFile.getName()));
        	if (testAuxFolder.isDirectory()) {
        	    builder.append(addPath(IoUtils.getRelativePath(testAuxFolder, outputFolder), "-begin"));
        	}
        */
        return builder.toString();
    }
}
