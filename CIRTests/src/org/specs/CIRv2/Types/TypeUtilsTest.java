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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.TypeUtils;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsFactory;

public class TypeUtilsTest {

    // private static final NumericFactoryV2 TYPES = new NumericFactoryV2(CTypeSizes.DEFAULT_SIZES);
    private static final NumericFactory TYPES = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

    @Test
    public void test() {
	List<VariableType> numericTypes = SpecsFactory.newArrayList();
	numericTypes.add(TYPES.newNumeric(CTypeV2.CHAR));
	numericTypes.add(TYPES.newNumeric(CTypeV2.FLOAT));

	assertTrue(TypeUtils.areOfType(numericTypes, NumericTypeV2.class));

	numericTypes.add(VoidType.newInstance());
	assertFalse(TypeUtils.areOfType(numericTypes, NumericTypeV2.class));

    }
}
