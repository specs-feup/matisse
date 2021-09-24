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

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToCTester.Auxiliary.CompilerOptions;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;
import org.specs.MatlabToCTester.Auxiliary.MatlabOptions;
import org.specs.MatlabToCTester.Auxiliary.OutputFolders;
import org.specs.MatlabToCTester.Auxiliary.TesterOptions;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.guihelper.BaseTypes.SetupData;

/**
 * Data fields for MatlabToCTester.
 * 
 * @author Pedro Pinto
 */
public class MatlabToCTesterData {

    public final LanguageMode languageMode;

    /* The input folders */
    public final InputFolders inputFolders;

    /* The output folders */
    public final OutputFolders outputFolders;

    /* Options of the tester */
    public final TesterOptions testerOptions;

    /* The compiler options */
    public final CompilerOptions compilerOptions;

    /* Options for the main() function */
    public final SetupData mainOptionsData;

    /* MATLAB options */
    public final MatlabOptions matlabOptions;

    /* Other variables */
    public final boolean deleteOutputContents;
    public final SetupData implementationData;

    public final boolean generateDataFiles;

    private final DataStore settings;

    /**
     * 
     * @param inputFolders
     * @param outputFolders
     * @param compilerOptions
     * @param matlabOptions
     * @param deleteOutputContents
     * @param implementationData
     */
    public MatlabToCTesterData(LanguageMode languageMode,
            InputFolders inputFolders,
            OutputFolders outputFolders,
            TesterOptions testerOptions,
            CompilerOptions compilerOptions,
            SetupData mainOptionsData,
            MatlabOptions matlabOptions,
            boolean deleteOutputContents,
            SetupData implementationData,
            boolean generateDataFiles,
            DataStore settings) {

        this.languageMode = languageMode;
        this.inputFolders = inputFolders;
        this.outputFolders = outputFolders;
        this.testerOptions = testerOptions;
        this.compilerOptions = compilerOptions;
        this.mainOptionsData = mainOptionsData;
        this.matlabOptions = matlabOptions;
        this.deleteOutputContents = deleteOutputContents;
        this.implementationData = implementationData;
        this.generateDataFiles = generateDataFiles;
        this.settings = settings;

    }

    public DataStore getSettings() {
        return settings;
    }

}
