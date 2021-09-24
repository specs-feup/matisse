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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.MatlabToCTester.MatlabToCTesterData;
import org.specs.MatlabToCTester.MatlabToCTesterGlobalData;
import org.specs.MatlabToCTester.MatlabToCTesterGlobalSetup;
import org.specs.MatlabToCTester.MatlabToCTesterLauncher;
import org.specs.MatlabToCTester.MatlabToCTesterSetup;
import org.specs.MatlabToCTester.CGeneration.CGenerator;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.io.PostTypeInferenceRecipeReader;
import org.specs.matisselib.io.PostTypeInferenceRecipeWriter;
import org.specs.matisselib.io.PreTypeInferenceSsaRecipeWriter;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matlabtocl.v2.CLRecipes;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.ssa.typeinference.ParallelBlockTypeInferenceRule;
import org.specs.matlabtocl.v2.targets.TargetAspectGenerator;
import org.specs.matlabtocl.v2.targets.TargetResource;
import org.suikasoft.CMainFunction.Builder.MainFunctionTarget;
import org.suikasoft.CMainFunction.Builder.TestMainSetup;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.persistence.XmlPersistence;

import pt.up.fe.specs.guihelper.App;
import pt.up.fe.specs.guihelper.AppSource;
import pt.up.fe.specs.guihelper.AppUsesGlobalOptions;
import pt.up.fe.specs.guihelper.GuiHelperUtils;
import pt.up.fe.specs.guihelper.Base.SetupDefinition;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.gui.SimpleGui;
import pt.up.fe.specs.matisse.weaver.MWeaver;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.utilities.StringList;

public class MatlabToCLTesterLauncher implements App, AppSource, AppUsesGlobalOptions {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        MatlabToCLTesterLauncher app = new MatlabToCLTesterLauncher();

        if (args.length > 0) {
            GuiHelperUtils.trySingleConfigMode(args, app);
            return;
        }

        SimpleGui gui = new SimpleGui(app);

