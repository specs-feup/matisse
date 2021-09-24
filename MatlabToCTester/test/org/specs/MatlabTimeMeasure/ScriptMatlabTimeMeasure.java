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

import java.io.File;

import org.junit.Test;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

public class ScriptMatlabTimeMeasure {

    @Test
    public void test() {
        SpecsSystem.programStandardInit();
        /*
        	File baseFolder = new File("D:\\Dropbox\\MatlabCompiler\\Benchmarks\\CodeInPubs\\ARRAY2014\\matlab");
        	String mainName = "main_preallocated";
        
        	File sourceFolder = new File(baseFolder, "src_matlab_preallocated");
        	File inputsFolder = new File(baseFolder, "input_vectors_matlab");
        	File auxFolder = new File(baseFolder, "auxi");
        */
        File baseFolder = new File(
                "C:\\Users\\JoaoBispo\\Desktop\\13-dynamic-matlab");
        String mainName = "matlab_measure";

        File sourceFolder = new File(baseFolder, "src");
        File inputsFolder = new File(baseFolder, "inputs");
        File auxFolder = new File(baseFolder, "auxi");

        File outputFolder = baseFolder;

        MatlabTimeMeasure timeMeasure = MatlabTimeMeasure.newInstance(LanguageMode.MATLAB, sourceFolder, inputsFolder,
                auxFolder);

        SpecsIo.write(new File(outputFolder, mainName + ".m"), timeMeasure.build(outputFolder));
        // System.out.println(timeMeasure.build(outputFolder));
    }
}
