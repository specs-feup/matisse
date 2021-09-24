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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.Matisse.MatisseUtils;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.Program.Global.MatlabToCGlobalData;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matisselib.MatisseLibOption;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.CMainFunction.Builder.TestMainSetup;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Utils.SetupFile;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupDefinition;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice;
import pt.up.fe.specs.guihelper.SetupFieldOptions.SingleSetup;
import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.collections.HashSetString;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Setup definition for program MatlabToC.
 * 
 * @author Joao Bispo
 */
public enum MatlabToCSetup implements SetupFieldEnum, DefaultValue, SingleSetup, MultipleChoice {

    Language(FieldType.multipleChoice),
    MFilesFolder(FieldType.string),
    AspectsFolder(FieldType.string),

    MFiles(FieldType.stringList),
    TopLevelMFiles(FieldType.stringList),
    Aspects(FieldType.stringList),
    AspectDataFile(FieldType.string),
    IgnoreImplementationSettings(FieldType.bool),
    UsePassSystem(FieldType.bool),
    // ResetAspectsBeforeEachRun(FieldType.bool),

    OutputFolder(FieldType.string),
    DataFolder(FieldType.string),
    GenerateSingleFile(FieldType.string),
    PutMainInFunction(FieldType.string),
    MFileWithInputVectors(FieldType.string),
    DeleteOutputFolderContents(FieldType.bool),
    GenerateDataFiles(FieldType.bool),
    ShowCTree(FieldType.bool),
    ShowInfoMessages(FieldType.bool),
    DetectMissingFunctions(FieldType.bool),
    OnlineVersionHack(FieldType.bool),
    OptionsForMainFunction(FieldType.integratedSetup),
    ImplementationSettings(FieldType.integratedSetup),
    Optimizations(FieldType.multipleChoiceStringList),
    EnableZ3(FieldType.bool),
    SuppressPrinting(FieldType.bool),
    CustomPreTypeSsaRecipeFile(FieldType.string),
    CustomRecipeFile(FieldType.string);

    public static MatlabToCData newData(SetupData setupData, MatlabToCGlobalData globalData) {
        return newData(setupData, globalData, null);
    }

