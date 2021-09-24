/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.ArgumentRole;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.loopconverters.KernelBuilder;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGlobalSizeInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGroupSizeInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InitializationInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.specs.matlabtocl.v2.types.api.WorkSizeType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Finds set_gpu_range instructions that apply to SVM buffers and replaces them with explicit calls to kernels.
 * 
 * <p>
 * To be used on platforms where clEnqueueSVMMemFill is inefficient, so we'd rather use explicit OpenCL code instead.
 * 
 * <p>
 * Enabled only is svm_set_range_forbidden is set to true.
 * 
 * @author Luís Reis
 *
 */
public class GpuSVMSetRangeEliminationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "gpu_svm_set_range_elimination";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification().getName() + " due to %!disable directive.");
            return;
        }

        CodeGenerationStrategyProvider codeGenerationStrategyProvider = passData
                .get(CLServices.CODE_GENERATION_STRATEGY_PROVIDER);
        boolean enablePass = codeGenerationStrategyProvider.isSvmSetRangeForbidden();
        if (!enablePass) {
            logger.log("Skipping " + instance.getFunctionIdentification().getName()
                    + " because svm_set_range_forbidden is not set.");
            return;
        }

        logger.log("Starting");

        KernelInstanceSink sink = passData.get(CLServices.KERNEL_INSTANCE_SINK);

        for (SsaBlock block : instance.getBlocks()) {
            for (ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator(); iterator
                    .hasNext();) {

                SsaInstruction instruction = iterator.next();
                if (instruction instanceof SetGpuRangeInstruction) {
                    SetGpuRangeInstruction setRange = (SetGpuRangeInstruction) instruction;

                    if (!setRange.getOutput().isPresent()) {
                        logger.log("Skipping " + instruction + " because it is not applied to an SVM buffer.");
                        continue;
                    }

                    String begin = setRange.getBegin();
                    String end = setRange.getEnd();
                    String value = setRange.getValue();
                    String input = setRange.getBuffer();
                    String output = setRange.getOutput().get();

                    // FIXME
                    assert ConstantUtils.isConstantOne(instance, begin) : "FIXME";

                    String numThreadsInDim = end;

                    ParallelRegionSettings settings = setRange.getSettings();

                    ProviderData providerData = instance.getProviderData();

                    VariableType matrixType = instance.getVariableType(output).get();
                    String matrixStart = instance.makeTemporary("matrix_start", matrixType);
                    String matrixEnd = instance.makeTemporary("matrix_end", matrixType);

                    VariableType intType = providerData.getNumerics().newInt();
                    String iter = instance.makeTemporary("iter", intType);

                    FunctionBody body = new FunctionBody("set_gpu_range_impl", 0);
                    SsaBlock kernelBlock = new SsaBlock();
                    kernelBlock.addInstruction(new InitializationInstruction(begin));
                    kernelBlock.addInstruction(new InitializationInstruction(end));
                    kernelBlock.addInstruction(new InitializationInstruction(value));
                    kernelBlock.addInstruction(new InitializationInstruction(input));
                    kernelBlock.addInstruction(new ForInstruction(begin, begin, end, 1, 2));
                    body.addBlock(kernelBlock);
                    SsaBlock kernelLoop = new SsaBlock();
                    kernelLoop.addInstruction(
                            new PhiInstruction(matrixStart, Arrays.asList(input, matrixEnd), Arrays.asList(0, 1)));
                    kernelLoop.addInstruction(new IterInstruction(iter));
                    kernelLoop.addInstruction(
                            new SimpleSetInstruction(matrixEnd, matrixStart, Arrays.asList(iter), value));
                    body.addBlock(kernelLoop);
                    SsaBlock kernelAfterLoop = new SsaBlock();
                    kernelAfterLoop.addInstruction(
                            new PhiInstruction(output, Arrays.asList(input, matrixEnd), Arrays.asList(0, 1)));
                    body.addBlock(kernelAfterLoop);

                    Map<String, VariableType> types = new HashMap<>();
                    types.put(begin, instance.getVariableType(begin).get());
                    types.put(end, instance.getVariableType(end).get());
                    types.put(value, instance.getVariableType(value).get());
                    types.put(input, instance.getVariableType(input).get());
                    types.put(output, instance.getVariableType(input).get());
                    types.put(matrixStart, matrixType);
                    types.put(matrixEnd, matrixType);
                    types.put(iter, intType);

                    List<String> inputs = new ArrayList<>();
                    inputs.add(begin);
                    inputs.add(end);
                    inputs.add(value);
                    inputs.add(input);

                    for (String extraVar : settings.getKernelInputVariables()) {
                        inputs.add(extraVar);
                        types.put(extraVar, instance.getVariableType(extraVar).get());
                    }

                    ParallelRegionInstance parallelInstance = new ParallelRegionInstance(
                            settings,
                            body,
                            inputs,
                            Arrays.asList(output),
                            types);
                    Reporter reporter = providerData.getReportService();

                    List<Reduction> reductions = new ArrayList<>();
                    reductions.add(new Reduction(Arrays.asList(new LoopVariable(input, matrixStart, matrixEnd, output)),
                            ReductionType.MATRIX_SET, matrixType,
                            Arrays.asList(input, matrixStart, matrixEnd, output)));

                    GeneratedKernel generatedKernel = (GeneratedKernel) new KernelBuilder().generateCode(
                            instance,
                            parallelInstance,
                            new ParallelLoopInformation(Arrays.asList(0), reductions),
                            settings,
                            codeGenerationStrategyProvider,
                            sink,
                            passData,
                            instance::makeTemporary,
                            providerData,
                            reporter).get();

                    iterator.remove();
                    String workgroupSize;
                    if (settings.localSizes.isEmpty()) {
                        int size = 128;
                        workgroupSize = instance.makeTemporary("local_size", providerData.getNumerics().newInt(size));
                        iterator.add(AssignmentInstruction.fromInteger(workgroupSize, size));
                    } else {
                        workgroupSize = settings.localSizes.get(0);
                    }
                    String numGroupsInDim = instance.makeTemporary("num_groups", WorkSizeType.BASE_TYPE);
                    iterator.add(new ComputeGroupSizeInstruction(numGroupsInDim, numThreadsInDim, workgroupSize));
                    String globalSize = instance.makeTemporary("global_size", WorkSizeType.BASE_TYPE);
                    iterator.add(new ComputeGlobalSizeInstruction(globalSize, workgroupSize, numGroupsInDim));

                    List<String> arguments = new ArrayList<>();
                    int outputIndex = -1;
                    List<KernelArgument> kernelArguments = generatedKernel.getArguments();
                    for (int i = 0; i < kernelArguments.size(); i++) {
                        KernelArgument argument = kernelArguments.get(i);
                        if (argument.role == ArgumentRole.NUM_TASKS) {
                            arguments.add(numThreadsInDim);
                        } else {
                            if (argument.referencedReduction != null) {
                                assert outputIndex == -1;

                                outputIndex = i;
                            }
                            arguments.add(argument.referencedVariable);
                        }
                    }
                    assert outputIndex != -1;

                    InvokeKernelInstruction invokeInstruction = new InvokeKernelInstruction(generatedKernel,
                            Arrays.asList(globalSize), arguments,
                            Arrays.asList(output),
                            Arrays.asList(outputIndex));

                    for (SsaInstruction injectedInstruction : parallelInstance.getBody().getBlock(0)
                            .getInstructions()) {
                        if (injectedInstruction instanceof InitializationInstruction) {
                            break;
                        }

                        iterator.add(injectedInstruction);
                    }

                    iterator.add(invokeInstruction);
                }
            }
        }
    }

}
