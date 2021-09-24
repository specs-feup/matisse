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

package org.specs.MatlabToC.CodeBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Portability.MatisseExportDefinitionInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ArgumentProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.AssignmentProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.AssumeProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.BranchProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.BreakProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.BuiltinProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.CellGetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.CommentProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ForProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.GetOrFirstProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.IterProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.LineProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.MakeEmptyMatrixProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.MatrixGetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.MatrixSetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.MultiSetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ParallelCopyProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.PhiProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ReadGlobalProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.RelativeGetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SimpleGetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SimpleSetProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.StringProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.TypedFunctionCallProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateAtLeastOneEmptyMatrixProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateBooleanProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateEqualProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateLooseMatchProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateSameSizeProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.ValidateTrueProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.WhileProcessor;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.WriteGlobalProcessor;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;
import org.specs.MatlabToC.MFunctions.MFunctionsUtils;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.DumpSsaProperty;
import org.specs.matisselib.functionproperties.ExportProperty;
import org.specs.matisselib.helpers.InputProcessor;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matisselib.unssa.ControlFlowGraphBuilder;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matisselib.unssa.VariableNameChooser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.reporting.Reporter;

/**
 * The new C code generator, based on the pass system
 * 
 * @author Luís Reis
 *
 */
public class SsaToCBuilder extends SsaToCBuilderService {

    // For debugging purposes
    private final ProjectPassCompilationManager manager;
    private final DataStore passData;
    private final TypedInstance instance;
    private final ControlFlowGraph cfg;
    private final MatlabFunctionTable systemFunctions;
    private final VariableAllocator allocator;
    private final VariableAllocation allocations;
    private final List<String> variableNames;
    private ProviderData currentProvider;
    private final DefaultReportService rootReportService;
    private final UsageMap usageMap;
    private final StringBuilder prefixComments = new StringBuilder();
    private final DefaultVariableManager variableManager;
    private final Set<String> extraHeaderFiles = new HashSet<>();
    private final Map<Variable, String> namedTemporaries = new HashMap<>();
    private final Set<Variable> literalVariables = new HashSet<>();

    public static final SsaToCRuleList DEFAULT_SSA_TO_C_RULES = new SsaToCRuleList(Arrays.asList(
            new ArgumentProcessor(),
            new AssignmentProcessor(),
            new BuiltinProcessor(),
            new TypedFunctionCallProcessor(),
            new MatrixGetProcessor(),
            new SimpleGetProcessor(),
            new GetOrFirstProcessor(),
            new RelativeGetProcessor(),
            new CellGetProcessor(),
            new ParallelCopyProcessor(),
            new ForProcessor(),
            new BranchProcessor(),
            new PhiProcessor(),
            new IterProcessor(),
            new MatrixSetProcessor(),
            new SimpleSetProcessor(),
            new MultiSetProcessor(),
            new LineProcessor(),
            new StringProcessor(),
            new CommentProcessor(),
            new ValidateBooleanProcessor(),
            new WhileProcessor(),
            new BreakProcessor(),
            new ReadGlobalProcessor(),
            new WriteGlobalProcessor(),
            new MakeEmptyMatrixProcessor(),
            new AssumeProcessor(),
            new ValidateEqualProcessor(),
            new ValidateTrueProcessor(),
            new ValidateSameSizeProcessor(),
            new ValidateLooseMatchProcessor(),
            new ValidateAtLeastOneEmptyMatrixProcessor()

    // Some instructions never reach this class, because of the elimination passes.
    ));

    private final SsaToCRuleList ssaToCRules;