        gui.setTitle("Matisse CL v0.3");
        gui.setIcon(MWeaver.getWeaverIcon());
        gui.execute();
    }

    @Override
    public App newInstance() {
        return new MatlabToCLTesterLauncher();
    }

    @Override
    public int execute(File setupFile) throws InterruptedException {
        SetupData setupData = GuiHelperUtils.loadData(setupFile);

        return execute(setupData);
    }

    public static int execute(SetupData setupData) {
        MatlabToCLTesterGlobalData globalData = MatlabToCLTesterGlobalSetup.getData();

        MatlabToCLTesterData data = MatlabToCLTesterSetup.newData(setupData, globalData);

        List<String> tests = new ArrayList<>();
        if (!data.runOnlyOneTest.isEmpty()) {
            tests.add(data.runOnlyOneTest);
        } else {
            for (File file : data.sourceFilesDirectory.listFiles()) {
                if (file.isFile() && SpecsIo.getExtension(file).equals("m")) {
                    tests.add(SpecsIo.removeExtension(file.getName()));
                }
            }
        }

        int failedResult = 0;
        for (String test : tests) {
            int result = executeForTest(setupData, data, test);
            if (result != 0) {
                failedResult = result;
            }
        }

        return failedResult;
    }

    private static int executeForTest(SetupData setupData, MatlabToCLTesterData data, String test) {
        File outputFolder = data.outputFilesDirectory;
        if (!outputFolder.getName().equals("output")) {
            outputFolder = new File(outputFolder, "output");
        }
        outputFolder.mkdirs();
        File preTypeSsaRecipeFile = new File(outputFolder, "pre-type-ssa-passes.recipe");
        File postTypeRecipeFile = new File(outputFolder, "post-type-passes.recipe");
        File setupFile = new File(outputFolder, "extra-options.xml");

        File generatedAspects = new File(outputFolder, "generatedAspects");
        generatedAspects.mkdirs();
        TargetResource target = data.target;

        File laraWrapperFile = TargetAspectGenerator.generateTargetAwareAspect(data.aspectFilePath, test,
                generatedAspects, target);

        List<TypeInferenceRule> rules = new ArrayList<>();
        rules.addAll(TypeInferencePass.BASE_TYPE_INFERENCE_RULES);
        rules.add(new ParallelBlockTypeInferenceRule());
        InferenceRuleList inferenceRuleList = new InferenceRuleList(rules);
        CLRecipes clRecipes = new CLRecipes(data.outputFilesDirectory, data.profilingOptions,
                "output/matlabWeaver/mweaver_output/mweaver.aspectdata");
        List<String> customRecipePathsList = Arrays.asList(CLRecipes.PASS_PACKAGE);
        StringList customRecipePaths = new StringList(customRecipePathsList);

        DataStore clData = DataStore.newInstance("cl-extra-options");

        clData.set(MatlabToCKeys.CUSTOM_PRE_TYPE_SSA_RECIPE_PATHS, customRecipePaths);
        clData.set(MatlabToCKeys.CUSTOM_POST_TYPE_RECIPE_PATHS, customRecipePaths);

        if (!data.disableParallelism) {
            List<PostCodeGenAction> actions = clRecipes.postCodeGenActions;

            clData.set(MatlabToCKeys.CUSTOM_VARIABLE_ALLOCATOR, CLRecipes.PREFERRED_VARIABLE_ALLOCATOR);
            clData.set(MatlabToCKeys.TYPE_INFERENCE_RULES, inferenceRuleList);
            clData.set(MatlabToCKeys.ADDITIONAL_SERVICES, clRecipes.services);
            clData.set(MatlabToCKeys.ADDITIONAL_SSA_TO_C_RULES, clRecipes.additionalSsaToCRules);
            clData.set(MatlabToCKeys.POST_CODEGEN_STEP,
                    file -> {
                        actions.forEach(action -> action.run(file));
                    });
        }
        clData.set(MatlabToCKeys.MATLAB_BUILDERS, CLRecipes.EXTRA_FUNCTIONS);

        if (!new XmlPersistence().saveData(setupFile, clData)) {
            return 1;
        }

        SetupData optionsForMainFunction = SetupData.create(TestMainSetup.class);
        optionsForMainFunction.put(TestMainSetup.Target, MainFunctionTarget.MultiTarget);
        if (!data.disableParallelism) {
            optionsForMainFunction.put(TestMainSetup.ExtraMainPreparationCode,
                    "MATISSE_cl_initialize(argc, argv);\n\n");
            optionsForMainFunction.put(TestMainSetup.ExtraMainFinalizationCode, "MATISSE_cl_print_times();");
            optionsForMainFunction.put(TestMainSetup.ExtraMainFilePrefixCode,
                    "#include \"" + CLCodeGenUtils.HEADER_NAME + "\"\n");
            optionsForMainFunction.put(TestMainSetup.ExtraMainAfterWarmupCode, "MATISSE_cl_reset_times();");
        }
        optionsForMainFunction.put(TestMainSetup.PrintExecutionTime, data.executionMode == ExecutionMode.Benchmarking);
        optionsForMainFunction.put(TestMainSetup.WriteOutputs, data.executionMode == ExecutionMode.Validation);
        optionsForMainFunction.put(TestMainSetup.Warmup, data.warmup);

        SetupData cSetupData = SetupData.create(MatlabToCTesterSetup.class);
        cSetupData.put(MatlabToCTesterSetup.Language, data.languageMode);
        cSetupData.put(MatlabToCTesterSetup.SuppressPrinting, data.suppressPrinting);
        cSetupData.put(MatlabToCTesterSetup.MatlabSourceFilesDirectory, data.sourceFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.MatlabInputFilesDirectory, data.inputFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.MatlabResourceFilesDirectory,
                data.resourceFilesDirectory == null ? "" : data.resourceFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.MatlabAuxiliaryFiles, data.auxiliarFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.CombineAuxiliaryFolders, data.combinedAuxiliaryFolder);
        cSetupData.put(MatlabToCTesterSetup.MatlabOutputFilesDirectory,
                data.matlabOutputFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.OutputFolder, data.outputFilesDirectory.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.AspectFilesDirectory, laraWrapperFile.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.ExtraAspectDirectories,
                StringList.newInstance(
                        data.aspectFilePath.isDirectory() ? data.aspectFilePath.getAbsolutePath()
                                : data.aspectFilePath.getParentFile().getAbsolutePath()));
        cSetupData.put(MatlabToCTesterSetup.RunOnlyOneTest, test);
        cSetupData.put(MatlabToCTesterSetup.UsePassSystem, true);
        cSetupData.put(MatlabToCTesterSetup.MatlabToCCompiler, CGenerator.MATISSE.toString());
        cSetupData.put(MatlabToCTesterSetup.OptionsForMainFunction, optionsForMainFunction);
        cSetupData.put(MatlabToCTesterSetup.CompilerName, data.compiler);
        cSetupData.put(MatlabToCTesterSetup.CustomPreTypeSsaRecipe, preTypeSsaRecipeFile.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.CustomRecipe, postTypeRecipeFile.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.SetupFile, setupFile.getAbsolutePath());
        cSetupData.put(MatlabToCTesterSetup.StopAfter, data.stopAfter);
        cSetupData.put(MatlabToCTesterSetup.GenerateDataFiles, data.generateDataFiles);
        List<String> compilerOptions = new ArrayList<>();
        if (!data.disableParallelism) {
            compilerOptions.add("-isystem\"" + data.clSdk.getIncludeDirectory() + "\"");

            String compileTimeFixedPlatform = data.configurationSettings.compileTimeFixedPlatform;
            if (!compileTimeFixedPlatform.equals("")) {
                int value = Integer.parseInt(compileTimeFixedPlatform, 10);
                compilerOptions.add("-DCHOSEN_PLATFORM=" + value);
            }
            String compileTimeFixedDevice = data.configurationSettings.compileTimeFixedDevice;
            if (!compileTimeFixedDevice.equals("")) {
                int value = Integer.parseInt(compileTimeFixedDevice, 10);
                compilerOptions.add("-DCHOSEN_DEVICE=" + value);
            }
        }
        compilerOptions.addAll(data.compilationFlags.getStringList());

        cSetupData.put(MatlabToCTesterSetup.CompilerOptions, new StringList(compilerOptions));
        cSetupData.put(MatlabToCTesterSetup.CompilerOptimization, data.optimizationLevel);
        List<String> linkerOptions = new ArrayList<>();
        if (!data.disableParallelism) {
            linkerOptions.add("-L\"" + data.clSdk.getLibraryDirectory() + "\"");
            linkerOptions.add("-l" + data.clSdk.getLibraryName());
        }
        cSetupData.put(MatlabToCTesterSetup.LinkerOptions, new StringList(linkerOptions));
        cSetupData.put(MatlabToCTesterSetup.TestWithMatlab, data.matlabOptions.testWithMatlab());
        cSetupData.put(MatlabToCTesterSetup.AbsoluteError, Double.toString(data.matlabOptions.getAbsEpsilon()));
        cSetupData.put(MatlabToCTesterSetup.RelativeError, Double.toString(data.matlabOptions.getRelEpsilon()));
        cSetupData.put(MatlabToCTesterSetup.AssumeMatrixAccessesInRange,
                data.optimizationOptions.assumeMatrixIndicesInRange);
        cSetupData.put(MatlabToCTesterSetup.AssumeMatrixSizesMatch,
                data.optimizationOptions.assumeMatrixSizesMatch);
        cSetupData.put(MatlabToCTesterSetup.EnableZ3, data.optimizationOptions.enableZ3);

        MatlabToCTesterGlobalData cGlobalData = MatlabToCTesterGlobalSetup.getData();
        MatlabToCTesterData cData = MatlabToCTesterSetup.newData(cSetupData, cGlobalData);

        try {
            PreTypeInferenceSsaRecipeWriter.write(clRecipes.preSsaRecipe, preTypeSsaRecipeFile);
        } catch (IOException e) {
            e.printStackTrace();

            return 1;
        }

        try {
            File customRecipePath = new File(setupData.getSetupFile().getParentFile(),
                    data.customRecipeFilePath);
            PostTypeInferenceRecipe postTypeInferenceRecipe;
            if (customRecipePath.isFile()) {
                postTypeInferenceRecipe = PostTypeInferenceRecipeReader.read(customRecipePath,
                        customRecipePathsList);
            } else if (data.disableParallelism) {
                postTypeInferenceRecipe = DefaultRecipes.getOptimizingBasePostTypeInferenceRecipe();
            } else {
                postTypeInferenceRecipe = clRecipes.postTypeInferenceRecipe;
            }

            PostTypeInferenceRecipeWriter.write(postTypeInferenceRecipe, postTypeRecipeFile);
        } catch (IOException e) {
            e.printStackTrace();

            return 1;
        }

        return new MatlabToCTesterLauncher().execute(cData);
    }

    @Override
    public SetupDefinition getEnumKeys() {
        return SetupDefinition.create(MatlabToCLTesterSetup.class);
    }

    @Override
    public Class<? extends SetupFieldEnum> getGlobalOptions() {
        return MatlabToCLTesterGlobalSetup.class;
    }
}
