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

package org.specs.matlabprocessorprinter;

public enum DemoMode {
    AST("MATLAB", PrintMode.MATLAB, false, false),
    PROCESSED_AST("Processed MATLAB", PrintMode.MATLAB, true, false),
    BYTECODE("Original Bytecode", PrintMode.BYTECODE, false, false),
    PROCESSED_BYTECODE("Processed Bytecode", PrintMode.BYTECODE, true, false),
    TYPED_BYTECODE("Typed Bytecode", PrintMode.INFERRED_BYTECODE, false, false),
    PROCESSED_TYPED_BYTECODE("Processed Typed Bytecode", PrintMode.INFERRED_BYTECODE, true, false),
    CSSA_BYTECODE("CSSA", PrintMode.INFERRED_BYTECODE, true, true),
    CIR("CIR", PrintMode.CIR, false, false),
    C("C Code", PrintMode.C, false, false),
    PROCESSED_C("Processed C", PrintMode.C, true, false),
    C_AND_OPENCL("C + OpenCL", PrintMode.C_AND_OPENCL, true, false),
    LEGACY_C("Processed Legacy C", PrintMode.LEGACY_C, true, false);

    private final String label;
    private final PrintMode mode;
    private final boolean applyPasses;
    private final boolean convertToCssa;

    private DemoMode(String label, PrintMode mode, boolean applyPasses, boolean convertToCssa) {
        this.label = label;
        this.mode = mode;
        this.applyPasses = applyPasses;
        this.convertToCssa = convertToCssa;
    }

    public String getLabel() {
        return this.label;
    }

    public PrintMode getMode() {
        return this.mode;
    }

    public boolean getApplyPasses() {
        return this.applyPasses;
    }

    public boolean getConvertToCssa() {
        return this.convertToCssa;
    }
}
