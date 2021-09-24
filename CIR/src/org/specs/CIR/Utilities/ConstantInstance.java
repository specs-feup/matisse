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

package org.specs.CIR.Utilities;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

/**
 * A function that returns a constant.
 * 
 * @author Joao Bispo
 * 
 */
public class ConstantInstance extends InlinedInstance {

    /**
     * @param functionTypes
     * @param functionName
     * @param inlineCode
     */
    private ConstantInstance(FunctionType functionTypes, String functionName, InlineCode inlineCode) {
        super(functionTypes, functionName, inlineCode);
    }

    /**
     * 
     * @param inputTypes
     *            information is not extracted from them, they are used for the input types, to avoid problems when
     *            checking the inputs of the function and the C tree
     * @param outputType
     * @param constant
     * @return
     */
    public static FunctionInstance newInstanceInternal(List<VariableType> inputTypes, ScalarType outputType,
            Number constant) {

        // Create return type
        String constantString = constant.toString();

        outputType = outputType.scalar().setConstantString(constantString);

        // Create function name
        String functionName = "constant_" + outputType.getSmallId() + "_" + constantString;

        FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputTypes, outputType);

        LiteralCode code = new LiteralCode(constantString);

        return new ConstantInstance(fTypes, functionName, code);
    }

    static class LiteralCode implements InlineCode {

        private final String literalValue;

        public LiteralCode(String literalValue) {
            this.literalValue = literalValue;
        }

        /* (non-Javadoc)
         * @see org.specs.CIR.Functions.Instances.InlineCode#getInlineCode(java.util.List)
         */
        @Override
        public String getInlineCode(List<CNode> arguments) {
            return literalValue;
        }

    }

}
