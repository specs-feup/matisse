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

package org.specs.matisselib.typeinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.OutputData;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.functionproperties.ExportProperty;
import org.specs.matisselib.helpers.TypesMapUtils;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.services.TypeInformationService;
import org.specs.matisselib.services.reporting.CommonInstructionReportingService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.UndefinedInput;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.rules.ArgumentInstructionRule;
import org.specs.matisselib.typeinference.rules.AssignmentInstructionRule;
import org.specs.matisselib.typeinference.rules.AssumeInstructionRule;
import org.specs.matisselib.typeinference.rules.AssumeMatrixIndicesInRangeDirectiveInstructionRule;
import org.specs.matisselib.typeinference.rules.AssumeMatrixSizesMatchDirectiveInstructionRule;
import org.specs.matisselib.typeinference.rules.BranchInstructionRule;
import org.specs.matisselib.typeinference.rules.BreakInstructionRule;
import org.specs.matisselib.typeinference.rules.BuiltinVariableInstructionRule;
import org.specs.matisselib.typeinference.rules.CellGetInstructionRule;
import org.specs.matisselib.typeinference.rules.CellSetInstructionRule;
import org.specs.matisselib.typeinference.rules.CommentInstructionRule;
import org.specs.matisselib.typeinference.rules.DisableOptimizationDirectiveInstructionRule;
import org.specs.matisselib.typeinference.rules.EndInstructionRule;
import org.specs.matisselib.typeinference.rules.ForInstructionRule;
import org.specs.matisselib.typeinference.rules.IterInstructionRule;
import org.specs.matisselib.typeinference.rules.LineInstructionRule;
import org.specs.matisselib.typeinference.rules.MakeEmptyMatrixRule;
import org.specs.matisselib.typeinference.rules.MatrixGetInstructionRule;
import org.specs.matisselib.typeinference.rules.MatrixSetInstructionRule;
import org.specs.matisselib.typeinference.rules.PhiInstructionRule;
import org.specs.matisselib.typeinference.rules.ReadGlobalInstructionRule;
import org.specs.matisselib.typeinference.rules.SpecializeScalarValueDirectiveRule;
import org.specs.matisselib.typeinference.rules.StringInstructionRule;
import org.specs.matisselib.typeinference.rules.UntypedFunctionCallTypeInferenceRule;
import org.specs.matisselib.typeinference.rules.ValidateAtLeastOneEmptyMatrixInstructionRule;
import org.specs.matisselib.typeinference.rules.ValidateBooleanRule;
import org.specs.matisselib.typeinference.rules.VerticalFlattenRule;
import org.specs.matisselib.typeinference.rules.WhileInstructionRule;
import org.specs.matisselib.typeinference.rules.WriteGlobalInstructionRule;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.reporting.Reporter;

public class TypeInferencePass implements KeyUser {

    /**
     * Type information of the tree.
     */
    public static final DataKey<TypeInformationService> TYPE_INFORMATION = KeyFactory.object("type_information",
            TypeInformationService.class);
    public static final DataKey<InstructionReportingService> INSTRUCTION_REPORT_SERVICE = KeyFactory.object(
            "instruction_report_service", InstructionReportingService.class);
    public static final DataKey<InferenceRuleList> TYPE_INFERENCE_RULES = KeyFactory.object(
            "type_inference_rules", InferenceRuleList.class);

