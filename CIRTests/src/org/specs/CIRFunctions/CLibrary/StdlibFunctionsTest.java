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

package org.specs.CIRFunctions.CLibrary;

import static org.junit.Assert.*;

import org.junit.Test;
import org.specs.CIR.CirTestUtils;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

/**
 * @author Joao Bispo
 *
 */
public class StdlibFunctionsTest {

    @Test
    public void test() {
	InstanceBuilder helper = CirTestUtils.createHelper();

	CNode call = helper.getFunctionCall(helper.getFunctions().getStdlib().free(),
		CNodeFactory.newVariable("anInt", NumericFactory.defaultFactory().newFloat()));

	assertEquals("free(anInt)", call.getCode());
    }

}
