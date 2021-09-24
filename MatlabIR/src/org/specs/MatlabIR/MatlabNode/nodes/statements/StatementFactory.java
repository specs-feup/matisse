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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.BaseClassesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommentNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.InvokeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;

import pt.up.fe.specs.util.Preconditions;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * Utility methods for creating Statements.
 * 
 * @author JoaoBispo
 *
 */
public class StatementFactory {

    public static FunctionDeclarationSt newFunctionDeclaration(int lineNumber, String functionName,
            List<MatlabNode> inputs,
            List<MatlabNode> outputs) {

        FunctionDeclarationSt st = new FunctionDeclarationSt(lineNumber,
                MatlabNodeFactory.newOutputs(outputs),
                MatlabNodeFactory.newIdentifier(functionName),
                MatlabNodeFactory.newFunctionInputs(inputs));

        return st;
    }

    public static CommentSingleSt newComment(int lineNumber, String comment) {
        // TODO: Remove comment node, save comment information in statement
        CommentNode commentNode = MatlabNodeFactory.newComment(comment);

        // Create statement with the comment
        return new CommentSingleSt(lineNumber, commentNode);
    }

    /**
     * Builds a new Statement of the type Assignment
     * 
     * @param leftHand
     * @param rightHand
     * @return
     */
    public static AssignmentSt newAssignment(MatlabNode leftHand, MatlabNode rightHand) {
        return new AssignmentSt(0, false, leftHand, rightHand);
    }

    public static AssignmentSt newAssignment(StatementData data, MatlabNode leftHand, MatlabNode rightHand) {
        return new AssignmentSt(data.getLine(), data.isDisplay(), leftHand, rightHand);
    }

    public static AccessCallSt newAccessCall(AccessCallNode accessCall) {
        return new AccessCallSt(0, false, accessCall);
    }

    // public static AccessCallSt newAccessCall(StatementData data, AccessCallNode accessCall) {
    public static AccessCallSt newAccessCall(int lineNumber, boolean displayResults, MatlabNode accessCall) {
        return new AccessCallSt(lineNumber, displayResults, (AccessCallNode) accessCall);
    }

    public static AccessCallSt newAccessCall(int lineNumber, boolean displayResults, List<MatlabNode> children) {
        return new AccessCallSt(lineNumber, displayResults, children);
    }

    public static List<CommentSingleSt> newCommentStatements(String comments, int lineNumber) {
        List<CommentSingleSt> commentTokens = new ArrayList<>();
        StringLines.newInstance(comments)
                .forEach(line -> commentTokens.add(StatementFactory.newComment(lineNumber, line)));

        return commentTokens;
    }

    public static CommentBlockSt newCommentBlockStatementFromStrings(List<String> comments, int lineNumber) {
        List<MatlabNode> commentNodes = comments.stream()
                .map(comment -> StatementFactory.newComment(lineNumber, comment))
                .collect(Collectors.toList());

        return newCommentBlockStatement(commentNodes, lineNumber);
    }

    /*
    public static BlockSt newBlockStatement(List<MatlabNode> comments, int lineNumber) {
    return new BlockSt(lineNumber, comments);
    }
    */

    public static CommentBlockSt newCommentBlockStatement(List<? extends MatlabNode> comments, int lineNumber) {
        List<CommentSt> newComments = new ArrayList<>();
        for (MatlabNode comment : comments) {
            newComments.add((CommentSt) comment);
        }

        // return new CommentBlockSt(lineNumber, comments);
        return new CommentBlockSt(lineNumber, newComments);
    }

    public static BreakSt newBreak(int lineNumber) {
        return new BreakSt(lineNumber);
        /*
        List<MatlabNode> breakTokens = new ArrayList<>();
        
        breakTokens.add(MatlabNodeFactory.newReservedWord(ReservedWord.Break));
        
        return StatementFactory.newStatement(breakTokens, lineNumber, false);
        */
    }

    public static IfSt newIf(int lineNumber, MatlabNode condition) {
        return new IfSt(lineNumber, condition);
    }

    public static IfSt newIf(int lineNumber, List<MatlabNode> children) {
        return new IfSt(lineNumber, children);
    }

    public static ElseSt newElse(int lineNumber) {
        return new ElseSt(lineNumber);
    }

    public static ElseIfSt newElseIf(int lineNumber, List<MatlabNode> children) {
        return new ElseIfSt(lineNumber, children);
    }

    public static WhileSt newWhile(int lineNumber, MatlabNode expression) {
        return new WhileSt(lineNumber, expression);
    }

    public static WhileSt newWhile(int lineNumber, List<MatlabNode> children) {
        return new WhileSt(lineNumber, children);
    }

    public static SwitchSt newSwitch(int lineNumber, List<MatlabNode> children) {
        return new SwitchSt(lineNumber, children);
    }

    public static CaseSt newCase(int lineNumber, List<MatlabNode> children) {
        return new CaseSt(lineNumber, children);
    }

