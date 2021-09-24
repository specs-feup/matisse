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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRFunctions.Utilities.TransformationUtils;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeUtils;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AccessCallSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BreakSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentBlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSingleSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ContinueSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ReturnSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.Utilities.ForInfo;
import org.specs.MatlabToC.CodeBuilder.CodeBuilderUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.TokenRules;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.AssignUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.ElementWiseTransform;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.MultipleSetToForAdvanced;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.UnfoldAssignment;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.suikasoft.MvelPlus.MvelSolver;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.classmap.MultiFunction;

/**
 * Rules that transform Matlab tokens representing Statements into C Tokens.
 * 
 * @author Joao Bispo
 * 
 *         TODO: Refactor break, else functions into reserved word function
 * 
 */
public class GeneralStatementRules {

    private static final BiFunctionClassMap<MatlabNode, MatlabToCFunctionData, CNode> matlabToCRules;

    static {
        // matlabToCRules = Maps.newEnumMap(MStatementType.class);
        matlabToCRules = new BiFunctionClassMap<>();

        GeneralStatementRules.matlabToCRules.put(FunctionDeclarationSt.class, GeneralStatementRules::functionDecRule);
        GeneralStatementRules.matlabToCRules.put(AssignmentSt.class, GeneralStatementRules::assignmentRule);
        GeneralStatementRules.matlabToCRules.put(AccessCallSt.class, GeneralStatementRules::accessCallRule);
        // GeneralStatementRules.matlabToCRules.put(EndSt.class, newEndRule()); // It seems it is not needed anymore
        GeneralStatementRules.matlabToCRules.put(BlockSt.class, GeneralStatementRules::blockRule);
        GeneralStatementRules.matlabToCRules.put(IfSt.class, GeneralStatementRules::ifRule);
        GeneralStatementRules.matlabToCRules.put(ElseIfSt.class, GeneralStatementRules::elseIfRule);
        GeneralStatementRules.matlabToCRules.put(ElseSt.class, GeneralStatementRules::elseRule);
        GeneralStatementRules.matlabToCRules.put(ReturnSt.class, GeneralStatementRules::returnRule);
        GeneralStatementRules.matlabToCRules.put(CommentSingleSt.class, GeneralStatementRules::commentRule);
        GeneralStatementRules.matlabToCRules.put(CommentBlockSt.class, GeneralStatementRules::commentBlockRule);
        GeneralStatementRules.matlabToCRules.put(ForSt.class, GeneralStatementRules::forRule);

        GeneralStatementRules.matlabToCRules.put(WhileSt.class, GeneralStatementRules::whileRule);

        GeneralStatementRules.matlabToCRules.put(BreakSt.class,
                reservedWordRule(ReservedWord.Break, InstructionType.Break));
        GeneralStatementRules.matlabToCRules.put(ContinueSt.class,
                reservedWordRule(ReservedWord.Continue, InstructionType.Continue));
    }

    /**
     * @return the inference rules
     */
    public static BiFunctionClassMap<MatlabNode, MatlabToCFunctionData, CNode> getMatlabToCRules() {
        return GeneralStatementRules.matlabToCRules;
    }

    public static CNode convert(StatementNode token, MatlabToCFunctionData data) {
        return GeneralStatementRules.matlabToCRules.apply(token, data);
        /*
        	.get(token.getClass());
        // MatlabToCRule rule = GeneralStatementRules.matlabToCRules.get(StatementUtils.getType(token));
        if (rule == null) {
            throw new RuntimeException("Could not find rule for '" + token.getNodeName() + "'");
        }
        
        return rule.matlabToC(token, data);
        */
    }

    /**
     * @return
     */
    private static CNode accessCallRule(AccessCallSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        return TokenRules.convertTokenExpr(statement.getAccessCall(), data);

    }

    /**
     * @return
     */
    private static CNode whileRule(WhileSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        CNode expression = TokenRules.convertTokenExpr(statement.getExpression(), data);
        return CNodeFactory.newWhileInstruction(expression);

    }

