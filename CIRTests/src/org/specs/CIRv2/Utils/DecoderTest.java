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

package org.specs.CIRv2.Utils;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

/**
 * @author Joao Bispo
 *
 */
public class DecoderTest {

    @Test
    public void test() {
	NumericFactory numerics = NumericFactory.defaultFactory();
	// TypeDecoder decoder = BaseTypesUtils.newTypeDecode(numerics);

	String doubleString = "8.213339e+000";
	VariableType doubleType = numerics.newDouble(8.213339);

	// decoder.decode(floatString);
	CLiteral literal = CLiteral.newNumber(doubleString);

	Assert.assertEquals(doubleType, literal.getType());

	// literal.getType()
    }
}
