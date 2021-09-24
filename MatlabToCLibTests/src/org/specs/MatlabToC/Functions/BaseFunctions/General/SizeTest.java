/**
 * Copyright 2016 SPeCS.
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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

public class SizeTest {

    private final NumericFactory numerics;

    public SizeTest() {
        numerics = new NumericFactory();
    }

    public NumericFactory getNumerics() {
        return numerics;
    }

    @Test
    public void testSize2() {

        ScalarType intType = getNumerics().newInt();

        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType, Arrays.asList(-1, -1));

        testOutputShape(TypeShape.newInstance(1, 2), Arrays.asList(sizeType));
    }

    @Test
    public void testSize3Undefined() {

        ScalarType intType = getNumerics().newInt();

        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType, Arrays.asList(-1, -1, -1));

        testOutputShape(TypeShape.newInstance(1, -1), Arrays.asList(sizeType));
    }

    @Test
    public void testSize3Defined() {

        ScalarType intType = getNumerics().newInt();

        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType, Arrays.asList(-1, -1, 3));

        testOutputShape(TypeShape.newInstance(1, 3), Arrays.asList(sizeType));
    }

    @Test
    public void testSizeFullUndefined() {

        ScalarType intType = getNumerics().newInt();

        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType);

        testOutputShape(TypeShape.newInstance(1, -1), Arrays.asList(sizeType));
    }

    private static void testOutputShape(TypeShape expected, List<VariableType> inputs) {
        ProviderData providerData = ProviderData.newInstance("test");
        providerData = providerData.create(inputs);
        providerData.setNargouts(1);

        FunctionType functionType = Size.newMatrixBuilder().getType(providerData);
        List<VariableType> outputTypes = functionType.getOutputTypes();

        Assert.assertEquals(1, outputTypes.size());

        Assert.assertEquals(expected, ((MatrixType) outputTypes.get(0)).getTypeShape());
    }
}
