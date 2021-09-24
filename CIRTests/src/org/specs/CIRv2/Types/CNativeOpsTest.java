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

package org.specs.CIRv2.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.specs.CIR.CirTestUtils;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

/**
 * @author Joao Bispo
 *
 */
public class CNativeOpsTest {

    @Test
    public void test() {
	NumericFactory numerics = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

	NumericTypeV2 aDouble = numerics.newDouble();
	NumericTypeV2 anInt = numerics.newInt();
	MatrixType intMatrix = DynamicMatrixType.newInstance(anInt);

	InstanceBuilder helper = CirTestUtils.createHelper();

	// NumericType add
	CNode call = helper.getFunctionCall(aDouble.scalar().functions().cOperator(COperator.Addition),
		CNodeFactory.newCNumber(2), CNodeFactory.newCNumber(2.5, aDouble));
	assertEquals("2.0 + 2.5", call.getCode());

	// Mix Numeric and StdInt
	call = helper.getFunctionCall(aDouble.scalar().functions().cOperator(COperator.Addition),
		CNodeFactory.newCNumber(2, StdIntType.newInstance(64, false)), CNodeFactory.newCNumber(3, anInt));

	assertEquals("2l + 3l", call.getCode());

	// Mix Numeric and Matrix

	// Output will be 'int' and not int64 because first argument is a literal
	call = helper.getFunctionCall(aDouble.scalar().functions().cOperator(COperator.Addition),
		CNodeFactory.newCNumber(2, StdIntType.newInstance(64, false)),
		CNodeFactory.newVariable("intMatrix", intMatrix));

	assertEquals("2l + intMatrix->data[0]", call.getCode());

	call = helper.getFunctionCall(aDouble.scalar().functions().cOperator(COperator.Addition),
		CNodeFactory.newVariable("anInt64", StdIntType.newInstance(64, false)),
		CNodeFactory.newVariable("intMatrix", intMatrix));

	assertEquals("anInt64 + (int64_t) intMatrix->data[0]", call.getCode());

	CNode minusOne = CNodeFactory.newCNumber(-1);
	NumericTypeV2 unsignedInt = numerics.newNumeric(CTypeV2.INT_UNSIGNED);
	CNode unsignedVar = CNodeFactory.newVariable("uint_var", unsignedInt);
	try {
	    helper.getFunctionCall(unsignedInt.scalar().functions().cOperator(COperator.Equal), unsignedVar,
		    minusOne).getCode();
	    // Should fail if succeeds
	    fail();
	} catch (Exception e) {
	    // Must throw exception
	}
    }
}