    private SsaToCBuilder(ProjectPassCompilationManager manager,
            DataStore passData,
            TypedInstance instance,
            MatlabFunctionTable systemFunctions,
            VariableAllocator allocator,
            SsaToCRuleList ssaToCRules) {

        this.passData = passData;
        this.cfg = ControlFlowGraphBuilder.build(instance.getFunctionBody());
        this.manager = manager;
        this.instance = instance;
        this.systemFunctions = systemFunctions;
        currentProvider = instance.getProviderData();
        rootReportService = (DefaultReportService) instance.getProviderData().getReportService();
        this.ssaToCRules = ssaToCRules;

        usageMap = UsageMap.build(instance.getFunctionBody());

        manager.log("Performing variable allocations");
        this.allocator = allocator;
        GlobalTypeProvider globalTypeProvider = passData.get(ProjectPassServices.GLOBAL_TYPE_PROVIDER);
        allocations = allocator.performAllocation(instance, globalTypeProvider);

        final Set<String> blacklistedNames = new HashSet<>();
        final FunctionType functionType = instance.getFunctionType();
        final List<String> argumentNames = functionType.getArgumentsNames();
        VariableNameBlacklist.addCKeywords(blacklistedNames);
        VariableNameBlacklist.addAdditionalCppKeywords(blacklistedNames);
        VariableNameBlacklist.addLibraryNames(blacklistedNames);

        blacklistedNames.addAll(argumentNames);
        blacklistedNames.addAll(functionType.getOutputAsInputNames());

        variableNames = VariableNameChooser.getNames(allocations, blacklistedNames);
        for (final String name : variableNames) {
            assert name.matches("^[a-zA-Z][a-zA-Z_0-9]*$") : "Invalid name \"" + name + "\"";
        }

        SsaCodegenUtils.processVariableNameChoices(instance, globalTypeProvider, variableNames, allocations);

        variableManager = new DefaultVariableManager(variableNames,
                allocations,
                usageMap,
                name -> Optional.ofNullable(manager.getGlobalTypes().getSymbol(name)),
                instance::getCombinedVariableTypeFromVariables,
                this::decorateType);
    }

    public static InstructionsInstance buildImplementation(ProjectPassCompilationManager manager,
            TypedInstance instance,
            MatlabFunctionTable systemFunctions,
            VariableAllocator allocator,
            SsaToCRuleList ssaToCRules) {
        Preconditions.checkArgument(instance != null);
        Preconditions.checkArgument(systemFunctions != null);
        Preconditions.checkArgument(allocator != null);

        return new SsaToCBuilder(manager, manager.getPassData(instance), instance, systemFunctions, allocator,
                ssaToCRules)
                        .buildImplementation();
    }

    private InstructionsInstance buildImplementation() {
        manager.log("Generating C code");

        final String filename = instance.getFunctionIdentification().getFileNoExtension();

        final FunctionType functionType = instance.getFunctionType();
        final CInstructionList body = new CInstructionList(functionType);

        generateCodeForBlock(0, body);

        String returnedVariable = null;
        CNode returnStatement = null;

        if (functionType.getCReturnType() instanceof VoidType) {
            body.addReturn();
        } else {
            if (functionType.getOutputAsInputTypes().size() > 0) {
                returnedVariable = functionType.getOutputAsInputNames().get(0) + "$ret";
            } else {
                returnedVariable = functionType.getCOutputName() + "$ret";
            }

            // We use a dummy return statement that we later remove
            // because at this point we haven't yet introduced calls to free
            // that might prevent the inlining.
            returnStatement = CNodeFactory.newInstruction(org.specs.CIR.Tree.Instructions.InstructionType.Return,
                    CNodeFactory.newReturn(generateVariableNodeForSsaName(returnedVariable)));
            body.addInstruction(returnStatement);
        }

        for (Variable namedTemporary : namedTemporaries.keySet()) {
            String generatedName = namedTemporaries.get(namedTemporary);
            body.addLiteralVariable(new Variable(generatedName, namedTemporary.getType()));
        }

        for (Variable variable : literalVariables) {
            body.addLiteralVariable(variable);

            variable.getType().code().getInstances().forEach(getVariableManager()::addDependency);
        }

        new MFunctionsUtils(currentProvider).addCallsToFree(body);

        if (returnedVariable != null) {
            body.get().remove(returnStatement);
            body.addReturn(generateVariableExpressionForSsaName(body, returnedVariable));
        }

        final String instanceName = generateFunctionName();

        if (instance.getPropertyStream(ExportProperty.class).anyMatch(x -> true)) {
            getVariableManager().addDependency(new MatisseExportDefinitionInstance());
        }

        final InstructionsInstance functionInstance = new InstructionsInstance(instanceName, filename,
                body);
        functionInstance.setComments(prefixComments.toString());
        functionInstance.setCustomIncludes(extraHeaderFiles);
        functionInstance.setCustomImplementationInstances(getVariableManager().getDependencies());

        return functionInstance;
    }

