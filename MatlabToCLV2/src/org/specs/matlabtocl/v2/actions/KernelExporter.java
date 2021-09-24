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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.RequiredCLExtensionAnnotation;
import org.specs.matlabtocl.v2.services.KernelInstanceCollection;

public final class KernelExporter implements PostCodeGenAction {
    private static final long serialVersionUID = 1L;

    private final KernelInstanceCollection kernelCollection;

    public KernelExporter(KernelInstanceCollection kernelCollection) {
        this.kernelCollection = kernelCollection;
    }

    @Override
    public void run(File outputFolder) {
        File output = new File(outputFolder, CLCodeGenUtils.PROGRAM_SOURCE_CODE_NAME);
        CLVersion requiredVersion = kernelCollection.getRequiredVersion();
        List<FunctionInstance> generatedInstances = this.kernelCollection.getKernels();

        if (generatedInstances.isEmpty()) {
            return;
        }

        List<FunctionInstance> includedInstances = new ArrayList<>();
        Stack<FunctionInstance> pendingInstances = new Stack<>();
        pendingInstances.addAll(generatedInstances);

        while (!pendingInstances.isEmpty()) {
            FunctionInstance topInstance = pendingInstances.peek();

            if (includedInstances.contains(topInstance)) {
                pendingInstances.pop();
                continue;
            }

            Set<FunctionInstance> missingInstances = new HashSet<>();
            for (FunctionInstance dependency : topInstance.getDeclarationInstances()) {
                if (!includedInstances.contains(dependency)) {
                    missingInstances.add(dependency);
                }
            }
            for (FunctionInstance dependency : topInstance.getImplementationInstances()) {
                if (!includedInstances.contains(dependency)) {
                    missingInstances.add(dependency);
                }
            }

            if (missingInstances.isEmpty()) {
                includedInstances.add(topInstance);
                pendingInstances.pop();
            } else {
                pendingInstances.addAll(missingInstances);
            }
        }

        try (FileWriter outputWriter = new FileWriter(output)) {
            if (isDoublePrecisionEnabled(includedInstances) && requiredVersion.compareTo(CLVersion.V1_2) < 0) {
                outputWriter.write("#if __OPENCL_VERSION__ < 120\n");
                outputWriter.write(
                        "// Since OpenCL 1.2, the pragma is no longer necessary and in fact may trigger warnings.\n");
                outputWriter.write("#pragma OPENCL EXTENSION cl_khr_fp64 : enable\n");
                outputWriter.write("#endif\n\n");
            }

            if (isSubGroupExtensionRequired(includedInstances) && requiredVersion.compareTo(CLVersion.V2_1) < 0) {
                outputWriter.write("#if __OPENCL_VERSION__ < 210\n");
                outputWriter.write(
                        "// Since OpenCL 2.1, the pragma is no longer necessary.\n");
                outputWriter.write("#pragma OPENCL EXTENSION cl_khr_subgroups : enable\n");
                outputWriter.write("#endif\n\n");
            }

            for (FunctionInstance includedInstance : includedInstances) {
                outputWriter.write(includedInstance.hasImplementation() ? includedInstance.getImplementationCode()
                        : includedInstance.getDeclarationCode());

                outputWriter.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDoublePrecisionEnabled(List<FunctionInstance> includedInstances) {
        for (FunctionInstance instance : includedInstances) {
            FunctionType functionType = instance.getFunctionType();

            if (functionType != null && functionType
                    .getArgumentsTypes()
                    .stream()
                    .anyMatch(KernelExporter::isDoubleTypeBased)) {
                return true;
            }

            if (instance instanceof InstructionsInstance) {
                InstructionsInstance instructionsInstance = (InstructionsInstance) instance;
                if (instructionsInstance
                        .getInstructions()
                        .getLocalVars().values().stream()
                        .anyMatch(KernelExporter::isDoubleTypeBased)) {
                    return true;
                }

                // TODO: Casts?
            }
        }

        return false;
    }

    private static boolean isDoubleTypeBased(VariableType type) {
        return type.code().getSimpleType().equals("double")
                || (type instanceof MatrixType
                        && ((MatrixType) type).matrix().getElementType().code().getSimpleType().equals("double"));
    }

    private static boolean isSubGroupExtensionRequired(List<FunctionInstance> includedInstances) {
        for (FunctionInstance instance : includedInstances) {
            FunctionType functionType = instance.getFunctionType();

            if (functionType != null &&
                    functionType.getAnnotationsStream(RequiredCLExtensionAnnotation.class)
                            .anyMatch(annotation -> annotation.getExtension().equals("cl_khr_subgroups"))) {
                return true;
            }
        }

        return false;
    }
}
