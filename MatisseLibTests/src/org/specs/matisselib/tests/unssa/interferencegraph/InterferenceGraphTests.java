/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib.tests.unssa.interferencegraph;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.unssa.InterferenceGraph;

public class InterferenceGraphTests {
    @Test
    public void testInSet() {
        InterferenceGraph g = new InterferenceGraph();
        g.addInterference("v1", "v2");

        Assert.assertTrue(g.hasInterference("v1", "v2"));
        Assert.assertTrue(g.hasInterference("v2", "v1"));
    }

    @Test
    public void testNotInSet() {
        InterferenceGraph g = new InterferenceGraph();
        g.addInterference("v1", "v2");
        g.addInterference("v4", "v3");

        Assert.assertFalse(g.hasInterference("v1", "v3"));
        Assert.assertFalse(g.hasInterference("v2", "v3"));
    }

    @Test
    public void testMerge() {
        InterferenceGraph g = new InterferenceGraph();
        g.addInterference("v1", "v2");
        g.addInterference("v3", "v4");
        g.mergeGroup(Arrays.asList("v1", "v3"), "v1");

        Assert.assertTrue(g.hasInterference("v1", "v2"));
        Assert.assertTrue(g.hasInterference("v1", "v4"));
        Assert.assertTrue(g.hasInterference("v2", "v1"));
        Assert.assertTrue(g.hasInterference("v4", "v1"));
    }

    @Test
    public void testMerge2() {
        InterferenceGraph g = new InterferenceGraph();
        g.addInterference("v1", "v2");
        g.addInterference("v3", "v4");
        g.addInterference("v5", "v6");
        g.mergeGroup(Arrays.asList("v1", "v3"), "v1");
        g.mergeGroup(Arrays.asList("v1", "v3", "v5"), "v1");

        Assert.assertTrue(g.hasInterference("v1", "v6"));
    }
}
