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

package org.specs.MatlabToC.Utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 *
 */
public class ChecksTest {

    @Test
    public void test() {
        ProviderData data = ProviderData.newInstance(DataStore.newInstance("base"));

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        StaticMatrixType staticMatrix = StaticMatrixType.newInstance(intType, 2, 3, 4);
        StringType stringType = StringType.create("hellp", 8);

        // Number of inputs
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).numOfInputs(1).check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix)).numOfInputs(2).check());

        // Number of inputs at least
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).numOfInputsAtLeast(1).check());
        assertTrue(new CirInputsChecker(data.create(intType, staticMatrix)).numOfInputsAtLeast(1).check());
        assertFalse(new CirInputsChecker(data.create(intType)).numOfInputsAtLeast(2).check());

        // Number of inputs range
        assertTrue(new CirInputsChecker(data.create(staticMatrix, intType)).numOfInputsRange(2, 3).check());
        assertTrue(new CirInputsChecker(data.create(staticMatrix, intType)).numOfInputsRange(2, 2).check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix)).numOfInputsRange(2, 3).check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix, intType, intType, intType)).numOfInputsRange(2, 3)
                .check());

        // Are matrices
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).areMatrices().check());
        assertTrue(new CirInputsChecker(data.create(staticMatrix, staticMatrix)).areMatrices().check());
        assertFalse(new CirInputsChecker(data.create(intType)).areMatrices().check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix, intType)).areMatrices().check());

        // Is matrix
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).isMatrix(0).check());
        assertTrue(new CirInputsChecker(data.create(intType, staticMatrix)).isMatrix(1).check());
        // Ignores checks on indexes that are not present
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).isMatrix(1).check());
        assertFalse(new CirInputsChecker(data.create(intType)).isMatrix(0).check());

        // Are scalar
        assertTrue(new CirInputsChecker(data.create()).areScalar().check());
        assertTrue(new CirInputsChecker(data.create(intType)).areScalar().check());
        assertTrue(new CirInputsChecker(data.create(intType, intType)).areScalar().check());
        assertFalse(new CirInputsChecker(data.create(intType, staticMatrix)).areScalar().check());

        // Is scalar
        assertTrue(new CirInputsChecker(data.create(intType)).isScalar(0).check());
        assertTrue(new CirInputsChecker(data.create(staticMatrix, intType)).isScalar(1).check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix, intType)).isScalar(0).check());
        assertTrue(new CirInputsChecker(data.create(intType)).isScalar(1).check());

        // Has matrix
        assertTrue(new CirInputsChecker(data.create(staticMatrix)).hasMatrix().check());
        assertTrue(new CirInputsChecker(data.create(staticMatrix, intType)).hasMatrix().check());
        assertFalse(new CirInputsChecker(data.create(intType)).hasMatrix().check());

        // Is String
        assertTrue(new CirInputsChecker(data.create(stringType)).isString(0).check());
        assertTrue(new CirInputsChecker(data.create(staticMatrix, stringType)).isString(1).check());
        assertFalse(new CirInputsChecker(data.create(staticMatrix, stringType)).isString(0).check());
        assertTrue(new CirInputsChecker(data.create(stringType)).isString(1).check());
    }
}
