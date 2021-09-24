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

package org.specs.CIR.Tree.Utils;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Class used to build C tokens that represent if blocks.
 * 
 * 
 * @author Pedro Pinto
 * 
 */
public class IfNodes {

    /**
     * Convenience method with variadic inputs.
     * 
     * @param condition
     * @param thenInstructions
     * @return
     */
    public static CNode newIfThen(CNode condition, CNode... thenInstructions) {
	return newIfThen(condition, Arrays.asList(thenInstructions));
    }

    /**
     * Builds a new IF THEN block.
     * 
     * @param condition
     *            - the condition to be tested, does not need to be an instruction token
     * @param thenInstructions
     *            - the instructions inside the body, do not need to be an instruction tokens
     * @return a {@link CNode} with the block
     */
    public static CNode newIfThen(CNode condition, List<CNode> thenInstructions) {

	// The list if the new instructions
	List<CNode> newInstructions = SpecsFactory.newArrayList();

	// The IF keyword
	CNode ifWord = CNodeFactory.newReservedWord(ReservedWord.If);

	// The IF instruction ( keyword and condition )
	// CToken ifInstruction = CTokenFactory.newInstruction(InstructionType.If, ifWord,
	// CTokenFactory.newExpression(condition));
	CNode ifInstruction = CNodeFactory.newInstruction(InstructionType.If, ifWord, condition);

	// Add the if instruction to the start
	newInstructions.add(ifInstruction);

	// Process each instruction
	for (CNode cToken : thenInstructions) {
	    CNode instruction = CInstructionList.newInstruction(cToken);
	    newInstructions.add(instruction);
	}

	// Add the if instruction to the start
	// newInstructions.add(0, ifInstruction);

	// The block
	CNode ifThenBlock = CNodeFactory.newBlock(newInstructions);

	return ifThenBlock;
    }

    public static CNode newIfThenElse(CNode condition, CNode thenInstructions, CNode elseInstructions) {

	return newIfThenElse(condition, Arrays.asList(thenInstructions), Arrays.asList(elseInstructions));
    }

    /**
     * Builds a new IF THEN ELSE block.
     * 
     * @param condition
     *            - the condition to be tested, does not need to be an instruction token
     * @param thenInstructions
     *            - the instructions inside the THEN block, do not need to be instruction tokens
     * @param elseInstructions
     *            - the instructions inside the THEN block, do not need to be instruction tokens
     * 
     * @return a {@link CNode} with the block
     */
    public static CNode newIfThenElse(CNode condition, List<CNode> thenInstructions, List<CNode> elseInstructions) {

	// The list if the new instructions
	List<CNode> newInstructions = SpecsFactory.newArrayList();

	// The IF keyword
	CNode ifWord = CNodeFactory.newReservedWord(ReservedWord.If);

	// The IF instruction ( keyword and condition )
	// CToken ifInstruction = CTokenFactory.newInstruction(InstructionType.If, ifWord,
	// CTokenFactory.newExpression(condition));
	CNode ifInstruction = CNodeFactory.newInstruction(InstructionType.If, ifWord, condition);

	// Add the IF instruction to the start
	newInstructions.add(ifInstruction);

	// Process each THEN instruction
	for (CNode thenI : thenInstructions) {
	    CNode instruction = CInstructionList.newInstruction(thenI);
	    newInstructions.add(instruction);
	}

	// The ELSE keyword
	CNode elseWord = CNodeFactory.newReservedWord(ReservedWord.Else);

	// The ELSE instruction
	CNode elseInstruction = CNodeFactory.newInstruction(InstructionType.Else, elseWord);

	// Add the ELSE instruction
	newInstructions.add(elseInstruction);

	// Process each ELSE instruction
	for (CNode elseI : elseInstructions) {
	    CNode instruction = CInstructionList.newInstruction(elseI);
	    newInstructions.add(instruction);
	}

	// The block
	CNode ifThenBlock = CNodeFactory.newBlock(newInstructions);

	return ifThenBlock;
    }
}
