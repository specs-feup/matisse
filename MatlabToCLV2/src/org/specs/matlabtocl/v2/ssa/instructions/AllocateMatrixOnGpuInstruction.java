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

package org.specs.matlabtocl.v2.ssa.instructions;

/**
 * Allocates a matrix on the GPU, with a given CPU matrix being used to provide the size/shape, but not the data.
 * 
 * @author Luís Reis
 *
 */
public final class AllocateMatrixOnGpuInstruction extends BufferBuilderFromMatrixInstruction {

    public AllocateMatrixOnGpuInstruction(String output, String input) {
        super(output, input);
    }

    @Override
    public AllocateMatrixOnGpuInstruction copy() {
        return new AllocateMatrixOnGpuInstruction(getOutput(), getInput());
    }

    @Override
    public String toString() {
        return getOutput() + " = allocate_on_gpu " + getInput();
    }
}
