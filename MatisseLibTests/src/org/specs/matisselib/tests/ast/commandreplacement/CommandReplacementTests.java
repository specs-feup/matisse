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

package org.specs.matisselib.tests.ast.commandreplacement;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.passes.ast.CommandReplacementPass;
import org.specs.matisselib.tests.MockPassData;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;

public class CommandReplacementTests {
    @Test
    public void testSimple() {
	test(CommandReplacementResource.SIMPLE);
    }

    private static void test(CommandReplacementResource resource) {
	MatlabParser parser = new MatlabParser();

	String expected = SpecsIo.getResource(resource.getExpectedResource());
	DataStore passData = new MockPassData();
	String obtained = new CommandReplacementPass().apply(parser.parse(resource), passData).getCode();

	Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
