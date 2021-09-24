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

package org.specs.CIR;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.Void.VoidType;

public class CTokensTest {

    @Test
    public void testAssignment() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	NumericTypeV2 intType = helper.getNumerics().newInt();

	CNode token = CNodeFactory.newAssignment(CNodeFactory.newVariable("i", intType),
		CNodeFactory.newCNumber(CLiteral.newInstance(2, intType)));

	// Test type
	assertEquals(intType, token.getVariableType());

	// Test code
	assertEquals("i = 2", token.getCode());
    }

    @Test
    public void testFunctionCall() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	DynamicMatrixType matrixType = DynamicMatrixType.newInstance(helper.getNumerics().newFloat());

	FunctionInstance getInstance = helper.getInstance(matrixType.matrix().functions().get(), matrixType, helper
		.getNumerics().newInt());

	CNode token = CNodeFactory.newFunctionCall(getInstance,
		Arrays.asList(CNodeFactory.newVariable("mat", matrixType), CNodeFactory.newCNumber(1)));

	// Test type
	assertEquals(matrixType.getElementType(), token.getVariableType());

	// Test code
	assertEquals("mat->data[1]", token.getCode());
    }

    @Test
    public void testReturn() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	DynamicMatrixType matrixType = DynamicMatrixType.newInstance(helper.getNumerics().newFloat());

	CNode varToken = CNodeFactory.newVariable("mat", matrixType);
	CNode token = CNodeFactory.newReturn(varToken);

	// Test return type
	assertEquals(matrixType, token.getVariableType());

	// Test empty return
	assertEquals(VoidType.newInstance(), CNodeFactory.newReturn().getVariableType());

	// Test code
	assertEquals("\nreturn " + varToken.getCode(), token.getCode());
    }

    @Test
    public void testVariable() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	DynamicMatrixType matrixType = DynamicMatrixType.newInstance(helper.getNumerics().newFloat());

	CNode token = CNodeFactory.newVariable("mat", matrixType);

	// Test variable type
	assertEquals(matrixType, token.getVariableType());

	// Test code
	assertEquals("mat", token.getCode());
    }

    @Test
    public void testCNumber() {
	InstanceBuilder helper = CirTestUtils.createHelper();
	VariableType charType = helper.getNumerics().newChar();
	CNode token = CNodeFactory.newCNumber(3, charType);

	// Test variable type
	assertEquals(charType, token.getVariableType());

	// Test code
	assertEquals("3", token.getCode());

	// FINDS BUGS test
	/*
	int x = 2;
	if (x > 1) {
	    System.out.println();
	} else {
	    System.out.println();
	}
	*/
    }

    @Test
    public void testLiteral() {
	String literal = "i = 0;";
	CNode token = CNodeFactory.newLiteral(literal);

	// Test variable type
	assertEquals(VoidType.newInstance(), token.getVariableType());

	// Test code
	assertEquals(literal, token.getCode());
    }

    @Test
    public void testString() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	String string = "a string";
	CNode token = helper.newString(string);

	// Test variable type
	assertEquals(StringType.create(string, helper.getNumerics().newChar().getBits()), token.getVariableType());

	// Test code
	assertEquals("\"" + string + "\"", token.getCode());
    }

    @Test
    public void testComment() {
	String comment = "a comment";
	CNode token = CNodeFactory.newComment(comment);

	// Test code
	assertEquals("// " + comment, token.getCode());
    }

    @Test
    public void testReservedWord() {
	ReservedWord reservedWord = ReservedWord.For;
	CNode token = CNodeFactory.newReservedWord(reservedWord);

	// Test code
	assertEquals(reservedWord.getLiteral(), token.getCode());
    }

    @Test
    public void testInstruction() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	DynamicMatrixType matrixType = DynamicMatrixType.newInstance(helper.getNumerics().newFloat());

	FunctionInstance getInstance = helper.getInstance(matrixType.matrix().functions().get(), matrixType, helper
		.getNumerics().newInt());

	CNode fcToken = CNodeFactory.newFunctionCall(getInstance,
		Arrays.asList(CNodeFactory.newVariable("mat", matrixType), CNodeFactory.newCNumber(1)));

	CNode token = CNodeFactory.newInstruction(InstructionType.FunctionCall, fcToken);

	// Test variable type
	assertEquals(matrixType.getElementType(), token.getVariableType());

	// Test code
	assertEquals("mat->data[1];\n", token.getCode());
    }

    @Test
    public void testParenthesis() {
	InstanceBuilder helper = CirTestUtils.createHelper();
	VariableType charType = helper.getNumerics().newChar();
	CNode numberToken = CNodeFactory.newCNumber(3, charType);

	CNode token = CNodeFactory.newParenthesis(numberToken);

	// Test variable type
	assertEquals(charType, token.getVariableType());

	// Test code
	assertEquals("(" + numberToken.getCode() + ")", token.getCode());

    }

    @Test
    public void testBlock() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	NumericTypeV2 intType = helper.getNumerics().newInt();

	CNode assignToken = CNodeFactory.newAssignment(CNodeFactory.newVariable("i", intType),
		CNodeFactory.newCNumber(CLiteral.newInstance(2, intType)));

	CNode token = CNodeFactory.newBlock(assignToken);

	// Test code
	assertEquals("i = 2;\n", token.getCode());
    }
}
