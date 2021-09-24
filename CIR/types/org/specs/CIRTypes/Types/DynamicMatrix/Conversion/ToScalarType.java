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

package org.specs.CIRTypes.Types.DynamicMatrix.Conversion;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * When target is a scalar type, do a get(0).
 * 
 * @author Joao Bispo
 * 
 */
public class ToScalarType implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    public CNode convert(CNode token, VariableType targetType) {

        // Token is a DynamicMatrixType
        VariableType tokenType = token.getVariableType();
        DynamicMatrixType matrixType = (DynamicMatrixType) tokenType;
        // VariableType tokenElement = MatrixUtilsV2.getElementType(tokenType);
        ScalarType tokenElement = matrixType.getElementType();

        // Return call to get
        List<CNode> inputTokens = Arrays.asList(token, CNodeFactory.newCNumber(0));
        // Create setup that inlines the 'get' function
        DataStore setup = DataStore.newInstance("dynamic matrix to scalar");
        setup.get(CirKeys.INLINE).setInline(MatrixFunctionName.GET);

        ProviderData data = ProviderData.newInstance(CNodeUtils.getVariableTypes(inputTokens), setup);
        CNode getCall = CNodeFactory.newFunctionCall(matrixType.matrix().functions().get().newCInstance(data),
                inputTokens);

        // Convert element type to target type
        getCall = tokenElement.conversion().to(getCall, targetType);

        return getCall;

    }
}
