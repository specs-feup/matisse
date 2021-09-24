/**
 * Copyright 2013 SPeCS.
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Code.CodeView;
import org.specs.CIRTypes.Types.Literal.LiteralType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Undefined.UndefinedCode;
import org.specs.CIRTypes.Types.Undefined.UndefinedType;
import org.specs.CIRTypes.Types.Void.VoidType;

public class CodeTypeTest {

    // private static final NumericFactoryV2 TYPES = new NumericFactoryV2(CTypeSizes.DEFAULT_SIZES);
    private static final NumericFactory TYPES = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

    @Test
    public void testInt() {
	// Create Numeric
	VariableType testType = TYPES.newNumeric(CTypeV2.INT);
	testCode(testType, "int");

    }

    @Test
    public void testDouble() {
	// Create Numeric
	VariableType testType = TYPES.newNumeric(CTypeV2.DOUBLE);
	testCode(testType, "double");

    }

    @Test
    public void testFloat() {
	// Create Numeric
	VariableType testType = TYPES.newNumeric(CTypeV2.FLOAT);
	testCode(testType, "float");

    }

    @Test
    public void testChar() {
	// Create Numeric
	VariableType testType = TYPES.newNumeric(CTypeV2.CHAR);
	testCode(testType, "char");

    }

    @Test
    public void testLiteral() {
	// Create Numeric
	LiteralType testType = LiteralType.newInstance("a_struct");
	testCode(testType, "a_struct");

    }

    @Test
    public void testVoid() {
	// Create Numeric
	VoidType testType = VoidType.newInstance();
	testCode(testType, "void");

    }

    @Test
    public void testUndefined() {
	// Create Numeric
	UndefinedType testType = UndefinedType.newInstance();
	testCode(testType, UndefinedCode.UNDEFINED);

    }

    private static <T extends CodeView> void testCode(T testType, String declaration) {
	// Test Code
	Code code = testType.code();
	assertEquals(code.getType(), declaration);
    }
}
