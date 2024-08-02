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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.Program.ImplementationSettings;
import org.specs.MatlabToC.Program.ImplementationSetup;
import org.specs.MatlabToC.jOptions.IvdepType;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToCTester.Auxiliary.CompilerOptions;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;
import org.specs.MatlabToCTester.Auxiliary.MatlabOptions;
import org.specs.MatlabToCTester.Auxiliary.OptimizationLevel;
import org.specs.MatlabToCTester.Auxiliary.OutputFolders;
import org.specs.MatlabToCTester.Auxiliary.TesterOptions;
import org.specs.MatlabToCTester.Auxiliary.TesterState;
import org.specs.MatlabToCTester.CGeneration.CGenerator;
import org.specs.matisselib.MatisseLibOption;
import org.suikasoft.CMainFunction.Builder.TestMainSetup;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.persistence.XmlPersistence;

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
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Setup definition for program MatlabToCTester.
 * 
 * @author Pedro Pinto
 */
public enum MatlabToCTesterSetup implements SetupFieldEnum, MultipleChoice, SingleSetup, DefaultValue {

    Language(FieldType.multipleChoice),
    MatlabInputFilesDirectory(FieldType.folder),
    MatlabOutputFilesDirectory(FieldType.folder),
    MatlabResourceFilesDirectory(FieldType.folder),
    MatlabSourceFilesDirectory(FieldType.folder),
    MatlabAuxiliaryFiles(FieldType.folder),
    CombineAuxiliaryFolders(FieldType.bool),
    AspectFilesDirectory(FieldType.string),
    ExtraAspectDirectories(FieldType.stringList),
    SetupFile(FieldType.string),
    OutputFolder(FieldType.string),
    SingleCFile(FieldType.string),
    RunOnlyOneTest(FieldType.string),
    GenerateDataFiles(FieldType.bool),
    StopAfter(FieldType.multipleChoice),
    MatlabToCCompiler(FieldType.multipleChoice),
    CompilerName(FieldType.string),
    CompilerOptimization(FieldType.multipleChoice),
    CompilerOptions(FieldType.stringList),
    LinkerOptions(FieldType.stringList),
    LinuxOnlyCompilerOptions(FieldType.stringList),
    TestWithMatlab(FieldType.bool),
    AbsoluteError(FieldType.doublefloat),
    RelativeError(FieldType.doublefloat),
    DeleteOutputContents(FieldType.bool),
    EnableStubs(FieldType.bool),
    UsePassSystem(FieldType.bool),
    IvdepType(FieldType.multipleChoice),
    CustomPreTypeSsaRecipe(FieldType.string),
    CustomRecipe(FieldType.string),
    OptionsForMainFunction(FieldType.integratedSetup),
    ImplementationSettings(FieldType.integratedSetup),
    Optimizations(FieldType.multipleChoiceStringList),
    SuppressPrinting(FieldType.bool),
    EnableZ3(FieldType.bool),
    BlasThreshold(FieldType.integer),
    BlasIncludeFolder(FieldType.string),
    BlasLibFolder(FieldType.string),
    AssumeMatrixAccessesInRange(FieldType.bool),
    AssumeMatrixSizesMatch(FieldType.bool);

