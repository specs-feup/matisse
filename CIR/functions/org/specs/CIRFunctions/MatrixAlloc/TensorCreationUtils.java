/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIRFunctions.MatrixAlloc;

import static org.specs.CIRFunctions.LibraryFunctions.CLibraryConstantsV2.Constant.*;

import java.util.Arrays;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.LibraryFunctions.CLibraryConstantsV2;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdlibFunction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 * 
 */
public class TensorCreationUtils extends CirBuilder {

    public TensorCreationUtils(DataStore settings) {
        super(settings);
    }

    /**
     * Generates an "if" block with a failure message and exit.
     * 
     * @param condition
     * @param message
     * @return
     */
    public CNode getIfFailure(CNode condition, String message) {
        // Error printf
        String errorMsg = "\"" + message + "\\n\"";
        CNode printfArg = CNodeFactory.newLiteral(errorMsg);
        FunctionInstance printfInstance = CStdioFunction.PRINTF.newCInstance(ProviderData.newInstance(getSettings()));
        CNode printfCall = printfInstance.newFunctionCall(printfArg);

        // Exit function
        FunctionInstance exitFunction = CStdlibFunction.EXIT.newCInstance(ProviderData.newInstance(getSettings()));
        CNode exitConstant = new CLibraryConstantsV2(getNumerics()).getCToken(EXIT_FAILURE);
        CNode exitCall = exitFunction.newFunctionCall(exitConstant);

        return IfNodes.newIfThen(condition, Arrays.asList(printfCall, exitCall));
    }
}
