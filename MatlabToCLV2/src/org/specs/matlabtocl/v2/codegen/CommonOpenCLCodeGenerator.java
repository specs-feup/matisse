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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.CodeBuilder.DefaultVariableManager;
import org.specs.MatlabToC.CodeBuilder.VariableNameBlacklist;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.DumpSsaProperty;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.specs.matisselib.typeinference.TypeCombiner;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ParallelCopySequentializer;
import org.specs.matisselib.unssa.ParallelCopySequentializer.Copy;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matisselib.unssa.VariableNameChooser;
import org.specs.matlabtocl.v2.CLRecipes;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.functions.extra.MatisseExtraCLFunctions;
import org.specs.matlabtocl.v2.functions.matlab.MathFunctions;
import org.specs.matlabtocl.v2.functions.matlab.MatlabBuiltin;
import org.specs.matlabtocl.v2.functions.matlab.MatlabOperator;
import org.specs.matlabtocl.v2.ssa.CLPass;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.reporting.Reporter;

public abstract class CommonOpenCLCodeGenerator {
    protected FunctionBody body;
    protected DataStore passData;
    private final ProviderData baseProviderData;
    private ProviderData providerData;
    private List<String> variableNames;
    private final VariableAllocator variableAllocator;
    private VariableAllocation variableAllocation;
    private DefaultVariableManager variableManager;
    private final MatlabFunctionTable functions;
    private CLTypeDecorator typeDecorator;
    private final DefaultReportService rootReportService;

    protected CommonOpenCLCodeGenerator(
            DataStore passData,
            VariableAllocator allocator,
            ProviderData providerData) {

        this.passData = passData;
        this.providerData = baseProviderData = providerData;
        rootReportService = (DefaultReportService) providerData.getReportService();
        assert rootReportService != null;

        variableAllocator = allocator;

        functions = new MatlabFunctionTable();
        functions.addPrototypes(MatlabOperator.class);
        functions.addPrototypes(MathFunctions.class);
        functions.addPrototypes(MatlabBuiltin.class);
        functions.addPrototypes(MatisseExtraCLFunctions.class);
    }

    protected void setUp() {
        body = getBody();
        new SsaValidatorPass("after-generation").apply(body, null);

        Set<String> blacklistedNames = new HashSet<>();
        buildVariableNameBlacklist(blacklistedNames);

        variableAllocation = buildVariableAllocation(variableAllocator);
        variableNames = VariableNameChooser.getNames(variableAllocation, blacklistedNames);
        processVariableNames(variableNames);

        typeDecorator = new CLTypeDecoratorBuilder(body,
                variableNames,
                variableAllocation,
                this::getRawTypeForSsaVariable).buildTypeDecorator();

        UsageMap usageMap = UsageMap.build(body); // TODO: Is this correct?
        variableManager = new DefaultVariableManager(variableNames,
                variableAllocation,
                usageMap,
                passData.get(ProjectPassServices.GLOBAL_TYPE_PROVIDER),
                this::getCombinedVariableType,
                getTypeDecorator()::decorateType);
    }

    protected CLTypeDecorator getTypeDecorator() {
        return typeDecorator;
    }

    protected void processVariableNames(List<String> variableNames) {

    }

    protected void buildVariableNameBlacklist(Set<String> blacklistedNames) {
        VariableNameBlacklist.addCKeywords(blacklistedNames);
        CLVariableNameBlacklist.addAdditionalOpenCLKeywords(blacklistedNames);
        CLVariableNameBlacklist.addOpenCLFunctions(blacklistedNames);
    }

    protected abstract FunctionBody getBody();

    protected void applyPasses(FunctionBody body) {
        DataStore passData = new SimpleDataStore("cl-data");

        // We don't decorate the type, because when this runs we don't have the type decorator yet.
        passData.add(CLServices.TYPE_GETTER, this::getRawTypeForSsaVariable);
        passData.add(CLServices.TEMPORARY_ALLOCATOR, this::makeTemporary);

        for (CLPass pass : CLRecipes.CL_RECIPE.getPasses()) {
            pass.apply(body, passData);
        }
    }

