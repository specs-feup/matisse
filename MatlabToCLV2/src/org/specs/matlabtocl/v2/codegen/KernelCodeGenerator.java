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

package org.specs.matlabtocl.v2.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Passes.CPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.CodeBuilder.VariableManager;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.functionproperties.DumpSsaProperty;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matlabtocl.v2.MatisseCLKeys;
import org.specs.matlabtocl.v2.MatisseCLSettingsKeys;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionOutput;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.functions.builtins.CLBuiltinMathFunction;
import org.specs.matlabtocl.v2.functions.builtins.CLBuiltinPositioningFunction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.instructions.InitializationInstruction;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.collections.MultiMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.reporting.Reporter;

public final class KernelCodeGenerator extends CommonOpenCLCodeGenerator {
    private static final boolean ENABLE_LOG = false;

    private final ParallelRegionInstance parallelInstance;
    private final ParallelLoopInformation parallelLoop;
    private final ParallelRegionSettings parallelSettings;
    private Map<String, String> initialReductionNames;
    private Map<String, String> finalReductionNames;
    private Set<String> importedVariables;
    private HashMap<String, String> extraCopies;
    private final int kernelId;
    private CLVersion requiredVersion;
    private List<KernelArgument> kernelArguments;
    private FunctionTypeBuilder functionTypeBuilder;
    private final CInstructionList argumentConstructionCode = new CInstructionList();
    private MultiMap<Integer, Reduction> localReductions;
    private Map<String, ReductionComputation> localReductionComputations = new HashMap<>();

    private List<CNode> numTasksNodes = new ArrayList<>();
    private List<VariableNode> globalIdNodes = new ArrayList<>();
    private List<VariableNode> globalSizeNodes = new ArrayList<>();
    private List<CNode> localIdNodes = new ArrayList<>();
    private List<CNode> localSizeNodes = new ArrayList<>();
    private List<CNode> groupIdNodes = new ArrayList<>();
    private CNode subGroupLocalIdNode;
    private CNode subGroupSizeNode;
    private CNode numSubGroupsNode;
    private CNode subGroupIdNode;
    private CNode subGroupTrueEndCheckNode;
    private List<VariableNode> distributionIdNodes = new ArrayList<>();
    private List<VariableNode> absoluteTaskIdNodes = new ArrayList<>();
    private Map<String, String> parallelCopySource = new HashMap<>();

    private KernelCodeGenerator(
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            VariableAllocator allocator,
            ParallelRegionSettings parallelSettings,
            MultiMap<Integer, Reduction> localReductions,
            DataStore passData,
            Reporter reporter,
            int kernelId) {

        super(passData,
                allocator,
                buildProviderData(codeGenerationStrategyProvider,
                        parallelSettings,
                        parallelInstance.getTypeGetter(),
                        reporter));

        this.parallelInstance = parallelInstance;
        this.parallelLoop = parallelLoop;
        this.kernelId = kernelId;
        this.parallelSettings = parallelSettings;
        this.localReductions = localReductions;
    }

    private static ProviderData buildProviderData(CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            ParallelRegionSettings parallelSettings,
            Function<String, Optional<VariableType>> rawTypeGetter,
            Reporter reporter) {
        ProviderData providerData = ProviderData.newInstance("cl-provider-data");

        providerData = providerData.withReportService(reporter);

        Integer[] localSizes = new Integer[parallelSettings.localSizes.size()];
        for (int index = 0; index < localSizes.length; ++index) {
            String var = parallelSettings.localSizes.get(index);
            Number constant = ScalarUtils.getConstant(rawTypeGetter.apply(var).get());

            if (constant != null) {
                int value = constant.intValue();
                localSizes[index] = value;
            }
        }

        DataStore oldSettings = providerData.getSettings();
        DataStore newSettings = DataStore.newInstance("kernel.getProviderData().getSettings()");
        newSettings.set(oldSettings);
        newSettings.set(MatisseCLSettingsKeys.GROUP_SIZES, localSizes);
        newSettings.set(MatisseCLKeys.SUB_GROUP_SIZE, codeGenerationStrategyProvider.getSubGroupSize());
        newSettings.set(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK,
                codeGenerationStrategyProvider.getNvidiaSubgroupAsWarpFallback());
        providerData = providerData.withSettings(newSettings);

        return providerData;
    }

    private boolean calledGetBody = false;

    @Override
    protected FunctionBody getBody() {
        assert !calledGetBody;
        calledGetBody = true;

        FunctionBody body = new FunctionBody();
        if (this.parallelInstance.getBody().hasProperty(DumpSsaProperty.class)) {
            body.addProperty(new DumpSsaProperty());
        }
        int declarationBlockId = this.parallelLoop.loopDeclarationBlockIds.get(0);
        ForInstruction rootFor = (ForInstruction) this.parallelInstance
                .getBody()
                .getBlock(declarationBlockId)
                .getEndingInstruction()
                .get();
        Map<Integer, Integer> newNames = new HashMap<>();

        Set<String> usedVariables = new HashSet<>();
        usedVariables.addAll(parallelSettings.getKernelInputVariables());
        Set<String> declaredVariables = new HashSet<>();
        this.initialReductionNames = new HashMap<>();
        this.finalReductionNames = new HashMap<>();

        Queue<Integer> pendingBlocks = new LinkedList<>();
        int loopStartBlockId = rootFor.getLoopBlock();
        pendingBlocks.add(loopStartBlockId);
        while (!pendingBlocks.isEmpty()) {
            int blockId = pendingBlocks.poll();
            if (newNames.containsKey(blockId)) {
                continue;
            }
            SsaBlock newBlock = this.parallelInstance.getBody().getBlock(blockId).copy();

            int newBlockId = body.addBlock(newBlock);
            newNames.put(blockId, newBlockId);

            ListIterator<SsaInstruction> it = newBlock.getInstructions().listIterator();
            while (it.hasNext()) {
                SsaInstruction instruction = it.next();
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phiInstruction = (PhiInstruction) instruction;
                    int declarationBlockIndex = phiInstruction.getSourceBlocks().indexOf(declarationBlockId);
                    int loopBlockIndex = 1 - declarationBlockIndex;
                    if (declarationBlockIndex != -1) {
                        assert loopBlockIndex == 0 || loopBlockIndex == 1 : "Expected index at 0 or 1, got "
                                + loopBlockIndex + ", at " + phiInstruction;

                        String initialName = phiInstruction.getOutput();

                        this.initialReductionNames.put(initialName,
                                phiInstruction.getInputVariables().get(declarationBlockIndex));
                        this.finalReductionNames.put(initialName,
                                phiInstruction.getInputVariables().get(loopBlockIndex));

                        it.remove();
                        continue;
                    }
                }

                usedVariables.addAll(instruction.getInputVariables());
                declaredVariables.addAll(instruction.getOutputs());
                pendingBlocks.addAll(instruction.getOwnedBlocks());
            }
        }

