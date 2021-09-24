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

package org.specs.matlabtocl.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.specs.MatlabToC.jOptions.BuildersMap;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.sizeinfo.InstructionInformationBuilder;
import org.specs.matisselib.passes.posttype.LoopIterationSimplificationPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.AdditionalInformationBuildersService;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipeBuilder;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matlabtocl.v2.actions.CLHeaderGenerator;
import org.specs.matlabtocl.v2.actions.HelperCodeGenerator;
import org.specs.matlabtocl.v2.actions.KernelExporter;
import org.specs.matlabtocl.v2.actions.KernelSourceToOutputCopy;
import org.specs.matlabtocl.v2.actions.ResetKernelCollection;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CommonCodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.ssatocrules.AllocateGlobalReductionBufferProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.AllocateLocalBufferProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.AllocateMatrixOnGpuProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.CompleteReductionProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.ComputeGlobalSizeProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.ComputeGroupSizeProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.CopyToGpuProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.InvokeKernelProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.OverrideGpuBufferContentsProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.SetGpuRangeProcessor;
import org.specs.matlabtocl.v2.codegen.ssatocrules.UseWorkGroupSizeProcessor;
import org.specs.matlabtocl.v2.functions.extra.MatisseExtraCFunctions;
import org.specs.matlabtocl.v2.services.KernelInstanceCollection;
import org.specs.matlabtocl.v2.services.ParallelRegionCollection;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.services.informationbuilders.CompleteReductionInformationBuilder;
import org.specs.matlabtocl.v2.services.informationbuilders.ParallelBlockInformationBuilder;
import org.specs.matlabtocl.v2.ssa.CLRecipe;
import org.specs.matlabtocl.v2.ssa.CLRecipeBuilder;
import org.specs.matlabtocl.v2.ssa.ParallelDirectiveParser;
import org.specs.matlabtocl.v2.ssa.passes.ConstantBufferOptimizerPass;
import org.specs.matlabtocl.v2.ssa.passes.CopyAndOverwriteEliminationPass;
import org.specs.matlabtocl.v2.ssa.passes.DelayReductionCopyOptimizationPass;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMBufferEliminationPass;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMSetRangeEliminationPass;
import org.specs.matlabtocl.v2.ssa.passes.InvokeParallelFunctionImplementationPass;
import org.specs.matlabtocl.v2.ssa.passes.LoopMutableBufferCopyExtractionPass;
import org.specs.matlabtocl.v2.ssa.passes.LoopReadOnlyBufferCopyExtractionPass;
import org.specs.matlabtocl.v2.ssa.passes.ParallelBlockBuilderPass;
import org.specs.matlabtocl.v2.ssa.passes.ParallelBlockExtractorPass;
import org.specs.matlabtocl.v2.ssa.passes.RedundantCopyForSizeEliminationPass;
import org.specs.matlabtocl.v2.ssa.passes.SetReductionBufferOptimizerPass;
import org.specs.matlabtocl.v2.ssa.passes.UndefinedCopyOptimizationPass;
import org.specs.matlabtocl.v2.ssa.passes.cl.ConvertToCssaPass;
import org.specs.matlabtocl.v2.unssa.CLAwareVariableAllocator;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

public class CLRecipes {
    public final SsaRecipe preSsaRecipe;
    public final PostTypeInferenceRecipe postTypeInferenceRecipe;
    public final SsaToCRuleList additionalSsaToCRules;
    public final List<PostCodeGenAction> postCodeGenActions;
    public final DataView services;

    public static final CLRecipe CL_RECIPE;
    public static final VariableAllocator PREFERRED_VARIABLE_ALLOCATOR = new CLAwareVariableAllocator();
    private static final List<InstructionInformationBuilder> ADDITIONAL_INFORMATION_BUILDERS;
    public static final String PASS_PACKAGE = "org/specs/matlabtocl/v2/ssa/passes";
    public static final BuildersMap EXTRA_FUNCTIONS;

    static {
        ADDITIONAL_INFORMATION_BUILDERS = new ArrayList<>();
        ADDITIONAL_INFORMATION_BUILDERS.add(new ParallelBlockInformationBuilder());
        ADDITIONAL_INFORMATION_BUILDERS.add(new CompleteReductionInformationBuilder());

        EXTRA_FUNCTIONS = new BuildersMap();
        for (MatlabFunctionProviderEnum function : MatisseExtraCFunctions.values()) {
            EXTRA_FUNCTIONS.add(function.getName(), function.getMatlabFunction());
        }
    }