    protected abstract Optional<VariableType> getRawTypeForSsaVariable(String name);

    protected abstract String makeTemporary(String proposedName, VariableType type);

    protected VariableAllocation buildVariableAllocation(VariableAllocator allocator) {
        VariableAllocation allocation = allocator.performAllocation(
                body,
                variables -> getCombinedVariableType(variables).isPresent());

        return allocation;
    }

    protected void addDependentInstances(InstructionsInstance instance) {
        Set<FunctionInstance> dependencies = new HashSet<>();

        for (VariableType type : instance.getInstructions().getLocalVars().values()) {
            dependencies.addAll(type.code().getInstances());
        }

        instance.setCustomImplementationInstances(dependencies);
    }

    public void generateCodeForBlock(int blockId, CInstructionList currentBlock, int depth) {
        SsaBlock block = body.getBlocks().get(blockId);

        for (final SsaInstruction instruction : block.getInstructions()) {
            if (passData.get(MatisseLibOption.DUMP_SSA_INSTRUCTIONS) || body.hasProperty(DumpSsaProperty.class)) {
                currentBlock.addComment(instruction.toString());
            }
            try {
                generateCodeForInstruction(blockId, instruction, currentBlock, depth);
            } catch (final RuntimeException e) {
                System.err.println("At: " + instruction);
                throw e;
            }
        }
    }