    /**
     * @return
     */
    /*
    private static MatlabToCRule newBreakRule() {
    return new MatlabToCRule() {
    
        @Override
        public CNode matlabToC(MatlabToken statement, MatlabToCFunctionData data) throws MatlabToCException {
    
    	// Generate reserved word 'break'
    	CNode breakWord = CNodeFactory.newReservedWord(ReservedWord.Break);
    
    	CNode inst = CNodeFactory.newInstruction(InstructionType.Break, breakWord);
    
    	return inst;
        }
    };
    }
    */

    /*
    private static MatlabToCRule newContinueRule() {
    return new MatlabToCRule() {
    
        @Override
        public CNode matlabToC(MatlabToken statement, MatlabToCFunctionData data) throws MatlabToCException {
    
    	// Generate reserved word 'continue'
    	CNode continueWord = CNodeFactory.newReservedWord(ReservedWord.Continue);
    
    	CNode inst = CNodeFactory.newInstruction(InstructionType.Continue, continueWord);
    
    	return inst;
        }
    };
    }
    */

    private static BiFunction<StatementNode, MatlabToCFunctionData, CNode> reservedWordRule(ReservedWord word,
            InstructionType type) {

        return (statement, data) -> {

            // Generate reserved word 'continue'
            CNode cWord = CNodeFactory.newReservedWord(word);
            return CNodeFactory.newInstruction(type, cWord);
        };
    }

    /**
     * @return
     */
    private static CNode forRule(ForSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // Check if induction variable exists
        // If not, assume integer as default
        IdentifierNode inductionId = statement.getIndexVar();

        String inductionVarName = inductionId.getName();

        MatlabNode exprId = statement.getExpression();
        ForInfo matlabForInfo = ForInfo.parseForExpression(exprId);

        // TODO: Disable multiline inlining

        CNode startValue = TokenRules.convertTokenExpr(matlabForInfo.startValue, data);
        CNode endValue = TokenRules.convertTokenExpr(matlabForInfo.endValue, data);
        CNode increment = TokenRules.convertTokenExpr(matlabForInfo.increment, data);

        // Get induction variable type
        VariableType inductionVarType = getIndunctionVarType(data, inductionVarName,
                Arrays.asList(startValue, endValue, increment));

        // Remove constant from induction variable, if present
        inductionVarType = ScalarUtils.setConstantString(inductionVarType, null);

        // Update variable type
        if (data.hasType(inductionVarName)) {
            data.setVariableType(inductionVarName, inductionVarType);
        }

        Variable inductionVar = new Variable(inductionVarName, inductionVarType);

        // Add variable
        data.addVariableType(inductionVarName, inductionVarType);

        COperator operator = getCOperator(increment);

        // COperator operator = COperator.LessThanOrEqual;
        // if(!matlabForInfo.increasing) {
        // operator = COperator.GreaterThanOrEqual;
        // }

        CNode forToken = new ForNodes(data.getProviderData()).newForInstruction(inductionVar, startValue,
                operator, endValue, COperator.Addition, increment);

        return forToken;
    }

    private static VariableType getIndunctionVarType(MatlabToCFunctionData data, String inductionVarName,
            List<CNode> ctokens) {

        List<VariableType> types = CNodeUtils.getVariableTypes(ctokens);
        // NumericType highestNumericType = VariableTypeUtils.getHighestNumericPriority(types);
        // VariableType highestNumericType = VariableTypeUtilsG.getMaximalFit(types);

        ScalarType highestNumericType = ScalarUtils.getMaxRank(ScalarUtils.cast(types));

        if (highestNumericType == null) {
            return data.getVariableType(inductionVarName);
        }

        // CToken varToken = CTokenFactory.newVariable(inductionVarName,
        // VariableTypeFactoryOld.newNumeric(highestNumericType));
        CNode varToken = CNodeFactory.newVariable(inductionVarName, highestNumericType);

        return data.getVariableType(inductionVarName, varToken);
    }

