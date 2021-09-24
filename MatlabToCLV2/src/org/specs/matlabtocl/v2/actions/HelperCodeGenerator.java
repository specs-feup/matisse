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

package org.specs.matlabtocl.v2.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.ImplementationResources;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.services.KernelInstanceCollection;
import org.specs.matlabtocl.v2.services.ProfilingOptions;

import pt.up.fe.specs.util.SpecsIo;

public final class HelperCodeGenerator implements PostCodeGenAction {
    private static final long serialVersionUID = 1L;

    private final KernelInstanceCollection kernelCollection;
    private final ProfilingOptions profilingOptions;
    private final CodeGenerationStrategyProvider codeStrategyProvider;

    public HelperCodeGenerator(KernelInstanceCollection kernelCollection,
            CodeGenerationStrategyProvider codeStrategyProvider,
            ProfilingOptions profilingOptions) {
        this.kernelCollection = kernelCollection;
        this.codeStrategyProvider = codeStrategyProvider;
        this.profilingOptions = profilingOptions;
    }

    @Override
    public void run(File outputFolder) {
        File output = new File(outputFolder, CLCodeGenUtils.HELPER_IMPLEMENTATION_NAME);
        List<FunctionInstance> generatedInstances = this.kernelCollection.getKernels();

        String generalTemplate = SpecsIo.getResource(ImplementationResources.MATISSE_CL);
        String loadKernelTemplate = SpecsIo.getResource(ImplementationResources.LOAD_KERNEL);

        StringBuilder loadKernels = new StringBuilder();
        for (FunctionInstance generatedInstance : generatedInstances) {
            loadKernels.append(loadKernelTemplate.replace("<KERNEL_NAME>", generatedInstance.getCName()));
        }

        List<String> programOptions = new ArrayList<String>();

        CLVersion requiredVersion = kernelCollection.getRequiredVersion();
        if (!requiredVersion.getFlag().isEmpty()) {
            programOptions.add(requiredVersion.getFlag());
        }

        String programFileName = codeStrategyProvider.getProgramFileName();

        String programLoaderCode;
        if (codeStrategyProvider.loadProgramFromSource()) {
            programLoaderCode = SpecsIo.getResource(ImplementationResources.LOAD_PROGRAM_FROM_SOURCE);
        } else {
            programLoaderCode = SpecsIo.getResource(ImplementationResources.LOAD_PROGRAM_FROM_BINARY);
        }

        try (FileWriter outputWriter = new FileWriter(output)) {
            boolean enableProfiling = profilingOptions.isKernelProfilingEnabled()
                    || profilingOptions.isDataTransferProfilingEnabled();

            outputWriter.write(generalTemplate.replace("<PROGRAM_FILE_NAME>", programFileName)
                    .replace("<LOAD_PROGRAM>", generatedInstances.isEmpty() ? "0" : "1")
                    .replace("<ENABLE_PROFILING>", enableProfiling ? "1" : "0")
                    .replace("<PRINT_KERNEL_TIME>", profilingOptions.isKernelProfilingEnabled() ? "1" : "0")
                    .replace("<PRINT_DATA_TRANSFER_TIME>",
                            profilingOptions.isDataTransferProfilingEnabled() ? "1" : "0")
                    .replace("<PROFILE_MODE>", profilingOptions.getProfileMode().getCode())
                    .replace("<PROGRAM_OPTIONS>", String.join(" ", programOptions))
                    .replace("<PROGRAM_LOADER_CODE>", programLoaderCode)
                    .replace("<LOAD_KERNELS>", loadKernels.toString()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
