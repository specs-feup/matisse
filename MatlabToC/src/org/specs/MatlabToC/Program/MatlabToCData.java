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
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Data fields for MatlabToC.
 * 
 * @author Joao Bispo
 */
public class MatlabToCData {

    public final LanguageMode languageMode;
    public final List<File> matlabFiles;
    public final List<String> topLevelMFiles;
    public final List<File> aspectFiles;
    public final File customPreTypeSsaRecipeFile;
    public final File customRecipeFile;
    public final File outputFolder;
    public final File dataFolder;
    public final boolean deleteOutputFolderContents;
    public final String generateSingleFile;
    public final String functionToPutMain;
    public final TestMainOptions mainOptions;
    public final File mInputVectors;
    public final boolean showCTree;
    public final boolean showInfoMessages;
    public final boolean detectMissingFunctions;
    public final boolean generateDataFiles;
    private final DataStore settings;

    /**
     * @param matlabFiles
     * @param topLevelMFiles
     * @param aspectFiles
     * @param outputFolder
     * @param deleteOutputFolderContents
     * @param functionToPutMain
     * @param simplePrintfInMain
     * @param functionToPutMain2
     * @param mInputVectors
     * @param showCTree
     * @param detectMissingFunctions
     * @param implementationSettings
     */
    public MatlabToCData(LanguageMode languageMode,
            List<File> matlabFiles,
            List<String> topLevelMFiles,
            List<File> aspectFiles,
            File customPreTypeSsaRecipeFile,
            File customRecipeFile,
            File outputFolder,
            File dataFolder,
            boolean deleteOutputFolderContents,
            String generateSingleFile,
            String functionToPutMain,
            TestMainOptions mainOptions,
            File mInputVectors,
            boolean showCTree,
            boolean showInfoMessages,
            boolean detectMissingFunctions,
            boolean generateDataFiles,
            DataStore settings) {

        this.languageMode = languageMode;
        this.matlabFiles = matlabFiles;
        this.topLevelMFiles = topLevelMFiles;
        this.aspectFiles = aspectFiles;
        this.customPreTypeSsaRecipeFile = customPreTypeSsaRecipeFile;
        this.customRecipeFile = customRecipeFile;
        // this.resetAspectsBeforEachRun = resetAspectsBeforEachRun;
        this.outputFolder = outputFolder;
        this.dataFolder = dataFolder;
        this.deleteOutputFolderContents = deleteOutputFolderContents;
        this.generateSingleFile = generateSingleFile;
        this.functionToPutMain = functionToPutMain;
        this.mainOptions = mainOptions;
        // this.returnFromMainWithOutputVariables = simplePrintfInMain;
        this.mInputVectors = mInputVectors;
        this.showCTree = showCTree;
        this.showInfoMessages = showInfoMessages;
        this.detectMissingFunctions = detectMissingFunctions;
        this.generateDataFiles = generateDataFiles;

        this.settings = settings;
    }

    public DataStore getSettings() {
        return settings;
    }

}
