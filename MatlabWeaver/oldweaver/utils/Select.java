/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. under the License.
 */

package org.specs.oldweaver.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reflect.larai.Joinpoint;
import org.specs.MatlabIR.MTokenType;
import org.specs.MatlabIR.MatlabToken;
import org.specs.MatlabIR.MatlabTokenContent;
import org.specs.MatlabIR.MatlabTokenUtils;
import org.specs.MatlabIR.Statements.StatementType;
import org.specs.MatlabIR.Statements.StatementUtils;

public class Select {

    /**
     * Enumeration of the possible join points in Matisse (TOM tree)
     * 
     * @author Tiago
     * 
     */
    public static enum JoinPoints {
	file,
	function,
	inputs,
	outputs,
    };

    /**
     * Select all functions inside a MatlabToken
     * 
     * @param parent
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> function(MatlabToken parent) {
	List<MatlabToken> functions = MatlabTokenUtils.getTokens(parent, MTokenType.Function);
	// If erro: TokenUtils.get...
	List<Joinpoint> functionsJPset = new ArrayList<Joinpoint>();
	for (MatlabToken function : functions) {
	    Joinpoint jp = new Joinpoint(function);
	    MatlabToken functionDeclaration = StatementUtils.getFirstToken(
		    StatementType.FunctionDeclaration, function.getChildren());
	    String name = MatlabTokenContent.getFunctionDeclarationName(functionDeclaration);
	    int numArgins = MatlabTokenContent.getFunctionOrScriptNumberOfInputs(function);
	    int numArgouts = MatlabTokenContent.getFunctionDeclarationOutputNames(
		    functionDeclaration).size();
	    jp.addAttribute("name", name);
	    jp.addAttribute("numArgins", numArgins);
	    jp.addAttribute("numArgouts", numArgouts);
	    functionsJPset.add(jp);
	}
	return functionsJPset;
    }

    public static Joinpoint file(MatlabToken root, File sourceFile) {
	Joinpoint jpFile = new Joinpoint(root);
	Map<String, Object> fileAttrs = new HashMap<String, Object>();
	fileAttrs.put("name", sourceFile.getName());
	fileAttrs.put("absolutePath", sourceFile.getAbsolutePath());
	fileAttrs.put("ast", root);
	// sandbox(this.root);
	jpFile.setAttributes(fileAttrs);
	return jpFile;
    }

}
