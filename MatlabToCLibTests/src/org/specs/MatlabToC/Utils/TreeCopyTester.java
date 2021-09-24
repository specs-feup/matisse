/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.Utils;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * @author Joao Bispo
 *
 */
public class TreeCopyTester {

    @Test
    @Ignore
    public void test() {
	File bigMfile = new File("D:\\Dropbox\\MatlabCompiler\\Benchmarks\\Suites\\MATCH\\src\\adapted\\adapt.m");

	FileNode bigTree = new MatlabParser().parse(bigMfile);

	// Measure system.nano
	long tic = System.nanoTime();
	long toc = System.nanoTime();

	// 622~1244, resolution is 1us
	System.out.println("OVERHEAD:" + (toc - tic));

	tic = System.nanoTime();
	for (int i = 0; i < 4; i++) {
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	    bigTree.copy();
	}
	toc = System.nanoTime();
	System.out.println("Copy 3:" + SpecsStrings.parseTime(toc - tic));

	MatlabNode child = bigTree.getChildren().get(0);
	tic = System.nanoTime();
	NodeInsertUtils.insertAfter(child, MatlabNodeFactory.newCharArray("HEHEHEHEHE"));
	toc = System.nanoTime();
	System.out.println("Insert:" + (toc - tic));
	// fail("Not yet implemented");
    }

}
