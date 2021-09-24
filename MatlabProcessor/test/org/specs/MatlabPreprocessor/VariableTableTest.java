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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabProcessor.Utils.VariableTable;

/**
 * @author Joao Bispo
 *
 */
public class VariableTableTest {

    @Test
    public void functionTest() {
	String filename = "subfunction_test.m";
	// String mContents = IoUtils.getResource("mFiles/" + filename);
	// String matlabFilename =
	// "C:\\Users\\Joao Bispo\\Dropbox\\MatlabCompiler\\MatlabToCTests\\MatlabLanguage\\src\\subfunction_test.m";

	FileNode token = new MatlabParser().parse(() -> "mFiles/subfunction_test.m");

	VariableTable vT = new VariableTable();
	vT.addToken(filename, token);

	assertTrue(vT.containsVariable(filename, "subfunction_test", "arg1"));
	assertTrue(vT.containsVariable(filename, "subfunction_test", "out"));

	assertTrue(vT.containsVariable(filename, "sub1", "a"));
	assertTrue(vT.containsVariable(filename, "sub1", "out"));

	assertFalse(vT.containsVariable(filename, "sub1", "b"));
    }

    @Test
    public void scriptTest() {
	String filename = "script.m";
	// String mContents = IoUtils.getResource("mFiles/" + filename);

	FileNode token = new MatlabParser().parse(() -> "mFiles/script.m");

	VariableTable vT = new VariableTable();
	vT.addToken(filename, token);

	assertTrue(vT.containsVariable(filename, null, "i"));
	assertTrue(vT.containsVariable(filename, null, "a"));
	assertFalse(vT.containsVariable(filename, null, "b"));
    }

    @Test
    public void codeTest() {
	String filename = null;
	String mContents = "r=3;";

	FileNode token = new MatlabParser().parse(mContents);
	// System.out.println("TOKEN:"+token);
	VariableTable vT = new VariableTable();
	vT.addToken(filename, token);

	assertTrue(vT.containsVariable(filename, null, "r"));
	assertFalse(vT.containsVariable(filename, null, "y"));
    }

}
