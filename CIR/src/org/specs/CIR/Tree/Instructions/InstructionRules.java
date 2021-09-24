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

package org.specs.CIR.Tree.Instructions;

import java.util.HashMap;
import java.util.Map;

import org.specs.CIR.CodeGenerator.CodeGeneratorRule;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.ParenthesisNode;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class InstructionRules {

    public static final Map<InstructionType, CodeGeneratorRule> instructionCodeGenerationRules;
    static {
        instructionCodeGenerationRules = new HashMap<>();

        instructionCodeGenerationRules.put(InstructionType.Block, newBlockRule());
        instructionCodeGenerationRules.put(InstructionType.If, newIfRule());
        instructionCodeGenerationRules.put(InstructionType.ElseIf, newElseIfRule());
        instructionCodeGenerationRules.put(InstructionType.Else, newElseRule());
        instructionCodeGenerationRules.put(InstructionType.For, newForRule());
        instructionCodeGenerationRules.put(InstructionType.While, newWhileRule());
        instructionCodeGenerationRules.put(InstructionType.Pragma, token -> token.getChild(0).getCode()); // No ";" at
                                                                                                          // the end

    }

    public static Map<InstructionType, CodeGeneratorRule> getRules() {
        return instructionCodeGenerationRules;
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newIfRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {
                CNode ifWord = InstructionAccess.getIfWord(cToken);
                CNode expression = InstructionAccess.getIfExpression(cToken);

                while (expression instanceof ParenthesisNode) {
                    expression = ((ParenthesisNode) expression).getChild(0);
                }

                // Write reserved word
                StringBuilder builder = new StringBuilder();

                // builder.append(CodeGeneratorUtils.tokenCode(cToken
                // .getChild(0)));
                builder.append(ifWord.getCode());
                builder.append("(");
                // builder.append(CodeGeneratorUtils.tokenCode(cToken
                // .getChild(1)));
                builder.append(expression.getCode());
                builder.append(")");

                return builder.toString();
            }

        };
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newElseIfRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {

                CNode elseWord = InstructionAccess.getElseIfFirstWord(cToken);
                CNode ifWord = InstructionAccess.getElseIfSecondWord(cToken);
                CNode expression = InstructionAccess.getElseIfExpression(cToken);

                while (expression instanceof ParenthesisNode) {
                    expression = ((ParenthesisNode) expression).getChild(0);
                }

                // Write reserved word
                StringBuilder builder = new StringBuilder();

                builder.append("}\n");
                builder.append(elseWord.getCode());
                builder.append(" ");
                builder.append(ifWord.getCode());
                builder.append("(");
                builder.append(expression.getCode());
                builder.append(")");

                return builder.toString();
            }

        };
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newElseRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {

                CNode elseWord = InstructionAccess.getElseWord(cToken);

                // Write reserved word
                StringBuilder builder = new StringBuilder();

                builder.append("}\n");
                builder.append(elseWord.getCode());

                return builder.toString();
            }

        };
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newBlockRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {

                CNode block = InstructionAccess.getBlock(cToken);

                // Instruction block does not need ';'
                return block.getCode();
            }

        };
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newForRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {

                CNode forWord = InstructionAccess.getForWord(cToken);
                CNode forInit = InstructionAccess.getForInit(cToken);
                CNode forStop = InstructionAccess.getForStop(cToken);
                CNode forIncrement = InstructionAccess.getForChange(cToken);

                if (cToken.getNumChildren() != 4) {
                    SpecsLogs.warn("For statement with '" + cToken.getNumChildren()
                            + "' childs instead of 4:\n" + cToken);
                }
                // Write reserved word
                StringBuilder builder = new StringBuilder();

                builder.append(forWord.getCode());
                builder.append("(");
                builder.append(forInit.getCode());
                builder.append("; ");
                builder.append(forStop.getCode());
                builder.append("; ");
                builder.append(forIncrement.getCode());
                builder.append(")");

                return builder.toString();
            }

        };
    }

    /**
     * @return
     */
    private static CodeGeneratorRule newWhileRule() {
        return new CodeGeneratorRule() {

            @Override
            public String apply(CNode cToken) {

                CNode whileWord = InstructionAccess.getWhileWord(cToken);
                CNode whileExpression = InstructionAccess.getWhileExpression(cToken);

                if (cToken.getNumChildren() != 2) {
                    SpecsLogs.warn("For statement with '" + cToken.getNumChildren()
                            + "' childs instead of 2:\n" + cToken);
                }

                // Write reserved word
                StringBuilder builder = new StringBuilder();

                builder.append(whileWord.getCode());
                builder.append("(");
                builder.append(whileExpression.getCode());
                builder.append(")");

                return builder.toString();
            }

        };
    }
}
