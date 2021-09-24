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

package org.specs.MatlabToC;

import org.junit.Ignore;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.RowDecNumericInstance;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMax;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxFunctions;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;

import pt.up.fe.specs.util.SpecsSystem;

public class InlineInMultipleCallsTester {

    @Ignore
    @Test
    public void test() {
        SpecsSystem.programStandardInit();

        // Create succession of calls that can be inlined -> numel(min(new_row()))
        ProviderData data = ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings());
        InstanceBuilder helper = new GenericInstanceBuilder(data);

        StaticMatrixType staticMatrix = StaticMatrixType.newInstance(StdIntFactory.newInt32(), 1, 3);

        CNode matrixVar = CNodeFactory.newVariable("matrix", staticMatrix);
        CNode one = CNodeFactory.newCNumber(1);
        // helper.getFunctionCall(RowDecNumericInstance.getProvider(), one, one, one, matrixVar);
        // FunctionInstance newRowInstance =
        // RowDecNumericInstance.getProvider().create(data.create(CTokenUtils.getVariableTypes(one, one, one)));
        // helper.getFunctionCall(, one, one, one);
        FunctionInstance newRowInstance = helper.getInstance(RowDecNumericInstance.getProvider(),
                CNodeUtils.getVariableTypes(one, one, one));

        CNode newRowCall = newRowInstance.newFunctionCall(one, one, one, matrixVar);
        CNode minResult = CNodeFactory.newVariable("min_result", staticMatrix.getElementType().pointer()
                .getType(true));

        FunctionInstance newMinInstance = helper.getInstance(MinMaxFunctions.getProviderVectorDec(MinMax.MIN),
                CNodeUtils.getVariableTypes(newRowCall));
        CNode minCall = newMinInstance.newFunctionCall(newRowCall, minResult);

        FunctionInstance newMinInstance2 = helper.getInstance(MinMaxFunctions.getProviderVectorDec(MinMax.MIN),
                CNodeUtils.getVariableTypes(matrixVar));
        CNode minCall2 = newMinInstance2.newFunctionCall(matrixVar, minResult);

        System.out.println("CODE 1:\n" + minCall.getCode());
        System.out.println("CODE 2:\n" + minCall2.getCode());

        // fail("Not yet implemented");
    }
}
