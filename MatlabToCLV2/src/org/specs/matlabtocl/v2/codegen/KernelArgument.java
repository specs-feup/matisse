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

import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public final class KernelArgument {
    public final String name;
    public final ArgumentRole role;
    public final boolean isReadOnly;
    public final String referencedVariable;
    public final String referencedReduction;
    public final ScalarType clType;
    public final int dim;

    private KernelArgument(String name,
            ArgumentRole role,
            boolean isReadOnly,
            String referencedVariable,
            String referencedReduction,
            ScalarType clType,
            int dim) {

        this.name = name;
        this.role = role;
        this.isReadOnly = isReadOnly;
        this.referencedVariable = referencedVariable;
        this.referencedReduction = referencedReduction;
        this.clType = clType;
        this.dim = dim;
    }

    public static KernelArgument importData(String name, String referencedVariable) {
        return new KernelArgument(name, ArgumentRole.IMPORTED_DATA, true, referencedVariable, null, null, -1);
    }

    public static KernelArgument importReductionData(String name, String referendedVariable,
            String referencedReduction) {
        return new KernelArgument(name, ArgumentRole.IMPORTED_DATA, false, referendedVariable, referencedReduction,
                null, -1);
    }

    public static KernelArgument importNumel(String name, String referencedVariable, ScalarType clType) {
        return new KernelArgument(name, ArgumentRole.IMPORTED_NUMEL, true, referencedVariable, null, clType, -1);
    }

    public static KernelArgument importDim(String name, String referencedVariable, ScalarType clType, int dim) {
        return new KernelArgument(name, ArgumentRole.IMPORTED_DIM, true, referencedVariable, null, clType, dim);
    }

    public static KernelArgument importValue(String name, String referencedVariable, ScalarType clType) {
        return new KernelArgument(
                name,
                ArgumentRole.IMPORTED_VALUE,
                true,
                referencedVariable,
                null,
                clType,
                -1);
    }

    public static KernelArgument importLocalReductionBuffer(String name, String referencedVariable, ScalarType clType) {
        return new KernelArgument(name,
                ArgumentRole.LOCAL_REDUCTION_BUFFER,
                false,
                referencedVariable,
                null,
                clType,
                -1);
    }

    public static KernelArgument importGlobalPerWorkGroupBuffer(String name, String referencedVariable,
            String referencedReduction, ScalarType clType) {
        return new KernelArgument(name,
                ArgumentRole.GLOBAL_PER_WORK_GROUP_BUFFER,
                false,
                referencedVariable,
                referencedReduction,
                clType,
                -1);
    }

    public static KernelArgument importGlobalPerWorkItemBuffer(String name, String referencedVariable,
            String referencedReduction, ScalarType clType) {
        return new KernelArgument(name,
                ArgumentRole.GLOBAL_PER_WORK_ITEM_BUFFER,
                false,
                referencedVariable,
                referencedReduction,
                clType,
                -1);
    }

    public static KernelArgument importNumTasks(String numThreads, ScalarType clType, int index) {
        return new KernelArgument(numThreads,
                ArgumentRole.NUM_TASKS,
                true,
                null,
                null,
                clType,
                index);
    }

    @Override
    public String toString() {
        return String.format("%s (%s):%s, %s, %s", this.name, this.role, this.referencedVariable,
                this.referencedReduction, this.clType);
    }
}
