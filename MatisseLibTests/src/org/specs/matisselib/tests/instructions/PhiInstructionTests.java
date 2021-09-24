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

package org.specs.matisselib.tests.instructions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.ssa.instructions.PhiInstruction;

public class PhiInstructionTests {
    @Test
    public void testRenameOutput() {
	PhiInstruction phi = new PhiInstruction("$out", Arrays.asList("$1", "$2"), Arrays.asList(1, 2));

	Map<String, String> newNames = new HashMap<>();
	newNames.put("$out", "$new");
	phi.renameVariables(newNames);

	Assert.assertEquals("$new = phi #1:$1, #2:$2", phi.toString());
    }

    @Test
    public void testRenameEmpty() {
	PhiInstruction phi = new PhiInstruction("$out", Arrays.asList("$1", "$2"), Arrays.asList(1, 2));

	Map<String, String> newNames = new HashMap<>();
	phi.renameVariables(newNames);

	Assert.assertEquals("$out = phi #1:$1, #2:$2", phi.toString());
    }

    @Test
    public void testInput() {
	PhiInstruction phi = new PhiInstruction("$out", Arrays.asList("$1", "$2"), Arrays.asList(1, 2));

	Map<String, String> newNames = new HashMap<>();
	newNames.put("$1", "$x");
	phi.renameVariables(newNames);

	Assert.assertEquals("$out = phi #1:$x, #2:$2", phi.toString());
    }

    @Test
    public void testBreak() {
	PhiInstruction phi = new PhiInstruction("$out", Arrays.asList("$1", "$2"), Arrays.asList(1, 2));

	phi.breakBlock(1, 3, 4);

	Assert.assertEquals("$out = phi #4:$1, #2:$2", phi.toString());
    }

    @Test
    public void testRenameBlocks() {
	PhiInstruction phi = new PhiInstruction("$out", Arrays.asList("$1", "$2"), Arrays.asList(1, 2));

	phi.renameBlocks(Arrays.asList(1, 2), Arrays.asList(3, 4));

	Assert.assertEquals("$out = phi #3:$1, #4:$2", phi.toString());
    }
}
