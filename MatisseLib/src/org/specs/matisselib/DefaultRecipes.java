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

package org.specs.matisselib;

import org.specs.CIR.Passes.CRecipe;
import org.specs.CIR.Passes.CRecipeBuilder;
import org.specs.matisselib.passes.ast.BasicReturnRemoverPass;
import org.specs.matisselib.passes.ast.CellReplacementPass;
import org.specs.matisselib.passes.ast.CommandReplacementPass;
import org.specs.matisselib.passes.ast.ElseIfUnrollerPass;
import org.specs.matisselib.passes.ast.GlobalArgumentConflictCheckerPass;
import org.specs.matisselib.passes.ast.MatrixReplacementPass;
import org.specs.matisselib.passes.ast.OperatorReplacementPass;
import org.specs.matisselib.passes.ast.ReturnRemoverPass;
import org.specs.matisselib.passes.ast.StringConversionPass;
import org.specs.matisselib.passes.ast.WhileSimplifierPass;
import org.specs.matisselib.passes.cir.ConstantPropagationPass;
import org.specs.matisselib.passes.cir.ElseIfBuilderPass;
import org.specs.matisselib.passes.cir.EmptyElseEliminationPass;
import org.specs.matisselib.passes.cir.ForSimplifierPass;
import org.specs.matisselib.passes.cir.RedundantReturnRemovalPass;
import org.specs.matisselib.passes.cir.RepeatedIfPass;
import org.specs.matisselib.passes.cir.ShortCircuitedConditionalBuilderPass;
import org.specs.matisselib.passes.cir.WhileConditionBuilderPass;
import org.specs.matisselib.passes.posttype.AccessSizeEliminationPass;
import org.specs.matisselib.passes.posttype.AllocationSimplifierPass;
import org.specs.matisselib.passes.posttype.AllocationValueEliminationPass;
import org.specs.matisselib.passes.posttype.BasicAccessSimplifierPass;
import org.specs.matisselib.passes.posttype.ColonEliminationPass;
import org.specs.matisselib.passes.posttype.CombineSizeEliminationPass;
import org.specs.matisselib.passes.posttype.ConstantBranchEliminationPass;
import org.specs.matisselib.passes.posttype.ConvertMatrixAccessesPass;
import org.specs.matisselib.passes.posttype.ConvertToRangePass;
import org.specs.matisselib.passes.posttype.ConvertToSetAllPass;
import org.specs.matisselib.passes.posttype.DuplicatedReadEliminationPass;
import org.specs.matisselib.passes.posttype.ElementWisePass;
import org.specs.matisselib.passes.posttype.EndEliminationPass;
import org.specs.matisselib.passes.posttype.FullRangeEliminationPass;
import org.specs.matisselib.passes.posttype.GetOrFirstSimplificationPass;
import org.specs.matisselib.passes.posttype.HorzcatEliminationPass;
import org.specs.matisselib.passes.posttype.LogicalMatrixAccessPass;
import org.specs.matisselib.passes.posttype.LoopAccumulatorExtractorPass;
import org.specs.matisselib.passes.posttype.LoopFusionPass;
import org.specs.matisselib.passes.posttype.LoopInterchangePass;
import org.specs.matisselib.passes.posttype.LoopInvariantCodeMotionPass;
import org.specs.matisselib.passes.posttype.LoopIterationSimplificationPass;
import org.specs.matisselib.passes.posttype.LoopMatrixCopyEliminationPass;
import org.specs.matisselib.passes.posttype.LoopMatrixDependencyEliminationPass;
import org.specs.matisselib.passes.posttype.LoopStartNormalizationPass;
import org.specs.matisselib.passes.posttype.MatrixAllocationFromSizeSimplicationPass;
import org.specs.matisselib.passes.posttype.MultiGetEliminationPass;
import org.specs.matisselib.passes.posttype.MultiSetConstructionPass;
import org.specs.matisselib.passes.posttype.NoDuplicatedExportsValidationPass;
import org.specs.matisselib.passes.posttype.OptimizationOpportunityReporterPass;
import org.specs.matisselib.passes.posttype.RedundantAllocationEliminationPass;
import org.specs.matisselib.passes.posttype.RedundantCastEliminationPass;
import org.specs.matisselib.passes.posttype.RedundantOutputEliminationPass;
import org.specs.matisselib.passes.posttype.RedundantSizeCheckPass;
import org.specs.matisselib.passes.posttype.RedundantTransposeEliminationPass;
import org.specs.matisselib.passes.posttype.ReferenceArgumentDuplicationPass;
import org.specs.matisselib.passes.posttype.RemoveTypeStringsPass;
import org.specs.matisselib.passes.posttype.ScalarValidateBooleanRemovalPass;
import org.specs.matisselib.passes.posttype.SetAllEliminationPass;
import org.specs.matisselib.passes.posttype.SetOutputPropagationPass;
import org.specs.matisselib.passes.posttype.ShapePropagationPass;
import org.specs.matisselib.passes.posttype.SimpleAccessSimplificationPass;
import org.specs.matisselib.passes.posttype.SimpleMultiSetEliminationPass;
import org.specs.matisselib.passes.posttype.SymjaConstantBuilderPass;
import org.specs.matisselib.passes.posttype.TableSimplificationPass;
import org.specs.matisselib.passes.posttype.TrivialAccessSizeEliminationPass;
import org.specs.matisselib.passes.posttype.TrivialLoopEliminationPass;
import org.specs.matisselib.passes.posttype.UnnecessaryValidationEliminationPass;
import org.specs.matisselib.passes.posttype.UselessMatrixAssignmentRemovalPass;
import org.specs.matisselib.passes.posttype.ValidateSameSizeEliminationPass;
import org.specs.matisselib.passes.posttype.ValueAllocationToSetAllConversionPass;
import org.specs.matisselib.passes.posttype.VerticalFlattenEliminationPass;
import org.specs.matisselib.passes.posttype.loopgetsimplifier.BoundsCheckMotionPass;
import org.specs.matisselib.passes.posttype.loopgetsimplifier.MatrixPreallocatorPass;
import org.specs.matisselib.passes.posttype.loopinterchange.MemoryLoopInterchangeFormat;
import org.specs.matisselib.passes.posttype.reductionelimination.CumulativeReductionEliminationPass;
import org.specs.matisselib.passes.posttype.reductionelimination.DotReductionEliminationPass;
import org.specs.matisselib.passes.posttype.reductionelimination.MinMax3ReductionEliminationPass;
import org.specs.matisselib.passes.ssa.AddAssumeMatrixIndicesInRangeDirectivePass;
import org.specs.matisselib.passes.ssa.AddAssumeMatrixSizesMatchDirectivePass;
import org.specs.matisselib.passes.ssa.ArrayAccessSimplifierPass;
import org.specs.matisselib.passes.ssa.AssumeBuilderPass;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.passes.ssa.ConvertToCssaPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.EmptyBranchEliminationPass;
import org.specs.matisselib.passes.ssa.FixedAccessPropagationPass;
import org.specs.matisselib.passes.ssa.RedundantAssignmentEliminationPass;
import org.specs.matisselib.passes.ssa.ReorderPhiInstructionsPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipeBuilder;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class DefaultRecipes {
    public static final MatlabRecipe DefaultMatlabASTTypeInferenceRecipe;
    public static final CRecipe DefaultCRecipe;

    static {
        MatlabRecipeBuilder preBuilder = new MatlabRecipeBuilder();

        preBuilder.addPass(new GlobalArgumentConflictCheckerPass());
        preBuilder.addPass(new BasicReturnRemoverPass());
        preBuilder.addPass(new ReturnRemoverPass());
        preBuilder.addPass(new ElseIfUnrollerPass());
        preBuilder.addPass(new CommandReplacementPass());
        preBuilder.addPass(new OperatorReplacementPass());
        preBuilder.addPass(new MatrixReplacementPass());
        preBuilder.addPass(new CellReplacementPass());
        preBuilder.addPass(new WhileSimplifierPass());
        preBuilder.addPass(new StringConversionPass());
        // preBuilder.addPass(new CommentRemoverPass());

        DefaultMatlabASTTypeInferenceRecipe = preBuilder.getRecipe();

        CRecipeBuilder cBuilder = new CRecipeBuilder();

        cBuilder.addPass(new ConstantPropagationPass());
        cBuilder.addPass(new ForSimplifierPass());
        cBuilder.addPass(new ShortCircuitedConditionalBuilderPass());
        cBuilder.addPass(new WhileConditionBuilderPass());
        cBuilder.addPass(new ElseIfBuilderPass());
        cBuilder.addPass(new RedundantReturnRemovalPass());
        cBuilder.addPass(new EmptyElseEliminationPass());
        cBuilder.addPass(new RepeatedIfPass());

        DefaultCRecipe = cBuilder.getRecipe();
    }

    public static SsaRecipe getDefaultPreTypeInferenceRecipe(DataStore setup) {
        SsaRecipeBuilder ssaBuilder = new SsaRecipeBuilder();

        ssaBuilder.addPass(new SsaValidatorPass("initial-ssa"));

        if (setup.get(MatisseLibOption.ASSUME_ALL_MATRIX_ACCESSES_ARE_IN_RANGE)) {
            ssaBuilder.addPass(new AddAssumeMatrixIndicesInRangeDirectivePass());
        }
        if (setup.get(MatisseLibOption.ASSUME_ALL_MATRIX_SIZES_MATCH)) {
            ssaBuilder.addPass(new AddAssumeMatrixSizesMatchDirectivePass());
        }

        ssaBuilder.addPass(new RedundantAssignmentEliminationPass(false));
        ssaBuilder.addPass(new DeadCodeEliminationPass());
        ssaBuilder.addPass(new EmptyBranchEliminationPass());
        ssaBuilder.addPass(new BlockReorderingPass());
        ssaBuilder.addPass(new AssumeBuilderPass());
        ssaBuilder.addPass(new SsaValidatorPass("before-type-inference"));

        return ssaBuilder.getRecipe();
    }

    public static SsaRecipe getTestPreTypeInferenceRecipe() {
        return getDefaultPreTypeInferenceRecipe(DataStore.newInstance("test-recipe"));
    }

    public static PostTypeInferenceRecipe getOptimizingBasePostTypeInferenceRecipe() {
        PostTypeInferenceRecipeBuilder postBuilder = new PostTypeInferenceRecipeBuilder();

        postBuilder.addPass(new LogicalMatrixAccessPass());
        postBuilder.addPass(new ScalarValidateBooleanRemovalPass());
        postBuilder.addPass(new RedundantOutputEliminationPass());
        postBuilder.addPass(new RemoveTypeStringsPass());
        postBuilder.addPass(new ConstantBranchEliminationPass());
        postBuilder.addPass(new RedundantCastEliminationPass());
        postBuilder.addPass(new RedundantAssignmentEliminationPass(false));
        postBuilder.addPass(new TableSimplificationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());

        postBuilder.addPass(new ConvertMatrixAccessesPass()); // HACK: Make sizegroupinformation faster
        postBuilder.addPass(new HorzcatEliminationPass());
        postBuilder.addPass(new ConvertToRangePass());
        postBuilder.addPass(new ConvertToSetAllPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new FullRangeEliminationPass());
        postBuilder.addPass(new SetAllEliminationPass());
        postBuilder.addPass(new ColonEliminationPass());
        postBuilder.addPass(new MultiGetEliminationPass());
        postBuilder.addPass(new SimpleMultiSetEliminationPass());
        postBuilder.addPass(new ConvertMatrixAccessesPass());
        postBuilder.addPass(new TrivialLoopEliminationPass());
        postBuilder.addPass(new BasicAccessSimplifierPass());
        postBuilder.addPass(new MatrixPreallocatorPass());
        postBuilder.addPass(new VerticalFlattenEliminationPass());
        postBuilder.addPass(new ElementWisePass());
        postBuilder.addPass(new RedundantSizeCheckPass());
        postBuilder.addPass(new ValidateSameSizeEliminationPass());
        postBuilder.addPass(new DotReductionEliminationPass());
        postBuilder.addPass(new CumulativeReductionEliminationPass());
        postBuilder.addPass(new MinMax3ReductionEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new LoopAccumulatorExtractorPass());
        postBuilder.addPass(new AllocationValueEliminationPass());
        postBuilder.addPass(new ValueAllocationToSetAllConversionPass());
        postBuilder.addPass(new SetAllEliminationPass());
        postBuilder.addPass(new SsaValidatorPass("mid-optimizations"));

        postBuilder.addPass(new HorzcatEliminationPass());
        postBuilder.addPass(new TrivialLoopEliminationPass());
        postBuilder.addPass(new SimpleAccessSimplificationPass());
        postBuilder.addPass(new GetOrFirstSimplificationPass());
        postBuilder.addPass(new RedundantAssignmentEliminationPass(false));
        postBuilder.addPass(new RedundantTransposeEliminationPass());
        postBuilder.addPass(new ShapePropagationPass());
        postBuilder.addPass(new LoopAccumulatorExtractorPass());
        postBuilder.addPass(new SsaValidatorPass("after-accumulator-extractor"));
        postBuilder.addPass(new BlockReorderingPass());
        postBuilder.addPass(new TrivialAccessSizeEliminationPass());
        postBuilder.addPass(new LoopStartNormalizationPass());
        postBuilder.addPass(new LoopInvariantCodeMotionPass());
        postBuilder.addPass(new LoopInterchangePass("loop_interchange", MemoryLoopInterchangeFormat.class));
        postBuilder.addPass(new LoopFusionPass());
        postBuilder.addPass(new SsaValidatorPass("after-loop-fusion"));
        postBuilder.addPass(new DuplicatedReadEliminationPass());
        postBuilder.addPass(new ArrayAccessSimplifierPass());
        postBuilder.addPass(new UselessMatrixAssignmentRemovalPass());
        postBuilder.addPass(new AllocationSimplifierPass());
        postBuilder.addPass(new MatrixAllocationFromSizeSimplicationPass());
        postBuilder.addPass(new LoopMatrixCopyEliminationPass());
        postBuilder.addPass(new AccessSizeEliminationPass());
        postBuilder.addPass(new BoundsCheckMotionPass());
        postBuilder.addPass(new UnnecessaryValidationEliminationPass());
        postBuilder.addPass(new FixedAccessPropagationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new RedundantAllocationEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new LoopMatrixDependencyEliminationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new RedundantSizeCheckPass());
        postBuilder.addPass(new RedundantAssignmentEliminationPass(true));
        postBuilder.addPass(new LoopInvariantCodeMotionPass());
        postBuilder.addPass(new LoopInterchangePass("loop_interchange", MemoryLoopInterchangeFormat.class));
        postBuilder.addPass(new SetOutputPropagationPass());
        postBuilder.addPass(new SymjaConstantBuilderPass());
        postBuilder.addPass(new SsaValidatorPass("after-optimizations"));

        return postBuilder.getRecipe();
    }

    public static PostTypeInferenceRecipe getTestBasePostTypeInferenceRecipe() {
        return getOptimizingBasePostTypeInferenceRecipe();
    }

    public static PostTypeInferenceRecipe getTestPostTypeInferenceRecipe(boolean convertToCssa) {
        return PostTypeInferenceRecipe.combine(getTestBasePostTypeInferenceRecipe(),
                getFinalPasses(convertToCssa));
    }

    public static PostTypeInferenceRecipe getFinalPasses(boolean convertToCssa) {
        PostTypeInferenceRecipeBuilder postBuilder = new PostTypeInferenceRecipeBuilder();

        postBuilder.addPass(new SsaValidatorPass("after-optimizations"));

        // constant branch is used because the type inference skips "dead" branches.
        postBuilder.addPass(new ConstantBranchEliminationPass());

        // The C code generator for multi-get is unreliable
        postBuilder.addPass(new MultiGetEliminationPass());

        // Instructions that are not supported by the C code generator
        postBuilder.addPass(new LogicalMatrixAccessPass());
        postBuilder.addPass(new VerticalFlattenEliminationPass());
        postBuilder.addPass(new FullRangeEliminationPass());
        postBuilder.addPass(new SetAllEliminationPass());
        postBuilder.addPass(new EndEliminationPass());
        postBuilder.addPass(new AccessSizeEliminationPass());
        postBuilder.addPass(new CombineSizeEliminationPass());
        postBuilder.addPass(new LoopIterationSimplificationPass());
        postBuilder.addPass(new DeadCodeEliminationPass());
        postBuilder.addPass(new MultiSetConstructionPass());
        postBuilder.addPass(new DeadCodeEliminationPass());

        // Diagnostics
        postBuilder.addPass(new OptimizationOpportunityReporterPass());

        postBuilder.addPass(new ReorderPhiInstructionsPass());
        postBuilder.addPass(new BlockReorderingPass());
        postBuilder.addPass(new NoDuplicatedExportsValidationPass());

        if (convertToCssa) {
            postBuilder.addPass(new SsaValidatorPass("before-cssa"));
            postBuilder.addPass(new ReferenceArgumentDuplicationPass());
            postBuilder.addPass(new ConvertToCssaPass());
        }

        return postBuilder.getRecipe();
    }

    private DefaultRecipes() {
    }
}
