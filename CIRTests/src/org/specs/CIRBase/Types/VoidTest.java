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

package org.specs.CIRBase.Types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;

public class VoidTest {

    // private static final NumericFactoryV2 TYPES = new NumericFactoryV2(CTypeSizes.DEFAULT_SIZES);
    private static final NumericFactory TYPES = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

    @Test
    public void test() {
	assertTrue(VoidTypeUtils.isVoid(VoidType.newInstance()));
	assertFalse(VoidTypeUtils.isVoid(TYPES.newNumeric(CTypeV2.INT)));
    }

}