    public static final List<TypeInferenceRule> BASE_TYPE_INFERENCE_RULES = Arrays.asList(
            new ArgumentInstructionRule(),
            new BuiltinVariableInstructionRule(),
            new AssignmentInstructionRule(),
            new BreakInstructionRule(),
            new BranchInstructionRule(),
            new WhileInstructionRule(),
            new ForInstructionRule(),
            new IterInstructionRule(),
            new PhiInstructionRule(),
            new UntypedFunctionCallTypeInferenceRule(),
            new MatrixGetInstructionRule(),
            new MatrixSetInstructionRule(),
            new LineInstructionRule(),
            new CommentInstructionRule(),
            new StringInstructionRule(),
            new EndInstructionRule(),
            new ValidateBooleanRule(),
            new ValidateAtLeastOneEmptyMatrixInstructionRule(),
            new MakeEmptyMatrixRule(),
            new VerticalFlattenRule(),
            new CellGetInstructionRule(),
            new CellSetInstructionRule(),
            new ReadGlobalInstructionRule(),
            new WriteGlobalInstructionRule(),
            new SpecializeScalarValueDirectiveRule(),
            new AssumeInstructionRule(),
            new AssumeMatrixIndicesInRangeDirectiveInstructionRule(),
            new AssumeMatrixSizesMatchDirectiveInstructionRule(),
            new DisableOptimizationDirectiveInstructionRule());

    private final TypesMap defaultTypes;
    private final DataStore setupTable;
    private final ProjectPassCompilationManager compilationManager;

    public TypeInferencePass(TypesMap defaultTypes, DataStore setupTable,
            ProjectPassCompilationManager compilationManager) {

        Preconditions.checkArgument(defaultTypes != null);
        Preconditions.checkArgument(setupTable != null);
        Preconditions.checkArgument(compilationManager != null);

        this.defaultTypes = defaultTypes;
        this.setupTable = setupTable;
        this.compilationManager = compilationManager;
    }

    @Override
    public Collection<DataKey<?>> getWriteKeys() {
        return Arrays.asList(TypeInferencePass.TYPE_INFORMATION, PreTypeInferenceServices.COMMON_NAMING);
    }

    @Override
    public Collection<DataKey<?>> getReadKeys() {
        return Arrays.asList(TypeInferencePass.TYPE_INFORMATION, PassManager.NODE_REPORTING,
                PreTypeInferenceServices.WIDE_SCOPE, MatisseInit.PASS_LOG);
    }

    public TypedInstance inferMainFunctionTypes(FunctionIdentification rootFunction) {
        Preconditions.checkArgument(rootFunction != null);

        FunctionNode functionNode = (FunctionNode) compilationManager.getFunctionNode(rootFunction).get();
        List<String> inputs = functionNode.getDeclarationNode().getInputs().getNames();

        List<VariableType> obtainedTypes = new ArrayList<>();
        for (String input : inputs) {
            VariableType type = defaultTypes.getSymbol(Arrays.asList(), input);

            if (type == null) {
                // Try to use LARA type
                type = defaultTypes.getSymbol(rootFunction.getName(), input);

                assert type != null : "Type of " + input + " is null.";
            }
            obtainedTypes.add(type);
        }

        for (int i = 0; i < obtainedTypes.size(); ++i) {
            assert obtainedTypes.get(i) != null : "Got undefined input type in: " + inputs.get(i);
        }

        ProviderData data = ProviderData.newInstance(obtainedTypes, setupTable);
        data.setNargouts(functionNode.getDeclarationNode().getOutputs().getNumChildren());
        data.setPropagateConstants(true);
        return compilationManager.inferFunction(rootFunction, data);
    }

