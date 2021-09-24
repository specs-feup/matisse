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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.jOptions.IvdepType;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.sizeinfo.ScalarValueInformation;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;

import pt.up.fe.specs.util.reporting.Reporter;

public class ForProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ForInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ForInstruction forInstruction = (ForInstruction) instruction;

        Reporter reporter = builder.getReporter();

        // FIXME: Once we have proper variable coalescing, the variable name choice is likely to
        // go wrong.
        // Most likely, the interval/end variables should be considered to be alive for the duration of the block.
        // If that is not doable, then perhaps, we create temporaries before the loop for the interval and end.
        CNode startVariable = builder.generateVariableExpressionForSsaName(currentBlock,
                forInstruction.getStart(),
                true);
        CNode intervalVariable = builder.generateVariableExpressionForSsaName(currentBlock,
                forInstruction.getInterval(),
                false,
                true);
        CNode endVariable = builder.generateVariableExpressionForSsaName(currentBlock,
                forInstruction.getEnd(),
                false,
                true);

        int loopBlockId = forInstruction.getLoopBlock();
        int endBlockId = forInstruction.getEndBlock();

        SsaBlock loopBlock = builder.getInstance().getBlocks().get(loopBlockId);

        NumericTypeV2 intType = builder.getCurrentProvider().getNumerics()
                .newInt();
        Variable inductionVar = findInductionVariable(builder, loopBlock)
                .orElseGet(() -> builder.generateTemporary("i", intType));

        CInstructionList loopInstructionList = new CInstructionList(builder.getInstance().getFunctionType());

        builder.generateCodeForBlock(loopBlockId, loopInstructionList);

        VariableType intervalType = builder.getInstance()
                .getVariableType(forInstruction.getInterval())
                .get();

        boolean intervalCanBeZero = true;
        boolean intervalCanBeNegative = true;
        boolean intervalIsKnownNegative = false;

        if (ScalarUtils.isScalar(intervalType)) {
            if (ScalarUtils.hasConstant(intervalType)) {
                String constantString = ScalarUtils.getConstantString(intervalType);
                intervalCanBeZero = constantString.matches("^-?0(\\.0+)?$");
                intervalCanBeNegative = intervalIsKnownNegative = !intervalCanBeZero && constantString.startsWith("-");
            } else {
                intervalCanBeNegative = !((ScalarType) intervalType).scalar().isUnsigned();
            }
        }
        CNode signMultiplier = builder.generateTemporaryNode("sign", intType);
        CNode forHeader = generateForHeader(builder,
                startVariable, intervalVariable, endVariable,
                signMultiplier,
                inductionVar,
                intervalCanBeNegative, intervalIsKnownNegative);

        List<CNode> annotations = new ArrayList<>();

        IvdepType ivdep = builder.getInstance().getProviderData().getSettings().get(MatlabToCKeys.IVDEP_TYPE);
        if (ivdep != IvdepType.NONE && !hasLoopCarriedDependencies(builder, forInstruction)) {
            annotations.add(CNodeFactory.newPragma(ivdep.getPragmaContent()));
        }

        List<CNode> forInstructions = new ArrayList<>();
        forInstructions.addAll(annotations);
        forInstructions.add(forHeader);
        forInstructions.addAll(loopInstructionList.get());
        CNode forBlock = CNodeFactory.newBlock(forInstructions);

        boolean notifiedOptimizationIssue = false;

        List<CNode> nodesToInsert = new ArrayList<>();
        if (intervalCanBeNegative && !intervalIsKnownNegative) {
            List<CNode> lessThanZeroNodes = Arrays.asList(intervalVariable, CNodeFactory.newCNumber(0));
            ProviderData lessThanZeroData = builder.getCurrentProvider().createFromNodes(lessThanZeroNodes);
            CNode lessThanZeroCondition = COperator.LessThan
                    .getCheckedInstance(lessThanZeroData)
                    .newFunctionCall(lessThanZeroNodes);

            List<CNode> thenInstructions = Arrays.asList(CNodeFactory.newAssignment(signMultiplier,
                    CNodeFactory.newCNumber(-1)));
            List<CNode> elseInstructions = Arrays.asList(CNodeFactory.newAssignment(signMultiplier,
                    CNodeFactory.newCNumber(1)));

            reporter.emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY,
                    "Sign of interval value unknown at compile time.");
            notifiedOptimizationIssue = true;

            nodesToInsert
                    .add(CNodeFactory
                            .newComment("MATISSE: Sign of interval value unknown at compile time."));
            nodesToInsert.add(IfNodes.newIfThenElse(lessThanZeroCondition, thenInstructions, elseInstructions));
        }
        nodesToInsert.add(forBlock);

        if (intervalCanBeZero) {
            if (!notifiedOptimizationIssue) {
                reporter.emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY,
                        "MATISSE could not prove that the interval value is not 0.");
            }

            List<CNode> notZeroNodes = Arrays.asList(intervalVariable, CNodeFactory.newCNumber(0));
            ProviderData notZeroData = builder.getCurrentProvider().createFromNodes(notZeroNodes);
            CNode notZeroCondition = COperator.NotEqual
                    .getCheckedInstance(notZeroData)
                    .newFunctionCall(notZeroNodes);
            nodesToInsert = Arrays.asList(IfNodes.newIfThen(notZeroCondition, nodesToInsert));
        }

        currentBlock.addInstructions(nodesToInsert);

        builder.generateCodeForBlock(endBlockId, currentBlock);
    }

    private boolean hasLoopCarriedDependencies(SsaToCBuilderService builder, ForInstruction xfor) {
        TypedInstance instance = builder.getInstance();
        ControlFlowGraph cfg = builder.getControlFlowGraph();

        try (ScalarValueInformation info = builder.newScalarValueInformationSolver()) {
            int loopBlockId = xfor.getLoopBlock();
            SsaBlock block = instance.getBlock(loopBlockId);

            Set<String> declaredVariables = new HashSet<>();
            List<SimpleGetInstruction> gets = new ArrayList<>();
            List<SimpleSetInstruction> sets = new ArrayList<>();
            String iter = null;
            Map<String, String> copies = new HashMap<>();

            for (SsaInstruction instruction : block.getInstructions()) {
                declaredVariables.addAll(instruction.getOutputs());
            }

            instance
                    .getFlattenedInstructionsList()
                    .stream()
                    .flatMap(instruction -> instruction.getOutputs().stream())
                    .distinct()
                    .filter(variable -> !declaredVariables.contains(variable))
                    .forEach(variable -> info.addAlias(variable + "#ctx1", variable + "#ctx2"));

            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof FunctionCallInstruction) {
                    info.addScalarFunctionCallInformation((FunctionCallInstruction) instruction, "ctx1");
                    info.addScalarFunctionCallInformation((FunctionCallInstruction) instruction, "ctx2");
                }

                if (instruction instanceof IterInstruction) {
                    iter = ((IterInstruction) instruction).getOutput();
                    continue;
                }

                if (!instruction.getOwnedBlocks().isEmpty()) {
                    // Only add ivdep to innermost loops
                    return true;
                }

                if (instruction.getInstructionType() == InstructionType.HAS_SIDE_EFFECT
                        || instruction.getInstructionType() == InstructionType.HAS_VALIDATION_SIDE_EFFECT) {

                    // Treat side effects as a loop carried dependency.
                    log("Side effect: " + instruction);
                    return true;
                }

                if (instruction instanceof SimpleGetInstruction) {
                    gets.add((SimpleGetInstruction) instruction);
                    continue;
                }

                if (instruction instanceof SimpleSetInstruction) {
                    sets.add((SimpleSetInstruction) instruction);
                    continue;
                }
                if (instruction instanceof ParallelCopyInstruction) {
                    ParallelCopyInstruction copy = (ParallelCopyInstruction) instruction;
                    for (int i = 0; i < copy.getInputVariables().size(); ++i) {
                        copies.put(copy.getOutputs().get(i), copy.getInputVariables().get(i));
                    }
                }
                if (instruction.getInstructionType() == InstructionType.DECORATOR
                        || instruction.getInstructionType() == InstructionType.NO_SIDE_EFFECT ||
                        instruction.getInstructionType() == InstructionType.LINE) {
                    continue;
                }

                // Unsupported instruction type.
                log("Unsupported instruction: " + instruction);
                return true;
            }

            int endBlockId = xfor.getEndBlock();
            for (SsaInstruction instruction : instance.getBlock(endBlockId).getInstructions()) {
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    String loopVar = phi.getInputVariables().get(phi.getSourceBlocks().indexOf(loopBlockId));
                    String sourceLoopVar = copies.get(loopVar);

                    if (sourceLoopVar == null) {
                        return true;
                    }

                    if (!sets.stream().anyMatch(set -> set.getOutput().equals(sourceLoopVar))) {
                        // Loop carried dependencies must be sets.
                        log("Loop carried dependency is not a set: " + sourceLoopVar);
                        return true;
                    }
                }
            }

            for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
                if (blockId == loopBlockId) {
                    continue;
                }

                SsaBlock blockToCheck = instance.getBlock(blockId);
                for (int instructionId = blockId == endBlockId
                        ? BlockUtils.getAfterPhiInsertionPoint(instance.getBlock(endBlockId))
                        : 0; instructionId < blockToCheck
                                .getInstructions().size(); ++instructionId) {

                    for (String variable : declaredVariables) {
                        if (blockToCheck.getInstructions().get(instructionId).getInputVariables().contains(variable)) {

                            log("Variable " + variable + " is used outside loop");
                            return true;
                        }
                    }
                }
            }

            for (int i = 0; i < sets.size(); ++i) {
                SimpleSetInstruction set1 = sets.get(i);
                String set1Name = builder.generateVariableNodeForSsaName(set1.getOutput()).getCode();

                for (int j = i + 1; j < sets.size(); ++j) {
                    SimpleSetInstruction set2 = sets.get(j);

                    String set2Name = builder.generateVariableNodeForSsaName(set2.getOutput()).getCode();
                    if (!set1Name.equals(set2Name)) {
                        continue;
                    }

                    if (iter == null) {
                        // Iteration not used, collision happens.
                        log("Iteration not used, collision at " + set1 + ", " + set2);
                        return true;
                    }

                    if (info.mayCollide(set1.getIndices(), set2.getIndices(), Arrays.asList(iter), "ctx1", "ctx2")) {
                        log("Collision at " + set1 + ", " + set2);
                        return true;
                    }
                }
            }

            for (SimpleSetInstruction set : sets) {
                for (SimpleGetInstruction get : gets) {
                    String setName = builder.generateVariableNodeForSsaName(set.getOutput()).getCode();
                    String getName = builder.generateVariableNodeForSsaName(get.getInputMatrix()).getCode();

                    if (!setName.equals(getName)) {
                        continue;
                    }

                    if (iter == null) {
                        // Iteration not used, collision happens.
                        log("Iteration not used, collision at " + set + ", " + get);
                        return true;
                    }

                    if (info.mayCollide(set.getIndices(), get.getIndices(), Arrays.asList(iter), "ctx1", "ctx2")) {
                        log("Collision at " + set + ", " + get);
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static void log(String message) {
        // System.out.println(message);
    }

    private static CNode generateForHeader(SsaToCBuilderService builder,
            CNode startVariable, CNode intervalVariable, CNode endVariable,
            CNode signMultiplier,
            Variable inductionVar,
            boolean intervalCanBeNegative, boolean intervalIsKnownNegative) {

        CNode startValue = startVariable;
        CNode endValue = endVariable;
        COperator comparisonOperator = COperator.LessThanOrEqual;
        CNode comparisonLeft = CNodeFactory.newVariable(inductionVar);
        CNode comparisonRight = endValue;

        if (intervalIsKnownNegative) {
            comparisonOperator = COperator.GreaterThanOrEqual;
        } else if (intervalCanBeNegative) {
            List<CNode> leftMultiplyNodes = Arrays.asList(CNodeFactory.newVariable(inductionVar), signMultiplier);
            ProviderData leftMultiplyData = builder.getCurrentProvider()
                    .createFromNodes(leftMultiplyNodes);

            comparisonLeft = COperator.Multiplication
                    .getCheckedInstance(leftMultiplyData)
                    .newFunctionCall(leftMultiplyNodes);

            List<CNode> rightMultiplyNodes = Arrays.asList(endValue, signMultiplier);
            ProviderData rightMultiplyData = builder.getCurrentProvider()
                    .createFromNodes(rightMultiplyNodes);

            comparisonRight = COperator.Multiplication
                    .getCheckedInstance(rightMultiplyData)
                    .newFunctionCall(rightMultiplyNodes);
        }

        List<CNode> comparisonExpressionNodes = Arrays.asList(comparisonLeft, comparisonRight);
        ProviderData comparisonExpressionData;
        if (ScalarUtils.isUnsigned(comparisonLeft.getVariableType()) != ScalarUtils
                .isUnsigned(comparisonRight.getVariableType())) {
            VariableType doubleType = builder.getCurrentProvider().getNumerics().newDouble();

            comparisonExpressionData = builder.getCurrentProvider()
                    .create(doubleType, doubleType);
        } else {
            comparisonExpressionData = builder.getCurrentProvider()
                    .createFromNodes(comparisonExpressionNodes);
        }
        CNode comparisonNode = comparisonOperator
                .getCheckedInstance(comparisonExpressionData)
                .newFunctionCall(comparisonExpressionNodes);

        AssignmentNode assignment = CNodeFactory.newAssignment(inductionVar, startValue);

        List<CNode> addNodes = Arrays.asList(CNodeFactory.newVariable(inductionVar), intervalVariable);
        ProviderData addData = builder.getCurrentProvider()
                .createFromNodes(addNodes);
        FunctionCallNode addExpr = COperator.Addition
                .getCheckedInstance(addData)
                .newFunctionCall(addNodes);
        AssignmentNode incrExpr = CNodeFactory.newAssignment(inductionVar, addExpr);

        CNode forHeader = new ForNodes(builder.getCurrentProvider())
                .newForInstruction(assignment, comparisonNode, incrExpr);
        return forHeader;
    }

    private static Optional<Variable> findInductionVariable(SsaToCBuilderService builder, SsaBlock loopBlock) {
        for (SsaInstruction instruction : loopBlock.getInstructions()) {
            if (instruction instanceof IterInstruction) {
                String variable = instruction.getOutputs().get(0);

                CNode node = builder.generateVariableNodeForSsaName(variable);
                return Optional.of(new Variable(node.getCode(), node.getVariableType()));
            }
        }

        return Optional.empty();
    }

}
