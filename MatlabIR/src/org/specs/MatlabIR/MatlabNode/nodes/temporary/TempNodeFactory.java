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

package org.specs.MatlabIR.MatlabNode.nodes.temporary;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Utility methods for MatlabNodes which are used during processing of the MatlabIR.
 * 
 * @author JoaoBispo
 *
 */
public class TempNodeFactory {

    public static CommentBlockStartSt newCommentBlockStart(int lineNumber) {
	return new CommentBlockStartSt(lineNumber);
    }

    public static SubscriptSeparatorNode newSubscriptSeparator() {
	return new SubscriptSeparatorNode();
    }

    public static MatlabNode newExpression(List<MatlabNode> children) {
	return new ExpressionNode(children);
    }

    /**
     * @return
     */
    public static MatlabNode newExpression(MatlabNode... children) {
	return newExpression(Arrays.asList(children));
    }

    /**
     * Creates a 'Space' token.
     * 
     * @return
     */
    public static SpaceNode newSpace() {
	return new SpaceNode();
    }

    public static FunctionHandleSymbolNode newFunctionHandlerSymbol() {
	return new FunctionHandleSymbolNode();
    }

    public static LambdaInputsNode newLambdaInputs(Collection<MatlabNode> inputs) {
	return new LambdaInputsNode(inputs);
    }

    public static UnknownSymbolNode newUnknownSymbol(String symbol) {
	return new UnknownSymbolNode(symbol);
    }

    public static AssignmentNode newAssignment() {
	return new AssignmentNode();
    }

    public static QuestionMarkSymbol newQuestionMark() {
	return new QuestionMarkSymbol();
    }

    public static CellStartNode newCellStart() {
	return new CellStartNode();
    }

    public static ParenthesisStartNode newParenthesisStart() {
	return new ParenthesisStartNode();
    }

    public static SquareBracketsStartNode newSquareBracketsStart() {
	return new SquareBracketsStartNode();
    }

    public static FieldAccessSeparatorNode newFieldAccessSeparator() {
	return new FieldAccessSeparatorNode();
    }
}
