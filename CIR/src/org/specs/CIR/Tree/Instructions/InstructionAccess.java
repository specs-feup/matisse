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

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.ReservedWordNode;
import org.specs.CIR.Tree.CNodes.ReturnNode;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class InstructionAccess {

    /**
     * @param ifInstruction
     * @return
     */
    public static ReservedWordNode getIfWord(CNode ifInstruction) {
	assert ifInstruction instanceof InstructionNode;
	// TokenUtils.checkType(ifInstruction, CNodeType.Instruction);

	// If(0) -> ReservedWord
	return ifInstruction.getChild(ReservedWordNode.class, 0);
	// return TokenUtils.getChild(ifInstruction, new Idx(0, CNodeType.ReservedWord));
    }

    /**
     * @param cToken
     * @return
     */
    public static CNode getIfExpression(CNode ifInstruction) {
	assert ifInstruction instanceof InstructionNode;
	// TokenUtils.checkType(ifInstruction, CNodeType.Instruction);

	// If(1) -> Expression
	// return TokenUtils.getChild(ifInstruction, new Idx(1, CTokenType.Expression));
	// return TokenUtils.getChild(ifInstruction, 1);
	return ifInstruction.getChild(1);
    }

    /**
     * @param elseIfInstruction
     * @return
     */
    public static ReservedWordNode getElseIfFirstWord(CNode elseIfInstruction) {
	// TokenUtils.checkType(elseIfInstruction, CNodeType.Instruction);
	assert elseIfInstruction instanceof InstructionNode;

	// ElseIf(0) -> Else
	return elseIfInstruction.getChild(ReservedWordNode.class, 0);
	// return TokenUtils.getChild(elseIfInstruction, new Idx(0, CNodeType.ReservedWord));
    }

    /**
     * @param elseIfInstruction
     * @return
     */
    public static ReservedWordNode getElseIfSecondWord(CNode elseIfInstruction) {
	// TokenUtils.checkType(elseIfInstruction, CNodeType.Instruction);
	assert elseIfInstruction instanceof InstructionNode;

	// ElseIf(1) -> If
	return elseIfInstruction.getChild(ReservedWordNode.class, 1);
	// return TokenUtils.getChild(elseIfInstruction, new Idx(1, CNodeType.ReservedWord));
    }

    /**
     * @param elseIfInstruction
     * @return
     */
    public static CNode getElseIfExpression(CNode elseIfInstruction) {
	// TokenUtils.checkType(elseIfInstruction, CNodeType.Instruction);
	assert elseIfInstruction instanceof InstructionNode;

	// ElseIf(2) -> expression
	// return TokenUtils.getChild(elseIfInstruction, new Idx(2, CTokenType.Expression));
	// return TokenUtils.getChild(elseIfInstruction, 2);
	return elseIfInstruction.getChild(2);
    }

    /**
     * @param elseInstruction
     * @return
     */
    public static ReservedWordNode getElseWord(CNode elseInstruction) {
	// TokenUtils.checkType(elseInstruction, CNodeType.Instruction);
	assert elseInstruction instanceof InstructionNode;

	// Else(0) -> Else
	return elseInstruction.getChild(ReservedWordNode.class, 0);
	// return TokenUtils.getChild(elseInstruction, new Idx(0, CNodeType.ReservedWord));
    }

    /**
     * @param blockInstruction
     * @return
     */
    public static BlockNode getBlock(CNode blockInstruction) {
	// TokenUtils.checkType(blockInstruction, CNodeType.Instruction);
	assert blockInstruction instanceof InstructionNode;

	if (blockInstruction.getNumChildren() != 1) {
	    SpecsLogs.warn("Instruction of type Block with more than one child.");
	}

	// Block(0) -> 'block'
	// return TokenUtils.getChild(blockInstruction, new Idx(0, CNodeType.Block));
	return blockInstruction.getChild(BlockNode.class, 0);
    }

    /**
     * Fetches the Return token from the Return instruction.
     * 
     * @param returnInstruction
     * @return
     */
    public static ReturnNode getReturn(CNode returnInstruction) {
	// TokenUtils.checkType(returnInstruction, CNodeType.Instruction);
	assert returnInstruction instanceof InstructionNode;

	// Return(0) -> 'return'
	return returnInstruction.getChild(ReturnNode.class, 0);
	// return TokenUtils.getChild(returnInstruction, new Idx(0, CNodeType.Return));
    }

    /**
     * @param forInstruction
     * @return
     */
    public static ReservedWordNode getForWord(CNode forInstruction) {
	assert forInstruction instanceof InstructionNode;
	// TokenUtils.checkType(forInstruction, CNodeType.Instruction);

	// For(0) -> For
	return forInstruction.getChild(ReservedWordNode.class, 0);
	// return TokenUtils.getChild(forInstruction, new Idx(0, CNodeType.ReservedWord));
    }

    /**
     * @param forInstruction
     * @return
     */
    public static CNode getForInit(CNode forInstruction) {
	// TokenUtils.checkType(forInstruction, CNodeType.Instruction);
	assert forInstruction instanceof InstructionNode;

	// For(1) -> Initialization
	// Can be several kinds of token
	return forInstruction.getChild(1);
	// return TokenUtils.getChild(forInstruction, 1);
    }

    /**
     * @param forInstruction
     * @return
     */
    public static CNode getForStop(CNode forInstruction) {
	// TokenUtils.checkType(forInstruction, CNodeType.Instruction);
	assert forInstruction instanceof InstructionNode;

	// For(2) -> Stop Condition
	// Can be several kinds of token
	return forInstruction.getChild(2);
	// return TokenUtils.getChild(forInstruction, 2);
    }

    /**
     * @param forInstruction
     * @return
     */
    public static CNode getForChange(CNode forInstruction) {
	// TokenUtils.checkType(forInstruction, CNodeType.Instruction);
	assert forInstruction instanceof InstructionNode;

	// For(3) -> Induction variable change
	// Can be several kinds of token
	// return TokenUtils.getChild(forInstruction, 3);
	return forInstruction.getChild(3);
    }

    public static CNode getAssignment(CNode assignmentInstruction) {
	// TokenUtils.checkType(assignmentInstruction, CNodeType.Instruction);
	assert assignmentInstruction instanceof InstructionNode;

	// Assignment (0) -> Assignment token
	// return TokenUtils.getChild(assignmentInstruction, 0);
	return assignmentInstruction.getChild(0);
    }

    /**
     * @param cToken
     * @return
     */
    public static CNode getWhileWord(CNode whileInstruction) {
	// TokenUtils.checkType(whileInstruction, CNodeType.Instruction);
	assert whileInstruction instanceof InstructionNode;

	// While(0) -> While
	// return TokenUtils.getChild(whileInstruction, new Idx(0, CNodeType.ReservedWord));
	return whileInstruction.getChild(ReservedWordNode.class, 0);
    }

    /**
     * @param cToken
     * @return
     */
    public static CNode getWhileExpression(CNode whileInstruction) {
	// TokenUtils.checkType(whileInstruction, CNodeType.Instruction);
	assert whileInstruction instanceof InstructionNode;

	// While(1) -> token representing the expression
	// return TokenUtils.getChild(whileInstruction, 1);
	return whileInstruction.getChild(1);
    }

}