    public static MatlabToCData newData(SetupData setupData, MatlabToCGlobalData globalData,
            DataStore generalSetup) {

        SetupAccess setup = new SetupAccess(setupData);

        // Check if there is an AspectData file
        File aspectDataFile = setup.getFile(null, AspectDataFile, false);

        // IF file exists, perform changes in SetupData
        if (aspectDataFile.isFile()) {
            generalSetup = MatisseUtils.getPersistence().loadData(aspectDataFile);
        }

        // If aspect setup is still null, create default setup
        if (generalSetup == null) {
            // LoggingUtils.msgInfo("AspectData file not found, using default values");
            SetupFile setupFile = new SetupFile();
            setupFile.setFile(aspectDataFile);
            generalSetup = DataStore.newInstance("Default MatlabToC Options");
            // generalSetup.setSetupFile(setupFile);
            // aspectSetup = new SimpleSetup("Default MatlabToC Options", new OptionTable(), setupFile);
        }

        LanguageMode languageMode = setup.getEnum(Language, LanguageMode.class);

        // Get base folder for M-files. It can be null
        File mfilesFolder = null;
        if (!setup.getString(MFilesFolder).isEmpty()) {
            mfilesFolder = setup.getFolderV2(globalData.baseInputFolder, MFilesFolder, true);
        }

        // Get the names of M-files
        StringList mFileNames = setup.getStringList(MFiles);

        // Get M-files
        List<File> mFiles = getFiles(mfilesFolder, mFileNames.getStringList(), MatlabToCUtils.getMfileExtension());
        mFiles = Collections.unmodifiableList(mFiles);

        // ONLINE VERSION HACK
        boolean onlineVersionHack = setup.getBoolean(OnlineVersionHack);
        if (onlineVersionHack) {

            List<File> newMFiles = SpecsFactory.newArrayList();

            // Read M-files and rewrite them to files with the right name
            for (File mFile : mFiles) {
                // Read function
                String mFileContents = SpecsIo.read(mFile);

                // FileNode token = MatlabProcessorUtils.fromMFile(mFileContents, null);
                FileNode token = new MatlabParser().parse(mFile);

                Optional<FunctionNode> functionTry = token.getMainFunctionTry();
                if (!functionTry.isPresent()) {
                    continue;
                }

                FunctionNode functionNode = functionTry.get();

                // MatlabNode functionToken = MatlabTokenUtils.getFirstToken(MType.Function, token.getChildren());
                // MatlabNode functionDeclaration = StatementUtils.getFirstToken(MStatementType.FunctionDeclaration,
                // functionToken.getChildren());

                // MatlabNode identifier = StatementAccess.getFunctionDeclarationIdentifier(functionDeclaration);

                // String functionName = MatlabTokenContent.getIdentifierName(identifier);
                String functionName = functionNode.getFunctionName();

                File folder = mFile.getParentFile();
                File newMFile = new File(folder, functionName + ".m");
                SpecsIo.write(newMFile, mFileContents);

                newMFiles.add(newMFile);
            }

            mFiles = newMFiles;
        }

        // Get the names of M-files to be considered as top level
        List<String> topLevelMFiles = setup.getListOfStrings(TopLevelMFiles);

        // Get base folder for M-files. It can be null
        File aspectsFolder = setup.getExistingPath(globalData.baseInputFolder, AspectsFolder);

        // Get the names of aspect files
        StringList aspectsNames = setup.getStringList(Aspects);

        // Get aspect files
        List<File> aspectFiles = SpecsFactory.newArrayList();
        if (aspectsFolder.isFile()) {
            aspectFiles.add(aspectsFolder);
        } else {
            aspectFiles.addAll(getFiles(aspectsFolder, aspectsNames.getStringList(),
                    MatlabToCUtils.getAspectExtension()));
        }

        aspectFiles = Collections.unmodifiableList(aspectFiles);

        File customPreTypeSsaRecipeFile = setup.getFile(null, CustomPreTypeSsaRecipeFile, false);
        File customRecipeFile = setup.getFile(null, CustomRecipeFile, false);

        // boolean resetAspectsBeforeEachRun = setup.getBoolean(ResetAspectsBeforeEachRun);

        // File outputFolder = setup.getFolder(globalData.baseOutputFolder, OutputFolder);
        File outputFolder = setup.getFolderV2(globalData.baseOutputFolder, OutputFolder, false);
        if (outputFolder == null) {
            return null;
        }

        boolean deleteOutputFolderContents = setup.getBoolean(DeleteOutputFolderContents);

        // If automatic delete is active, adds 'output' to output folder, for
        // safety
        if (deleteOutputFolderContents) {
            outputFolder = new File(outputFolder, "output");
        }

        File dataFolder = setup.getFolderV2(DataFolder, false);

        String generateSingleFile = setup.getString(GenerateSingleFile).trim();
        String functionToPutMain = setup.getString(PutMainInFunction);

        TestMainOptions mainOptions = TestMainSetup.newData(setup.getSetup(OptionsForMainFunction));

        // Check if empty
        File mInputVectors = null;
        String mInputVectorsPath = setup.getString(MFileWithInputVectors);
        if (!mInputVectorsPath.trim().isEmpty()) {
            mInputVectors = SpecsIo.getFile(globalData.baseInputFolder, mInputVectorsPath);
        }

        boolean showCTree = setup.getBoolean(ShowCTree);
        boolean showInfoMessages = setup.getBoolean(ShowInfoMessages);
        boolean detectMissingFunctions = setup.getBoolean(DetectMissingFunctions);

        SetupData implSettingsData = setup.getSetup(ImplementationSettings);
        ImplementationSettings implSettings = ImplementationSetup.newData(implSettingsData);

        // Apply FunctionSettings to Setup
        if (!setup.getBoolean(IgnoreImplementationSettings)) {
            implSettings.set(generalSetup);

            StringList optNames = setup.getStringList(Optimizations);
            addOptimizations(generalSetup, optNames);
        }

        // Add 'use new path'
        // if (!generalSetup.hasValue(MatlabToCKeys.USE_PASS_SYSTEM)) {
        // generalSetup.set(MatlabToCKeys.USE_PASS_SYSTEM, setup.getBoolean(UsePassSystem));
        // }
        // if (!generalSetup.hasValue(MatlabToCKeys.ENABLE_Z3)) {
        // generalSetup.set(MatlabToCKeys.ENABLE_Z3, setup.getBoolean(EnableZ3));
        // }
        generalSetup.setIfNotPresent(MatlabToCKeys.USE_PASS_SYSTEM, setup.getBoolean(UsePassSystem));
        generalSetup.setIfNotPresent(MatlabToCKeys.ENABLE_Z3, setup.getBoolean(EnableZ3));
        generalSetup.setIfNotPresent(MatisseLibOption.SUPPRESS_PRINTING, setup.getBoolean(SuppressPrinting));

        boolean generateDataFiles = setup.getBoolean(GenerateDataFiles);

        return new MatlabToCData(languageMode,
                mFiles,
                topLevelMFiles,
                aspectFiles,
                customPreTypeSsaRecipeFile,
                customRecipeFile,
                outputFolder,
                dataFolder,
                deleteOutputFolderContents,
                generateSingleFile,
                functionToPutMain,
                mainOptions,
                mInputVectors,
                showCTree,
                showInfoMessages,
                detectMissingFunctions,
                generateDataFiles,
                generalSetup);
    }

