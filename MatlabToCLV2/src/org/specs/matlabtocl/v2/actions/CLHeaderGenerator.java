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
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Portability.PortabilityResource;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.codegen.HeaderResources;
import org.specs.matlabtocl.v2.services.KernelInstanceCollection;

import pt.up.fe.specs.util.SpecsIo;

public final class CLHeaderGenerator implements PostCodeGenAction {
    private static final long serialVersionUID = 1L;

    private final KernelInstanceCollection kernelCollection;

    public CLHeaderGenerator(KernelInstanceCollection kernelCollection) {
        this.kernelCollection = kernelCollection;
    }

    @Override
    public void run(File outputFolder) {
        File output = new File(outputFolder, CLCodeGenUtils.HEADER_NAME);
        List<FunctionInstance> generatedInstances = this.kernelCollection.getKernels();

        String generalTemplate = SpecsIo.getResource(HeaderResources.GENERAL_TEMPLATE);
        String kernelTemplate = SpecsIo.getResource(HeaderResources.KERNEL_DECLARATION_TEMPLATE);

        StringBuilder kernels = new StringBuilder();
        for (FunctionInstance instance : generatedInstances) {
            kernels.append(kernelTemplate.replace("<KERNEL_NAME>", instance.getCName()));
        }

        String exportLibDefinition = SpecsIo.getResource(PortabilityResource.DLLEXPORT)
                .replace("MATISSE_EXPORT", "MATISSE_CL_LIB_EXPORT");

        try (FileWriter outputWriter = new FileWriter(output)) {
            outputWriter.write(generalTemplate
                    .replace("<CL_EXPORT_LIB_DEFINITION>", exportLibDefinition)
                    .replace("<KERNELS>", kernels));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
