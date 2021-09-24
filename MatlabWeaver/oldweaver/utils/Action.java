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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MTokenType;
import org.specs.MatlabIR.MatlabToken;
import org.specs.MatlabIR.MatlabTokenContent;
import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.Statements.StatementType;
import org.specs.MatlabIR.Statements.StatementUtils;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.suikasoft.SharedLibrary.IoUtils;
import org.suikasoft.SharedLibrary.TreeToken.TokenWithParentUtils;

public class Action {

    /**
     * Enumeration for the actions available in Matisse
     * 
     * @author Tiago
     */
    public static enum Actions {
	insert,
	def,
	specialization,
    };

    public static enum When {
	before,
	after,
	around
    }

    /**
     * Insert code "around" the join point
     * 
     * @param joinpointReference
     *            reference to a join point
     * @param when
     *            location for insertion: before, after or around
     * @param parameters
     *            contains the file location and possible variable replacement
     *            needed
     */
    public static void insert(MatlabToken joinpointReference, String when, Map<?, ?> parameters) {
	MatlabToken commentToken = generateStatementCommentToken(when, parameters);
	insertTokenByTokenType(commentToken, joinpointReference);
    }

    private static void insertTokenByTokenType(MatlabToken insertingToken,
	    MatlabToken referenceToken) {

	switch (referenceToken.getType()) {
	case Function:
	    referenceToken = StatementUtils.getFirstToken(StatementType.FunctionDeclaration,
		    referenceToken.getChildren());
	default:
	    MatlabToken statementParentOfSelect = StatementUtils.getStatement(referenceToken);
	    TokenWithParentUtils.insertBefore(statementParentOfSelect, insertingToken);
	}
    }

    /**
     * Generates a MatlabToken of the inserting script, according to the
     * parameters specified
     * 
     * @param when
     * 
     * @param parameters
     *            map containing the "code" location and the replacements needed
     * @return a MatlabToken
     */
    private static MatlabToken generateStatementCommentToken(String when, Map<?, ?> parameters) {

	String codeSource = (String) parameters.get("code");
	parameters.remove("code");

	/**
	 * insert tag structure "@<tagName> insert + when + file = + code +
	 * (varName + = + varValue)*
	 */
	StringBuilder insertingTag = new StringBuilder("@insertion insert ");
	insertingTag.append(when);
	insertingTag.append(" file=\"");
	insertingTag.append(codeSource);
	insertingTag.append("\"");

	for (Object param : parameters.keySet()) {
	    insertingTag.append(" ");
	    String paramStr = (String) param;
	    insertingTag.append(paramStr.substring(2, paramStr.length() - 2));
	    insertingTag.append("=");
	    insertingTag.append(parameters.get(param));
	}
	MatlabToken comment = new MatlabToken(MTokenType.Comment, null, insertingTag.toString());

	List<MatlabToken> childToken = new ArrayList<MatlabToken>();
	childToken.add(comment);
	StatementData sd = new StatementData(0, false, StatementType.Comment);

	MatlabToken commentStatement = new MatlabToken(MTokenType.Statement, childToken, sd);
	return commentStatement;
    }
}