    private static void addOptimizations(DataStore aspectSetup, StringList optNames) {
        // Create HashSetString
        HashSetString set = new HashSetString(optNames.getStringList());
        aspectSetup.set(MatlabToCKeys.MATISSE_OPTIMIZATIONS, set);
    }

    /**
     * If foldername is null, returns null.<br>
     * If foldername is empty, returns null.<br>
     * Otherwise, applies safeFolder over baseFolder + foldername
     * 
     * @param baseInputFolder
     * @param mfilesFoldername
     * @return
     */
    /*
    private static File getFolder(File baseFolder, String foldername) {
    if (foldername == null) {
        return null;
    }
    
    if (foldername.isEmpty()) {
        return null;
    }
    
    return IoUtils.safeFolder(baseFolder, foldername);
    }
    */

    /**
     * @param extension
     * @param mfilesFolder2
     * @param stringList
     * @return
     */
    private static List<File> getFiles(File baseFolder, List<String> filenames, String extension) {
        // If list of filenames is empty, and base folder is not null, add all
        // files in base folder, by name
        if (filenames.isEmpty() && (baseFolder != null)) {
            List<File> files = SpecsIo.getFiles(baseFolder, extension);
            for (File file : files) {
                filenames.add(file.getName());
            }
        }

        // Build files with full path
        List<File> files = SpecsFactory.newArrayList();
        for (String fileName : filenames) {
            File file = new File(baseFolder, fileName);
            files.add(file);
        }

        return files;
    }

    private MatlabToCSetup(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
        return fieldType;
    }

    @Override
    public String getSetupName() {
        return "MatlabToC";
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {
        switch (this) {
        case Language:
            return FieldValue.create(LanguageMode.MATLAB.name(), FieldType.multipleChoice);
        case GenerateDataFiles:
            return FieldValue.create(true, FieldType.bool);
        case MFilesFolder:
            return FieldValue.create("./", FieldType.string);
        case AspectsFolder:
            return FieldValue.create("./", FieldType.string);
        case OutputFolder:
            return FieldValue.create("./matlabToC_output", FieldType.string);
        case DataFolder:
            return FieldValue.create("./matlabToC_data", FieldType.string);
        case DetectMissingFunctions:
            return FieldValue.create(Boolean.TRUE, FieldType.bool);
        case ShowInfoMessages:
            return FieldValue.create(Boolean.TRUE, FieldType.bool);
        default:
            return null;
        }
    }

    @Override
    public SetupDefinition getSetupOptions() {
        if (this == ImplementationSettings) {
            return SetupDefinition.create(ImplementationSetup.class);
        }

        if (this == OptionsForMainFunction) {
            return SetupDefinition.create(TestMainSetup.class);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        if (this == PutMainInFunction) {
            return "Put main()InFuntion";
        }

        if (this == TopLevelMFiles) {
            return "TopLevelMFiles (use '" + MatlabToCOldExecute.ALL_FILES + "' for all)";
        }

        return super.toString();
    }

    @Override
    public StringList getChoices() {
        if (this == Language) {
            return new StringList(SpecsEnums.buildList(LanguageMode.values()));
        }

        if (this == Optimizations) {
            return new StringList(SpecsEnums.buildList(MatisseOptimization.values()));
        }

        return null;
    }

}