    private static COperator getCOperator(CNode increment) {
        String incrementExpr = increment.getCode();

        Number result = MvelSolver.evaltoNumber(incrementExpr);
        if (result == null) {
            SpecsLogs.msgInfo("Could not evaluate 'for' step '" + incrementExpr
                    + "', does not know how indunction variable changes. Assuming variable increases.");
            return COperator.LessThanOrEqual;
        }

        // if (result.dequals(0)) {
        double doubleValue = result.doubleValue();
        if (doubleValue == 0) {
            throw new RuntimeException("'for' with step 0");
        }

        // if (result > 0) {
        if (doubleValue > 0) {
            return COperator.LessThanOrEqual;
        }

        return COperator.GreaterThanOrEqual;

    }

    /**
     * @return
     */
    private static CNode elseRule(ElseSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // Generate reserved words 'else'
        CNode elseWord = CNodeFactory.newReservedWord(ReservedWord.Else);

        return CNodeFactory.newInstruction(InstructionType.Else, elseWord);

    }

    /**
     * @return
     */
    private static CNode elseIfRule(ElseIfSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // Generate reserved words 'else' and 'if'
        CNode elseWord = CNodeFactory.newReservedWord(ReservedWord.Else);
        CNode ifWord = CNodeFactory.newReservedWord(ReservedWord.If);

        // Generate expression
        CNode convertedExpr = TokenRules.convertTokenExpr(statement.getExpression(), data);

        return CNodeFactory.newInstruction(InstructionType.ElseIf, elseWord, ifWord, convertedExpr);
    }

    /**
     * @return
     */
    private static CNode commentRule(CommentSingleSt statement, MatlabToCFunctionData data)
            throws MatlabToCException {

        // Get comment
        String comment = statement.getComment();

        // Check if function declaration has not been found yet
        if (!data.haveFoundFunctionDeclaration()) {
            data.getFunctionComments().add(comment);
            return CNodeFactory.newEmptyToken();
        }

        CNode commentCToken = CNodeFactory.newComment(comment);
        return CNodeFactory.newInstruction(InstructionType.Comment, commentCToken);

    }

    /**
     * @return
     */
    private static CNode commentBlockRule(CommentBlockSt commentBlock, MatlabToCFunctionData data)
            throws MatlabToCException {

        // Get list of comments
        List<String> commentLines = commentBlock.getCommentLines();

        StringBuilder builder = new StringBuilder();
        for (String commentLine : commentLines) {
            builder.append(commentLine).append("\n");
        }

        String comment = builder.toString();

        // Check if function declaration has not been found yet
        if (!data.haveFoundFunctionDeclaration()) {
            data.getFunctionComments().add(comment);
            // return null;
            return CNodeFactory.newEmptyToken();
        }

        CNode commentCToken = CNodeFactory.newComment(comment);
        CNode inst = CNodeFactory.newInstruction(InstructionType.Comment, commentCToken);

        return inst;

    }

    /**
     * @return
     */
    private static CNode returnRule(ReturnSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // MatLab returns are always empty, but in C we need to respect
        // the function declaration.

        // If there is a single output name, add a return for that name.
        // Otherwise, add a return without variable.

        Collection<String> outputNames = data.getOutputNames();
        if (outputNames.size() != 1) {
            // Add "return"
            CNode returnToken = CNodeFactory.newReturn();

            CNode instruction = CNodeFactory.newInstruction(InstructionType.Return, returnToken);
            return instruction;
        }

        // Add return to the output name
        // String outputName = outputNames.get(0);
        String outputName = outputNames.iterator().next();
        VariableType returnType = data.getVariableType(outputName);
        Variable returnVariable = new Variable(outputName, returnType);

        CNode varToken = CNodeFactory.newVariable(returnVariable);

        // Add "return outputName"
        CNode returnToken = CNodeFactory.newReturn(varToken);
        CNode instruction = CNodeFactory.newInstruction(InstructionType.Return, returnToken);

        return instruction;

    }

    /**
     * @return
     */
    private static CNode ifRule(IfSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // Generate reserved word 'if'
        CNode ifWord = CNodeFactory.newReservedWord(ReservedWord.If);

        // Generate expression
        CNode convertedExpr = TokenRules.convertTokenExpr(statement.getExpression(), data);

        return CNodeFactory.newInstruction(InstructionType.If, ifWord, convertedExpr);
    }

