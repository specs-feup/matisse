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

package org.specs.matlabtocl.v2.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;

import com.google.common.base.Preconditions;

public final class KernelInstanceCollection implements KernelInstanceSink {
    private final AtomicInteger nextKernelId = new AtomicInteger();
    private final List<GeneratedKernel> kernelInstances = new ArrayList<>();

    @Override
    public int generateNextId() {
        return this.nextKernelId.incrementAndGet();
    }

    @Override
    public void addKernel(GeneratedKernel kernel) {
        Preconditions.checkArgument(kernel != null);

        synchronized (this.kernelInstances) {
            this.kernelInstances.add(kernel);
        }
    }

    public List<FunctionInstance> getKernels() {
        synchronized (this.kernelInstances) {
            return kernelInstances
                    .stream()
                    .map(instance -> instance.getInstance())
                    .collect(Collectors.toList());
        }
    }

    public void clearKernels() {
        synchronized (this.kernelInstances) {
            this.kernelInstances.clear();
        }
    }

    public CLVersion getRequiredVersion() {
        List<CLVersion> versions = kernelInstances
                .stream()
                .map(kernel -> kernel.getRequiredVersion())
                .collect(Collectors.toList());

        return CLVersion.getMaximumVersion(versions);
    }
}
