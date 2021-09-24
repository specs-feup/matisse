/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabPreprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.LoopSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.treenode.IteratorUtils;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;
import pt.up.fe.specs.util.treenode.TokenTester;

/**
 * @author Joao Bispo
 * 
 */
public class LoopRankTest {

    @Test
    public void test() {

	// Parse file
	FileNode token = new MatlabParser().parse(TestResources.LoopRank);

	// Loops
	TokenTester loopTest = newLoopTest();

	final Iterator<MatlabNode> depthIterator = IteratorUtils.getDepthIterator(token, loopTest);

	List<String> ranks = SpecsFactory.newArrayList();
	for (MatlabNode loopToken : SpecsCollections.iterable(depthIterator)) {
	    List<Integer> rank = NodeInsertUtils.getRank(loopToken, loopTest);
	    ranks.add(rank.toString());
	    // System.out.println("RANK:" + rank);
	}

	// Results
	String expectedRanks = "[[1], [2], [2, 1], [2, 1, 1], [2, 2], [3], [3, 1], [4]]";

	assertEquals(expectedRanks, ranks.toString());
    }

    /**
     * @return
     */
    private static TokenTester newLoopTest() {
	// final Set<MStatementType> loopTypes = EnumSet.of(MStatementType.For, MStatementType.Parfor,
	// MStatementType.While);

	return token -> {
	    // Check if token is a block
	    if (!(token instanceof BlockSt)) {
		return false;
	    }

	    // At this point, given token is a BlockSt
	    BlockSt matlabToken = (BlockSt) token;

	    // Check if block is a loop
	    return matlabToken.getHeaderNode() instanceof LoopSt;
	};

    }

}
