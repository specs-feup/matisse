/**
 * Copyright 2016 SPeCS.
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

package org.specs.CIR.Operators;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CToCRules.CToCData;
import org.specs.CIR.Tree.CToCRules.CToCRules;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

public class ComparisonTests {
    @Test
    public void testBug37() {
	// https://bitbucket.org/specsfeup/specs-java/issues/37/bug-in-implementation-of-operator

	VariableType doubleType = NumericFactory.defaultFactory().newDouble(1.2);
	VariableType intType = NumericFactory.defaultFactory().newInt(1);

	List<CNode> tokens = new ArrayList<>();
	tokens.add(CNodeFactory.newCNumber(1.2, doubleType));
	tokens.add(CNodeFactory.newCNumber(1));

	ProviderData data = ProviderData.newInstance("test-bug-37")
		.createFromNodes(tokens);

	FunctionCallNode node = COperator.GreaterThan.getCheckedInstance(data)
		.newFunctionCall(tokens);

	CNode assignment = CNodeFactory.newAssignment(CNodeFactory.newVariable("x", intType), node);

	CToCRules.processCToken(assignment,
		new CToCData(FunctionTypeBuilder.newSimple().returningVoid().build(), false));

	String code = assignment.getCode();

	Assert.assertEquals("x = 1.2 > 1", code);
    }
}