    public static MatlabToCTesterData newData(SetupData setupData, MatlabToCTesterGlobalData globalData) {

        SetupAccess setup = new SetupAccess(setupData);

        if (globalData == null) {
            globalData = new MatlabToCTesterGlobalData(null, null);
        }

        LanguageMode languageMode = setup.getEnum(Language, LanguageMode.class);

        /*
         * Input directories, all of them inside the base
         * input folder, all of them defined by the user
         * 
         */
        File matlabInputFilesDirectory = setup.getFolderV2(globalData.baseInputFolder, MatlabInputFilesDirectory, true);

        // JBispo: fix
        // File matlabResourceFilesDirectory = setup.getFolderV2(globalData.baseInputFolder,
        // MatlabResourceFilesDirectory,
        // true, false);
        File matlabResourceFilesDirectory = setup.getFolderV2(globalData.baseInputFolder, MatlabResourceFilesDirectory,
                true);

        File matlabSourceFilesDirectory = setup.getFolderV2(globalData.baseInputFolder, MatlabSourceFilesDirectory,
                true);

        File matlabAuxiliaryFilesDirectory = null;
        if (setup.getString(MatlabAuxiliaryFiles).trim().isEmpty()) {
            matlabAuxiliaryFilesDirectory = SpecsIo.mkdir("./empty");
        } else {
            matlabAuxiliaryFilesDirectory = setup.getFolderV2(globalData.baseInputFolder, MatlabAuxiliaryFiles, true);
        }

        File matlabOutputFilesDirectory = null;
        if (!setup.getString(MatlabOutputFilesDirectory).trim().isEmpty()) {
            matlabOutputFilesDirectory = setup
                    .getFolderV2(globalData.baseInputFolder, MatlabOutputFilesDirectory, true);
        }

        // File matlabAuxiliaryFilesDirecotry = setup.getFolder(globalData.baseInputFolder,
        // MatlabAuxiliaryFiles, true);

        // File aspectFilesDirectory = setup.getFolder(globalData.baseInputFolder,
        // AspectFilesDirectory, true);
        File aspectFilesDirectory = setup.getExistingPath(globalData.baseInputFolder, AspectFilesDirectory);

        List<String> extraLaraFolderNames = setup.getListOfStrings(ExtraAspectDirectories);
        List<File> extraLaraFolders = new ArrayList<>();
        for (String extraFolderName : extraLaraFolderNames) {
            File file = new File(extraFolderName);
            if (!file.isAbsolute()) {
                file = new File(globalData.baseInputFolder, extraFolderName);
            }
            extraLaraFolders.add(file);
        }

        InputFolders inputsFolder = new InputFolders(matlabSourceFilesDirectory,
                matlabInputFilesDirectory,
                matlabResourceFilesDirectory,
                aspectFilesDirectory, extraLaraFolders, matlabAuxiliaryFilesDirectory, matlabOutputFilesDirectory);

        boolean deleteOutputContents = setup.getBoolean(DeleteOutputContents);

        /*
         * Base directories;
         * outputDirectory is user defined
         * 
         */
        File outputDirectory = setup.getFolderV2(globalData.baseOutputFolder, OutputFolder, false);
        // File outputDirectory = SetupAccess.getFolder(setupData, globalData.baseOutputFolder, OutputFolder, false);
        // File outputDirectory = setup.getFolder(globalData.baseOutputFolder, OutputFolder);
        if (outputDirectory == null) {
            return null;
        }

        // Do it always
        outputDirectory = new File(outputDirectory, "output");
        SpecsIo.mkdir(outputDirectory);
        /*
        	if (deleteOutputContents) {
        	    outputDirectory = new File(outputDirectory, "output");
        	    IoUtils.safeFolder(outputDirectory);
        	}
         */
        File baseDirectory = globalData.baseOutputFolder;
        // If base directory is null, set as the current working directory
        if (baseDirectory == null) {
            baseDirectory = SpecsIo.getWorkingDir();
        }

        String singleCFile = setup.getString(SingleCFile).trim();

        /*
         * Output directories, all of them are inside
         * outputDirectory, which is defined by the user
         *
         */
        OutputFolders outputFolders = OutputFolders.newInstance(outputDirectory, singleCFile);

        String runOnlyOneTest = setup.getString(RunOnlyOneTest).trim();
        if (runOnlyOneTest.isEmpty()) {
            runOnlyOneTest = null;
        } else {
            runOnlyOneTest = SpecsIo.normalizePath(runOnlyOneTest);
        }

        TesterState testerState = setup.getEnum(StopAfter, TesterState.class);

        CGenerator cGenerator = setup.getEnum(MatlabToCCompiler, CGenerator.class);

        TesterOptions testerOptions = new TesterOptions(runOnlyOneTest,
                setup.getBoolean(CombineAuxiliaryFolders),
                testerState, cGenerator);

        /*
         * 
         * Compiler options
         * 
         */

        final String compiler = setup.getString(CompilerName);

        OptimizationLevel optimizationLevel = setup.getEnum(CompilerOptimization, OptimizationLevel.class);
        String optimizationFlag = optimizationLevel.getFlag();

        final List<String> compilerFlags = setup.getStringList(CompilerOptions).getStringList();
        final List<String> linkerFlags = setup.getStringList(LinkerOptions).getStringList();
        final List<String> linuxOnlyCompilerFlags = setup.getStringList(LinuxOnlyCompilerOptions).getStringList();

        CompilerOptions compilerOptions = new CompilerOptions(compiler,
                optimizationFlag,
                compilerFlags,
                linuxOnlyCompilerFlags,
                linkerFlags);

        // Main options settings
        SetupData mainOptionsData = setup.getSetup(OptionsForMainFunction);

        /*
         * 
         * Other values
         * 
         */
        boolean testWithMatlab = setup.getBoolean(TestWithMatlab);

        // The absolute and relative error tolerance values used in the
        // comparison of the C outputs and Matlab outputs
        double absEpsilon = setup.getDouble(AbsoluteError);
        double relEpsilon = setup.getDouble(RelativeError);

        MatlabOptions matlabOptions = new MatlabOptions(absEpsilon, relEpsilon, testWithMatlab);

        // Implementation settings
        SetupData implementationData = setup.getSetup(ImplementationSettings);

        // General Setup
        File setupFile = setup.getFile(null, SetupFile, false);
        String setupName = "MatlabToCTester Setup Default";
        DataStore generalSetup = DataStore.newInstance(setupName);
        if (setupFile != null && setupFile.isFile()) {
            // Read setup
            generalSetup = new XmlPersistence().loadData(setupFile);
        }

        // Build FunctionSettings
        ImplementationSettings implSettings = ImplementationSetup.newData(implementationData);
        // Add definitions from function settings
        implSettings.set(generalSetup);

        // Add optimizations to general setup
        var optimizations = new HashSet<String>(setup.getStringList(Optimizations).getStringList());

        generalSetup.set(MatlabToCKeys.MATISSE_OPTIMIZATIONS, optimizations);

        generalSetup.set(MatlabToCKeys.ENABLE_Z3, setup.getBoolean(EnableZ3));
        generalSetup.set(MatisseLibOption.SUPPRESS_PRINTING, setup.getBoolean(SuppressPrinting));

        // Stubs
        boolean enableStubs = setup.getBoolean(EnableStubs);
        generalSetup.set(MatlabToCKeys.ENABLE_STUBS, Boolean.valueOf(enableStubs));

        Integer blasThreshold = setup.getInteger(BlasThreshold);
        generalSetup.set(MatlabToCKeys.BLAS_THRESHOLD, blasThreshold);

        // Blas library
        File blasLibFolder = setup.getFile(null, BlasLibFolder, false);
        if (blasLibFolder.isDirectory()) {
            generalSetup.set(MatlabToCKeys.BLAS_LIB_FOLDER, SpecsIo.getPath(blasLibFolder));
        } else {
            generalSetup.set(MatlabToCKeys.BLAS_LIB_FOLDER, "");
        }

        File blasIncludeFolder = setup.getFile(null, BlasIncludeFolder, false);
        if (blasIncludeFolder.isDirectory()) {
            generalSetup.set(MatlabToCKeys.BLAS_INCLUDE_FOLDER,
                    SpecsIo.getPath(blasIncludeFolder));
        } else {
            generalSetup.set(MatlabToCKeys.BLAS_INCLUDE_FOLDER, "");
        }

        generalSetup.set(MatlabToCKeys.USE_PASS_SYSTEM, setup.getBoolean(UsePassSystem));
        generalSetup.set(MatlabToCKeys.IVDEP_TYPE, setup.getEnum(IvdepType, IvdepType.class));

        File customPreTypeSsaRecipeFile = setup.getFile(null,
                CustomPreTypeSsaRecipe,
                false);
        generalSetup.set(MatlabToCKeys.CUSTOM_PRE_TYPE_SSA_RECIPE, customPreTypeSsaRecipeFile.getAbsolutePath());

        File customRecipeFile = setup.getFile(null, CustomRecipe, false);
        generalSetup.set(MatlabToCKeys.CUSTOM_RECIPE, customRecipeFile.getAbsolutePath());

        boolean assumeMatrixAccessesInRange = setup.getBoolean(AssumeMatrixAccessesInRange);
        generalSetup.set(MatisseLibOption.ASSUME_ALL_MATRIX_ACCESSES_ARE_IN_RANGE, assumeMatrixAccessesInRange);

        boolean assumeMatrixSizesMatch = setup.getBoolean(AssumeMatrixSizesMatch);
        generalSetup.set(MatisseLibOption.ASSUME_ALL_MATRIX_SIZES_MATCH, assumeMatrixSizesMatch);

        boolean generateDataFiles = setup.getBoolean(GenerateDataFiles);

        return new MatlabToCTesterData(languageMode,
                inputsFolder,
                outputFolders,
                testerOptions,
                compilerOptions,
                mainOptionsData,
                matlabOptions,
                deleteOutputContents,
                implementationData,
                generateDataFiles,
                generalSetup);
    }