    public TypedInstance inferTypes(FunctionIdentification functionIdentification,
            FunctionNode originalNode,
            DataStore passData,
            ProviderData data,
            SsaRecipe ssaRecipe,
            InferenceRuleList inferenceRuleList) {

        Preconditions.checkArgument(functionIdentification != null);
        Preconditions.checkArgument(originalNode != null);
        Preconditions.checkArgument(data != null);
        // TODO: Get a better way to get the stream
        StringProvider originalCode = ((FileNode) originalNode.getRoot()).getOriginalCode();

        DefaultReportService newReportService = new DefaultReportService(data.getReportService(),
                passData.get(MatisseInit.PRINT_STREAM),
                false,
                functionIdentification,
                originalCode);
        data = data.withReportService(newReportService);

        FunctionDeclarationSt declarationNode = originalNode.getDeclarationNode();
        String functionName = declarationNode.getNameNode().getName();

        FunctionBody functionBody = SsaBuilder.buildSsa(originalNode, passData);

        passData.set(TypeInferencePass.INSTRUCTION_REPORT_SERVICE,
                new CommonInstructionReportingService(newReportService));
        passData.set(TypeInferencePass.TYPE_INFERENCE_RULES, inferenceRuleList);

        for (SsaPass pass : ssaRecipe.getPasses()) {
            passData.get(MatisseInit.PASS_LOG)
                    .append("Applying pre-type-inference SSA pass " + pass.getName() + " to " + functionName);
            pass.apply(functionBody, passData);
        }

        List<String> inputNames = declarationNode.getInputs().getNames();
        Set<String> globals = functionBody.getFlattenedInstructionsStream()
                .flatMap(instruction -> instruction.getReferencedGlobals().stream())
                .map(global -> global.substring(1))
                .collect(Collectors.toSet());
        for (int i = 0; i < inputNames.size(); ++i) {
            // Rename inputs with same name as globals
            String originalInput = inputNames.get(i);

            int rename = 0;
            String newName;
            for (;;) {
                String candidate = rename == 0 ? originalInput : originalInput + "_" + rename;

                if (!globals.contains(candidate) && (rename == 0 || !inputNames.contains(candidate))) {
                    newName = candidate;
                    break;
                }

                ++rename;
            }

            if (rename != 0) {
                if (functionBody.isByRef(originalInput)) {
                    throw newReportService.emitError(PassMessage.CORRECTNESS_ERROR,
                            "%!by_ref inputs/outputs can not have the same as a global used by the function.");
                }
            }

            inputNames.set(i, newName);
        }

        List<String> outputNames = declarationNode
                .getOutputs()
                .getNames();
        List<String> originalOutputNames = new ArrayList<>(outputNames);

        Map<String, String> overrideNames = new HashMap<>();
        // Rename outputs, as appropriate, so that:
        // 1. No inputs and outputs share the same name, unless it's a by-ref
        // 2. No globals and outputs share the same name.
        // 3. No two outputs share the same name.
        Map<String, String> newNames = new HashMap<>();
        for (int i = 0; i < outputNames.size(); ++i) {
            String outputName = outputNames.get(i);
            if (functionBody.isByRef(outputName)) {
                continue;
            }

            int rename = 0;
            String name;
            for (;;) {
                String candidateName = rename == 0 ? outputName : outputName + "_" + rename;

                if (!globals.contains(candidateName) &&
                        !inputNames.contains(candidateName) &&
                        (rename == 0 || !outputNames.contains(candidateName))) {
                    name = candidateName;
                    break;
                }

                ++rename;
            }

            if (!name.equals(outputName)) {
                outputNames.set(i, name);
                newNames.put(outputName + "$ret", name + "$ret");
                overrideNames.put(name + "$ret", outputName);
            }
        }
        if (!newNames.isEmpty()) {
            functionBody.renameVariables(newNames);
        }

        TypedInstance typedInstance = new TypedInstance(functionIdentification, inputNames, functionBody, originalCode,
                data);

        TypesMap typesForFunction = new TypesMap();
        typesForFunction.addSymbols(defaultTypes);
        List<String> scope = TypesMapUtils.getVariableTypeScope(functionIdentification);

        originalNode.getDescendants(GlobalSt.class)
                .stream()
                .flatMap(global -> global.getIdentifiers().stream())
                .forEach(identifier -> {
                    if (!typesForFunction.containsSymbol(scope, identifier)) {
                        VariableType type = typesForFunction.getSymbol("global", identifier);
                        if (type != null) {
                            typesForFunction.addSymbol(scope, identifier, type);
                        }
                    }
                });

        RootTypeInferenceContext context = new RootTypeInferenceContext(typedInstance, typesForFunction,
                overrideNames,
                passData);
        try {
            inferTypes(context, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Inference failure at: " + functionBody);
            throw e;
        }
        context.applyModifications(typedInstance.getFunctionBody());

        Optional<Integer> nargouts = data.getNargouts();
        if (nargouts.isPresent()) {
            if (nargouts.get() > outputNames.size()) {
                throw newReportService.emitError(PassMessage.TYPE_INFERENCE_FAILURE,
                        "Function " + functionName + " called with too many outputs (expected " + outputNames.size()
                                + ")");
            }

            if (nargouts.get() <= functionBody.getLastByRefOutputIndex()) {
                throw newReportService.emitError(PassMessage.SPECIALIZATION_FAILURE,
                        "Function " + functionName + " called with too few outputs: !by_ref outputs are not optional.");
            }

            outputNames = outputNames.subList(0, nargouts.get());
        }

        FunctionTypeBuilder typeBuilder = FunctionTypeBuilder.newAuto();
        List<VariableType> inputTypes = data.getInputTypes();

        for (int i = 0; i < data.getNumInputs(); ++i) {
            String inputName = inputNames.get(i);
            VariableType inputType = inputTypes.get(i);

            if (functionBody.isByRef(inputName)) {
                if (inputType instanceof ScalarType || inputType instanceof StringType) {
                    // TODO: This shouldn't be hardcoded.

                    throw newReportService.emitError(PassMessage.SPECIALIZATION_FAILURE,
                            "Type " + inputType + " can't be used as %!by_ref.");

                }

                typeBuilder.addReferenceInput(inputName, inputType);
            } else {
                typeBuilder.addInput(inputName, inputType);
            }
        }

        for (int i = 0; i < outputNames.size(); ++i) {
            String outputName = outputNames.get(i);

            List<VariableType> explicitOutputTypes = data.getOutputTypes();
            if (explicitOutputTypes != null && i >= explicitOutputTypes.size()) {
                // Unused return. Ignore it.
            } else if (explicitOutputTypes == null || explicitOutputTypes.get(i) == null) {
                VariableType inferredOutputType = typedInstance.getVariableType(outputName + "$ret")
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Could not infer type for return variable " + outputName + ", at "
                                                + functionIdentification.getName()));

                tryAddOutput(functionBody, typeBuilder,
                        outputName, inferredOutputType,
                        inputNames, inputTypes,
                        newReportService);
            } else {
                VariableType outputType = explicitOutputTypes.get(i);
                typedInstance.addOrOverwriteVariable(outputName + "$ret", outputType);

                tryAddOutput(functionBody, typeBuilder,
                        outputName, outputType,
                        inputNames, inputTypes,
                        newReportService);
            }
        }

