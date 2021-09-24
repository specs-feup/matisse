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

package org.specs.matlabprocessorprinter.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.Interfaces.DataStore;

public interface CodePrinter {
    List<ContentPage> getCode(String code, DataStore laraSetup, FileNode file, PrintStream reportStream,
            CodeGenerationSettings codeGenSettings) throws IOException;

    default boolean applyLara(boolean selected) {
        return true;
    }

    default void processSetup(DataStore basicSetup, CodeGenerationSettings codeGenSettings) {
    }
}