        this.importedVariables = new HashSet<>(usedVariables);
        this.importedVariables.removeAll(declaredVariables);

        SsaBlock firstBlock = body.getBlock(0);
        for (String variable : this.importedVariables) {
            firstBlock.prependInstruction(new InitializationInstruction(variable));
        }

        body.renameBlocks(newNames);

        applyPasses(body);

        // Update localReductions block IDs
        MultiMap<Integer, Reduction> newLocalReductions = new MultiMap<>();
        for (Integer localReductionOldBlockId : localReductions.keySet()) {
            assert localReductionOldBlockId != null;
            Integer localReductionNewBlockIdNullable = newNames.get(localReductionOldBlockId);
            assert localReductionNewBlockIdNullable != null : "Missing value for old block ID #"
                    + localReductionOldBlockId
                    + ", for newNames=" + newNames;
            int localReductionNewBlockId = localReductionNewBlockIdNullable;
            newLocalReductions.put(localReductionNewBlockId, localReductions.get(localReductionOldBlockId));
        }
        localReductions = newLocalReductions;

        for (ParallelCopyInstruction instruction : body
                .getFlattenedInstructionsIterable(ParallelCopyInstruction.class)) {
            for (int i = 0; i < instruction.getOutputs().size(); ++i) {
                String input = instruction.getInputVariables().get(i);
                String output = instruction.getOutputs().get(i);

                parallelCopySource.put(output, input);
            }
        }

        for (Reduction reduction : localReductions.flatValues()) {

            String loopStartName = reduction.getLoopVariables().get(0).loopStart;
            reduction.setInitialName(loopStartName); // HACK

            String nameInFinalPhi = parallelCopySource.get(reduction.getFinalName());
            assert nameInFinalPhi != null;
            reduction.setFinalName(nameInFinalPhi);
        }

