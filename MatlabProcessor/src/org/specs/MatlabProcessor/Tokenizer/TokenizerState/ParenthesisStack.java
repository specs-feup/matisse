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

package org.specs.MatlabProcessor.Tokenizer.TokenizerState;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabIR.Processor.TreeTransformException;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Keeps track of the currently opened parenthesis (includes cells and squareBrackets).
 * 
 * @author Joao Bispo
 * 
 */
public class ParenthesisStack {

    private List<Class<? extends MatlabNode>> parenthesisCalls;

    /**
     * 
     */
    public ParenthesisStack() {
	clear();
    }

    public void clear() {
	parenthesisCalls = new ArrayList<>();
    }

    /**
     * @return the insideParen
     */
    public List<Class<? extends MatlabNode>> getParenthesisCalls() {
	return parenthesisCalls;
    }

    public void addParenthesisCall(Class<? extends MatlabNode> parenthesisType) {
	parenthesisCalls.add(parenthesisType);
    }

    public Class<? extends MatlabNode> getCurrentParenthesis() {
	if (parenthesisCalls.size() == 0) {
	    return null;
	}

	int lastIndex = parenthesisCalls.size() - 1;
	return parenthesisCalls.get(lastIndex);
    }

    /**
     * Removes the given type from the last position of the parenthesis call stack. If the last token is not equal to
     * the given type, a ParsingException is thrown.
     * 
     * @param parenthesisType
     * @param paren
     */
    public void removeParenthesisCall(Class<? extends MatlabNode> parenthesisType, String paren)
	    throws TreeTransformException {

	int lastIndex = parenthesisCalls.size() - 1;
	if (lastIndex < 0) {
	    SpecsLogs.msgInfo("Parethesis call stack is empty.");
	    throw new TreeTransformException();
	}

	Class<? extends MatlabNode> tokenType = parenthesisCalls.get(lastIndex);
	if (!tokenType.equals(parenthesisType)) {
	    throw new CodeParsingException("Unbalanced parenthesis '" + paren + "'");
	    // LoggingUtils.msgInfo("Unbalanced parenthesis for type '"
	    // + parenthesisType + "'");
	    // throw new TreeTransformException();
	}

	parenthesisCalls.remove(lastIndex);
    }

    public boolean hasParanthesis() {
	return !parenthesisCalls.isEmpty();
    }

    public boolean isInsideSquareBrackets() {
	Class<? extends MatlabNode> currentParenthesis = getCurrentParenthesis();

	return SquareBracketsStartNode.class.equals(currentParenthesis);
    }

    public boolean isInsideCurlyBrackets() {
	Class<? extends MatlabNode> currentParenthesis = getCurrentParenthesis();

	return CellStartNode.class.equals(currentParenthesis);
    }

    /**
     * 
     * @return true if we are currently in a state where 'space' has meaning
     */
    public boolean isSpaceSensible() {
	return isInsideSquareBrackets() || isInsideCurlyBrackets();
    }
}
