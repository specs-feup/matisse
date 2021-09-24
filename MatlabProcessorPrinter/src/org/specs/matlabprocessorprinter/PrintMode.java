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

import org.specs.matlabprocessorprinter.printers.AstCodePrinter;
import org.specs.matlabprocessorprinter.printers.BytecodeCodePrinter;
import org.specs.matlabprocessorprinter.printers.CCodePrinter;
import org.specs.matlabprocessorprinter.printers.CIRCodePrinter;
import org.specs.matlabprocessorprinter.printers.CodePrinter;
import org.specs.matlabprocessorprinter.printers.InferredBytecodeCodePrinter;
import org.specs.matlabprocessorprinter.printers.MatlabCodePrinter;
import org.specs.matlabprocessorprinter.printers.OldCCodePrinter;
import org.specs.matlabprocessorprinter.printers.ParallelCCodePrinter;
import org.specs.matlabprocessorprinter.printers.XmlCodePrinter;

public enum PrintMode {
    MATLAB("Parsed MATLAB", MatlabCodePrinter.class),
    AST("AST", AstCodePrinter.class),
    BYTECODE("Bytecode", BytecodeCodePrinter.class),
    INFERRED_BYTECODE("Inferred bytecode", InferredBytecodeCodePrinter.class),
    LEGACY_C("C (Old System)", OldCCodePrinter.class),
    CIR("CIR", CIRCodePrinter.class),
    C("C", CCodePrinter.class),
    C_AND_OPENCL("C and OpenCL", ParallelCCodePrinter.class),
    XML("XML", XmlCodePrinter.class);

    private final String label;
    private final Class<? extends CodePrinter> clazz;

    private PrintMode(String label, Class<? extends CodePrinter> clazz) {
        this.label = label;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return this.label;
    }

    public Class<? extends CodePrinter> getPrinterClass() {
        return this.clazz;
    }
}
