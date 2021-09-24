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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.specs.MatlabIR.MatlabParsingUtils;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerRule;
import org.specs.MatlabProcessor.Tokenizer.TokenizerUtils;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.treenode.TreeNodeUtils;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class OperatorRules {

    public static final Set<Character> elementOperators = new HashSet<>();

    static {
        OperatorRules.elementOperators.add('*');
        OperatorRules.elementOperators.add('/');
        OperatorRules.elementOperators.add('\\');
        OperatorRules.elementOperators.add('^');
    }

    /**
     * Matlab ~.
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule tildeRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // '~='
                if (state.getCurrentLine().startsWith("~=")) {
                    RulesUtils.applyOperatorRule("~=", state);
                    return;
                }

                // Assume '~'
                RulesUtils.applyOperatorRule("~", state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("~");
            }
        };

        return rule;
    }

    /**
     * Octave !.
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule octaveExclamationRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                state.setCurrentLine(new StringSlice("~" + state.getCurrentLine().substring(1)));

                tildeRule().apply(state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("!");
            }
        };

        return rule;
    }

    /**
     * Matlab ^.
     * 
     * <p>
     * Produces a caret token.
     * 
     * @return
     */
    public static TokenizerRule caretRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // Assume '^'
                RulesUtils.applyOperatorRule("^", state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("^");
            }
        };

        return rule;
    }

    /**
     * Matlab <, >, <=, >= .
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule relationalRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // '<='
                if (state.getCurrentLine().startsWith("<=")) {
                    RulesUtils.applyOperatorRule("<=", state);
                    return;
                }

                // '>='
                if (state.getCurrentLine().startsWith(">=")) {
                    RulesUtils.applyOperatorRule(">=", state);
                    return;
                }

                // '>'
                if (state.getCurrentLine().startsWith(">")) {
                    RulesUtils.applyOperatorRule(">", state);
                    return;
                }

                // '<'
                if (state.getCurrentLine().startsWith("<")) {
                    RulesUtils.applyOperatorRule("<", state);
                    return;
                }

                throw new TreeTransformException();
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("<")
                        || state.getCurrentLine().startsWith(">");
            }
        };

        return rule;
    }

    /**
     * Matlab :.
     * 
     * <p>
     * Produces an operator token for the colon.
     * 
     * @return
     */
    public static TokenizerRule colonRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // Check if previous token that is not a space is an illegal
                // operand
                // MatlabNode lastToken = MatlabTokenUtils.getLastToken(
                // state.getCurrentTokens(), MType.Space);
                Optional<MatlabNode> lastToken = TreeNodeUtils.lastNodeExcept(state.getCurrentTokens(),
                        Arrays.asList(SpaceNode.class));

                boolean isInvalidOperand = false;
                // if (lastToken == null) {
                if (!lastToken.isPresent()) {
                    isInvalidOperand = true;
                } else {
                    isInvalidOperand = !MatlabOperator.isValidLeftOperand(lastToken.get(), state.isInsideIfCondition());
                }

                // Assume 'ColonNotation'
                if (isInvalidOperand) {
                    MatlabNode token = MatlabNodeFactory.newColonNotation();
                    // MatlabNode token = new GenericMNode(MType.ColonNotation, null, ":");
                    state.pushProducedToken(token, ":");

                    // RulesUtils.applyLiteralTokenRule(state,
                    // MType.ColonNotation, ":");
                } else {
                    // Assume ':' operator
                    RulesUtils.applyOperatorRule(":", state);
                }

            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(":");
            }
        };

        return rule;
    }

    /**
     * Matlab & or &&.
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule andRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // Check if it is single or double &
                String advance = "";
                if (state.getCurrentLine().startsWith("&&")) {
                    advance += "&";
                }
                advance += "&";

                RulesUtils.applyOperatorRule(advance, state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("&");
            }
        };

        return rule;
    }

    /**
     * Matlab | or ||.
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule orRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // Check if it is single or double &
                // boolean isDouble = false;
                // TokenType type = TokenType.orSingle;
                String advance = "";
                if (state.getCurrentLine().startsWith("||")) {
                    advance += "|";
                }
                advance += "|";

                RulesUtils.applyOperatorRule(advance, state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("|");
            }
        };

        return rule;
    }

    /**
     * Matlab + or -.
     * 
     * <p>
     * Can produces several types of tokens. Distinguishes between addition/subtaction and unitary plus/minus.
     * 
     * @return
     */
    public static TokenizerRule signalRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                StringSlice signal = state.getCurrentLine().substring(0, 1);

                // Check if inside square brackets
                boolean parseAsExpression = TokenizerUtils
                        .isSignalParsable(state);

                if (parseAsExpression) {
                    boolean previousTokenIsSpace = state.getLastNode() instanceof SpaceNode;
                    boolean previousTokenIsSeparator = state.getLastNode() instanceof SubscriptSeparatorNode;

                    // Check if there is a number of an identifier next
                    MatlabNumber number = MatlabNumber.getMatlabNumber(state
                            .getCurrentLine().substring(1)).number;

                    boolean nextIsNumber = number != null;

                    StringSlice id = MatlabParsingUtils.getPrefixWord(state
                            .getCurrentLine().substring(1));
                    boolean nextIsId = id != null;

                    // Check if after the signal there is a space
                    boolean nextIsSpace = state.getCurrentLine().substring(1)
                            .startsWith(" ");

                    // If a number or id next to signal inside square brackets,
                    // and there is a space or separator before
                    boolean nextToValidOperand = (nextIsNumber || nextIsId) && !nextIsSpace;
                    boolean separatorBefore = previousTokenIsSeparator
                            || previousTokenIsSpace;

                    // if (nextIsNumber || nextIsId) {
                    if (nextToValidOperand && separatorBefore) {

                        // If previous token is a space,
                        // replace with subscript separator
                        /*
                        if (previousTokenIsSpace) {
                            int index = state.getCurrentTokens().size() - 1;
                            state.getCurrentTokens().set(
                        	    index,
                        	    new MatlabToken(
                        TokenType.SubscriptSeparator, null,
                        null));
                        }
                        */

                        // Parse signal as unitary operator
                        RulesUtils.applyUnitarySignalRule(signal.toString(), state);
                        return;
                    }
                }
                // If previous token that is not a space is an illegal
                // operand, parse signal as unitary operator. Otherwise,
                // parse as addition/subtraction

                // MatlabNode lastToken = MatlabTokenUtils.getLastToken(
                // state.getCurrentTokens(), MType.Space);
                Optional<MatlabNode> lastToken = TreeNodeUtils.lastNodeExcept(state.getCurrentTokens(),
                        Arrays.asList(SpaceNode.class));

                // If null, assume unary
                if (!lastToken.isPresent()) {
                    RulesUtils.applyUnitarySignalRule(signal.toString(), state);
                } else if (!MatlabOperator.isValidLeftOperand(lastToken.get(), state.isInsideIfCondition())) {
                    RulesUtils.applyUnitarySignalRule(signal.toString(), state);
                } else {
                    RulesUtils.applyOperatorRule(signal.toString(), state);
                }

            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("+")
                        || state.getCurrentLine().startsWith("-");
            }
        };

        return rule;
    }

    /**
     * Matlab * or /.
     * 
     * <p>
     * Produces a notLogical token.
     * 
     * @return
     */
    public static TokenizerRule mulDivRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state)
                    throws TreeTransformException {

                // Assume '*' or '/' operation
                char op = state.getCurrentLine().charAt(0);
                RulesUtils.applyOperatorRule(Character.toString(op), state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("*")
                        || state.getCurrentLine().startsWith("/");
            }
        };

        return rule;
    }

}
