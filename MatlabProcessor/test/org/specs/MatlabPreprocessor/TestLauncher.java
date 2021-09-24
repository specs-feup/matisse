/**
 * Copyright 2012 SPeCS Research Group.
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

import java.io.File;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsSystem;

/**
 * @author Joao Bispo
 * 
 */
public class TestLauncher {

    public static void main(String[] args) {
	SpecsSystem.programStandardInit();

	testParser();
    }

    /**
     * 
     */
    private static void testParser() {

	// Define MATLAB m-file
	// currently in folder ...\Users\...\pro\projects\JavaSe\MatlabPreprocessor
	String matlabFilename = "..\\..\\..\\matlab_codes\\src\\matlab_language\\reserved_words_test.m";

	// File matlabFile = new File(matlabFilename);

	FileNode fileNode = new MatlabParser().parse(new File(matlabFilename));

	Map<String, MatlabUnitNode> functions = fileNode.getUnitsMap();

	/*
	if (functions == null) {
	    return;
	}
	*/

	for (String name : functions.keySet()) {
	    System.out.println("Function '" + name + "'");
	    // System.out.println(functions.get(name));
	}

    }
    /*
    	public static void MatlabTokenUtils() {
    		// getContent
    		System.out.println(MatlabTokenUtils.getContent(new MatlabToken(
    				TokenType.accessCall, null, 1), Integer.class));
    		System.out.println(MatlabTokenUtils.getContent(new MatlabToken(
    				TokenType.accessCall, null, "ASD"), Integer.class));
    	}
    	*/
}
