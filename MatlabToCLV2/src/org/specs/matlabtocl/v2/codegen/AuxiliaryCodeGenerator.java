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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Passes.CPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.MatlabToC.CodeBuilder.SsaCodegenUtils;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class AuxiliaryCodeGenerator extends CommonOpenCLCodeGenerator {
    private final TypedInstance instance;
    private final Map<String, VariableType> newTypes = new HashMap<>();
    private FunctionBody createdBody;

    private AuxiliaryCodeGenerator(DataStore passData,
            VariableAllocator variableAllocator,
            TypedInstance instance) {

        super(passData, variableAllocator, instance.getProviderData());

        Preconditions.checkArgument(instance != null);

        this.instance = instance;
    }

    @Override
    protected void buildVariableNameBlacklist(Set<String> blacklistedNames) {
        super.buildVariableNameBlacklist(blacklistedNames);

        blacklistedNames.addAll(this.instance.getFunctionType().getOutputAsInputNames());
    }

    @Override
    protected void processVariableNames(List<String> variableNames) {
        SsaCodegenUtils.processVariableNameChoices(
                this.instance,
                this.passData.get(ProjectPassServices.GLOBAL_TYPE_PROVIDER),
                variableNames,
                getVariableAllocation(),
                this::getCombinedVariableType);
    }

    @Override
    public FunctionBody getBody() {
        this.createdBody = this.instance.getFunctionBody().copy();

        applyPasses(createdBody);

        return createdBody;
    }

    @Override
    protected Optional<VariableType> getRawTypeForSsaVariable(String name) {
        return instance.getVariableType(name);
    }

    @Override
    protected String makeTemporary(String proposedName, VariableType type) {
        String newName = createdBody.makeTemporary(proposedName);

        this.newTypes.put(newName, type);

        return newName;
    }

    public static FunctionInstance buildImplementation(
            DataStore passData,
            VariableAllocator variableAllocator,
            TypedInstance instance) {

        AuxiliaryCodeGenerator codeGenerator = new AuxiliaryCodeGenerator(passData,
                variableAllocator,
                instance);
        codeGenerator.setUp();
        return codeGenerator
                .buildImplementation();
    }

    private FunctionInstance buildImplementation() {
        FunctionType baseFunctionType = this.instance.getFunctionType();

        boolean withOutputsAsInputs = baseFunctionType.hasOutputsAsInputs();
        FunctionTypeBuilder functionTypeBuilder = withOutputsAsInputs ? FunctionTypeBuilder.newWithOutputsAsInputs()
                : FunctionTypeBuilder.newSimple();

        for (Variable argument : baseFunctionType.getArguments()) {
            functionTypeBuilder.addInput(argument.getName(),
                    getTypeDecorator().decorateType(argument.getName(), argument.getType()));
        }

        if (withOutputsAsInputs) {
            for (Variable output : baseFunctionType.getOutputAsInputVariables()) {
                // We don't use normal outputs-as-inputs because we need the "local" address space.

                VariableType baseType = output.getType().pointer().getType(false);
                VariableType clType = getTypeDecorator().decorateType(null, baseType);
                String outputName = output.getName();
                VariableType outputType = clType.pointer().getType(true);

                functionTypeBuilder.addOutputAsInput(outputName, outputType);
            }
        } else {
            functionTypeBuilder.returning(getTypeDecorator().decorateType(null, baseFunctionType.getCReturnType()));
        }

        FunctionType functionType = functionTypeBuilder.build();

        CInstructionList body = new CInstructionList(functionType);
        generateCodeForBlock(0, body, 0);

        if (!(baseFunctionType.getCReturnType() instanceof VoidType)) {
            String returnName = baseFunctionType.getCOutputName() + "$ret";

            body.addReturn(getVariableManager().generateVariableExpressionForSsaName(body, returnName));
        }

        String functionName = this.instance.getFunctionIdentification().getName()
                + FunctionInstanceUtils.getTypesSuffix(functionType);
        InstructionsInstance instance = new InstructionsInstance(functionType, functionName, "kernels.cl", body);
        addDependentInstances(instance);

        // STUB
        for (CPass pass : DefaultRecipes.DefaultCRecipe.getPasses()) {
            pass.apply(instance, getProviderData());
        }

        return instance;
    }

    @Override
    protected void generateCodeForInstruction(int blockId, SsaInstruction instruction, CInstructionList currentBlock,
            int depth) {
        if (instruction instanceof ArgumentInstruction) {
            ArgumentInstruction argument = (ArgumentInstruction) instruction;

            CNode argumentNode = getVariableManager().generateVariableNodeForSsaName(argument.getOutput());
            int argumentIndex = argument.getArgumentIndex();

            if (instance.getFunctionType().getArgumentsNames().get(argumentIndex)
                    .equals(argumentNode.getCode())) {
                // No action needed
                return;
            }

            currentBlock.addAssignment(argumentNode,
                    CNodeFactory.newVariable(instance.getFunctionType().getArguments().get(argumentIndex)));

            return;
        }
        super.generateCodeForInstruction(blockId, instruction, currentBlock, depth);
    }

    @Override
    protected Optional<VariableType> getOriginalSsaVariableType(String variableName) {
        if (this.newTypes.containsKey(variableName)) {
            return Optional.ofNullable(this.newTypes.get(variableName));
        }

        return this.instance.getVariableType(variableName);
    }

    @Override
    protected boolean isParallelDepth(int depth) {
        return false;
    }
}