        int totalOutputs = typeBuilder.getOutputsSoFar();
        typeBuilder.withDisplayNames(originalOutputNames.subList(0, totalOutputs));

        // It is possible for the size of inputTypes to be greater than the size of inputNames
        // And particularly common in functions that check for nargin.
        // These are functions with "optional arguments".
        if (inputTypes.size() < inputNames.size()) {
            inputNames.subList(inputTypes.size(), inputNames.size()).clear();

            assert inputNames.size() == inputTypes.size();
        }

        for (SsaInstruction instruction : typedInstance.getFlattenedInstructionsIterable()) {
            if (instruction.getInstructionType() == InstructionType.HAS_SIDE_EFFECT) {
                typeBuilder.withSideEffects();
                break;
            }
        }
        for (SsaInstruction instruction : typedInstance.getFlattenedInstructionsIterable()) {
            if (instruction.dependsOnGlobalState()) {
                typeBuilder.withGlobalStateDependency();
                break;
            }
        }

        if (typedInstance.getPropertyStream(ExportProperty.class).anyMatch(x -> true)) {
            typeBuilder.withPrefix("MATISSE_EXPORT");
        }

        typedInstance.setFunctionType(typeBuilder.build());

        return typedInstance;
    }

    private void tryAddOutput(FunctionBody body,
            FunctionTypeBuilder typeBuilder,
            String outputName,
            VariableType variableType,
            List<String> inputNames,
            List<VariableType> inputTypes, DefaultReportService reportService) {

        if (body.isByRef(outputName)) {
            int index = inputNames.indexOf(outputName);
            assert index >= 0;

            VariableType refType = inputTypes.get(index);
            if (!refType.equals(variableType)) {
                throw reportService.emitError(PassMessage.CORRECTNESS_ERROR,
                        "by_ref input and output types must match, instead got " + variableType + " and " + refType);
            }
        }

        typeBuilder.addOutputAsInput(outputName, variableType);
    }

    public static void inferTypes(TypeInferenceContext context, int blockId) {
        assert context != null;

        SsaBlock block = context.getInstance().getBlock(blockId);

        for (int instructionId = 0; instructionId < block.getInstructions().size(); ++instructionId) {
            if (context.isInterrupted()) {
                break;
            }

            SsaInstruction instruction = block.getInstructions().get(instructionId);
            InstructionLocation location = new InstructionLocation(blockId, instructionId);

            try {
                context.getTypeInferenceRules()
                        .stream()
                        .filter(rule -> rule.accepts(instruction))
                        .findFirst()
                        .orElseThrow(() -> {
                            String message = "At "
                                    + context.getFunctionIdentification().getName() + ": "
                                    + instruction.toString();

                            return new NotImplementedException(message);
                        })
                        .inferTypes(context, location, instruction);
            } catch (NotImplementedException e) {
                InstructionReportingService reportService = context.getInstructionReportService();
                e.printStackTrace();
                String message = e.getMessage();
                if (message.startsWith("Not yet implemented: ")) {
                    message = message.substring("Not yet implemented: ".length());
                }
                throw reportService.emitError(context.getInstance(),
                        instruction,
                        PassMessage.NOT_YET_IMPLEMENTED,
                        message);
            }
        }

        if (!context.isInterrupted()) {
            context.reachEndOfBlock(blockId);
        }
    }

    public static Optional<VariableType> getInputVariableType(TypeInferenceContext context,
            Input input) {
        if (input instanceof UndefinedInput) {
            // Do not fill the variable type for undefined input.
            return Optional.empty();
        }

        if (input instanceof NumberInput) {
            NumberInput numberInput = (NumberInput) input;

            VariableType variableType;

            double number = numberInput.getNumber();
            if (!numberInput.getNumericString().contains(".") && number == (int) number) {
                // TODO: Is this the best way to deal with this?
                // We are effectively treating MATLAB constants such as 1 as integers.
                // However, this is not accurate in MATLAB (since class(1) should be double).
                // We do this in order to handle matrix indices efficiently.
                // But we really should get a more robust system.
                // We check number == (int)number to avoid overflow.

                variableType = context.getProviderData()
                        .getNumerics()
                        .newInt((int) number);
            } else {
                variableType = context.getProviderData()
                        .getNumerics()
                        .newDouble(number);
            }

            return Optional.of(variableType);
        }

        if (input instanceof VariableInput) {
            VariableInput variableInput = (VariableInput) input;
            return context.getVariableType(variableInput.getName());
        }

        return Optional.empty();
    }

    public static ProviderData getCallProviderData(TypeInferenceContext context,
            UntypedFunctionCallInstruction instruction,
            List<OutputData> outputData) {

        List<VariableType> inputTypes = new ArrayList<>();
        for (String name : instruction.getInputVariables()) {
            VariableType inputType = context.getVariableType(name)
                    .orElseThrow(
                            () -> context.getInstructionReportService()
                                    .emitError(context.getInstance(), instruction,
                                            PassMessage.TYPE_INFERENCE_FAILURE,
                                            "Could not infer type for " + name));
            inputTypes.add(inputType);
        }

        ProviderData newData = context.getProviderData().createWithContext(inputTypes);
        Reporter reporter = newData.getReportService();

        if (reporter instanceof DefaultReportService) {
            int lineNumber = context.getInstance().getFunctionBody().getLineFromBlock(instruction, 0, -1);
            newData = newData.withReportService(((DefaultReportService) reporter).withLineNumber(lineNumber));
        } else {
            throw new IllegalStateException();
        }
        newData.setOutputData(outputData);

        return newData;
    }
}