    /**
     * 'End' StatementType represents Blocks of code, which contains either Statements or other Blocks of code.
     * 
     * @return
     */
    private static CNode blockRule(BlockSt statement, MatlabToCFunctionData data) throws MatlabToCException {

        // Entering a block, increase constant propagation guard level
        data.increasePropragationGuard();

        List<CNode> blockInstructions = Lists.newArrayList();
        for (StatementNode child : statement.getStatements()) {
            // Get CToken
            CNode newToken = CodeBuilderUtils.matlabToC(child, data);
            if (newToken == null) {
                continue;
            }

            // Add token to block instructions
            blockInstructions.add(newToken);
        }

        CNode blockToken = CNodeFactory.newBlock(blockInstructions);
        CNode inst = CNodeFactory.newInstruction(InstructionType.Block, blockToken);

        // Leaving a block, decrease constant propagation guard level
        data.decreasePropragationGuard();

        return inst;

    }

    /**
     * Assignments are right-to-left So in A(i) = f(x), f(x) is evaluated before A(i).
     * 
     * @return
     */
    private static CNode assignmentRule(AssignmentSt assign, MatlabToCFunctionData data) throws MatlabToCException {

        // Right-hand token
        // MatlabNode rightHand = StatementAccess.getAssignmentRightHand(statement);

        // Left-Hand token
        // MatlabNode leftHand = StatementAccess.getAssignmentLeftHand(statement);

        // Collect left hand ids
        List<IdentifierNode> leftHandIds = assign.getLeftHand().getDescendantsAndSelf(IdentifierNode.class);

        List<String> leftHandStrings = new ArrayList<>();
        leftHandIds.forEach(id -> leftHandStrings.add(id.getName()));

        data.setLeftHandIds(leftHandStrings);

        CNode assignC = assignmentWork(assign, data);

        data.setLeftHandIds(Collections.emptyList());

        return assignC;
    }

    private static CNode assignmentWork(AssignmentSt statement,
            MatlabToCFunctionData data) throws MatlabToCException {

        // Try to transform assignments with multiple colons, e.g. A = B(:, :, x) + C
        Optional<CNode> transformedStatement = new MultipleSetToForAdvanced(data).apply(statement);
        if (transformedStatement.isPresent()) {
            return transformedStatement.get();
        }

        AssignUtils assign = new AssignUtils(data);

        MatlabNode rightHand = statement.getRightHand();
        MatlabNode leftHand = statement.getLeftHand();

        // Check if needs to be unfolded in two statements -> Moved to MatlabToMatlabRule
        // if (AssignUtils.checkAssignUnfold(leftHand, rightHand, data)) {
        // System.out.println("POSSIBLE OPTIMIZATION:\n" + MatlabProcessorUtils.toMFile(statement));
        // }

        // Check if MATLAB tokens need any kind of processing (e.g.,
        // adding a type to a zeros/ones function, adding the number of outputs...)
        // assign.fixMatlabAssignment(leftHand, rightHand);

        // CHANGED PLACE
        // Check if there is an access call on the left hand
        // MatlabToken accessCall = MatlabTokenUtils.getToken(leftHand, MTokenType.AccessCall);
        // if (accessCall != null) {
        // return assign.parseLeftHandAccessCall(accessCall, rightHand);
        // }

        // It is important to know if the left hand is an access call, this means that the assignment
        // represents a matrix set, and has to be treated accordingly
        // MatlabNode leftAccessCall = MatlabTokenUtils.getToken(leftHand, MType.AccessCall);

        // Get output variables names
        List<String> resultNames = MatlabNodeUtils.getVariableNames(leftHand);

        List<Boolean> areAccessCall = GeneralStatementRules.areAccessCall(leftHand);

        // Build return types
        List<VariableType> returnTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < resultNames.size(); i++) {
            /*
            		    if (data.getFunctionName().equals("find_dynamic")) {
            			System.out.println("NAMES:" + resultNames);
            			System.out.println("TYPES:" + data.getLocalVariableTypes());
            		    }
            */
            VariableType variableType = data.getVariableType(resultNames.get(i));

            // Remove pointer information about the types, we just want to pass the "pure" types
            if (variableType != null) {
                variableType = ReferenceUtils.getType(variableType, false);
            }

            // If access call and type is a matrix, use element type
            if (areAccessCall.get(i)) {
                if (MatrixUtils.isMatrix(variableType)) {
                    variableType = MatrixUtils.getElementType(variableType);
                }
            }

            returnTypes.add(variableType);

        }