    /**
     * Catch without identifier.
     * 
     * @param lineNumber
     * @return
     */
    public static CatchSt newCatch(int lineNumber) {
        return new CatchSt(lineNumber);
    }

    /**
     * SPMD without parameters.
     * 
     * @param lineNumber
     * @return
     */
    public static SpmdSt newSpmd(int lineNumber) {
        return new SpmdSt(lineNumber, Collections.emptyList());
    }

    /**
     * Catch with identifier.
     * 
     * @param lineNumber
     * @param identifier
     * @return
     */
    public static CatchSt newCatch(int lineNumber, MatlabNode identifier) {
        return new CatchSt(lineNumber, identifier);
    }

    public static OtherwiseSt newOtherwise(int lineNumber, boolean display) {
        return new OtherwiseSt(lineNumber, display);
    }

    /**
     * TODO: Verify if children are needed.
     * 
     * @param lineNumber
     * @param children
     * @return
     */
    public static TrySt newTry(int lineNumber, List<MatlabNode> children) {
        return new TrySt(lineNumber, children);
    }

    /**
     * TODO: Verify if children are needed
     * 
     * @param lineNumber
     * @param children
     * @return
     */
    public static ContinueSt newContinue(int lineNumber, boolean display, List<MatlabNode> children) {
        return new ContinueSt(lineNumber, display, children);
    }

    public static EndSt newEnd(int lineNumber) {
        return new EndSt(lineNumber);
    }

    public static EndFunctionSt newEndFunction(int lineNumber) {
        return new EndFunctionSt(lineNumber);
    }

    public static EndIfSt newEndIf(int lineNumber) {
        return new EndIfSt(lineNumber);
    }

    public static EndWhileSt newEndWhile(int lineNumber) {
        return new EndWhileSt(lineNumber);
    }

    public static EndForSt newEndFor(int lineNumber) {
        return new EndForSt(lineNumber);
    }

    public static ClassdefSt newClassdef(int lineNumber, AttributesNode attributes, String classname,
            BaseClassesNode baseClasses) {

        List<MatlabNode> children = new ArrayList<>();
        children.add(attributes);
        children.add(MatlabNodeFactory.newIdentifier(classname));
        children.add(baseClasses);

        return new ClassdefSt(lineNumber, children);
    }

    /*
        public static ReturnSt newReturn(BuilderData data) {
    	return new ReturnSt(data.l);
        }
        */
    public static ReturnSt newReturn(int line) {
        return new ReturnSt(line);
    }

    public static BlockSt newBlock(int lineNumber, List<StatementNode> children) {
        return new BlockSt(lineNumber, children);

    }

    public static NestedFunctionSt newNestedFunction(int lineNumber, List<StatementNode> children) {
        return new NestedFunctionSt(lineNumber, children);

    }

    /**
     * Method just for StatementBuilder (consider moving to other class).
     * 
     * @param lineNumber
     * @param children
     * @return
     */
    public static SimpleForSt newFor(int lineNumber, List<MatlabNode> children) {
        return new SimpleForSt(lineNumber, children);
    }

    public static SimpleForSt newFor(int lineNumber, MatlabNode index, MatlabNode values) {
        return new SimpleForSt(lineNumber, index, values);
    }

    public static ParForSt newParfor(int lineNumber, List<MatlabNode> children) {
        return new ParForSt(lineNumber, children);
    }

    public static UndefinedSt newUndefined(int line, boolean display, List<MatlabNode> children) {
        return new UndefinedSt(line, display, children);
    }

    public static CommandSt newCommand(int line, boolean display, CommandNode command) {
        return new CommandSt(line, display, command);
    }

    public static InvokeSt newInvoke(int line, boolean display, InvokeNode invoke) {
        return new InvokeSt(line, display, invoke);
    }

    public static ExpressionSt newExpression(int line, boolean display, MatlabNode expression) {
        return new ExpressionSt(line, display, expression);
    }

    public static GlobalSt newGlobal(int line, boolean display, List<MatlabNode> children) {
        return new GlobalSt(line, display, children);
    }

    public static GlobalSt newGlobalFromIds(int line, boolean display, List<String> identifiers) {
        Preconditions.checkArgument(!identifiers.isEmpty(), "Identifiers must have size at least 1");
        ReservedWordNode reservedWord = MatlabNodeFactory.newReservedWord(ReservedWord.Global);
        List<IdentifierNode> identifierNodes = identifiers.stream()
                .map(MatlabNodeFactory::newIdentifier)
                .collect(Collectors.toList());

        return newGlobal(line, display, SpecsCollections.concat(reservedWord, identifierNodes));
    }

    public static GlobalSt newGlobalFromIds(int line, boolean display, String... identifiers) {
        return newGlobalFromIds(line, display, Arrays.asList(identifiers));
    }

    /**
     * 
     * @param children
     * @param data
     * @return
     * @deprecated
     */
    /*
    public static MatlabNode newBlock(List<MatlabNode> children, StatementData data) {
    // return new EndSt(data, children);
    return new BlockNode(data, children);
    }
    */

}