    protected void generateCodeForInstruction(int blockId, SsaInstruction instruction, CInstructionList currentBlock,
            int depth) {
        if (instruction instanceof LineInstruction) {
            Reporter reportService = rootReportService
                    .withLineNumber(((LineInstruction) instruction).getLine());
            providerData = baseProviderData.withReportService(reportService);

            return;
        }
        if (instruction instanceof CommentInstruction) {

            currentBlock.addComment(((CommentInstruction) instruction).getContent());
            return;
        }
        if (instruction instanceof BuiltinVariableInstruction) {

            BuiltinVariableInstruction builtin = (BuiltinVariableInstruction) instruction;

            String output = builtin.getOutput();

            CNode value;
            switch (builtin.getVariable()) {
            case PI:
                value = CNodeFactory.newCNumber(Math.PI, variableManager.getVariableTypeFromSsaName(output).get());
                break;
            case TRUE:
                value = CNodeFactory.newCNumber(1, variableManager.getVariableTypeFromSsaName(output).get());
                break;
            case FALSE:
                value = CNodeFactory.newCNumber(0, variableManager.getVariableTypeFromSsaName(output).get());
                break;
            default:
                throw new NotImplementedException("Builtin variable " + builtin.getVariable());
            }
            currentBlock.addAssignment(variableManager.generateVariableNodeForSsaName(output), value);

            return;
        }
        if (instruction instanceof AssignmentInstruction) {
            AssignmentInstruction assignment = (AssignmentInstruction) instruction;

            CNode output = variableManager.generateVariableNodeForSsaName(assignment.getOutput());

            Input input = assignment.getInput();
            if (input instanceof NumberInput) {
                currentBlock.addAssignment(output,
                        CNodeFactory.newCNumber(((NumberInput) input).getNumber(), output.getVariableType()));
            } else if (input instanceof VariableInput) {
                CNode inputNode = variableManager.generateVariableExpressionForSsaName(currentBlock,
                        ((VariableInput) input).getName());

                currentBlock.addAssignment(output, inputNode);
            } else {
                throw new NotImplementedException(assignment.toString());
            }

            return;
        }
        if (instruction instanceof TypedFunctionCallInstruction) {
            TypedFunctionCallInstruction typedCall = (TypedFunctionCallInstruction) instruction;

            String functionName = typedCall.getFunctionName();

            FunctionInstance functionInstance;

            List<CNode> typedArgs = new ArrayList<>();
            String blockSoFar = currentBlock.toString();
            for (String in : typedCall.getInputVariables()) {
                CNode inputNode = variableManager.generateVariableExpressionForSsaName(currentBlock, in, false);
                assert !(inputNode.getVariableType() instanceof VoidType) : in + " is of type void, in\n" + body
                        + ", block so far:\n" + blockSoFar;
                typedArgs.add(inputNode);
            }
            ProviderData typedData = providerData.createFromNodes(typedArgs);
            typedData.setNargouts(typedCall.getOutputs().size());

            List<CNode> outputArgs = new ArrayList<>();

            MatlabFunction function = functions.getPrototypes().get(functionName);
            if (function == null) {
                List<VariableType> originalInputTypes = new ArrayList<>();
                for (String in : typedCall.getInputVariables()) {
                    originalInputTypes.add(getUnprocessedVariableTypeFromSsaName(in).get());
                }
                List<VariableType> originalOutputTypes = new ArrayList<>();
                for (String out : typedCall.getOutputs()) {
                    originalOutputTypes.add(getUnprocessedVariableTypeFromSsaName(out).get());

                    CNode outputNode = variableManager.generateVariableNodeForSsaName(out);
                    outputArgs.add(outputNode);
                }

                FunctionIdentification calledFunction = passData
                        .get(PreTypeInferenceServices.WIDE_SCOPE)
                        .getUserFunction(functionName)
                        .orElseThrow(() -> new RuntimeException("Could not find function '" + functionName + "'"));

                ProviderData originalTypedData = providerData.create(originalInputTypes);
                originalTypedData.setOutputType(originalOutputTypes);

                TypedInstance newInstance = passData
                        .get(ProjectPassServices.TYPED_INSTANCE_PROVIDER)
                        .getTypedInstance(calledFunction,
                                originalTypedData);
                functionInstance = AuxiliaryCodeGenerator.buildImplementation(passData,
                        variableAllocator,
                        newInstance);
            } else {
                functionInstance = function.getCheckedInstance(typedData);
            }

            FunctionCallNode functionCallNode = functionInstance.newFunctionCall(typedArgs);
            if (typedCall.getOutputs().size() == 1) {
                CNode outputNode = variableManager.generateVariableNodeForSsaName(typedCall.getOutputs().get(0));
                currentBlock.addAssignment(outputNode, functionCallNode);
            } else {
                for (int i = 0; i < outputArgs.size(); ++i) {
                    functionCallNode.getFunctionInputs().setInput(i + typedArgs.size(), outputArgs.get(i));
                }

                currentBlock.addInstruction(functionCallNode);
            }

            return;
        }
        if (instruction instanceof SimpleGetInstruction) {
            SimpleGetInstruction get = (SimpleGetInstruction) instruction;

            CNode output = getVariableManager().generateVariableNodeForSsaName(get.getOutput());
            CNode matrix = getVariableManager().generateVariableExpressionForSsaName(currentBlock,
                    get.getInputMatrix(),
                    false);

            List<CNode> indexNodes = new ArrayList<>();
            for (String index : get.getIndices()) {
                CNode indexNode = getVariableManager()
                        .generateVariableExpressionForSsaName(currentBlock, index, false);

                List<CNode> indexMinusOneArgs = Arrays.asList(indexNode, CNodeFactory.newCNumber(1));
                InstanceProvider minusProvider = CLBinaryOperator.SUBTRACTION;
                ProviderData minusData = getProviderData().createFromNodes(indexMinusOneArgs);
                CNode indexMinusOne = minusProvider.getCheckedInstance(minusData).newFunctionCall(indexMinusOneArgs);

                VariableType variableType = indexMinusOne.getVariableType();
                assert variableType instanceof ScalarType : "This should not even be parallelized.";
                if (!((ScalarType) variableType).scalar().isInteger()) {
                    indexMinusOne = ConversionUtils.to(indexMinusOne, CLNativeType.SIZE_T);
                }

                indexNodes.add(indexMinusOne);
            }

            MatrixType matrixType = (MatrixType) matrix.getVariableType();
            InstanceProvider getProvider = matrixType.functions().get();
            List<CNode> getArgs = new ArrayList<>();
            getArgs.add(matrix);
            getArgs.addAll(indexNodes);
            ProviderData getData = getProviderData().createFromNodes(getArgs);
            CNode getNode = getProvider.getCheckedInstance(getData).newFunctionCall(getArgs);

            currentBlock.addAssignment(output, getNode);

            return;
        }
        if (instruction instanceof GetOrFirstInstruction) {
            GetOrFirstInstruction get = (GetOrFirstInstruction) instruction;

            CNode output = getVariableManager().generateVariableNodeForSsaName(get.getOutput());
            CNode matrix = getVariableManager().generateVariableNodeForSsaName(get.getInputMatrix());

            MatrixType matrixType = (MatrixType) matrix.getVariableType();

            String index = get.getIndex();
            CNode indexNode = getVariableManager()
                    .generateVariableExpressionForSsaName(currentBlock, index, false);

            List<CNode> indexMinusOneArgs = Arrays.asList(indexNode, CNodeFactory.newCNumber(1));
            InstanceProvider minusProvider = CLBinaryOperator.SUBTRACTION;
            ProviderData minusData = getProviderData().createFromNodes(indexMinusOneArgs);
            CNode indexMinusOne = minusProvider.getCheckedInstance(minusData).newFunctionCall(indexMinusOneArgs);

            InstanceProvider numelProvider = matrixType.functions().numel();
            List<CNode> numelArgs = Arrays.asList(matrix);
            ProviderData numelData = getProviderData().createFromNodes(numelArgs);
            CNode numelNode = numelProvider.getCheckedInstance(numelData).newFunctionCall(numelArgs);

            List<CNode> numelIsOneArgs = Arrays.asList(numelNode, CNodeFactory.newCNumber(1));
            ProviderData numelIsOneData = getProviderData().createFromNodes(numelIsOneArgs);
            CNode numelIsOne = CLBinaryOperator.EQUAL.getCheckedInstance(numelIsOneData)
                    .newFunctionCall(numelIsOneArgs);
            CNode adjustedIndexNode = CNodeFactory
                    .newLiteral(numelIsOne.getCodeForLeftSideOf(PrecedenceLevel.TernaryConditional) + " ? 1 : "
                            + indexMinusOne.getCodeForRightSideOf(PrecedenceLevel.TernaryConditional));

            InstanceProvider getProvider = matrixType.functions().get();
            List<CNode> getArgs = new ArrayList<>();
            getArgs.add(matrix);
            getArgs.add(adjustedIndexNode);
            ProviderData getData = getProviderData().createFromNodes(getArgs);
            CNode getNode = getProvider.getCheckedInstance(getData).newFunctionCall(getArgs);

            currentBlock.addAssignment(output, getNode);

            return;
        }
        if (instruction instanceof SimpleSetInstruction) {
            SimpleSetInstruction simpleSet = (SimpleSetInstruction) instruction;

            String inputMatrixSsa = simpleSet.getInputMatrix();
            String outputMatrixSsa = simpleSet.getOutput();

            if (variableAllocation.getGroupIdForVariable(inputMatrixSsa) != variableAllocation
                    .getGroupIdForVariable(outputMatrixSsa)) {
                throw new RuntimeException(
                        "Could not generate code for simple_set instruction, due to variable allocation failure");
            }

            CNode matrixVariable = variableManager.generateVariableNodeForSsaName(outputMatrixSsa);
            MatrixType matrixType = (MatrixType) matrixVariable.getVariableType();
            InstanceProvider setProvider = matrixType.functions().set();

            List<CNode> setArgs = new ArrayList<>();
            setArgs.add(matrixVariable);

            for (String index : simpleSet.getIndices()) {
                CNode indexVariable = variableManager.generateVariableExpressionForSsaName(currentBlock,
                        index,
                        false);

                List<CNode> minusArgs = Arrays.asList(indexVariable,
                        CNodeFactory.newCNumber(1, indexVariable.getVariableType()));
                CNode indexMinusOne = CLBinaryOperator.SUBTRACTION
                        .getCheckedInstance(providerData.createFromNodes(minusArgs)).newFunctionCall(minusArgs);

                VariableType variableType = indexMinusOne.getVariableType();
                assert variableType instanceof ScalarType : "This should not even be parallelized.";
                if (!((ScalarType) variableType).scalar().isInteger()) {
                    indexMinusOne = ConversionUtils.to(indexMinusOne, CLNativeType.SIZE_T);
                }

                setArgs.add(indexMinusOne);
            }

            CNode valueVariable = variableManager.generateVariableExpressionForSsaName(currentBlock,
                    simpleSet.getValue(),
                    false);
            setArgs.add(valueVariable);

            ProviderData setData = providerData.createFromNodes(setArgs);
            currentBlock.addInstruction(setProvider.getCheckedInstance(setData).newFunctionCall(setArgs));

            return;
        }
        if (instruction instanceof WhileInstruction) {
            WhileInstruction xwhile = (WhileInstruction) instruction;

            CNode condition = CNodeFactory.newCNumber(1);
            CInstructionList body = new CInstructionList();
            generateCodeForBlock(xwhile.getLoopBlock(), body, depth);

            currentBlock.addWhile(condition, body.get());

            generateCodeForBlock(xwhile.getEndBlock(), currentBlock, depth);
            return;
        }
        if (instruction instanceof BranchInstruction) {
            BranchInstruction branch = (BranchInstruction) instruction;

            CNode condition = variableManager.generateVariableExpressionForSsaName(currentBlock,
                    branch.getConditionVariable());

            CInstructionList thenBody = new CInstructionList();
            generateCodeForBlock(branch.getTrueBlock(), thenBody, depth);
            CInstructionList elseBody = new CInstructionList();
            generateCodeForBlock(branch.getFalseBlock(), elseBody, depth);

            if (elseBody.get().size() == 0) {
                currentBlock.addInstruction(IfNodes.newIfThen(condition, thenBody.get()));
            } else {
                currentBlock.addInstruction(IfNodes.newIfThenElse(condition, thenBody.get(), elseBody.get()));
            }

            generateCodeForBlock(branch.getEndBlock(), currentBlock, depth);

            return;
        }
        if (instruction instanceof ForInstruction && !isParallelDepth(depth)) {
            ForInstruction xfor = (ForInstruction) instruction;

            CNode variableNode = getLoopInductionVar(xfor);

            // TODO: Negative intervals

            List<CNode> forInstructions = new ArrayList<>();

            CNode start = getVariableManager().generateVariableExpressionForSsaName(currentBlock,
                    xfor.getStart());
            CNode interval = getVariableManager().generateVariableExpressionForSsaName(currentBlock,
                    xfor.getInterval(), false, true);
            CNode end = getVariableManager().generateVariableExpressionForSsaName(currentBlock,
                    xfor.getEnd(), false, true);

            InstructionNode forInstruction = generateForInstruction(variableNode, start, interval, end,
                    CLBinaryOperator.LESS_OR_EQUAL_TO);

            CInstructionList forBlock = new CInstructionList();
            generateCodeForBlock(xfor.getLoopBlock(), forBlock, depth + 1);

            forInstructions.add(forInstruction);
            forInstructions.addAll(forBlock.get());

            BlockNode blockNode = CNodeFactory.newBlock(forInstructions);
            currentBlock.addInstruction(blockNode);

            generateCodeForBlock(xfor.getEndBlock(), currentBlock, depth);

            return;
        }
        if (instruction instanceof PhiInstruction) {
            String variableName = getVariableManager()
                    .generateVariableNodeForSsaName(((PhiInstruction) instruction).getOutput()).getCode();

            for (String input : instruction.getInputVariables()) {
                String inputVariableName = getVariableManager().generateVariableNodeForSsaName(input).getCode();

                assert variableName.equals(inputVariableName);
            }

            return;
        }
        if (instruction instanceof ParallelCopyInstruction) {
            ParallelCopyInstruction parallelCopy = (ParallelCopyInstruction) instruction;

            List<String> ssaInputs = parallelCopy.getInputVariables();
            List<String> ssaOutputs = parallelCopy.getOutputs();

            assert ssaInputs.size() == ssaOutputs.size();

            Set<Copy> parallelCopies = new HashSet<>();

            for (int i = 0; i < ssaInputs.size(); ++i) {
                String ssaInput = ssaInputs.get(i);
                String ssaOutput = ssaOutputs.get(i);

                String actualInput = getVariableManager().convertSsaToFinalName(ssaInput);
                String actualOutput = getVariableManager().convertSsaToFinalName(ssaOutput);

                if (actualInput.equals(actualOutput)) {
                    continue;
                }

                parallelCopies.add(new Copy(actualInput, actualOutput));
            }

            // FIXME: freshVariable name
            // FIXME: Should we be worried about having a mix of variables of potentially different types?
            List<Copy> sequentialCopies = ParallelCopySequentializer.sequentializeParallelCopies(parallelCopies,
                    "<FIXME_SEQUENTIALIZER>");

            for (Copy copy : sequentialCopies) {
                CNode destination = getVariableManager().generateVariableNodeForFinalName(copy.getDestination()).get();
                CNode source = getVariableManager().generateVariableNodeForFinalName(copy.getSource()).get();
                currentBlock.addAssignment(destination, source);
            }

            return;
        }
        if (instruction instanceof BreakInstruction) {
            currentBlock.addBreak();

            return;
        }

        throw new NotImplementedException(instruction.toString());
    }

