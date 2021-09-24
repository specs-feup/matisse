/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Types.ATypes.Matrix.Conversion;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;

/**
 * When target is also a dynamic matrix, verify if elements are of the same type.
 * 
 * @author Joao Bispo
 * 
 */
public class ToMatrix implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    public CNode convert(CNode token, VariableType targetType) {

        MatrixType tokenType = (MatrixType) token.getVariableType();
        VariableType tokenElement = MatrixUtils.getElementType(tokenType);

        VariableType targetElement = MatrixUtils.getElementType(targetType);

        if (targetType.getClass().equals(tokenType.getClass())) {
            if (tokenElement.equals(targetElement)) {
                return token;
            }
        }

        // Create function call which assigns each element of the matrix, converting between types
        ProviderData data = ProviderData.newInstance(CirKeys.newDefaultInitialization("ToMatrixConversion"));

        return new GenericInstanceBuilder(data).getFunctionCall(tokenType.matrix().functions().copy(), token,
                CNodeFactory.newVariable("temp_mat_conversion_" + targetType.getSmallId(), targetType));
    }
}
