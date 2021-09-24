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

package org.specs.CIR.Utilities;

import static org.junit.Assert.*;

import org.junit.Test;
import org.specs.CIR.CirTestUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

public class InputsCheckerTest {

    @Test
    public void testOfType() {

	InstanceBuilder helper = CirTestUtils.createHelper();
	ProviderData data = CirTestUtils.newDefaultProviderData();

	DynamicMatrixType dynamicMatrix = DynamicMatrixType.newInstance(helper.getNumerics().newInt());
	StaticMatrixType staticMatrix = StaticMatrixType.newInstance(helper.getNumerics().newInt(), 2, 3);
	NumericTypeV2 intType = helper.getNumerics().newInt();
	NumericTypeV2 doubleType = helper.getNumerics().newDouble();

	CirInputsChecker matrixChecker = new CirInputsChecker()
		.ofType(MatrixType.class, 0);

	assertTrue(matrixChecker.create(data.create(staticMatrix)).check());
	assertTrue(matrixChecker.create(data.create(dynamicMatrix)).check());

	CirInputsChecker dynamicMatrixChecker = new CirInputsChecker()
		.ofType(DynamicMatrixType.class, 0);

	assertFalse(dynamicMatrixChecker.create(data.create(staticMatrix)).check());
	assertTrue(dynamicMatrixChecker.create(data.create(dynamicMatrix)).check());

	// Of type all
	CirInputsChecker scalarChecker = new CirInputsChecker()
		.ofType(ScalarType.class);

	assertTrue(scalarChecker.create(data.create(intType, intType)).check());
	assertFalse(scalarChecker.create(data.create(intType, staticMatrix, intType)).check());

	// Of type range (from 1 to end)
	CirInputsChecker scalarRangeChecker = new CirInputsChecker()
		.range(1).ofType(ScalarType.class);

	assertTrue(scalarRangeChecker.create(data.create(staticMatrix, intType, intType)).check());
	assertFalse(scalarRangeChecker.create(data.create(staticMatrix, staticMatrix, intType)).check());
	assertTrue(scalarRangeChecker.create(data.create(staticMatrix)).check());

	// Integer range type
	CirInputsChecker integerRangeChecker = new CirInputsChecker()
		.range(1).areInteger();

	assertTrue(integerRangeChecker.create(data.create(staticMatrix, intType, intType)).check());
	assertFalse(integerRangeChecker.create(data.create(staticMatrix, intType, doubleType)).check());
	assertFalse(integerRangeChecker.create(data.create(intType, staticMatrix)).check());
    }
}
