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
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.BaseClassesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;

public class ClassdefTest {
    @Test
    public void testSimpleClass() {
	AttributesNode attributes = MatlabNodeFactory.newAttributes(Arrays.asList());
	BaseClassesNode baseClasses = MatlabNodeFactory.newBaseClasses(Arrays.asList());
	ClassdefSt classdef = StatementFactory.newClassdef(1, attributes, "x", baseClasses);
	Assert.assertEquals("classdef x\n", classdef.getCode());
    }

    @Test
    public void testClassWithAttribute() {
	List<AttributeNode> attributesList = new ArrayList<>();
	attributesList.add(MatlabNodeFactory.newAttribute("Hidden", MatlabNodeFactory.newIdentifier("true")));
	AttributesNode attributes = MatlabNodeFactory.newAttributes(attributesList);
	BaseClassesNode baseClasses = MatlabNodeFactory.newBaseClasses(Arrays.asList());
	ClassdefSt classdef = StatementFactory.newClassdef(1, attributes, "x", baseClasses);
	Assert.assertEquals("classdef (Hidden = true) x\n", classdef.getCode());
    }

    @Test
    public void testSuperclasses() {
	AttributesNode attributes = MatlabNodeFactory.newAttributes(Arrays.asList());
	List<MatlabNode> baseClassesList = new ArrayList<>();
	baseClassesList.add(MatlabNodeFactory.newIdentifier("y"));
	baseClassesList.add(MatlabNodeFactory.newIdentifier("z"));
	BaseClassesNode baseClasses = MatlabNodeFactory.newBaseClasses(baseClassesList);
	ClassdefSt classdef = StatementFactory.newClassdef(1, attributes, "x", baseClasses);
	Assert.assertEquals("classdef x < y & z\n", classdef.getCode());
    }
}
