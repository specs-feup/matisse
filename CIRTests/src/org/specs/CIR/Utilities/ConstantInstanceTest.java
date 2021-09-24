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

import java.util.List;

import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;

import pt.up.fe.specs.util.SpecsFactory;

public class ConstantInstanceTest {

    @Test
    public void test() {
	List<VariableType> inputTypes = SpecsFactory.newArrayList();
	inputTypes.add(StdIntFactory.newInt64());
	inputTypes.add(StdIntFactory.newUInt32());

	ScalarType outputType = StdIntFactory.newInt32();
	Number constant = 5;

	FunctionInstance function = ConstantInstance.newInstanceInternal(inputTypes, outputType, constant);
	List<CNode> inputVars = CNodeFactory.newVariableList(new Variable("name1", inputTypes.get(0)), new Variable(
		"name2", inputTypes.get(1)));

	// Check function returns the constant
	assertEquals(function.getCallCode(inputVars), constant.toString());

	// Check the output type of the constant
	assertTrue(function.getFunctionType().getCReturnType().equals(outputType));

    }
}
