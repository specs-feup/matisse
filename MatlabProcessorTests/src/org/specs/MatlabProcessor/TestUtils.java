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

package org.specs.MatlabProcessor;

import java.util.Optional;

import org.junit.Assert;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.providers.ResourceProvider;

public class TestUtils {
    private TestUtils() {
    }

    public static String clean(String input) {
	return input.replace("\r\n", "\n")
		.replace("\r", "\n")
		.trim();
    }

    public static <T extends Enum<T> & ResourceProvider> void testErrors(Class<T> errorResource) {
	for (T resource : errorResource.getEnumConstants()) {
	    try {
		// new MatlabParser().parse(resource, Optional.empty());
		new MatlabParser(Optional.empty()).parse(resource);
		Assert.fail("Expected CodeParsingException in " + resource.name());
	    } catch (CodeParsingException e) {
		System.out.println(e.getMessage());
		// ok
	    } catch (Exception e) {
		throw new RuntimeException(resource.name() + ": Expected CodeParsingException, got "
			+ e.getClass().getSimpleName(), e);
	    }
	}
    }

    public static void compareCode(String resourceName, String expectedCode, MatlabNode actualCode) {
	Assert.assertEquals(resourceName + ": " + clean(expectedCode),
		resourceName + ": " + clean(actualCode.getCode()));
    }

    public static void compareTrees(String resourceName, String expectedTree, MatlabNode actualNode) {
	Assert.assertEquals(resourceName + ": " + clean(expectedTree),
		resourceName + ": " + clean(actualNode.toString()));
    }
}