        // assert returnTypes.stream().allMatch(type -> type != null) : "Types should not be null";

        // Only set return types if left hand is not an access call. Otherwise, other rules apply.
        // if (leftAccessCall == null) {
        Optional<AccessCallNode> leftAccessCall = leftHand.to(AccessCallNode.class);
        if (!leftAccessCall.isPresent()) {
            // if (!(leftHand instanceof AccessCallNode)) {
            data.setAssignmentReturnTypes(returnTypes);

        }

        // Before building the C expression from the right hand, check MATLAB transformation rules over
        // assignments.

        if (MatlabToCKeys.isActive(data.getSettings(), MatisseOptimization.InlineElementWiseMatrixOps))

        {
            Optional<CNode> transformedCode = new ElementWiseTransform(statement, leftHand, rightHand, data)
                    .apply();

            if (transformedCode.isPresent()) {
                // Before returning, set return types to empty
                data.setAssignmentReturnTypes(Collections.emptyList());
                return transformedCode.get();
            }

        }

        // Build C expression for right hand
        CNode rhExpression = TokenRules.convertTokenExpr(rightHand, data);

        // Unset option
        List<VariableType> emptyTypes = Collections.emptyList();
        data.setAssignmentReturnTypes(emptyTypes);
        // OptionUtils.set(MatlabGlobalOption.ASSIGNMENT_RETURN_TYPE, null);

        // Get output types
        List<VariableType> resultTypes = assign.getResultTypes(leftHand, rhExpression, resultNames);

        // Check if the is only one output, and assignment should be unfolded
        boolean oneOutput = resultNames.size() == 1;
        if (oneOutput)

        {
            UnfoldAssignment unfoldAssignment = new UnfoldAssignment(data);
            if (unfoldAssignment.check(leftHand, rightHand, rhExpression)) {
                return unfoldAssignment.unfold(leftHand, rightHand);
            }
        }

        // Check if there is an access call on the left hand
        // MatlabNode accessCall = MatlabTokenUtils.getToken(leftHand, MType.AccessCall);
        // if (accessCall != null) {
        // return assign.parseLeftHandAccessCall(accessCall, rightHand, rhExpression);
        // }
        Optional<AccessCallNode> accessCall = leftHand.to(AccessCallNode.class);
        if (accessCall.isPresent())

        {
            return assign.parseLeftHandAccessCall(accessCall.get(), rightHand, rhExpression);
        }

        // Updates the types of the result in the Symbol table
        /*
        		if (data.getFunctionName().equals("simple_find")) {
        		    System.out.println("BEFORE I_TEMP TYPE:" + data.getVariableType("I_temp"));
        		}
        */

        assign.updateResultTypes(resultNames, resultTypes, rhExpression);

        /*
        		if (data.getFunctionName().equals("simple_find")) {
        		    System.out.println("AFTER I_TEMP TYPE:" + data.getVariableType("I_temp"));
        		}
        */

        // Convert left hand to C
        /*
        if (leftHand.numChildren() > 1) {
        System.out.println("BEFORE!");
        }
        */
        List<CNode> cLeftHandList = assign.convertLeftHand2(leftHand, resultTypes);
        /*
        		if (leftHand.numChildren() > 1) {
        		    System.out.println("AFTER!");
        		}
        */
        // Try to create a function call assign
        CNode functionCall = AssignUtils.buildFunctionCallAssign(cLeftHandList, rhExpression, data);

        if (functionCall != null)

        {
            return functionCall;
        }

        if (cLeftHandList.size() != 1)

