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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.ClassWord;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Utility methods for creating MatlabNodes.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabNodeFactory {

    // public static FileNode newFile(Collection<? extends MatlabUnitNode> nodes, String filename, String wholeFile) {
    // return newFile(nodes, filename, StringProvider.newInstance(wholeFile));
    // }

    /*
    public static FileNode newFile(Collection<? extends MatlabUnitNode> nodes, String wholeFile) {
    return FileNode.newInstance(nodes, FileNode.getNoFilename(), wholeFile);
    }
    */

    public static CompositeAccessCallNode newCompositeAccessCall(MatlabNode node, List<MatlabNode> children) {
        return new CompositeAccessCallNode(node, children);
    }

    public static SimpleAccessCallNode newSimpleAccessCall(String functionName, MatlabNode... children) {

        return newSimpleAccessCall(functionName, Arrays.asList(children));
    }

    public static AccessCallNode newAccessCall(MatlabNode node, List<MatlabNode> children) {
        if (node instanceof IdentifierNode) {
            return new SimpleAccessCallNode(node, children);
        }

        return new CompositeAccessCallNode(node, children);
    }

    public static SimpleAccessCallNode newSimpleAccessCall(String identifierName, List<? extends MatlabNode> children) {
        return new SimpleAccessCallNode(identifierName, children);
    }

    public static FunctionInputsNode newFunctionInputs(MatlabNode... inputs) {
        return newFunctionInputs(Arrays.asList(inputs));
    }

    public static FunctionInputsNode newFunctionInputs(Collection<MatlabNode> inputs) {
        return new FunctionInputsNode(inputs);
    }

    public static CellAccessNode newCellAccess(MatlabNode left, List<MatlabNode> arguments) {
        return new CellAccessNode(left, arguments);
    }

    /**
     * Helper method which receives a string representation of the operator (e.g., "+" for Addition).
     * 
     * @param operator
     * @param operands
     * @return
     */
    public static OperatorNode newOperator(String operator, MatlabNode... operands) {
        return newOperator(MatlabOperator.getOp(operator), Arrays.asList(operands));
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param operator
     * @param operands
     * @return
     */
    public static OperatorNode newOperator(MatlabOperator operator, MatlabNode... operands) {
        return newOperator(operator, Arrays.asList(operands));
    }

    public static OperatorNode newOperator(MatlabOperator operator, List<MatlabNode> operands) {
        return new OperatorNode(operator, operands);
    }

    /**
     * Creates a token of type command.
     * 
     * <p>
     * Expects at least one argument.
     * 
     * @param command
     * @param arguments
     * @return
     */
    public static CommandNode newCommand(String command, List<String> arguments) {
        return new CommandNode(command, arguments);
    }

    public static MatlabCharArrayNode newCharArray(String literal) {
        return new MatlabCharArrayNode(literal);
    }

    public static MatlabStringNode newString(String literal) {
        return new MatlabStringNode(literal);
    }

    public static ReservedWordNode newReservedWord(ReservedWord reservedWord) {
        return new ReservedWordNode(reservedWord);
    }

    public static ClassWordNode newClassWord(ClassWord classWord) {
        return new ClassWordNode(classWord);
    }

    public static AttributesNode newAttributes(List<AttributeNode> attributes) {
        List<MatlabNode> children = new ArrayList<>();
        children.addAll(attributes);
        return new AttributesNode(null, children);
    }

    public static AttributeNode newAttribute(String name, MatlabNode expression) {
        return newAttribute(newIdentifier(name), expression);
    }

    public static AttributeNode newAttribute(MatlabNode name, MatlabNode expression) {
        List<MatlabNode> children = new ArrayList<>();
        children.add(name);
        children.add(expression);
        return new AttributeNode(null, children);
    }

    public static AttributeNode newAttribute(MatlabNode name) {
        List<MatlabNode> children = new ArrayList<>();
        children.add(name);
        return new AttributeNode(null, children);
    }

    public static AttributeNode newAttribute(String name) {
        return newAttribute(newIdentifier(name));
    }

    public static BaseClassesNode newBaseClasses(List<MatlabNode> children) {
        return new BaseClassesNode(null, children);
    }

    /**
     * Creates an output vector.
     * 
     * @param tokens
     * @return
     */
    public static OutputsNode newOutputs(MatlabNode... tokens) {
        return newOutputs(Arrays.asList(tokens));
    }

    public static OutputsNode newOutputs(List<MatlabNode> tokens) {
        return new OutputsNode(tokens);
    }

    public static RowNode newRow(MatlabNode... tokens) {
        return newRow(Arrays.asList(tokens));
    }

    public static RowNode newRow(List<MatlabNode> tokens) {
        // tokens = new ArrayList<>(tokens);
        return new RowNode(tokens);
    }

    /**
     * @return
     */
    public static ParenthesisNode newParenthesis(MatlabNode... tokens) {
        /*
        List<MatlabNode> children = new ArrayList<>();
        for (MatlabNode token : tokens) {
        children.add(token);
        }
        
        return newParenthesis(children);
        */
        return newParenthesis(Arrays.asList(tokens));
    }

    public static ParenthesisNode newParenthesis(List<MatlabNode> tokens) {
        return new ParenthesisNode(tokens);
    }

    /**
     * Creates an 'Identifier' token with the given name.
     * 
     * @param identifierName
     * @return
     */
    public static IdentifierNode newIdentifier(String identifierName) {
        return new IdentifierNode(identifierName);
    }

    /**
     * Creates an 'InferiorClass' token with the given name.
     * 
     * @param identifierName
     * @return
     */
    public static MetaClassNode newMetaClass(String identifierName) {
        return new MetaClassNode(identifierName);
    }

    /**
     * Creates an 'UnusedVariable' token.
     * 
     * @return An unused variable(~) token.
     */
    public static UnusedVariableNode newUnusedVariable() {
        return new UnusedVariableNode();
    }

    /**
     * Creates a 'Number' token with the given string value.
     * 
     * @param number
     * @return
     */
    public static MatlabNumberNode newNumber(String number) {
        return new MatlabNumberNode(number);
    }

    /**
     * Creates a new 'Number' token with the given integer value.
     * 
     * @param number
     *            The integer value of the number to create
     * @return The number token
     */
    public static MatlabNumberNode newNumber(int number) {
        return newNumber(Integer.toString(number));
    }

    public static MatrixNode newMatrix(List<MatlabNode> rows) {
        return new MatrixNode(rows);
    }

    public static MatrixNode newMatrix(MatlabNode... rows) {
        return newMatrix(Arrays.asList(rows));
    }

    public static CellNode newCell(List<MatlabNode> rows) {
        return new CellNode(rows);
    }

    // public static FieldAccessNode newFieldAccess(Collection<MatlabNode> elements) {
    // return new FieldAccessNode(elements);
    // }
    public static FieldAccessNode newFieldAccess(MatlabNode left, MatlabNode right) {
        return new FieldAccessNode(left, right);
    }

    public static DynamicFieldAccessNode newDynamicFieldAccess(MatlabNode left, MatlabNode right) {
        return new DynamicFieldAccessNode(left, right);
    }

    public static CommentNode newComment(String comment) {
        return new CommentNode(comment);
    }

    /**
     * TODO: Move to TemporaryNodeFactory
     * 
     * @return
     */
    public static DynamicFieldAccessSeparatorNode newDynamicFieldAccessSeparator() {
        return new DynamicFieldAccessSeparatorNode();
    }

    public static ColonNotationNode newColonNotation() {
        return new ColonNotationNode();
    }

    public static FunctionHandleNode newFunctionHandle(String identifier) {
        return new FunctionHandleNode(identifier);
    }

    public static LambdaNode newLambda(List<MatlabNode> inputs, MatlabNode expression) {
        return new LambdaNode(inputs, expression);
    }

    /**
     * Invoke symbol for operating system command.
     * 
     * @return
     */
    public static InvokeNode newInvoke(String command) {
        return new InvokeNode(command);
    }

}
