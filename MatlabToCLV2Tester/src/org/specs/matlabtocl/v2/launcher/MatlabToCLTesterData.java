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

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToCTester.Auxiliary.MatlabOptions;
import org.specs.MatlabToCTester.Auxiliary.OptimizationLevel;
import org.specs.MatlabToCTester.Auxiliary.TesterState;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.targets.TargetResource;

import pt.up.fe.specs.util.utilities.StringList;

public class MatlabToCLTesterData {

    public final LanguageMode languageMode;
    public final boolean suppressPrinting;
    public final File inputFilesDirectory;
    public final File resourceFilesDirectory;
    public final File sourceFilesDirectory;
    public final File auxiliarFilesDirectory;
    public final boolean combinedAuxiliaryFolder;
    public final File aspectFilePath;
    public final File matlabOutputFilesDirectory;
    public final File outputFilesDirectory;
    public final String runOnlyOneTest;
    public final boolean disableParallelism;
    public final boolean warmup;
    public final TargetResource target;
    public final String compiler;
    public final StringList compilationFlags;
    public final MatlabOptions matlabOptions;
    public final OptimizationLevel optimizationLevel;
    public final OptimizationOptions optimizationOptions;
    public final ExecutionMode executionMode;
    public final ProfilingOptions profilingOptions;

    public final TesterState stopAfter;

    public final OpenCLSDKInformation clSdk;
    public final OpenCLConfigurationSettings configurationSettings;

    public final boolean generateDataFiles;
    public final String customRecipeFilePath;

    public MatlabToCLTesterData(LanguageMode languageMode,
            boolean suppressPrinting,
            File inputFilesDirectory,
            File resourceFilesDirectory,
            File sourceFilesDirectory,
            File auxiliarFilesDirectory,
            boolean combinedAuxiliaryFolder,
            File aspectFilePath,
            File matlabOutputFilesDirectory,
            File outputFilesDirectory,
            String runOnlyOneTest,
            boolean disableParallelism,
            boolean warmup,
            TargetResource target,
            TesterState stopAfter,
            String compiler,
            StringList compilationFlags,
            MatlabOptions matlabOptions,
            OptimizationLevel optimizationLevel,
            OptimizationOptions optimizationOptions,
            ExecutionMode executionMode,
            ProfilingOptions profilingOptions,
            OpenCLSDKInformation clSdk,
            OpenCLConfigurationSettings configurationSettings,
            boolean generateDataFiles,
            String customRecipeFilePath) {

        this.languageMode = languageMode;
        this.suppressPrinting = suppressPrinting;
        this.inputFilesDirectory = inputFilesDirectory;
        this.resourceFilesDirectory = resourceFilesDirectory;
        this.sourceFilesDirectory = sourceFilesDirectory;
        this.auxiliarFilesDirectory = auxiliarFilesDirectory;
        this.combinedAuxiliaryFolder = combinedAuxiliaryFolder;
        this.aspectFilePath = aspectFilePath;
        this.matlabOutputFilesDirectory = matlabOutputFilesDirectory;
        this.outputFilesDirectory = outputFilesDirectory;
        this.runOnlyOneTest = runOnlyOneTest;
        this.disableParallelism = disableParallelism;
        this.warmup = warmup;
        this.target = target;
        this.stopAfter = stopAfter;
        this.compiler = compiler;
        this.compilationFlags = compilationFlags;
        this.matlabOptions = matlabOptions;
        this.optimizationLevel = optimizationLevel;
        this.optimizationOptions = optimizationOptions;
        this.profilingOptions = profilingOptions;
        this.clSdk = clSdk;
        this.configurationSettings = configurationSettings;
        this.customRecipeFilePath = customRecipeFilePath;
        this.executionMode = executionMode;
        this.generateDataFiles = generateDataFiles;
    }

}
