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

package org.specs.MatlabProcessor.Tokenizer.Rules;

import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class RulesUtils {

    /**
     * Produces a token of the given type which has a literal representation.
     * 
     * @param state
     */
    /*
    public static void applyLiteralTokenRule(TokenizerState state, MatlabNodeType type,
        String literal) throws TreeTransformException {
    
    MatlabNode token = new GenericMNode(type, null, literal);
    state.pushProducedToken(token, literal);
    }
    */

    /**
     * Produces a fieldAccess token.
     * 
     * @param state
     */
    public static void applyFieldAccess(TokenizerState state) throws TreeTransformException {
	// state.advanceAndTrim(".");

	MatlabNode token = TempNodeFactory.newFieldAccessSeparator();
	state.pushProducedToken(token, ".");
    }

    /**
     * Produces a dynamicFieldAccess token.
     * 
     * @param state
     * @throws TreeTransformException
     */
    public static void applyDynamicFieldAccess(TokenizerState state)
	    throws TreeTransformException {
	// state.advanceAndTrim(".");

	MatlabNode token = MatlabNodeFactory.newDynamicFieldAccessSeparator();
	state.pushProducedToken(token, ".");
    }

    /**
     * Produces an ElementOperator token.
     * 
     * @param state
     * @throws TreeTransformException
     */
    public static void applyElementOperator(TokenizerState state)
	    throws TreeTransformException {

	StringSlice workline = state.getCurrentLine();
	String advance = ".";

	char operation = workline.substring(advance.length()).charAt(0);
	advance += operation;
	RulesUtils.applyOperatorRule(advance, state);
    }

    /**
     * Produces an Operator token.
     * 
     * @param operatorString
     * @param state
     * @throws TreeTransformException
     */
    public static void applyOperatorRule(String operatorString, TokenizerState state)
	    throws TreeTransformException {
	MatlabNode token = MatlabNodeFactory.newOperator(operatorString);
	state.pushProducedToken(token, operatorString);
    }

    /**
     * Transforms a '+' or a '-' into an unary plus/minus.
     * 
     * @param signalString
     * @param state
     * @throws TreeTransformException
     */
    public static void applyUnitarySignalRule(String signalString, TokenizerState state)
	    throws TreeTransformException {

	MatlabOperator unitaryOperator = null;

	if (signalString.equals("+")) {
	    unitaryOperator = MatlabOperator.UnaryPlus;
	} else if (signalString.equals("-")) {
	    unitaryOperator = MatlabOperator.UnaryMinus;
	} else {
	    SpecsLogs.warn("Case not defined.");
	    return;
	}

	// Check if should insert a subscript separator
	boolean insertSeparator = testInsertSeparator(state.getCurrentTokens(), state.getCurrentTokens().size() - 1);
	if (insertSeparator) {
	    state.pushProducedToken(TempNodeFactory.newSubscriptSeparator(), "");
	}

	MatlabNode token = MatlabNodeFactory.newOperator(unitaryOperator);

	state.pushProducedToken(token, signalString);
    }

    /**
     * Starts from last, go backwards until Row or decision.
     * 
     * @param currentTokens
     * @return
     */
    private static boolean testInsertSeparator(List<MatlabNode> nodes, int currentIndex) {
	if (currentIndex < 0) {
	    return false;
	}

	MatlabNode currentNode = nodes.get(currentIndex);

	// If found a number, shoud insert separator
	if (currentNode instanceof MatlabNumberNode) {
	    return true;
	}

	// All nodes that are not a space node, consider that subscript should not be inserted
	if (!(currentNode instanceof SpaceNode)) {
	    return false;
	}

	// Ignore spaces
	assert currentNode instanceof SpaceNode;
	// int previousIndex = currentIndex - 1;
	// if (currentNode instanceof SpaceNode) {
	return testInsertSeparator(nodes, currentIndex - 1);
	// }

	// throw new ParserErrorException("Case not defined:" + currentNode.getNodeName());
    }

    /**
     * @param state
     * @throws TreeTransformException
     */
    public static void finishStatement(TokenizerState state) throws TreeTransformException {
	// Apply comma and semicolon rules
	if (GeneralRules.commaRule().isAppliable(state)) {
	    GeneralRules.commaRule().apply(state);
	} else if (GeneralRules.semicolonRule().isAppliable(state)) {
	    GeneralRules.semicolonRule().apply(state);
	} else {
	    // If no comma nor semicolon, display results
	    state.finishStatement(true);
	}
    }
}