    private MatlabToCTesterSetup(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
        return fieldType;
    }

    @Override
    public String getSetupName() {
        return "MatlabToCTester";
    }

    @Override
    public StringList getChoices() {
        if (this == Language) {
            return new StringList(SpecsEnums.buildList(LanguageMode.values()));
        }

        if (this == CompilerOptimization) {
            return new StringList(SpecsEnums.buildList(OptimizationLevel.values()));
        }

        if (this == StopAfter) {
            return new StringList(SpecsEnums.buildList(TesterState.values()));
        }

        if (this == MatlabToCCompiler) {
            return new StringList(SpecsEnums.buildList(CGenerator.values()));
        }

        if (this == Optimizations) {
            return new StringList(SpecsEnums.buildList(MatisseOptimization.values()));
        }

        if (this == IvdepType) {
            return new StringList(SpecsEnums.buildList(org.specs.MatlabToC.jOptions.IvdepType.values()));
        }

        return null;
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

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
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {

        if (this == Language) {
            return FieldValue.create(LanguageMode.MATLAB.name(), FieldType.multipleChoice);
        }

        if (this == TestWithMatlab) {
            return FieldValue.create(Boolean.TRUE, FieldType.bool);
        }

        if (this == MatlabOutputFilesDirectory) {
            return FieldValue.create("", FieldType.folder);
        }

        if (this == StopAfter) {
            return FieldValue.create(TesterState.TestOutputs.name(), FieldType.multipleChoice);
        }

        if (this == Optimizations) {
            return FieldValue.create(new StringList(SpecsEnums.buildList(MatisseOptimization.values())),
                    FieldType.multipleChoiceStringList);
        }

        if (this == BlasThreshold) {
            return FieldValue.create("256", FieldType.integer);
        }

        if (this == IvdepType) {
            return FieldValue.create("NONE", FieldType.multipleChoice);
        }

        if (this == MatlabToCTesterSetup.GenerateDataFiles) {
            return FieldValue.create(true, FieldType.bool);
        }

        return null;
    }

    @Override
    public String toString() {
        if (this == MatlabAuxiliaryFiles) {
            return "MatlabAuxiliaryFilesFolder";
        }

        if (this == RelativeError) {
            return "RelativeErrorPercentage";
        }

        if (this == BlasThreshold) {
            return "BlasThreshold (number of elements)";
        }
        if (this == BlasLibFolder) {
            return "BlasLibraryPath (e.g., ./blas/lib)";
        }

        if (this == BlasIncludeFolder) {
            return "BlasIncludePath (e.g., ./blas/include)";
        }

        return super.toString();
    }
}
