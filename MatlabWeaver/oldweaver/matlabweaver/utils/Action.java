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

package org.specs.matlabweaver.utils;

import java.io.File;
import java.util.Map;

import org.specs.MatlabIR.MatlabToken;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.matlabweaver.utils.Select.JoinPoints;
import org.specs.tom.TomTokenUtils;
import org.specs.tom.strategies.ParentUtils;
import org.specs.tom.strategies.TomActions;
import org.specs.tom.strategies.TomAttributes;
import org.specs.tom.tomtokenutilstom.list.types.TomToken;
import org.specs.tom.tomtokenutilstom.list.types.TomTokens;
import org.suikasoft.SharedLibrary.IoUtils;

public class Action {

    /**
     * Enumeration for the actions available in Matisse
     * 
     * @author Tiago
     */
    public static enum Actions {
	insert,
	def,
    };

    public static enum When {
	before,
	after,
	around
    }

    public static enum AttributesDef {
	TYPE,DISPLAY,DEFAULT;
	public static boolean contains(String attribute) {
	    for (AttributesDef attr : values())
		if (attr.name().equals(attribute))
		    return true;
	    return false;
	}
    }

    /**
     * Insert code "around" the join pointf
     * 
     * @param joinpointReference
     *            reference to a join point
     * @param when
     *            location for insertion: before, after or around
     * @param parameters
     *            contains the file location and possible variable replacement
     *            needed
     */
    public static TomToken insert(TomToken root, TomToken joinpointReference, String when,
	    Map<?, ?> parameters) {

	TomTokens statementList = generateStatementList(parameters);
	TomToken newRoot = TomActions.insert(when, statementList, joinpointReference, root);

	return newRoot;
    }

    private static TomTokens generateStatementList(Map<?, ?> parameters) {
	MatlabToken fileToken = generateMatlabIR(parameters);
	TomTokens insertingTokens = TomTokenUtils.generateTomToken(fileToken).getchildren();

	return insertingTokens;
    }

    /**
     * Generates a MatlabToken of the inserting script, according to the
     * parameters specified
     * 
     * @param parameters
     *            map containing the "code" location and the replacements needed
     * @return a MatlabToken
     */
    private static MatlabToken generateMatlabIR(Map<?, ?> parameters) {
	String codeSource = (String) parameters.get("code");
	File codeFile = new File(codeSource);
	if (!codeFile.exists()) { // TODO - Error messages and proceeds
	    System.err.println("Code file " + codeSource + " not found!");
	    System.exit(-1);
	}
	parameters.remove("code");
	String codeStr = IoUtils.read(codeFile);
	for (Object param : parameters.keySet()){
	    String paramStr = (String) param;
	    String paramValue = parameters.get(param).toString();
	    codeStr = codeStr.replace(paramStr, paramValue);
	}

	String defaultScriptName = MatlabProcessorUtils.getDefaultName(codeFile);
	//System.out.println(codeStr);
	MatlabToken returnValue = MatlabProcessorUtils.fromMFile(codeStr, defaultScriptName);
	// System.out.println(returnValue);
	return returnValue;
    }

    public static TomToken def(TomToken root, TomToken joinpointReference, String attribute,
	    Object value, VarType varType) {
    	String attributeUpper = attribute.toUpperCase();
	if (!AttributesDef.contains(attributeUpper)) {
	    throw new RuntimeException("Def action cannot define attribute " + attribute);
	}
	switch (AttributesDef.valueOf(attributeUpper)) {
	case TYPE:
	    TomToken functionToken = ParentUtils.getAncestorByType(joinpointReference, root,
		    "Function");
	    
	    String funcName = (String) TomAttributes.getAttributes(JoinPoints.FUNCTION,
		    functionToken, root).get("name");
	    String varName = (String) TomAttributes.getAttributes(JoinPoints.VAR,
		    joinpointReference, root).get("name");
	    varType.put(funcName, varName, value.toString());
	    break;
	case DISPLAY:
	    if(!(value instanceof Boolean)) {
		    throw new RuntimeException("Display define can only be done with a boolean, used " + value.getClass());
		}
	    root = TomActions.defDisplay(root,joinpointReference,(Boolean)value);
	    break;
    case DEFAULT:
    	MatlabToken assignMatlabValue = MatlabProcessorUtils.fromMFile(value.toString(), "assignment");
    	TomToken assignTokenValue = TomTokenUtils.generateTomToken(assignMatlabValue);
    	
    	//System.out.println(assignTokenValue);
	    root = TomActions.defDefault(root,joinpointReference,assignTokenValue);
	    break;
	}
	return root;
    }
}
