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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

public class AttributesTest {
    @Test
    public void testEmpty() {
	AttributesNode attributes = MatlabNodeFactory.newAttributes(Arrays.asList());

	Assert.assertEquals("", attributes.getCode());
    }

    @Test
    public void testSingle() {
	List<AttributeNode> attributeList = new ArrayList<>();
	attributeList.add(MatlabNodeFactory.newAttribute("Abstract", MatlabNodeFactory.newIdentifier("true")));
	AttributesNode attributes = MatlabNodeFactory.newAttributes(attributeList);

	Assert.assertEquals("(Abstract = true)", attributes.getCode());
    }

    @Test
    public void testMultiple() {
	List<AttributeNode> attributeList = new ArrayList<>();
	attributeList.add(MatlabNodeFactory.newAttribute("Abstract", MatlabNodeFactory.newIdentifier("true")));
	attributeList.add(MatlabNodeFactory.newAttribute("Hidden", MatlabNodeFactory.newIdentifier("false")));
	AttributesNode attributes = MatlabNodeFactory.newAttributes(attributeList);

	Assert.assertEquals("(Abstract = true, Hidden = false)", attributes.getCode());
    }

    @Test
    public void testFlag() {
	List<AttributeNode> attributeList = new ArrayList<>();
	attributeList.add(MatlabNodeFactory.newAttribute("Static"));
	AttributesNode attributes = MatlabNodeFactory.newAttributes(attributeList);

	Assert.assertEquals("(Static)", attributes.getCode());
    }
}
