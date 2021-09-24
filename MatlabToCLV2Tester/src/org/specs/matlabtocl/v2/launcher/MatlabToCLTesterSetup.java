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

package org.specs.matlabtocl.v2.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToCTester.Auxiliary.MatlabOptions;
import org.specs.MatlabToCTester.Auxiliary.OptimizationLevel;
import org.specs.MatlabToCTester.Auxiliary.TesterState;
import org.specs.matlabtocl.v2.services.ProfileBeginMode;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.targets.TargetResource;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice;
import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.utilities.StringList;

public enum MatlabToCLTesterSetup implements SetupFieldEnum, DefaultValue, MultipleChoice {
    Language(FieldType.multipleChoice),
    SuppressPrinting(FieldType.bool),

    MatlabInputFilesDirectory(FieldType.string),
    MatlabResourceFilesDirectory(FieldType.string),
    MatlabSourceFilesDirectory(FieldType.string),
    MatlabAuxiliarFilesDirectory(FieldType.string),
    CombinedAuxiliaryFolder(FieldType.bool),
    MatlabOutputFilesDirectory(FieldType.string),
    AspectFilePath(FieldType.string),
    OutputFilesDirectory(FieldType.string),
    RunOnlyOneTest(FieldType.string),
    TargetSpecialization(FieldType.multipleChoice),
    DisableParallelism(FieldType.bool),
    Warmup(FieldType.bool),

    Compiler(FieldType.string),
    CompilationFlags(FieldType.stringList),
    StopAfter(FieldType.multipleChoice),
    GenerateDataFiles(FieldType.bool),

    CLIncludeDirectory(FieldType.string),
    CLLibraryDirectory(FieldType.string),
    CLLibraryName(FieldType.string),
    CompileTimeFixedPlatform(FieldType.string),
    CompileTimeFixedDevice(FieldType.string),

    ExecutionMode(FieldType.multipleChoice),
    TestWithMatlab(FieldType.bool),
    AbsoluteError(FieldType.doublefloat),
    RelativeError(FieldType.doublefloat),

    EnableKernelProfiling(FieldType.bool),
    EnableDataTransferProfiling(FieldType.bool),
    ProfileMode(FieldType.multipleChoice),

    CustomRecipeFile(FieldType.string),

    CompilerOptimization(FieldType.multipleChoice),
    EnableZ3(FieldType.bool),
    AssumeMatrixIndicesInRange(FieldType.bool),
    AssumeMatrixSizesMatch(FieldType.bool);

    private final FieldType fieldType;

    private MatlabToCLTesterSetup(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
        return this.fieldType;
    }

    @Override
    public String getSetupName() {
        return "MatlabToCLTester";
    }