    public CLRecipes(File outputFilesDirectory, ProfilingOptions profilingOptions, String relativeWeaverFilePath) {
        ParallelRegionCollection parallelRegionCollection = new ParallelRegionCollection();
        KernelInstanceCollection kernelCollection = new KernelInstanceCollection();

        SsaRecipeBuilder builder = new SsaRecipeBuilder();
        builder.addPass(new ParallelBlockBuilderPass());
        builder.addPass(new SsaValidatorPass("after-cl-transformations"));
        this.preSsaRecipe = builder.getRecipe();

        PostTypeInferenceRecipeBuilder postBuilder = new PostTypeInferenceRecipeBuilder();
        postBuilder.addRecipe(
                DefaultRecipes.getOptimizingBasePostTypeInferenceRecipe());

        List<PostTypeInferencePass> finalPasses = new ArrayList<>(DefaultRecipes.getFinalPasses(false).getPasses());
        finalPasses.removeIf(LoopIterationSimplificationPass.class::isInstance);

        postBuilder.addRecipe(new PostTypeInferenceRecipe(finalPasses));
        postBuilder.addPass(new ParallelBlockExtractorPass());
        postBuilder.addPass(new InvokeParallelFunctionImplementationPass());
        postBuilder.addPass(new SetReductionBufferOptimizerPass());
        postBuilder.addPass(new ConstantBufferOptimizerPass(false));
        postBuilder.addPass(new UndefinedCopyOptimizationPass());
        postBuilder.addPass(new RedundantCopyForSizeEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new DelayReductionCopyOptimizationPass());
        postBuilder.addPass(new RedundantCopyForSizeEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new GpuSVMBufferEliminationPass());
        postBuilder.addPass(new CopyAndOverwriteEliminationPass());
        postBuilder.addPass(new SetReductionBufferOptimizerPass());
        postBuilder.addPass(new ConstantBufferOptimizerPass(false));
        postBuilder.addPass(new UndefinedCopyOptimizationPass());
        postBuilder.addPass(new RedundantCopyForSizeEliminationPass());
        postBuilder.addPass(new DelayReductionCopyOptimizationPass());
        postBuilder.addPass(new RedundantCopyForSizeEliminationPass());
        postBuilder.addPass(new LoopMutableBufferCopyExtractionPass());
        postBuilder.addPass(new LoopReadOnlyBufferCopyExtractionPass());
        postBuilder.addPass(new GpuSVMSetRangeEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new SsaValidatorPass("after-cl-optimizations"));
        this.postTypeInferenceRecipe = postBuilder.getRecipe();

        List<SsaToCRule> rules = new ArrayList<>();
        rules.add(new AllocateGlobalReductionBufferProcessor());
        rules.add(new AllocateLocalBufferProcessor());
        rules.add(new AllocateMatrixOnGpuProcessor());
        rules.add(new CompleteReductionProcessor());
        rules.add(new CopyToGpuProcessor());
        rules.add(new OverrideGpuBufferContentsProcessor());
        rules.add(new InvokeKernelProcessor());
        rules.add(new ComputeGroupSizeProcessor());
        rules.add(new ComputeGlobalSizeProcessor());
        rules.add(new UseWorkGroupSizeProcessor());
        rules.add(new SetGpuRangeProcessor());
        this.additionalSsaToCRules = new SsaToCRuleList(rules);

        DataStore additionalServices = new SimpleDataStore("cl-additional-services");
        additionalServices.add(CLServices.PARALLEL_REGION_SINK, parallelRegionCollection);
        additionalServices.add(CLServices.PARALLEL_REGION_SOURCE,
                parallelRegionCollection);
        additionalServices.add(CLServices.KERNEL_INSTANCE_SINK, kernelCollection);
        additionalServices.add(ProjectPassServices.ADDITIONAL_INFORMATION_BUILDERS,
                new AdditionalInformationBuildersService(ADDITIONAL_INFORMATION_BUILDERS));
        CodeGenerationStrategyProvider reductionStrategyProvider = new CommonCodeGenerationStrategyProvider(
                new File(outputFilesDirectory, relativeWeaverFilePath));
        additionalServices.add(CLServices.CODE_GENERATION_STRATEGY_PROVIDER,
                reductionStrategyProvider);
        additionalServices.add(CLServices.PROFILING_OPTIONS, profilingOptions);
        additionalServices.add(PassManager.DIRECTIVE_PARSER, new ParallelDirectiveParser());

        this.services = DataView.newInstance(additionalServices);

        this.postCodeGenActions = new ArrayList<>();
        this.postCodeGenActions.add(new KernelExporter(kernelCollection));
        this.postCodeGenActions.add(new CLHeaderGenerator(kernelCollection));
        this.postCodeGenActions
                .add(new HelperCodeGenerator(kernelCollection, reductionStrategyProvider, profilingOptions));
        this.postCodeGenActions.add(new KernelSourceToOutputCopy());
        this.postCodeGenActions.add(new ResetKernelCollection(kernelCollection));
    }

    static {
        CLRecipeBuilder clBuilder = new CLRecipeBuilder();
        clBuilder.addPass(new ConvertToCssaPass());
        CL_RECIPE = clBuilder.getRecipe();
    }
}
