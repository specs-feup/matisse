/**
 * Copyright 2016 SPeCS.
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

package org.specs.matlabprocessorprinter.printers;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.CIR.CProject;
import org.specs.CIR.CirUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Passes.CRecipe;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.PassAwareMatlabToCEngine;
import org.specs.MatlabToC.jOptions.BuildersMap;
import org.specs.MatlabToC.jOptions.IvdepType;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.specs.matlabtocl.v2.CLRecipes;
import org.specs.matlabtocl.v2.CLSetupUtils;
import org.specs.matlabtocl.v2.MatisseCLKeys;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumLocalReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumSubgroupReductionStrategy;
import org.specs.matlabtocl.v2.heuristics.decisiontree.DecisionNode;
import org.specs.matlabtocl.v2.heuristics.decisiontree.TerminalNode;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleMethod;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.services.ProfileBeginMode;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.ssa.typeinference.ParallelBlockTypeInferenceRule;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;
import org.suikasoft.jOptions.persistence.XmlPersistence;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.utilities.StringList;

public class ParallelCCodePrinter implements CodePrinter {

    @Override
    public List<ContentPage> getCode(String code,
            DataStore setup,
            FileNode file,
            PrintStream reportStream,
            CodeGenerationSettings settings) throws IOException {

        MatlabFunctionTable systemFunctions = new MatlabFunctionTable(InferredBytecodeCodePrinter.getSystemFunctions());
        BuildersMap buildersMap = CLRecipes.EXTRA_FUNCTIONS;
        for (String function : buildersMap.getFunctions()) {
            systemFunctions.addBuilderFirst(function, buildersMap.getBuilders(function));
        }

        VariableAllocator allocator = CLRecipes.PREFERRED_VARIABLE_ALLOCATOR;

        FunctionNode function = file.getMainFunction();
        String functionName = function.getFunctionName();
        String fileName = functionName + ".m";

        Map<String, StringProvider> files = new HashMap<>();
        files.put(fileName, StringProvider.newInstance(code));

        File tmpFolder = Files.createTempDirectory("matisse_tmp").toFile();
        File aspectData = new File(tmpFolder, "mweaver.aspectdata");
        DataStore weaverData = new SimpleDataStore("weaver-dummy-data");
        CLSetupUtils.setDeviceManagementStrategy(weaverData,
                settings.useUnifiedMemory() ? DeviceMemoryManagementStrategy.FINE_GRAINED_BUFFERS
                        : DeviceMemoryManagementStrategy.COPY_BUFFERS);
        weaverData.add(MatisseCLKeys.SUM_LOCAL_REDUCTION_STRATEGY, SumLocalReductionStrategy.INTERLEAVED_SUM_REDUCTION);
        weaverData.add(MatisseCLKeys.SUM_SUBGROUP_REDUCTION_STRATEGY,
                SumSubgroupReductionStrategy.INTERLEAVED_SUM_REDUCTION);
        weaverData.add(MatisseCLKeys.RANGE_SET_INSTRUCTION_ENABLED, true);
        weaverData.add(MatisseCLKeys.SVM_RESTRICT_COALESCED, true);
        weaverData.add(MatisseCLKeys.SVM_SET_RANGE_FORBIDDEN, true);
        weaverData.add(MatisseCLKeys.TRY_USE_SCHEDULE_COOPERATIVE, true);
        weaverData.add(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK, settings.useSubgroupAsWarpFallback());
        weaverData.add(MatisseCLKeys.SUB_GROUP_SIZE, settings.useSubgroupAsWarpFallback() ? 32 : 64);
        weaverData.add(MatisseCLKeys.SCHEDULE_DECISION_TREE, new ScheduleDecisionTree(
                new DecisionNode<>("compute_portion", 0.8, new TerminalNode<>((c, r) -> ScheduleMethod.DIRECT),
                        new TerminalNode<>((c, r) -> ScheduleMethod.DIRECT))));
        new XmlPersistence().saveData(aspectData, weaverData);

        boolean enableProfiling = settings.enableOpenCLProfiling();
        CLRecipes recipes = new CLRecipes(tmpFolder,
                new ProfilingOptions(enableProfiling, enableProfiling, ProfileBeginMode.SINCE_START),
                "mweaver.aspectdata");

        setup.addAll(weaverData);

        PostTypeInferenceRecipe postTypeInferenceRecipe = PostTypeInferenceRecipe.combine(
                recipes.postTypeInferenceRecipe,
                DefaultRecipes.getFinalPasses(true));

        List<TypeInferenceRule> rules = new ArrayList<>();
        rules.addAll(TypeInferencePass.BASE_TYPE_INFERENCE_RULES);
        rules.add(new ParallelBlockTypeInferenceRule());
        InferenceRuleList inferenceRuleList = new InferenceRuleList(rules);

        SsaRecipe preSsaRecipe = SsaRecipe.combine(recipes.preSsaRecipe,
                DefaultRecipes.getDefaultPreTypeInferenceRecipe(setup));

        DataStore additionalServices = new SimpleDataStore("parallel-c-services");
        additionalServices.addAll(recipes.services);
        additionalServices.add(MatisseLibOption.DUMP_SSA_INSTRUCTIONS, settings.dumpSsa());
        additionalServices.add(MatisseLibOption.DUMP_OUTPUT_TYPES, settings.dumpOutputTypes());
        additionalServices.add(MatisseLibOption.PASSES_TO_LOG, new StringList(settings.getPassesToLog()));

        ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(preSsaRecipe)
                        .withPostTypeInferenceRecipe(postTypeInferenceRecipe)
                        .withInferenceRuleList(inferenceRuleList)
                        .withAvailableFiles(files)
                        .withSystemFunctions(systemFunctions)
                        .withAdditionalServices(DataView.newInstance(additionalServices))
                        .withLanguageMode(settings.getLanguageMode())
                        .withZ3Enabled(settings.enableZ3()));
        manager.setErrorReportStream(reportStream);

        SsaToCRuleList ssaToCRules = new SsaToCRuleList(
                SsaToCBuilder.DEFAULT_SSA_TO_C_RULES,
                recipes.additionalSsaToCRules);
        PassAwareMatlabToCEngine engine = new PassAwareMatlabToCEngine(manager,
                systemFunctions,
                allocator,
                ssaToCRules);
        MFileProvider.setEngine(engine);

        manager.applyPreTypeInferencePasses(fileName);

        TypedInstance instance = InferredBytecodeCodePrinter.buildInstance(function, functionName, fileName, manager,
                setup);

        manager.applyPostTypeInferencePasses();

        CProject cProject = new CProject(settings.enableZ3() ? DefaultRecipes.DefaultCRecipe : CRecipe.empty(),
                MatlabToCOptionUtils.newDefaultSettings());

        FunctionInstance mainInstance = SsaToCBuilder
                .buildImplementation(manager, instance, systemFunctions, allocator,
                        ssaToCRules);
        cProject.addFunction(mainInstance);

        CirUtils.writeProjectUniqueFile(cProject, "file", tmpFolder);

        File cFile = new File(tmpFolder, "file.c");
        String cCode = SpecsIo.read(cFile);
        for (PostCodeGenAction action : recipes.postCodeGenActions) {
            action.run(tmpFolder);
        }
        String clCode = SpecsIo.read(new File(tmpFolder, "program.cl"));
        String clHeaderCode = SpecsIo.read(new File(tmpFolder, "matisse-cl.h"));
        String clUtilsCode = SpecsIo.read(new File(tmpFolder, "matisse_cl.c"));

        SpecsIo.deleteFolder(tmpFolder);

        return Arrays.asList(new ContentPage("C code", cCode, "text/java"),
                new ContentPage("program.cl", clCode, "text/c"),
                new ContentPage("matisse-cl.h", clHeaderCode, "text/java"),
                new ContentPage("matisse_cl.c", clUtilsCode, "text/java"));
    }

    @Override
    public void processSetup(DataStore basicSetup, CodeGenerationSettings settings) {
        basicSetup.set(MatlabToCKeys.IVDEP_TYPE, IvdepType.NONE);
        basicSetup.set(MatlabToCKeys.USE_PASS_SYSTEM, true);
        basicSetup.set(MatlabToCKeys.ENABLE_Z3, settings.enableZ3());
        basicSetup.set(MatisseLibOption.DUMP_SSA_INSTRUCTIONS, settings.dumpSsa());
        basicSetup.set(MatisseLibOption.DUMP_OUTPUT_TYPES, settings.dumpOutputTypes());
    }

}
