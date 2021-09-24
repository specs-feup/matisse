/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabIR.MatlabNode.codegen;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.UnusedVariableNode;

public class OutputsTest {
    @Test
    public void testEmpty() {
	OutputsNode outputs = MatlabNodeFactory.newOutputs();

	Assert.assertEquals("[]", outputs.getCode());
    }

    @Test
    public void testSingleArgument() {
	IdentifierNode a = MatlabNodeFactory.newIdentifier("a");
	OutputsNode outputs = MatlabNodeFactory.newOutputs(a);

	Assert.assertEquals("[a]", outputs.getCode());
    }

    @Test
    public void testTwoArguments() {
	IdentifierNode a = MatlabNodeFactory.newIdentifier("a");
	IdentifierNode b = MatlabNodeFactory.newIdentifier("b");
	OutputsNode outputs = MatlabNodeFactory.newOutputs(a, b);

	Assert.assertEquals("[a, b]", outputs.getCode());
    }

    @Test
    public void testUnused() {
	UnusedVariableNode unused = MatlabNodeFactory.newUnusedVariable();
	OutputsNode outputs = MatlabNodeFactory.newOutputs(unused);

	Assert.assertEquals("[~]", outputs.getCode());
    }
}