        return body;
    }

    @Override
    protected Optional<VariableType> getRawTypeForSsaVariable(String name) {
        return this.parallelInstance.getType(name);
    }

    @Override
    protected String makeTemporary(String proposedName, VariableType type) {
        return this.parallelInstance.makeTemporary(proposedName, type);
    }

    @Override
    protected VariableAllocation buildVariableAllocation(VariableAllocator allocator) {
        VariableAllocation variableAllocation = super.buildVariableAllocation(allocator);

        extraCopies = new HashMap<>();

        for (String reduction : this.initialReductionNames.keySet()) {
            String initialName = this.initialReductionNames.get(reduction);

            if (variableAllocation.getGroupIdForVariable(initialName) != -1) {
                System.out.println("Variable " + initialName + " already has group.");
                System.out.println("Reductions: " + initialReductionNames);
                System.out.println(body);
            }

            int previousNameGroup;
            if (variableAllocation.getGroupIdForVariable(initialName) == -1) {
                previousNameGroup = variableAllocation.addIsolatedVariable(initialName);

                int newNameGroup = variableAllocation.getGroupIdForVariable(reduction);

                variableAllocation.merge(previousNameGroup, newNameGroup);
            } else {
                extraCopies.put(initialName, reduction);
                extraCopies.put(reduction, initialName);
            }
        }

        for (String reduction : this.finalReductionNames.keySet()) {
            String finalName = this.finalReductionNames.get(reduction);

            int previousNameGroup = variableAllocation.getGroupIdForVariable(finalName);
            int newNameGroup = variableAllocation.getGroupIdForVariable(reduction);

            if (previousNameGroup != newNameGroup) {
                variableAllocation.merge(previousNameGroup, newNameGroup);
            }
        }

        return variableAllocation;
    }

    public static GeneratedKernel buildImplementation(
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            VariableAllocator allocator,
            ParallelRegionSettings parallelSettings,
            MultiMap<Integer, Reduction> localReductions,
            DataStore passData,
            Reporter reporter,
            int kernelId) {
        if (parallelSettings.schedule != ScheduleStrategy.COOPERATIVE &&
                parallelSettings.schedule != ScheduleStrategy.SUBGROUP_COOPERATIVE) {
            assert localReductions.keySet()
                    .isEmpty() : "Found local reductions, but schedule strategy is not cooperative (got "
                            + parallelSettings.schedule + ")";
        }

        KernelCodeGenerator codeGenerator = new KernelCodeGenerator(codeGenerationStrategyProvider,
                parallelInstance,
                parallelLoop,
                allocator,
                parallelSettings,
                localReductions,
                passData,
                reporter,
                kernelId);
        codeGenerator.setUp();
        return codeGenerator
                .buildImplementation();
    }

    private GeneratedKernel buildImplementation() {
        functionTypeBuilder = FunctionTypeBuilder.newSimple()
                .returningVoid();

        boolean nvidiaWarpFallback = getProviderData().getSettings()
                .get(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK);
        if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE && !nvidiaWarpFallback) {
            requiredVersion = CLVersion.V2_0;
            functionTypeBuilder.addAnnotation(new RequiredCLExtensionAnnotation("cl_khr_subgroups"));
        } else {
            requiredVersion = CLVersion.V1_0;
        }

        if (hasKnownLocalSizeAttribute()) {
            StringBuilder attribute = new StringBuilder();

            attribute.append("__attribute__((reqd_work_group_size(");

            attribute.append(getConstantSizeAtDim(0));
            attribute.append(", ");
            attribute.append(getConstantSizeAtDim(1));
            attribute.append(", ");
            attribute.append(getConstantSizeAtDim(2));

            attribute.append(")))");

            functionTypeBuilder.withPrefix(attribute.toString());
        }

        functionTypeBuilder.withPrefix("__kernel");

        kernelArguments = new ArrayList<>();

        boolean explicitNumTasks = true; // We need this because of the work group size.
        // If we ever make work group size optional, or if we can prove that work item count is a multiple of the
        // workgroup size then we might get away with no explicit number of threads.
        int nestedDepth = this.parallelLoop.loopDeclarationBlockIds.size();

        for (String variable : this.importedVariables) {
            if (isImportedVariableReduction(variable)) {
                continue;
            }

            String importedVariable = extraCopies.getOrDefault(variable, variable);
            String finalName = getFinalName(importedVariable);
            VariableType inputType = getVariableTypeFromSsaName(importedVariable)
                    .orElseThrow(() -> new RuntimeException("Could not find type of " + importedVariable));

            String dataVariableName = finalName + "_data";
            log("Importing " + importedVariable + " as " + dataVariableName);
            if (inputType instanceof RawBufferMatrixType) {
                KernelArgument kernelArgument = KernelArgument.importData(
                        dataVariableName,
                        importedVariable);
                kernelArguments.add(kernelArgument);
                functionTypeBuilder.addInput(finalName, inputType);
            } else if (inputType instanceof SizedMatrixType) {
                SizedMatrixType sizedMatrix = (SizedMatrixType) inputType;

                KernelArgument bufferArgument = KernelArgument.importData(dataVariableName,
                        importedVariable);
                kernelArguments.add(bufferArgument);
                functionTypeBuilder.addInput(dataVariableName, sizedMatrix.getUnderlyingRawMatrixType());

                SsaToOpenCLBuilderService.buildSizedMatrixExtraParameters(functionTypeBuilder,
                        kernelArguments,
                        argumentConstructionCode,
                        importedVariable,
                        finalName,
                        dataVariableName,
                        sizedMatrix);
            } else if (ScalarUtils.hasConstant(inputType)) {
                argumentConstructionCode.addAssignment(
                        getVariableManager().generateVariableNodeForFinalName(finalName).get(),
                        CNodeFactory.newCNumber(ScalarUtils.getConstant(inputType), inputType));
            } else {
                KernelArgument kernelArgument = KernelArgument.importValue(
                        dataVariableName,
                        importedVariable,
                        (ScalarType) inputType);
                functionTypeBuilder.addInput(finalName, inputType);

                kernelArguments.add(kernelArgument);
            }
        }

        List<ReductionComputation> reductionComputations = new ArrayList<>();

        for (Reduction reduction : this.parallelLoop.reductions) {
            log("Handling reduction of type " + reduction.getReductionType());

            List<ReductionStrategy> strategies = parallelSettings.reductionStrategies
                    .get(reduction.getReductionType());

            String baseInitialSsaName = reduction.getInitialName();
            log("Initial name: " + baseInitialSsaName);
            String initialSsaName = extraCopies.getOrDefault(baseInitialSsaName, baseInitialSsaName);
            log("After adaptation: " + initialSsaName);

            if (strategies == null) {
                throw new NotImplementedException("Reduction type: " + reduction.getReductionType());
            }

            VariableType elementType = this.parallelInstance.getType(initialSsaName).get();
            ReductionOutput output = strategies
                    .stream()
                    .map(strategy -> strategy.prepareReduction(reduction, initialSsaName,
                            getSsaToOpenCLBuilderService(),
                            elementType))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No available reduction mechanism"));

            explicitNumTasks |= output.requiresExplicitNumThreads();

            for (Variable variable : output.getKernelExtraArguments()) {
                functionTypeBuilder.addInput(variable);
            }

            kernelArguments.addAll(output.getKernelCallerArguments());
            ReductionComputation reductionComputation = output.getReductionComputation();
            if (reductionComputation != null) {
                reductionComputations.add(reductionComputation);
            }

            output.getArgumentConstructionCode()
                    .ifPresent(argumentConstructionCode::add);

            requiredVersion = CLVersion.getMaximumVersion(requiredVersion, output.getRequiredVersion());
        }

        if (parallelSettings.schedule != ScheduleStrategy.DIRECT) {
            // To deal with cases where the number of tasks is not a multiple of the coarsening factor.
            // or when the chunks are not evenly divided by the chunks.
            explicitNumTasks = true;
        }

        if (explicitNumTasks) {
            List<Integer> loopDeclarationBlockIds = this.parallelLoop.loopDeclarationBlockIds;
            for (int i = loopDeclarationBlockIds.size() - 1; i >= 0; i--) {
                int loopDeclarationBlockId = loopDeclarationBlockIds.get(i);
                SsaBlock containerBlock = this.parallelInstance.getBody()
                        .getBlock(loopDeclarationBlockId);
                ForInstruction forInstruction = (ForInstruction) containerBlock.getEndingInstruction().get();
                String endVariable = forInstruction.getEnd();

                CNode numTasksNode;

                if (this.importedVariables.contains(endVariable)) {
                    numTasksNode = getVariableManager().generateVariableNodeForSsaName(endVariable);

                    if (!ScalarUtils.isInteger(numTasksNode.getVariableType())) {
                        CLNativeType numTasksType = CLNativeType.UINT;
                        String numTasksVar = getVariableManager().generateTemporary("N", numTasksType).getName();

                        CNode castTasks = CNodeFactory.newVariable(numTasksVar, numTasksType);
                        argumentConstructionCode.addAssignment(castTasks, numTasksNode);
                        numTasksNode = castTasks;
                    }
                } else {
                    CLNativeType numTasksType = CLNativeType.UINT;
                    String numTasksVar = getVariableManager().generateTemporary("N", numTasksType).getName();

                    functionTypeBuilder.addInput(numTasksVar, numTasksType);
                    kernelArguments.add(KernelArgument.importNumTasks(numTasksVar,
                            CLNativeType.UINT,
                            i));

                    numTasksNode = CNodeFactory.newVariable(numTasksVar, numTasksType);
                }

                numTasksNodes.add(numTasksNode);
            }
        }

        CInstructionList body = new CInstructionList();

        String globalId = "global_id";
        String globalSize = "global_size";
        String localId = "local_id";
        String localSize = "local_size";
        String groupId = "group_id";

        for (int i = 0; i < nestedDepth; ++i) {
            List<CNode> getGlobalIdArgs = Arrays.asList(CNodeFactory.newCNumber(i));
            ProviderData globalIdData = getProviderData().createFromNodes(getGlobalIdArgs);
            VariableNode globalIdNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary(globalId + i, CLNativeType.SIZE_T));
            body.addAssignment(globalIdNode,
                    CLBuiltinPositioningFunction.GET_GLOBAL_ID.getCheckedInstance(globalIdData)
                            .newFunctionCall(getGlobalIdArgs));
            globalIdNodes.add(globalIdNode);

            VariableNode globalSizeNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary(globalSize + i, CLNativeType.SIZE_T));
            CNode globalSizeExpr = getConstantGlobalSize(i)
                    .orElseGet(() -> CLBuiltinPositioningFunction.GET_GLOBAL_SIZE
                            .getCheckedInstance(globalIdData)
                            .newFunctionCall(getGlobalIdArgs));

            body.addAssignment(globalSizeNode,
                    globalSizeExpr);
            globalSizeNodes.add(globalSizeNode);

            List<CNode> getLocalIdArgs = Arrays.asList(CNodeFactory.newCNumber(i));
            ProviderData localIdData = getProviderData().createFromNodes(getLocalIdArgs);
            CNode localIdNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary(localId + i, CLNativeType.SIZE_T));
            body.addAssignment(localIdNode,
                    CLBuiltinPositioningFunction.GET_LOCAL_ID.getCheckedInstance(localIdData)
                            .newFunctionCall(getLocalIdArgs));
            localIdNodes.add(localIdNode);

            List<CNode> getLocalSizeArgs = Arrays.asList(CNodeFactory.newCNumber(i));
            ProviderData localSizeData = getProviderData().createFromNodes(getLocalSizeArgs);
            FunctionCallNode localSizeValue = CLBuiltinPositioningFunction.GET_LOCAL_SIZE
                    .getCheckedInstance(localSizeData)
                    .newFunctionCall(getLocalSizeArgs);
            CNode localSizeNode = CNodeFactory
                    .newVariable(
                            getVariableManager().generateTemporary(localSize + i, localSizeValue.getVariableType()));
            body.addAssignment(localSizeNode, localSizeValue);
            localSizeNodes.add(localSizeNode);

            List<CNode> getGroupIdArgs = Arrays.asList(CNodeFactory.newCNumber(i));
            ProviderData groupIdData = getProviderData().createFromNodes(getGroupIdArgs);
            CNode groupIdNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary(groupId + i, CLNativeType.SIZE_T));
            body.addAssignment(groupIdNode,
                    CLBuiltinPositioningFunction.GET_GROUP_ID.getCheckedInstance(groupIdData)
                            .newFunctionCall(getGroupIdArgs));
            groupIdNodes.add(groupIdNode);

            VariableNode distributionIdNode = getVariableManager().generateTemporaryNode("distribution_id" + i,
                    CLNativeType.UINT);
            distributionIdNodes.add(distributionIdNode);

            VariableNode taskIdNode;
            if (parallelSettings.schedule == ScheduleStrategy.DIRECT) {
                taskIdNode = globalIdNode;
            } else {
                taskIdNode = getVariableManager().generateTemporaryNode("task_id" + i, CLNativeType.UINT);
            }
            absoluteTaskIdNodes.add(taskIdNode);
        }

        if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
            buildSubGroupVariables(body);
        }

        body.add(argumentConstructionCode);

        CInstructionList contentBody;

        if (explicitNumTasks) {
            contentBody = new CInstructionList();
        } else {
            contentBody = body;
        }

        CInstructionList reductionInitializationCode = new CInstructionList();
        CInstructionList reductionFinalizationCode = new CInstructionList();

        for (ReductionComputation reductionComputation : reductionComputations) {
            CNode defaultValue = reductionComputation.getDefaultValue();
            String initialSsaName = reductionComputation.getStartValueName();

            CNode leftHand = getVariableManager().generateVariableNodeForSsaName(initialSsaName);
            reductionInitializationCode.addAssignment(leftHand, defaultValue);
        }

        try {
            generateCodeForBlock(0, contentBody, 0);
        } catch (RuntimeException e) {
            System.out.println(body);
            throw new RuntimeException(e);
        }

        for (ReductionComputation reductionComputation : reductionComputations) {
            CNode localBuffer = reductionComputation.getLocalBuffer();
            if (localBuffer == null) {
                continue;
            }

            CNode endValue = getVariableManager().generateVariableExpressionForSsaName(contentBody,
                    reductionComputation.getInnerLoopEndName(),
                    true);

            MatrixType type = (MatrixType) localBuffer.getVariableType();

            assert localIdNodes.size() == 1;
            // FIXME
            List<CNode> setArgs = Arrays.asList(
                    localBuffer,
                    this.localIdNodes.get(0),
                    endValue);
            ProviderData setData = getProviderData().createFromNodes(setArgs);
            reductionFinalizationCode.addInstruction(type
                    .functions()
                    .set()
                    .getCheckedInstance(setData)
                    .newFunctionCall(setArgs));
        }

        constructBody(explicitNumTasks, reductionComputations, body, contentBody, reductionInitializationCode,
                reductionFinalizationCode);

        if (!reductionComputations.isEmpty()) {
            body.addLiteralInstruction("barrier(CLK_LOCAL_MEM_FENCE);");

            for (ReductionComputation reductionComputation : reductionComputations) {
                body.add(reductionComputation
                        .getStrategy()
                        .buildReductionOperation(reductionComputation, getSsaToOpenCLBuilderService()));
            }
        }

        String functionName = getFunctionName();
        FunctionType functionType = functionTypeBuilder.build();
        body.setFunctionTypes(functionType);

        InstructionsInstance instance = new InstructionsInstance(functionType, functionName, "kernels.cl", body);
        addDependentInstances(instance);

        // STUB
        for (CPass pass : DefaultRecipes.DefaultCRecipe.getPasses()) {
            pass.apply(instance, getProviderData());
        }

        return new GeneratedKernel(instance, parallelSettings, kernelArguments, localReductions, requiredVersion);
    }

    private Optional<CNode> getConstantGlobalSize(int i) {
        String localSize = parallelSettings.localSizes.get(i);

        if (parallelSettings.schedule != ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL &&
                parallelSettings.schedule != ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION) {
            return Optional.empty();
        }
        if (!ScalarUtils.hasConstant(getRawTypeForSsaVariable(localSize))) {
            return Optional.empty();
        }

        String distributionSize = parallelSettings.scheduleNames.get(i);
        if (!ScalarUtils.hasConstant(getRawTypeForSsaVariable(distributionSize))) {
            return Optional.empty();
        }

        CNode globalSize = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                getProviderData(),
                getVariableManager().generateVariableNodeForSsaName(localSize),
                getVariableManager().generateVariableNodeForSsaName(distributionSize));
        return Optional.of(globalSize);
    }

    private boolean hasKnownLocalSizeAttribute() {
        if (parallelSettings.localSizes.size() > 3) {
            // How to deal with this?
            // The OpenCL 2.0 specification seems to imply that only 3 parameters are valid, even though
            // that version has the CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS query.
            // It doesn't seem to be addressed at all.
            // So we'll just do the only sane thing we can and omit the attribute entirely.
            // Most platforms will not be affected as CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS will be 3, anyway.
            return false;
        }

        return parallelSettings.localSizes.stream()
                .map(size -> getRawTypeForSsaVariable(size).orElse(null))
                .allMatch(sizeType -> ScalarUtils.hasConstant(sizeType));
    }

    private String getConstantSizeAtDim(int i) {
        if (parallelSettings.localSizes.size() > i) {
            String var = parallelSettings.localSizes.get(i);
            int value = ScalarUtils.getConstant(getRawTypeForSsaVariable(var).get()).intValue();

            return Integer.toString(value);
        }

        return "1";
    }

    private void constructBody(boolean explicitNumTasks,
            List<ReductionComputation> reductionComputations,
            CInstructionList body,
            CInstructionList contentBody,
            CInstructionList reductionInitializationCode,
            CInstructionList reductionFinalizationCode) {

        body.add(reductionInitializationCode);

        if (parallelSettings.schedule == ScheduleStrategy.DIRECT) {
            if (explicitNumTasks) {
                CNode lessThan = null;

                for (int i = 0; i < globalIdNodes.size(); ++i) {
                    List<CNode> lessThanInDimensionArgs = Arrays.asList(
                            this.globalIdNodes.get(i),
                            this.numTasksNodes.get(i));
                    ProviderData lessThanInDimensionData = getProviderData().createFromNodes(lessThanInDimensionArgs);
                    CNode lessThanInDimension = CLBinaryOperator.LESS_THAN
                            .getCheckedInstance(lessThanInDimensionData)
                            .newFunctionCall(lessThanInDimensionArgs);

                    if (lessThan == null) {
                        lessThan = lessThanInDimension;
                    } else {
                        List<CNode> lessThanArgs = Arrays.asList(
                                lessThan,
                                lessThanInDimension);
                        ProviderData lessThanData = getProviderData()
                                .createFromNodes(lessThanArgs);
                        lessThan = CLBinaryOperator.LOGICAL_AND
                                .getCheckedInstance(lessThanData)
                                .newFunctionCall(lessThanArgs);
                    }
                }

                body.addInstruction(IfNodes.newIfThen(lessThan, contentBody.get()));
            }
        } else if (parallelSettings.schedule == ScheduleStrategy.COOPERATIVE) {
            assert explicitNumTasks;

            assert globalIdNodes.size() == 1;
            assert numTasksNodes.size() == 1;

            CNode groupIdNode = groupIdNodes.get(0);
            CNode localSizeNode = localSizeNodes.get(0);
            CNode numTasksNode = numTasksNodes.get(0);
            CNode absoluteTaskId = absoluteTaskIdNodes.get(0);

            CNode start = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                    getProviderData(),
                    groupIdNode,
                    localSizeNode);
            CNode naturalEnd = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                    getProviderData(),
                    FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            groupIdNode,
                            CNodeFactory.newCNumber(1)),
                    localSizeNode);
            CNode naturalEndCheck = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                    getProviderData(),
                    absoluteTaskId,
                    naturalEnd);
            CNode strictEndCheck = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                    getProviderData(),
                    absoluteTaskId,
                    numTasksNode);
            CNode endCheck = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LOGICAL_AND, getProviderData(),
                    naturalEndCheck,
                    strictEndCheck);
            AssignmentNode assignment = CNodeFactory.newAssignment(absoluteTaskId, start);
            CNode incrExpr = CNodeFactory.newAssignment(absoluteTaskId,
                    FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION, getProviderData(),
                            absoluteTaskId,
                            CNodeFactory.newCNumber(1)));

            CInstructionList newBody = new CInstructionList();
            newBody.addInstruction(new ForNodes(getProviderData()).newForInstruction(assignment, endCheck, incrExpr));
            newBody.add(contentBody);
            body.addInstruction(newBody.toCNode());

        } else if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
            assert explicitNumTasks;

            Variable subGroupAbsoluteIdVariable = getVariableManager().generateTemporary("subgroup_absolute_id",
                    CLNativeType.SIZE_T);
            CNode subGroupAbsoluteIdNode = CNodeFactory.newVariable(subGroupAbsoluteIdVariable);
            body.addAssignment(subGroupAbsoluteIdNode,
                    FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            FunctionInstanceUtils.getFunctionCall(
                                    CLBinaryOperator.MULTIPLICATION,
                                    getProviderData(),
                                    groupIdNodes.get(0),
                                    numSubGroupsNode),
                            subGroupIdNode));

            CNode numTasksNode = numTasksNodes.get(0);
            CNode absoluteTaskId = absoluteTaskIdNodes.get(0);

            CNode start = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                    getProviderData(),
                    subGroupAbsoluteIdNode,
                    subGroupSizeNode);

            boolean warpFallback = getProviderData().getSettings().get(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK);

            CNode trueNaturalEnd = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                    getProviderData(),
                    FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            subGroupAbsoluteIdNode,
                            CNodeFactory.newCNumber(1)),
                    subGroupSizeNode);
            CNode trueLastTask = FunctionInstanceUtils.getFunctionCall(CLBuiltinMathFunction.MIN_SIZE_TYPE,
                    getProviderData(),
                    trueNaturalEnd,
                    numTasksNode);
            CNode subGroupTrueEndCheckExpr = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                    getProviderData(),
                    absoluteTaskId,
                    trueLastTask);

            CNode endCheck;

            if (warpFallback) {
                CNode maxNaturalEnd = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                        getProviderData(),
                        FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                                getProviderData(),
                                subGroupAbsoluteIdNode,
                                CNodeFactory.newCNumber(1)),
                        subGroupSizeNode);

                CNode maxEndCheck = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                        getProviderData(),
                        absoluteTaskId,
                        maxNaturalEnd);

                endCheck = maxEndCheck;
            } else {
                endCheck = subGroupTrueEndCheckExpr;
            }

            AssignmentNode assignment = CNodeFactory.newAssignment(absoluteTaskId, start);
            CNode incrExpr = CNodeFactory.newAssignment(absoluteTaskId,
                    FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION, getProviderData(),
                            absoluteTaskId,
                            CNodeFactory.newCNumber(1)));

            CInstructionList newBody = new CInstructionList();
            newBody.addInstruction(new ForNodes(getProviderData()).newForInstruction(assignment, endCheck, incrExpr));
            if (warpFallback) {
                newBody.addAssignment(subGroupTrueEndCheckNode, subGroupTrueEndCheckExpr);
            }
            newBody.add(contentBody);
            body.addInstruction(newBody.toCNode());

        } else if (parallelSettings.schedule == ScheduleStrategy.COARSE_SEQUENTIAL ||
                parallelSettings.schedule == ScheduleStrategy.COARSE_GLOBAL_ROTATION) {

            assert explicitNumTasks;

            body.addComment("Coarse-grained workload strategy");

            CInstructionList bodySoFar = contentBody;

            for (int i = this.globalIdNodes.size() - 1; i >= 0; --i) {
                CInstructionList newBody = new CInstructionList();

                int invertedPosition = this.globalIdNodes.size() - i - 1;

                CNode coarseningFactor = getVariableManager()
                        .generateVariableNodeForSsaName(
                                parallelSettings.scheduleNames.get(i));

                CNode absoluteTaskId;

                VariableNode distributionId = distributionIdNodes.get(invertedPosition);
                CNode localSize = localSizeNodes.get(invertedPosition);

                if (parallelSettings.schedule == ScheduleStrategy.COARSE_SEQUENTIAL) {
                    CNode start = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                            getProviderData(),
                            globalIdNodes.get(invertedPosition), coarseningFactor);
                    absoluteTaskId = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            start,
                            distributionId);
                } else if (parallelSettings.schedule == ScheduleStrategy.COARSE_GLOBAL_ROTATION) {
                    CNode offset = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                            getProviderData(),
                            distributionId, globalSizeNodes.get(invertedPosition));
                    absoluteTaskId = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            globalIdNodes.get(invertedPosition),
                            offset);
                } else {
                    throw new NotImplementedException(parallelSettings.schedule);
                }

                VariableNode absoluteTaskIdNode = absoluteTaskIdNodes.get(invertedPosition);
                newBody.addAssignment(absoluteTaskIdNode, absoluteTaskId);

                CInstructionList ifBlock = new CInstructionList();

                CNode condition = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN, getProviderData(),
                        absoluteTaskIdNode, numTasksNodes.get(invertedPosition));
                ifBlock.addIf(condition, bodySoFar.get());

                newBody.addInstruction(CNodeFactory.newBlock(ifBlock.get()));
                bodySoFar = newBody;
            }

            for (int i = globalIdNodes.size() - 1; i >= 0; --i) {
                CInstructionList forBlock = new CInstructionList();

                int invertedPosition = this.globalIdNodes.size() - i - 1;

                CNode start = CNodeFactory.newCNumber(0, CLNativeType.INT);
                CNode interval = CNodeFactory.newCNumber(1, CLNativeType.INT);
                CNode coarseningFactor = getVariableManager()
                        .generateVariableNodeForSsaName(parallelSettings.scheduleNames.get(invertedPosition));

                CNode forInstruction = generateForInstruction(distributionIdNodes.get(i),
                        start,
                        interval,
                        coarseningFactor,
                        CLBinaryOperator.LESS_THAN);

                forBlock.addInstruction(forInstruction);
                forBlock.add(bodySoFar);
                bodySoFar = new CInstructionList();
                bodySoFar.addInstruction(CNodeFactory.newBlock(forBlock.get()));
            }

            body.add(bodySoFar);
        } else if (parallelSettings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL ||
                parallelSettings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION) {
            assert explicitNumTasks;

            body.addComment("Fixed number of work groups");

            CInstructionList bodySoFar = contentBody;

            for (int i = this.globalIdNodes.size() - 1; i >= 0; --i) {
                int invertedPosition = this.globalIdNodes.size() - i - 1;

                CNode globalId = globalIdNodes.get(invertedPosition);
                CNode numSegments = globalSizeNodes.get(invertedPosition);

                CNode numTasks = numTasksNodes.get(invertedPosition);

                CNode startNode;
                CNode endNode;
                CNode intervalNode;

                if (parallelSettings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL) {
                    CNode tasksPerSegmentExpr = getDivisionRoundUp(numTasks, numSegments);

                    CNode tasksPerSegment = getVariableManager().generateTemporaryNode("tasks_per_segment",
                            tasksPerSegmentExpr.getVariableType());
                    body.addAssignment(tasksPerSegment, tasksPerSegmentExpr);

                    CNode startNodeExpr = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                            getProviderData(),
                            globalId,
                            tasksPerSegment);
                    startNode = getVariableManager().generateTemporaryNode("start",
                            CLNativeType.SIZE_T);
                    body.addAssignment(startNode, startNodeExpr);

                    CNode normalEndExpr = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                            getProviderData(),
                            startNode,
                            tasksPerSegment);
                    CNode normalEnd = getVariableManager().generateTemporaryNode("normal_end",
                            numTasks.getVariableType());
                    body.addAssignment(normalEnd, normalEndExpr);

                    CNode endNodeExpr = FunctionInstanceUtils.getFunctionCall(CLBuiltinMathFunction.MIN,
                            getProviderData(),
                            normalEnd,
                            numTasks);
                    endNode = getVariableManager().generateTemporaryNode("end",
                            CLNativeType.SIZE_T);
                    body.addAssignment(endNode, endNodeExpr);

                    intervalNode = CNodeFactory.newCNumber(1);
                } else if (parallelSettings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION) {
                    startNode = globalId;
                    endNode = numTasks;
                    intervalNode = numSegments;
                } else {
                    throw new NotImplementedException(parallelSettings.schedule);
                }
                CNode forInstruction = generateForInstruction(absoluteTaskIdNodes.get(invertedPosition),
                        startNode,
                        intervalNode,
                        endNode,
                        CLBinaryOperator.LESS_THAN);

                CInstructionList forBlock = new CInstructionList();
                forBlock.addInstruction(forInstruction);
                forBlock.add(bodySoFar);
                bodySoFar = new CInstructionList();
                bodySoFar.addInstruction(CNodeFactory.newBlock(forBlock.get()));
            }

            body.add(bodySoFar);
        } else {
            throw new NotImplementedException(parallelSettings.schedule);
        }

        body.add(reductionFinalizationCode);
    }

    private void buildSubGroupVariables(CInstructionList body) {
        assert globalIdNodes.size() == 1;
        assert localSizeNodes.size() == 1;
        assert numTasksNodes.size() == 1;

        if (getProviderData().getSettings().get(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK)) {
            // It's OK to make it constant, because the local size will be a multiple of this value.
            subGroupSizeNode = CNodeFactory
                    .newCNumber(getProviderData().getSettings().get(MatisseCLKeys.SUB_GROUP_SIZE));

            CNode subGroupsPerGroupValue = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.DIVISION,
                    getProviderData(),
                    localSizeNodes.get(0),
                    subGroupSizeNode);
            Variable subGroupsPerGroup = getVariableManager().generateTemporary("num_subgroups",
                    CLNativeType.SIZE_T);
            body.addAssignment(subGroupsPerGroup, subGroupsPerGroupValue);

            numSubGroupsNode = CNodeFactory.newVariable(subGroupsPerGroup);

            subGroupLocalIdNode = CNodeFactory.newVariable("sub_group_local_id", CLNativeType.SIZE_T);
            body.addAssignment(subGroupLocalIdNode, FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MODULO,
                    getProviderData(),
                    localIdNodes.get(0),
                    subGroupSizeNode));

            subGroupIdNode = CNodeFactory.newVariable("sub_group_id", CLNativeType.SIZE_T);
            body.addAssignment(subGroupIdNode, FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.DIVISION,
                    getProviderData(),
                    localIdNodes.get(0),
                    subGroupSizeNode));

            subGroupTrueEndCheckNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary("in_task_range",
                            CLNativeType.SIZE_T));
        } else {
            subGroupSizeNode = FunctionInstanceUtils.getFunctionCall(
                    CLBuiltinPositioningFunction.GET_SUB_GROUP_SIZE,
                    getProviderData());
            numSubGroupsNode = FunctionInstanceUtils.getFunctionCall(
                    CLBuiltinPositioningFunction.GET_NUM_SUB_GROUPS,
                    getProviderData());
            subGroupLocalIdNode = FunctionInstanceUtils
                    .getFunctionCall(CLBuiltinPositioningFunction.GET_SUB_GROUP_LOCAL_ID, getProviderData());
            subGroupIdNode = FunctionInstanceUtils
                    .getFunctionCall(CLBuiltinPositioningFunction.GET_SUB_GROUP_ID, getProviderData());
        }
    }

    private CNode getDivisionRoundUp(CNode dividend, CNode divisor) {
        assert ScalarUtils.isInteger(dividend.getVariableType()) : "Expected integer, got "
                + dividend.getVariableType() + ", " + dividend.getCode();
        assert ScalarUtils.isInteger(divisor.getVariableType()) : "Expected integer, got "
                + dividend.getVariableType() + ", " + divisor.getCode();

        // (A + B - 1) / B
        // Example: A = 5, B = 2
        // > (5 + 2 - 1) / 2 == 6 / 2 == 3
        // Example: A = 4, B = 2
        // > (4 + 2 - 1) / 2 == 5 / 2 == 2

        return FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.DIVISION, getProviderData(),
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.SUBTRACTION, getProviderData(),
                        FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION, getProviderData(),
                                divisor,
                                dividend),
                        CNodeFactory.newCNumber(1)),
                divisor);
    }

    private SsaToOpenCLBuilderService getSsaToOpenCLBuilderService() {
        return new SsaToOpenCLBuilderService() {

            @Override
            public List<VariableNode> getGlobalIdNodes() {
                return globalIdNodes;
            }

            @Override
            public List<CNode> getLocalIdNodes() {
                return localIdNodes;
            }

            @Override
            public List<CNode> getLocalSizeNodes() {
                return localSizeNodes;
            }

            @Override
            public List<CNode> getGroupIdNodes() {
                return groupIdNodes;
            }

            @Override
            public CNode getSubGroupLocalIdNode() {
                return subGroupLocalIdNode;
            }

            @Override
            public CNode getSubGroupIdNode() {
                return subGroupIdNode;
            }

            @Override
            public CNode getNumSubGroupsNode() {
                return numSubGroupsNode;
            }

            @Override
            public CNode getSubGroupSizeNode() {
                return subGroupSizeNode;
            }

            @Override
            public ProviderData getCurrentProvider() {
                return KernelCodeGenerator.this.getProviderData();
            }

            @Override
            public VariableManager getVariableManager() {
                return KernelCodeGenerator.this.getVariableManager();
            }

            @Override
            public String getFinalName(String ssaName) {
                return KernelCodeGenerator.this.getFinalName(ssaName);
            }
        };
    }

    @Override
    protected void generateCodeForInstruction(int blockId, SsaInstruction instruction, CInstructionList currentBlock,
            int depth) {
        if (instruction instanceof InitializationInstruction) {
            // No action needed.

            return;
        }
        if (instruction instanceof IterInstruction) {
            if (isParallelDepth(depth - 1)) {
                buildParallelIter(instruction, currentBlock, depth);
            } else {
                // No action needed.
            }

            return;
        }

        if (instruction instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) instruction;
            String output = phi.getOutput();

            if (localReductionComputations.containsKey(output)) {
                ReductionComputation computation = localReductionComputations.get(output);

                ReductionStrategy strategy = computation.getStrategy();
                currentBlock.add(strategy.buildReductionOperation(computation, getSsaToOpenCLBuilderService()));

                return;
            }
        }

        if (instruction instanceof SimpleSetInstruction) {
            if (parallelSettings.schedule == ScheduleStrategy.COOPERATIVE) {
                CInstructionList inLeaderBlock = new CInstructionList();
                super.generateCodeForInstruction(blockId, instruction, inLeaderBlock, depth);

                CNode isLeaderThread = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.EQUAL,
                        getProviderData(),
                        localIdNodes.get(0),
                        CNodeFactory.newCNumber(0));
                currentBlock.addIf(isLeaderThread, inLeaderBlock.get());

                currentBlock.addLiteralInstruction("barrier(CLK_GLOBAL_MEM_FENCE);");
            } else if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
                CInstructionList inLeaderBlock = new CInstructionList();
                super.generateCodeForInstruction(blockId, instruction, inLeaderBlock, depth);

                CNode isLeaderThread = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.EQUAL,
                        getProviderData(),
                        subGroupLocalIdNode,
                        CNodeFactory.newCNumber(0));

                CNode condition;
                if (subGroupTrueEndCheckNode != null) {
                    condition = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LOGICAL_AND,
                            getProviderData(),
                            isLeaderThread,
                            subGroupTrueEndCheckNode);
                } else {
                    condition = isLeaderThread;
                }
                currentBlock.addIf(condition, inLeaderBlock.get());

                currentBlock.addLiteralInstruction(getSsaToOpenCLBuilderService().getSubGroupBarrierCode());
            } else {
                super.generateCodeForInstruction(blockId, instruction, currentBlock, depth);
            }

            return;
        }

        if (instruction instanceof ForInstruction) {
            if (isParallelDepth(depth)) {
                generateCodeForBlock(((ForInstruction) instruction).getLoopBlock(), currentBlock, depth + 1);
                return;
            } else if (localReductions.containsKey(blockId)) {
                for (Reduction reduction : localReductions.get(blockId)) {
                    Map<ReductionType, List<ReductionStrategy>> reductionStrategies;
                    if (parallelSettings.schedule == ScheduleStrategy.COOPERATIVE) {
                        reductionStrategies = parallelSettings.localReductionStrategies;
                    } else if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
                        reductionStrategies = parallelSettings.subgroupReductionStrategies;
                    } else {
                        throw new NotImplementedException(parallelSettings.schedule);
                    }
                    List<ReductionStrategy> availableReductionStrategies = reductionStrategies
                            .get(reduction.getReductionType());

                    String initialReductionName = reduction.getInitialName();
                    ReductionOutput output = availableReductionStrategies
                            .stream()
                            .map(reductionStrategy -> reductionStrategy.prepareReduction(reduction,
                                    initialReductionName,
                                    this.getSsaToOpenCLBuilderService(),
                                    getVariableManager().getVariableTypeFromSsaName(initialReductionName).get()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "No available reduction mechanism, out of " + availableReductionStrategies));

                    for (Variable extraArgument : output.getKernelExtraArguments()) {
                        functionTypeBuilder.addInput(extraArgument);
                    }
                    kernelArguments.addAll(output.getKernelCallerArguments());
                    requiredVersion = CLVersion.getMaximumVersion(requiredVersion, output.getRequiredVersion());

                    output.getLocalLoopPreparationCode()
                            .ifPresent(currentBlock::add);

                    output.getArgumentConstructionCode()
                            .ifPresent(argumentConstructionCode::add);

                    ReductionComputation reductionComputation = output.getReductionComputation();
                    localReductionComputations.put(reduction.getFinalName(), reductionComputation);
                }

                ForInstruction xfor = (ForInstruction) instruction;

                String interval = xfor.getInterval();
                ScalarType type = (ScalarType) getVariableTypeFromSsaName(interval).get();
                if (type.scalar().getConstant() == null || type.scalar().getConstant().doubleValue() != 1) {
                    throw new RuntimeException("cooperative mode only supports loops with interval 1.");
                }

                String start = xfor.getStart();
                String end = xfor.getEnd();

                CNode inductionVar = getLoopInductionVar(xfor);

                CNode threadOffset = null;
                CNode loopStep = null;
                if (parallelSettings.schedule == ScheduleStrategy.COOPERATIVE) {
                    threadOffset = localIdNodes.get(0);
                    loopStep = localSizeNodes.get(0);
                } else if (parallelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
                    assert subGroupLocalIdNode != null;
                    assert subGroupSizeNode != null;

                    threadOffset = subGroupLocalIdNode;
                    loopStep = subGroupSizeNode;
                } else {
                    assert false;
                }
                CNode localStart = FunctionInstanceUtils.getFunctionCall(
                        CLBinaryOperator.ADDITION,
                        getProviderData(),
                        getVariableManager().generateVariableNodeForSsaName(start),
                        threadOffset);

                List<CNode> forInstructions = new ArrayList<>();
                forInstructions
                        .add(new ForNodes(getProviderData()).newForInstruction(
                                CNodeFactory.newAssignment(
                                        inductionVar, localStart),
                                FunctionInstanceUtils.getFunctionCall(
                                        CLBinaryOperator.LESS_OR_EQUAL_TO,
                                        getProviderData(),
                                        inductionVar,
                                        getVariableManager().generateVariableNodeForSsaName(end)),
                                CNodeFactory.newAssignment(inductionVar,
                                        FunctionInstanceUtils.getFunctionCall(
                                                CLBinaryOperator.ADDITION,
                                                getProviderData(),
                                                inductionVar,
                                                loopStep))));

                CInstructionList forBlock = new CInstructionList();
                generateCodeForBlock(xfor.getLoopBlock(), forBlock, depth + 1);
                forInstructions.addAll(forBlock.get());

                BlockNode blockNode = CNodeFactory.newBlock(forInstructions);
                currentBlock.addInstruction(blockNode);

                generateCodeForBlock(xfor.getEndBlock(), currentBlock, depth);
                return;
            }
        }

        CInstructionList instructionBlock;
        if (subGroupTrueEndCheckNode != null) {
            instructionBlock = new CInstructionList();
        } else {
            instructionBlock = currentBlock;
        }
        super.generateCodeForInstruction(blockId, instruction, instructionBlock, depth);

        if (subGroupTrueEndCheckNode != null && !instructionBlock.get().isEmpty()) {
            currentBlock.addIf(subGroupTrueEndCheckNode, instructionBlock.get());
        }
    }

    private void buildParallelIter(SsaInstruction instruction, CInstructionList currentBlock, int depth) {
        IterInstruction iter = (IterInstruction) instruction;

        CNode absoluteTaskId = this.absoluteTaskIdNodes.get(this.absoluteTaskIdNodes.size() - depth - 1);

        List<CNode> iterCalcNodes = Arrays.asList(absoluteTaskId,
                CNodeFactory.newCNumber(1));
        ProviderData iterCalcData = getProviderData().createFromNodes(iterCalcNodes);
        CNode iterValue = CLBinaryOperator.ADDITION.getCheckedInstance(iterCalcData).newFunctionCall(iterCalcNodes);

        CNode outputNode = getVariableManager().generateVariableNodeForSsaName(iter.getOutput());
        currentBlock.addAssignment(outputNode, iterValue);
    }

    @Override
    protected boolean isParallelDepth(int depth) {
        return depth < parallelLoop.loopDeclarationBlockIds.size() - 1;
    }

    private String getFunctionName() {
        StringBuilder suggestedName = new StringBuilder(this.parallelInstance.getBody().getName());

        while (suggestedName.charAt(0) == '$') {
            suggestedName = suggestedName.deleteCharAt(0);
        }

        suggestedName.append("_");
        suggestedName.append(this.kernelId);

        return suggestedName.toString().replace('$', '_');
    }

    private boolean isImportedVariableReduction(String importedVariable) {
        for (Reduction reduction : this.parallelLoop.reductions) {
            String initialSsaName = reduction.getInitialName();

            int reductionGroupId = getVariableAllocation().getGroupIdForVariable(initialSsaName);
            if (getVariableAllocation().getVariableGroups().get(reductionGroupId).contains(importedVariable)) {
                return true;
            }
        }
        return false;
    }

    private Optional<VariableType> getVariableTypeFromSsaName(String ssaName) {
        return getVariableManager().getVariableTypeFromSsaName(ssaName);
    }

    @Override
    protected Optional<VariableType> getOriginalSsaVariableType(String ssaName) {
        return this.parallelInstance.getType(ssaName);
    }

    private static final void log(String message) {
        if (KernelCodeGenerator.ENABLE_LOG) {
            System.out.print("[kernel_codegen] ");
            System.out.println(message);
        }
    }
}