    private String generateFunctionName() {
        final String functionName = instance.getFunctionIdentification().getName();

        Optional<ExportProperty> exportProperty = instance.getPropertyStream(ExportProperty.class).findAny();
        if (exportProperty.isPresent()) {
            return exportProperty.get().getAbiName().orElse(functionName);
        }

        final FunctionType functionType = instance.getFunctionType();

        final StringBuilder builder = new StringBuilder();
        builder.append(functionName);
        builder.append(FunctionInstanceUtils.getTypesSuffix(functionType));
        for (int i = 0; i < functionType.getArgumentsNames().size(); ++i) {
            final String argumentName = functionType.getArgumentsNames().get(i);
            final VariableType argumentType = functionType.getArgumentsTypes().get(i);
            if (instance.isScalarConstantSpecialized(i) && argumentType instanceof ScalarType) {

                final ScalarType scalar = (ScalarType) argumentType;
                final String constant = scalar.scalar().getConstantString();

                builder.append("_");
                builder.append(argumentName);
                builder.append("_");
                builder.append(constant == null ? "generic" : constant.replace('.', '_'));
            }
            if (argumentType instanceof StringType) {
                builder.append("_");
                int hashCode = StringTypeUtils.getString(argumentType).hashCode();
                if (hashCode < 0) {
                    builder.append("m");
                    hashCode = -hashCode;
                }
                builder.append(hashCode);
            }
            if (argumentType instanceof DynamicMatrixType) {
                builder.append("_");
                TypeShape shape = InputProcessor.processDynamicMatrixInputShape((DynamicMatrixType) argumentType);
                if (shape.getDims().isEmpty()) {
                    builder.append("undef");
                } else if (shape.getDims().equals(Arrays.asList(1, -1))) {
                    builder.append("row");
                } else if (shape.getDims().equals(Arrays.asList(-1, 1))) {
                    builder.append("col");
                } else if (shape.isScalar()) {
                    builder.append("scalar");
                } else if (shape.getDims().equals(Arrays.asList(-1, -1))) {
                    builder.append("2d");
                } else {
                    int hashCode = shape.getDims().hashCode();
                    if (hashCode < 0) {
                        builder.append("m");
                        hashCode = -hashCode;
                    }
                    builder.append(hashCode);
                }
            }
        }
        builder.append("_");
        builder.append(functionType.getOutputTypes().size());

        return builder.toString();
    }

    @Override
    public void generateCodeForBlock(int blockId, CInstructionList currentBlock) {
        SsaBlock block = instance.getBlocks().get(blockId);

        for (final SsaInstruction instruction : block.getInstructions()) {
            try {
                generateCodeForInstruction(blockId, instruction, currentBlock);
            } catch (final RuntimeException e) {
                System.err.println("At: " + instruction);
                throw e;
            }
        }
    }

    private void generateCodeForInstruction(int blockId,
            SsaInstruction instruction,
            CInstructionList currentBlock) {

        if (passData.get(MatisseLibOption.DUMP_SSA_INSTRUCTIONS)
                || instance.getFunctionBody().hasProperty(DumpSsaProperty.class)) {
            currentBlock.addComment(instruction.toString());
        }

        if (passData.get(MatisseLibOption.DUMP_OUTPUT_TYPES)) {
            for (final String output : instruction.getOutputs()) {
                final Optional<VariableType> outputType = instance.getVariableType(output);
                currentBlock.addComment("Type of " + output + ": " + (outputType.isPresent() ? outputType.get()
                        : "undefined"));
            }
        }

        if (!ssaToCRules.tryApply(this, currentBlock, instruction)) {
            System.err.println("---");
            System.err.println(instance);
            throw new NotImplementedException(instruction.toString());
        }

    }