        {
            throw new RuntimeException(
                    "Left hand of assignment has more than one output and right hand is not a function.");
        }

        // If this is an assignment and return type is void, promote
        // outputs to inputs, returning an instruction with the
        // expression
        VariableType rightHandType = rhExpression.getVariableType();

        // If matrix copy between variables, create a copy function
        CNode cLeftHand = cLeftHandList.get(0);
        if (AssignUtils.isMatrixCopy(rhExpression, rightHandType, cLeftHand))

        {
            return new TransformationUtils(data.getProviderData().newInstance()).parseMatrixCopy(rhExpression,
                    cLeftHand);
        }

        return assign.buildAssignment(cLeftHand, rhExpression);

    }

    /**
     * @return
     */
    private static CNode functionDecRule(FunctionDeclarationSt functionDeclarationStatement, MatlabToCFunctionData data)
            throws MatlabToCException {

        // Get function name
        String functionName = functionDeclarationStatement.getFunctionName();

        // Get input names
        List<String> inputNames = functionDeclarationStatement.getInputNames();

        // Adds a function input. Also adds the type, according
        // to the current type definitions.
        // Function inputs need to be always defined, at this point.
        data.addInputs(inputNames);

        // Get outputs
        MatlabNode outputs = functionDeclarationStatement.getOutputs();

        // Add the names of the outputs
        CodeBuilderUtils.addOutputs(outputs.getChildren(), data);

        // Set function name
        data.setFunctionName(functionName);

        // Set flag
        data.setFoundFunctionDeclaration(true);

        // Does not produce tokens
        return CNodeFactory.newEmptyToken();
    }

    public static List<Boolean> areAccessCall(MatlabNode node) {
        return GeneralStatementRules.ARE_ACCESSCALL.apply(node);
    }

    private static final FunctionClassMap<MatlabNode, List<Boolean>> ARE_ACCESSCALL = new FunctionClassMap<>();

    static {
        GeneralStatementRules.ARE_ACCESSCALL.put(IdentifierNode.class, id -> Arrays.asList(Boolean.FALSE));
        GeneralStatementRules.ARE_ACCESSCALL.put(AccessCallNode.class, call -> Arrays.asList(Boolean.TRUE));
        GeneralStatementRules.ARE_ACCESSCALL.put(OutputsNode.class, GeneralStatementRules::areAccessCall);
    }

    private static List<Boolean> areAccessCall(OutputsNode outputs) {
        return outputs.getChildren().stream()
                // Apply on the children of Outputs
                .map(node -> GeneralStatementRules.ARE_ACCESSCALL.apply(node))
                .reduce(new ArrayList<>(), (id, list) -> SpecsCollections.add(id, list));
    }

    private static class AreAccessCall extends MultiFunction<MatlabNode, List<Boolean>> {
    }

    private static final AreAccessCall ARE_ACCESSCALL_V2 = new AreAccessCall();
    static {
        GeneralStatementRules.ARE_ACCESSCALL_V2.put(IdentifierNode.class, id -> Arrays.asList(Boolean.FALSE));
        GeneralStatementRules.ARE_ACCESSCALL_V2.put(AccessCallNode.class, call -> Arrays.asList(Boolean.TRUE));
        // ARE_ACCESSCALL_V2.put(OutputsNode.class, GeneralStatementRules::areAccessCallV2);
        GeneralStatementRules.ARE_ACCESSCALL_V2.put(OutputsNode.class,
                (areAccessCall, outputs) -> outputs.getChildren().stream()
                        // Apply on the children of Outputs
                        .map(node -> areAccessCall.apply(node))
                        .reduce(new ArrayList<>(), (id, list) -> SpecsCollections.add(id, list)));
    }

    // private static List<Boolean> areAccessCallV2(AreAccessCall areAccessCall, OutputsNode outputs) {
    // return outputs.getChildren().stream()
    // // Apply on the children of Outputs
    // .map(node -> areAccessCall.apply(node))
    // .reduce(new ArrayList<>(), (id, list) -> CollectionUtils.add(id, list));
    // }
}