    public static MatlabToCLTesterData newData(SetupData setupData, MatlabToCLTesterGlobalData globalData) {

        SetupAccess setup = new SetupAccess(setupData);

        LanguageMode languageMode = setup.getEnum(Language, LanguageMode.class);
        boolean suppressPrinting = setup.getBoolean(SuppressPrinting);

        File inputFilesDirectory = setup.getFolderV2(MatlabInputFilesDirectory, true);
        // JBispo: fix
        // File resourceFilesDirectory = setup.getFolderV2(null, MatlabResourceFilesDirectory, true, false);
        File resourceFilesDirectory = setup.getFolderV2(null, MatlabResourceFilesDirectory, true);
        File sourceFilesDirectory = setup.getFolderV2(MatlabSourceFilesDirectory, true);
        File auxiliarFilesDirectory = setup.getFolderV2(MatlabAuxiliarFilesDirectory, true);
        boolean combinedAuxiliaryFolder = setup.getBoolean(CombinedAuxiliaryFolder);
        File aspectFilePath = setup.getExistingPath(null, AspectFilePath);
        File matlabOutputFilesDirectory = setup.getFolderV2(MatlabOutputFilesDirectory, true);
        File outputFilesDirectory = setup.getFolderV2(OutputFilesDirectory, false);

        String runOnlyOneTest = setup.getString(RunOnlyOneTest).trim();
        boolean disableParallelism = setup.getBoolean(DisableParallelism);
        boolean warmup = setup.getBoolean(Warmup);

        String targetName = setup.getString(TargetSpecialization);
        TargetResource target = Arrays.stream(TargetResource.values())
                .filter(res -> res.getPlatformName().equals(targetName))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not find platform: " + targetName));

        String customRecipePath = setup.getString(CustomRecipeFile);

        String compiler = setup.getString(Compiler);
        StringList compilationFlags = setup.getStringList(CompilationFlags);

        File clIncludeDirectory = setup.getFolderV2(globalData.clSdk.getIncludeDirectory(), CLIncludeDirectory, true);
        File clLibraryDirectory = setup.getFolderV2(globalData.clSdk.getLibraryDirectory(), CLLibraryDirectory, true);
        String clLibraryName = setup.getString(CLLibraryName);
        if (clLibraryName == null || clLibraryName.trim().isEmpty()) {
            clLibraryName = globalData.clSdk.getLibraryName();
        }
        OpenCLSDKInformation clSdk = new OpenCLSDKInformation(clIncludeDirectory, clLibraryDirectory, clLibraryName);

        String compileTimeFixedPlatform = setup.getString(CompileTimeFixedPlatform).trim();
        String compileTimeFixedDevice = setup.getString(CompileTimeFixedDevice).trim();
        OpenCLConfigurationSettings configurationSettings = new OpenCLConfigurationSettings(
                compileTimeFixedPlatform,
                compileTimeFixedDevice);

        TesterState stopAfter = setup.getEnum(StopAfter, TesterState.class);

        boolean testWithMatlab = setup.getBoolean(TestWithMatlab);

        // The absolute and relative error tolerance values used in the
        // comparison of the C outputs and Matlab outputs
        double absEpsilon = setup.getDouble(AbsoluteError);
        double relEpsilon = setup.getDouble(RelativeError);

        MatlabOptions matlabOptions = new MatlabOptions(absEpsilon, relEpsilon, testWithMatlab);

        OptimizationLevel optimizationLevel = setup.getEnum(CompilerOptimization, OptimizationLevel.class);

        boolean assumeMatrixIndicesInRange = setup.getBoolean(AssumeMatrixIndicesInRange);
        boolean assumeMatrixSizesMatch = setup.getBoolean(AssumeMatrixSizesMatch);
        boolean enableZ3 = setup.getBoolean(EnableZ3);
        OptimizationOptions optimizationOptions = new OptimizationOptions(assumeMatrixIndicesInRange,
                assumeMatrixSizesMatch, enableZ3);

        ExecutionMode executionMode = setup.getEnum(ExecutionMode, ExecutionMode.class);

        ProfilingOptions profilingOptions = new ProfilingOptions(setup.getBoolean(EnableKernelProfiling),
                setup.getBoolean(EnableDataTransferProfiling), setup.getEnum(ProfileMode, ProfileBeginMode.class));

        boolean generateDataFiles = setup.getBoolean(GenerateDataFiles);

        return new MatlabToCLTesterData(languageMode,
                suppressPrinting,
                inputFilesDirectory,
                resourceFilesDirectory,
                sourceFilesDirectory,
                auxiliarFilesDirectory,
                combinedAuxiliaryFolder,
                aspectFilePath,
                matlabOutputFilesDirectory,
                outputFilesDirectory,
                runOnlyOneTest,
                disableParallelism,
                warmup,
                target,
                stopAfter,
                compiler,
                compilationFlags,
                matlabOptions,
                optimizationLevel,
                optimizationOptions,
                executionMode,
                profilingOptions,
                clSdk,
                configurationSettings,
                generateDataFiles,
                customRecipePath);
    }

    @Override
    public FieldValue getDefaultValue() {
        switch (this) {
        case Language:
            return FieldValue.create(LanguageMode.MATLAB.name(), FieldType.multipleChoice);
        case Compiler:
            return FieldValue.create("clang", FieldType.string);
        case CLLibraryName:
            return FieldValue.create("OpenCL", FieldType.string);
        case CompilerOptimization:
            return FieldValue.create(OptimizationLevel.O3.toString(), FieldType.string);
        case CompileTimeFixedDevice:
        case CompileTimeFixedPlatform:
            return FieldValue.create("", FieldType.string);
        case ProfileMode:
            return FieldValue.create("SINCE_START", FieldType.multipleChoice);
        case TargetSpecialization:
            return FieldValue.create(TargetResource.DEFAULT.getPlatformName(), FieldType.multipleChoice);
        case GenerateDataFiles:
            return FieldValue.create(true, FieldType.bool);
        default:
            return null;
        }
    }

    @Override
    public StringList getChoices() {
        switch (this) {
        case Language:
            return new StringList(SpecsEnums.buildList(LanguageMode.values()));
        case StopAfter:
            return new StringList(SpecsEnums.buildList(TesterState.values()));
        case CompilerOptimization:
            return new StringList(SpecsEnums.buildList(OptimizationLevel.values()));
        case ExecutionMode:
            return new StringList(SpecsEnums.buildList(org.specs.matlabtocl.v2.launcher.ExecutionMode.values()));
        case ProfileMode:
            return new StringList(SpecsEnums.buildList(ProfileBeginMode.values()));
        case TargetSpecialization:
            List<String> targets = new ArrayList<>();

            for (TargetResource target : TargetResource.values()) {
                targets.add(target.getPlatformName());
            }

            return new StringList(targets);
        default:
            return null;
        }
    }
}
