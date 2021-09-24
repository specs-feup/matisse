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

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber.MatlabNumberResult;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerRule;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

/**
 * @author Joao Bispo
 * 
 */
public class NumberRules {

    /**
     * Parses numbers with . (e.g., .323; 32.123). Prefix represents the numbers on the left of the dot.
     * 
     * <p>
     * Can produce several kinds of tokens.
     * 
     * @param state
     */
    /*
    public static void applyDotDecimalNumber(String prefix, TokenizerState state)
        throws ParsingException {
    
    String workline = state.getCurrentLine().substring(prefix.length());
    workline = workline.substring(".".length());
    
    // Get number part
    String numberSuffix = TokenizerUtils.getPrefixNumber(workline);
    
    String number = prefix+"."+numberSuffix;
    
    }
    */

    /**
     * Matlab number.
     * 
     * <p>
     * Can produce several kinds of tokens.
     * 
     * @return
     */
    public static TokenizerRule numberRule() {
	TokenizerRule rule = new TokenizerRule() {

	    @Override
	    public void apply(TokenizerState state) throws TreeTransformException {

		// Get number
		MatlabNumberResult numberResult = MatlabNumber.getMatlabNumber(state.getCurrentLine());

		String stringNumber = numberResult.number.toMatlabString();

		MatlabNode token = MatlabNodeFactory.newNumber(stringNumber);

		// Check if last non-space is not a number, when it is the first token
		// HACK: This is temporary, after UndefinedSt is made temporary, this case can be identified there
		MatlabNode lastNonSpace = state.getLastNonSpaceNode();
		if (lastNonSpace != null
			&& (!state.getParenthesisStack().isInsideSquareBrackets() && !state.getParenthesisStack()
				.isInsideCurlyBrackets())
			&& lastNonSpace instanceof MatlabNumberNode
			&& state.getNumNonSpaces() == 1) {

		    throw new CodeParsingException("Unexpected MATLAB expression.");
		}

		// state.pushProducedToken(token, stringNumber);
		state.pushProducedToken(token, numberResult.numParsedChars);

	    }

	    @Override
	    public boolean isAppliable(TokenizerState state) {
		if (!state.hasLine()) {
		    return false;
		}

		return Character.isDigit(state.getCurrentLine().charAt(0));

	    }
	};

	return rule;
    }
}
