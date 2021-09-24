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

package org.specs.CIRv2.Types.ATypes.Matrix.Functions;

import org.junit.Test;
import org.specs.CIR.CirTestUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.Functions.MatrixCopy;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;

import pt.up.fe.specs.util.SpecsCollections;

public class MatrixCopySnippet {

    @Test
    public void test() {
        InstanceBuilder helper = CirTestUtils.createHelper();

        MatrixType dynamicDouble = DynamicMatrixType.newInstance(helper.getNumerics().newDouble());
        MatrixType dynamicFloat = DynamicMatrixType.newInstance(helper.getNumerics().newFloat());
        ProviderData data = ProviderData.newInstance(
                SpecsCollections.asListT(VariableType.class, dynamicDouble, dynamicFloat), helper.getSettings());

        MatrixCopy matrixCopy = new MatrixCopy(data);

        System.out.println(matrixCopy.create().getImplementationCode());
    }
}
