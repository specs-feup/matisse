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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.Functions.BaseFunctions.General.DynamicMatrixBuilderUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayAllocBuilder;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ZerosTest {

    @Before
    public void setUp() {
        MFileProvider.setEngine(MFileProvider.buildEngine(DataStore.newInstance("dummy"), LanguageMode.MATLAB));
    }

    @Test
    public void testZerosWithInputDims() {
        NumericFactory numerics = NumericFactory.defaultFactory();

        ProviderData data = ProviderData.newInstance(
                Arrays.asList(numerics.newInt(2), numerics.newInt(3), StringType.create("single", 8)),
                MatlabToCOptionUtils.newDefaultSettings());
        FunctionInstance instance = new ConstantArrayAllocBuilder("zeros", CNodeFactory.newCNumber(0))
                .newCInstance(data);

        TypeShape resultShape = MatrixUtils.getShape(instance.getFunctionType().getCReturnType());
        assertEquals(Arrays.asList(2, 3), resultShape.getDims());
    }

    @Test
    public void testDynamicBuilder2() {
        NumericFactory numerics = NumericFactory.defaultFactory();
        ScalarType intType = numerics.newInt();
        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType, TypeShape.newRow(2));

        ProviderData providerData = ProviderData.newInstance("test");
        providerData = providerData.create(sizeType);

        InstanceProvider provider = DynamicMatrixBuilderUtils.newDynamicBuilder(0);
        FunctionType functionType = provider.getType(providerData);

        MatrixType outputType = (MatrixType) functionType.getOutputTypes().get(0);

        assertEquals(Arrays.asList(-1, -1), outputType.getTypeShape().getDims());
    }

    @Test
    public void testDynamicBuilder3() {
        NumericFactory numerics = NumericFactory.defaultFactory();
        ScalarType intType = numerics.newInt();
        DynamicMatrixType sizeType = DynamicMatrixType.newInstance(intType, TypeShape.newRow(3));

        ProviderData providerData = ProviderData.newInstance("test");
        providerData = providerData.create(sizeType);

        InstanceProvider provider = DynamicMatrixBuilderUtils.newDynamicBuilder(0);
        FunctionType functionType = provider.getType(providerData);

        MatrixType outputType = (MatrixType) functionType.getOutputTypes().get(0);

        assertEquals(Arrays.asList(-1, -1, -1), outputType.getTypeShape().getDims());
    }
}