    protected CNode getLoopInductionVar(ForInstruction xfor) {
        CNode variableNode = null;
        for (SsaInstruction instr : body.getBlock(xfor.getLoopBlock()).getInstructions()) {
            if (instr instanceof IterInstruction) {
                variableNode = getVariableManager().generateVariableNodeForSsaName(
                        ((IterInstruction) instr).getOutput());
                break;
            }
        }
        if (variableNode == null) {
            variableNode = CNodeFactory
                    .newVariable(getVariableManager().generateTemporary("iter", CLNativeType.UINT));
        }
        return variableNode;
    }

    public InstructionNode generateForInstruction(CNode variableNode, CNode start,
            CNode interval,
            CNode end,
            CLBinaryOperator stopOp) {

        AssignmentNode assignment = CNodeFactory.newAssignment(variableNode, start);
        ProviderData stopData = providerData.createFromNodes(variableNode, end);
        CNode stopExpr = stopOp.getCheckedInstance(stopData)
                .newFunctionCall(variableNode, end);
        ProviderData addData = providerData.createFromNodes(variableNode, interval);
        CNode addExpr = CLBinaryOperator.ADDITION.getCheckedInstance(addData)
                .newFunctionCall(variableNode, interval);
        CNode incrExpr = CNodeFactory.newAssignment(variableNode, addExpr);

        InstructionNode forInstruction = new ForNodes(providerData).newForInstruction(assignment, stopExpr,
                incrExpr);
        return forInstruction;
    }

