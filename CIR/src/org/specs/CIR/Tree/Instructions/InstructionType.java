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

import java.util.EnumSet;

/**
 * 
 * @author Joao Bispo
 * 
 */
public enum InstructionType {
    /**
     * Contains a single child, of type assignment. The first child is the left hand, the second child is the right
     * hand.
     */
    Assignment,
    /**
     * Contains a single token of the type 'FunctionCall'.
     */
    FunctionCall,
    /**
     * Contains a single token of the type 'Return'.
     */
    Return,
    /**
     * The child is a block of code.
     */
    Block,
    /**
     * An If statement
     * <p>
     * The first child is the reserved word "if", the second child is an expression representing the if condition.
     */
    If,
    /**
     * An Else-If statement
     * <p>
     * The first and second children are the reserved words "else" and "if", respectively. The third child is an
     * expression representing the if condition.
     */
    ElseIf,
    /**
     * An Else statement
     * <p>
     * The first child is the reserved word "else". Contains no other children.
     */
    Else,
    /**
     * A Break statement
     * <p>
     * The first child is the reserved work "break". Contains no other children.
     */
    Break,
    /**
     * A Continue statement
     * <p>
     * The first child is the reserved work "continue". Contains no other children.
     */
    Continue,
    /**
     * Contains a single child, of type 'Comment' token.
     */
    Comment,
    /**
     * Contains four children:
     * <p>
     * - For reserved word<br>
     * - <br>
     * - <br>
     * -
     */
    For,
    /**
     * Declaration of a 'while' loop.
     */
    While,
    /**
     * For statements that are literal strings to be copied into the code.
     */
    Literal,
    Pragma,
    /**
     * For statements which do not have a concrete type (e.g., Blocks)
     */
    Undefined;

    private static final EnumSet<InstructionType> startBlockInstruction;
    static {
        startBlockInstruction = EnumSet.noneOf(InstructionType.class);

        startBlockInstruction.add(If);
        startBlockInstruction.add(ElseIf);
        startBlockInstruction.add(Else);
        startBlockInstruction.add(For);
        startBlockInstruction.add(While);
    }

    /**
     * @param type
     * @return
     */
    public static boolean isInstructionBlockStart(InstructionType type) {
        return startBlockInstruction.contains(type);
    }

}
