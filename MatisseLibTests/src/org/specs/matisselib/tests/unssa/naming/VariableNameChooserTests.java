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

package org.specs.matisselib.tests.unssa.naming;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableNameChooser;

public class VariableNameChooserTests {
    @Test
    public void testTemporaryWithFixed() {
        VariableAllocation allocation = new VariableAllocation();
        allocation.addVariableGroup(Arrays.asList("$tmp$1", "hello$1"));

        List<String> names = VariableNameChooser.getNames(allocation, Collections.emptySet());
        Assert.assertEquals(Arrays.asList("hello"), names);
    }

    @Test
    public void testMultipleIdentical() {
        VariableAllocation allocation = new VariableAllocation();
        allocation.addVariableGroup(Arrays.asList("hello$1"));
        allocation.addVariableGroup(Arrays.asList("hello$2"));

        List<String> names = VariableNameChooser.getNames(allocation, Collections.emptySet());
        Assert.assertEquals(Arrays.asList("hello", "hello_1"), names);
    }

    @Test
    public void testTemporaryStrength() {
        VariableAllocation allocation = new VariableAllocation();
        allocation.addVariableGroup(Arrays.asList("$one$1", "$is_less$1"));

        List<String> names = VariableNameChooser.getNames(allocation, Collections.emptySet());
        Assert.assertEquals(Arrays.asList("is_less"), names);
    }

    @Test
    public void testGlobalStrength() {
        VariableAllocation allocation = new VariableAllocation();
        allocation.addVariableGroup(Arrays.asList("$a$1", "^b"));

        List<String> names = VariableNameChooser.getNames(allocation, Collections.emptySet());
        Assert.assertEquals(Arrays.asList("b"), names);
    }

    @Test
    public void testGlobalStrength2() {
        VariableAllocation allocation = new VariableAllocation();
        allocation.addVariableGroup(Arrays.asList("$b$1", "$b$2"));
        allocation.addVariableGroup(Arrays.asList("$a$1", "^b"));

        List<String> names = VariableNameChooser.getNames(allocation, Collections.emptySet());
        Assert.assertEquals(Arrays.asList("b_1", "b"), names);
    }
}