    protected abstract boolean isParallelDepth(int depth);

    public ProviderData getProviderData() {
        return providerData;
    }

    public String getFinalName(String ssaName) {
        return variableNames.get(variableAllocation.getGroupIdForVariable(ssaName));
    }

    public DefaultVariableManager getVariableManager() {
        return variableManager;
    }

    public VariableAllocation getVariableAllocation() {
        return variableAllocation;
    }

    public Optional<VariableType> getUnprocessedVariableTypeFromSsaName(String ssaName) {
        return variableManager.getUnprocessedVariableTypeFromSsaName(ssaName);
    }

    protected Optional<VariableType> getCombinedVariableType(List<String> variables,
            GlobalTypeProvider globalTypeProvider) {
        // CHECK: We ignore globalTypeProvider. Is this the right thing to do here?
        return getCombinedVariableType(variables);
    }

    private Optional<VariableType> getCombinedVariableType(List<String> variables) {
        // Used before variableManager has been initialized

        List<VariableType> types = new ArrayList<>();
        for (String variable : variables) {
            Optional<VariableType> possibleType = getOriginalSsaVariableType(variable);
            if (!possibleType.isPresent()) {
                return Optional.empty();
            }

            types.add(possibleType.get());
        }

        return TypeCombiner.getCombinedVariableType(providerData.getSettings().get(CirKeys.DEFAULT_REAL), types);
    }

    protected abstract Optional<VariableType> getOriginalSsaVariableType(String variableName);
}
