/**
 * Copyright 2015 SPeCS.
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

package org.specs.matisselib.passes.cir;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;

public class WhileConditionBuilderPass extends InstructionsBodyPass {

	private static final boolean ENABLE_DIAGNOSTICS = false;

	@Override
	public void apply(CInstructionList instructions, ProviderData providerData) {
		log("Starting");
		for (CNode node : instructions.get()) {
			visitInstruction(node, providerData);
		}
	}

	private void visitInstruction(CNode node, ProviderData providerData) {
		assert node instanceof InstructionNode;
		InstructionNode instruction = (InstructionNode) node;
		if (instruction.getInstructionType() == InstructionType.Block) {
			assert instruction.getNumChildren() == 1;
			apply((BlockNode) instruction.getChild(0), providerData);
		}
	}

	private void apply(BlockNode block, ProviderData providerData) {
		for (CNode child : block.getChildren()) {
			visitInstruction(child, providerData);
		}

		InstructionNode firstInstruction = block.getHeader();
		if (firstInstruction.getInstructionType() != InstructionType.While) {
			log("Not a while. Instead: " + firstInstruction.getCode());
			return;
		}

		CNode condition = firstInstruction.getChild(1);
		if (condition instanceof CNumberNode
				&& ((CNumberNode) condition).getCNumber().getNumber().doubleValue() == 1) {

			log("While condition is constant 1");
			condition = null;
		}

		List<CNode> afterElseStatements = new ArrayList<>();

		while (block.getNumChildren() > 1) {
			InstructionNode first = (InstructionNode) block.getChild(1);
			if (first.getInstructionType() != InstructionType.Block) {
				break;
			}

			CNode childBlock = first.getChild(0);
			InstructionNode blockHeader = (InstructionNode) childBlock.getChild(0);
			if (blockHeader.getInstructionType() != InstructionType.If) {
				break;
			}

			boolean foundBreak = false;
			boolean foundElse = false;
			boolean forbidInline = false;

			for (CNode child : childBlock.getChildren()) {
				InstructionNode instructionChild = (InstructionNode) child;
				if (instructionChild.getInstructionType() == InstructionType.If) {
					continue;
				}
				if (instructionChild.getInstructionType() == InstructionType.Else) {
					foundElse = true;
					continue;
				}
				if (instructionChild.getInstructionType() == InstructionType.ElseIf) {
					log("Can't inline: Found elseif");
					forbidInline = true;
					break;
				}
				if (instructionChild.getInstructionType() == InstructionType.Break) {
					if (foundElse) {
						log("Can't inline: break in else");
						forbidInline = true;
						break;
					}
					foundBreak = true;
					continue;
				}
				if (foundElse) {
					afterElseStatements.add(child);
					continue;
				}

				log("Can't inline due to unrecognized child: " + child.getCode());
				forbidInline = true;
				break;
			}

			if (forbidInline) {
				break;
			}
			if (!foundBreak) {
				log("Didn't find any break statements");
				break;
			}

			// Inline
			CNode ifExpression = blockHeader.getChild(1);

			if (!(ifExpression.getVariableType() instanceof CNativeType)) {
				log("Variable type is " + ifExpression.getVariableType());
				break;
			}

			if (ifExpression instanceof FunctionCallNode) {
				FunctionCallNode ifFunctionCall = (FunctionCallNode) ifExpression;
				if (ifFunctionCall.getFunctionInstance().getCName().equals("LogicalNegation")) {

					CNode extractedCondition = ifExpression.getChild(0).getChild(0);

					condition = buildCompositeCondition(condition, extractedCondition, providerData);

					block.removeChild(1);

					continue;

				}
			}

			CNode rightHand = COperator.LogicalNegation
					.getCheckedInstance(providerData.createFromNodes(ifExpression))
					.newFunctionCall(ifExpression);
			condition = buildCompositeCondition(condition, rightHand, providerData);
			block.removeChild(1);
		}

		if (condition != null) {
			log("Got ending condition: " + condition);
			firstInstruction.setChild(1, condition);
		}

		block.getChildren().addAll(1, afterElseStatements);
	}

	private static CNode buildCompositeCondition(CNode condition, CNode extractedCondition, ProviderData providerData) {
		if (condition == null) {
			log("Inlining if break into while condition");
			condition = extractedCondition;
		} else {
			log("Inlining if break into an additional while condition");
			condition = COperator.LogicalAnd
					.getCheckedInstance(providerData.createFromNodes(condition, extractedCondition))
					.newFunctionCall(condition, extractedCondition);
		}
		return condition;
	}

	private static void log(String message) {
		if (WhileConditionBuilderPass.ENABLE_DIAGNOSTICS) {
			System.out.print("[while_condition_builder] ");
			System.out.println(message);
		}
	}
}
