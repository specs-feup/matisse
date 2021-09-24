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

package org.specs.CIR.Tree.CNodes;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods for creating CTokens.
 * 
 * @author Joao Bispo
 * 
 */
public class CNodeFactory {

    private final DataStore settings;

    public CNodeFactory(DataStore settings) {
        this.settings = settings;
    }

    /**
     * A CToken for when we need to return no result (e.g., when converting an 'end' statement).
     * 
     * 
     * @return a literal CToken with an empty string as content
     */
    public static VerbatimNode newEmptyToken() {
        return newLiteral("");
    }

    public static ReservedWordNode newReservedWord(ReservedWord word) {
        return new ReservedWordNode(word);
    }

    public static AssignmentNode newAssignment(Variable variable, CNode expression) {
        CNode leftHandVar = newVariable(variable);

        return newAssignment(leftHandVar, expression);
    }

    public static AssignmentNode newAssignment(CNode leftHand, CNode expression) {
        return new AssignmentNode(leftHand, expression);
    }

    public static VariableNode newVariable(Variable variable) {
        return new VariableNode(variable);
    }

    /**
     * @param inputMatrixVar
     * @return
     */
    public static List<CNode> newVariableList(Variable... variables) {
        List<CNode> varList = SpecsFactory.newArrayList();

        for (Variable variable : variables) {
            varList.add(CNodeFactory.newVariable(variable));
        }

        return varList;
    }

    /**
     * Creates a new return statement that returns the given variable.
     * 
     * @param variable
     * @return
     */
    public static ReturnNode newReturn(CNode returnToken) {
        return new ReturnNode(returnToken);
    }

    /**
     * Creates a new return token with no children.
     * 
     * @return
     */
    public static ReturnNode newReturn() {
        return new ReturnNode();
    }

    public static FunctionCallNode newFunctionCall(FunctionInstance functionInstance, CNode... inputs) {
        return newFunctionCall(functionInstance, Arrays.asList(inputs));
    }

    /**
     * Creates a FunctionCall for the given implementation and arguments.
     * 
     * <p>
     * This method just creates a FunctionCallNode and does not perform any verification/transformation over the
     * arguments. When using this method, check if instead you should be using FunctionInstance.newFunctionCall.
     * 
     * @param division
     * @param inputVars
     * @return
     */
    public static FunctionCallNode newFunctionCall(FunctionInstance functionInstance, List<CNode> inputs) {
        if (functionInstance.getFunctionType().isNoOp()) {
            throw new IllegalArgumentException("Can't instantiate no-op functions: " + functionInstance.getCName());
        }

        return new FunctionCallNode(functionInstance, inputs);
    }

    public FunctionCallNode newFunctionCall(InstanceProvider provider, CNode... inputs) {
        return newFunctionCall(provider, Arrays.asList(inputs));
    }

    public FunctionCallNode newFunctionCall(InstanceProvider provider, List<CNode> inputs) {

        // Create FunctionInstance
        FunctionInstance instance = provider.getCheckedInstance(ProviderData.newInstance(
                CNodeUtils.getVariableTypes(inputs),
                settings));

        return newFunctionCall(instance, inputs);
    }

    public static FunctionInputsNode newFunctionInputs(List<CNode> inputs) {
        return new FunctionInputsNode(inputs);
    }

    public static VerbatimNode newLiteral(String string) {
        return newLiteral(string, VoidType.newInstance(), PrecedenceLevel.Unspecified);
    }

    public static VerbatimNode newLiteral(String string, PrecedenceLevel precedenceLevel) {
        return newLiteral(string, VoidType.newInstance(), precedenceLevel);
    }

    public static VerbatimNode newLiteral(String string, VariableType variableType) {
        return newLiteral(string, variableType, PrecedenceLevel.Unspecified);
    }

    /**
     * @param string
     * @return
     */
    public static VerbatimNode newLiteral(String string, VariableType variableType, PrecedenceLevel precedenceLevel) {
        return new VerbatimNode(string, variableType, precedenceLevel);
    }

    public static InstructionNode newInstruction(InstructionType type, CNode... ctoken) {
        return new InstructionNode(type, ctoken);
    }

    public static PragmaNode newPragma(String content) {
        return new PragmaNode(content);
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param instructions
     * @return
     */
    public static BlockNode newBlock(CNode... instructions) {
        return newBlock(Arrays.asList(instructions));
    }

    /**
     * Creates a new Block token with the given instructions.
     * 
     * <p>
     * Verifies if each of the given instructions is of type Instruction. If any of the tokens is not an instruction,
     * tries to infer the instruction type and builds the instruction type.
     * 
     * @param blockInstructions
     * @return
     */
    public static BlockNode newBlock(List<CNode> blockInstructions) {
        return BlockNode.create(blockInstructions);
    }

    /**
     * @param cnumber
     * @return
     */
    public static CNumberNode newCNumber(CNumber cnumber) {
        return new CNumberNode(cnumber);
    }

    /**
     * @param string
     * @return
     */
    public static CNumberNode newCNumber(int number) {
        return newCNumber(CLiteral.newInteger(number));
    }

    public static CNumberNode newCNumber(Number number, VariableType type) {
        CLiteral literal = CLiteral.newInstance(number,
                (ScalarType) ScalarUtils.toScalar(type).pointer().getType(false));
        return newCNumber(literal);
    }

    /**
     * @param comment
     * @return
     */
    public static CommentNode newComment(String comment) {
        return new CommentNode(comment);
    }

    /**
     * @param blockInstructions
     * @return
     */
    public static ParenthesisNode newParenthesis(CNode token) {
        return new ParenthesisNode(token);
    }

    /**
     * @param expression
     * @return
     */
    public static InstructionNode newWhileInstruction(CNode expression) {
        CNode reservedWordToken = newReservedWord(ReservedWord.While);

        return newInstruction(InstructionType.While, reservedWordToken, expression);
    }

    /**
     * @param returnVarName
     * @param returnVariable
     * @return
     */
    public static VariableNode newVariable(String varName, VariableType type) {

        Variable var = new Variable(varName, type);
        return newVariable(var);
    }

    /**
     * @param returnVarName
     * @param returnVariable
     * @return
     */
    public static VariableNode newVariable(String varName, VariableType type, boolean isGlobal) {
        Variable var = new Variable(varName, type, isGlobal);
        return newVariable(var);
    }

    /**
     * @param outputVar
     * @return
     */
    public static ReturnNode newReturn(Variable outputVar) {
        return newReturn(CNodeFactory.newVariable(outputVar));
    }

    public static StringNode newString(String string, int charBitSize) {
        return new StringNode(string, charBitSize);
    }

}
