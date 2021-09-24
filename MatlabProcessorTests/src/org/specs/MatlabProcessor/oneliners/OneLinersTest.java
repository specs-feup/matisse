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

package org.specs.MatlabProcessor.oneliners;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class OneLinersTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    public void testOneLiners() {
	for (OneLinersResource resource : OneLinersResource.values()) {
	    testOneLiner(resource.name(), resource.getInput(), resource.getExpected());
	}
    }

    @Test
    public void testErrors() {
	for (OneLinersError resource : OneLinersError.values()) {
	    System.out.println(resource.name());

	    try {
		new MatlabParser().parse(resource.getInput());
		Assert.fail("Expected CodeParsingException");
	    } catch (CodeParsingException e) {
		// ok
	    }
	}
    }

    private static void testOneLiner(String resourceName, String input, String expected) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(input);
	MatlabNode node = testToken.getChild(0);

	TestUtils.compareCode(resourceName, expected, node);
    }

}