    @Override
    public DefaultVariableManager getVariableManager() {
        return variableManager;
    }

    public Optional<VariableNode> generateVariableNodeForFinalName(String finalName) {
        return variableManager.generateVariableNodeForFinalName(finalName);
    }

    /**
     * 
     * @param isSafe
     *            True if the right hand variable is known to be defined. False if it can be an undefined value.
     */

    @Override
    public boolean generateAssignment(CInstructionList currentBlock, VariableNode leftHand, VariableNode rightHand) {
        if (leftHand.getCode().equals(rightHand.getCode())) {
            // If they are the same, then the assignment is pointless.
            return false;
        }

        final VariableType leftType = leftHand.getVariableType();

        // FIXME: What if rightType is not equal to leftType?
        // (e.g. matrix = scalar)
        CNode node = AssignmentUtils.buildAssignmentNode(leftHand, rightHand, getCurrentProvider());
        currentBlock.addInstruction(node);

        return true;
    }

    @Override
    public void generateAssignmentForFinalNames(CInstructionList currentBlock, String out, String in) {
        final Optional<VariableNode> potentialRightHand = generateVariableNodeForFinalName(in);
        potentialRightHand.ifPresent(rightHand -> {
            final VariableNode leftHand = generateVariableNodeForFinalName(out).get();

            generateAssignment(currentBlock, leftHand, rightHand);
        });
        if (!potentialRightHand.isPresent()) {
            currentBlock.addComment("Assignment from undefined value removed: " + out + " = " + in);
        }
    }

    @Override
    public TypedInstance getInstance() {
        return instance;
    }

    @Override
    public ProviderData getCurrentProvider() {
        return currentProvider;
    }

    @Override
    public Optional<MatlabFunction> getSystemFunction(String functionName) {
        return Optional.ofNullable(systemFunctions.getPrototypes().get(functionName));
    }

    @Override
    public FunctionInstance buildAuxiliaryImplementation(TypedInstance newInstance) {
        return buildImplementation(manager, newInstance, systemFunctions, allocator, ssaToCRules);
    }

    @Override
    public TypedInstance getSpecializedUserFunctionInScope(String functionName, ProviderData providerData) {
        return manager.getSpecializedUserFunction(instance.getFunctionIdentification(), functionName,
                providerData);
    }

    @Override
    public void setLine(int line) {
        final Reporter reportService = rootReportService.withLineNumber(line);
        currentProvider = instance.getProviderData().withReportService(reportService);
    }

    public VariableType decorateType(String variableName, VariableType proposedType) {

        if (instance.getFunctionType().getOutputAsInputNames().contains(variableName)) {
            assert !proposedType.pointer().isByReference();

            if (proposedType.pointer().supportsReference()) {
                return proposedType.pointer().getType(true);
            }
        }

        return proposedType;
    }

    @Override
    public MatlabToCEngine getEngine() {
        return MFileProvider.getEngine();
    }

    @Override
    public Reporter getReporter() {
        return currentProvider.getReportService();
    }

    @Override
    public void addPrefixComment(String content) {
        prefixComments.append(content);
        prefixComments.append("\n");
    }

    @Override
    public void removeUsage(String ssaName) {
        usageMap.decrement(ssaName);
    }

    public void addHeader(String headerName) {
        extraHeaderFiles.add(headerName);
    }

    @Override
    public VariableNode makeNamedTemporary(String name, VariableType type) {
        Variable variable = new Variable(name, type);
        String generatedName = namedTemporaries.get(variable);
        if (generatedName == null) {
            namedTemporaries.put(variable,
                    generatedName = variableManager.generateTemporary(name, type).getName());
        }

        return CNodeFactory.newVariable(generatedName, type);
    }

    @Override
    public void addLiteralVariable(Variable variable) {
        literalVariables.add(variable);
    }

    @Override
    public ControlFlowGraph getControlFlowGraph() {
        return cfg;
    }

    @Override
    public DataStore getPassData() {
        return passData;
    }

    @Override
    public void addDependency(FunctionInstance instance) {
        getVariableManager().addDependency(instance);
    }
}
