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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Matrix.Functions.SetRow;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabToC.CodeBuilder.CodeBuilderUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.MatlabToCRulesUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.TokenRules;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Functions.CustomFunctions.SetMultiple;
import org.specs.MatlabToC.Functions.CustomFunctions.SetWildcard;
import org.specs.MatlabToC.MFileInstance.MFileProvider;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.treenode.TokenTester;
import pt.up.fe.specs.util.treenode.TreeNode;

/**
 * Utility class with methods related to transforming the Assignment MATLAB token.
 * 
 * @author Joao Bispo
 * 
 */
public class AssignUtils {

    private static Set<String> functionNeedsNumberOfOutputs;

    static {
        AssignUtils.functionNeedsNumberOfOutputs = SpecsFactory.newHashSet();

        AssignUtils.functionNeedsNumberOfOutputs.add("max");
        AssignUtils.functionNeedsNumberOfOutputs.add("min");
    }

    private final MatlabToCFunctionData data;
    private final CNodeFactory cnodes;

    /**
     * @param data
     */
    public AssignUtils(MatlabToCFunctionData data) {
        // super(data.getProviderData().getSetupData());

        this.data = data;
        cnodes = new CNodeFactory(data.getSettings());
    }

    public MatlabToCFunctionData getData() {
        return data;
    }

    /**
     * @param rightHandExpression
     * @param rightHandType
     * @param cLeftHand
     * @return
     */
    public static boolean isMatrixCopy(CNode rightHandExpression, VariableType rightHandType, CNode cLeftHand) {

        // Check if expression returns a matrix
        if (!MatrixUtils.isMatrix(rightHandType)) {
            return false;
        }

        // Get variable in leftHand
        // CNode outputVarToken = CNodeUtils.getToken(cLeftHand, CNodeType.Variable);
        Optional<VariableNode> outputVarToken = cLeftHand.cast(VariableNode.class);
        // if (outputVarToken == null) {
        if (!outputVarToken.isPresent()) {
            SpecsLogs.warn("Not supported yet when left hand is:\n" + cLeftHand);
            return false;
        }

        // Check if output variable is matrix
        VariableType outputType = cLeftHand.getVariableType();
        if (!MatrixUtils.isMatrix(outputType)) {
            return false;
        }

        // Check if matrix types are the same
        // TODO needs to check if this side is the same variable?

        return true;
    }

    /**
     * If output is not set yet, adds the type to the table. Otherwise, checks if the current output type is assignment
     * compatible with the right hand type. If not, sets the output type to that of the right hand type.
     * 
     * 
     * @param data
     * @param resultName
     * @param rightHandType
     */
    private static void updateResultType(String resultName, VariableType rightHandType, MatlabToCFunctionData data) {

        VariableType resultType = data.getVariableType(resultName);

        // If guard active, remove constant information
        if (!data.isPropagateConstants() && resultType != null) {
            resultType = ScalarUtils.setConstantString(resultType, null);
            rightHandType = ScalarUtils.setConstantString(rightHandType, null);
        }

        // If output type not defined, add it to the table
        if (resultType == null) {

            // Check if result should be pointer, and update type
            if (data.isOutputPointer(resultName, rightHandType)) {
                rightHandType = ReferenceUtils.getType(rightHandType, true);
            }

            data.addVariableType(resultName, rightHandType);

            return;
        }

        // If both types are Numeric, and right hand has a constant, propagate constant (if allowed)
        if (data.isPropagateConstants()) {
            resultType = ScalarUtils.propagateConstant(rightHandType, resultType);
        }

        // If both types are Matrix, propagate shape (if allowed)
        if (data.isPropagateConstants()) {
            if (MatrixUtils.isMatrix(Arrays.asList(rightHandType, resultType))) {
                TypeShape shape = MatrixUtils.getShape(rightHandType);
                resultType = ((MatrixType) resultType).matrix().setShape(shape);
            }
        }

        // If output type is assignable from right hand type, set current output type and return
        if (ConversionUtils.isAssignable(rightHandType, resultType)) {
            // If right hand type has a view, pass that information to result type
            data.setVariableType(resultName, resultType);

            return;
        }

        // Check if there was a type promotion from Numeric to Matrix
        if (!MatrixUtils.isMatrix(resultType) && MatrixUtils.isMatrix(rightHandType)) {
            data.setVariableType(resultName, rightHandType);
            return;
            /*
            // If variable was defined externally, update table
            if (data.isVariableDefinedExternally(resultName)) {
            data.setVariableType(resultName, rightHandType);
            return;
            }
            
            // If left type is the same as the element type of the right hand
            if (resultType.isWeakType()) {
            data.setVariableType(resultName, rightHandType);
            return;
            }
            */

        }

        // Check if output variables are variables and not access calls
        String firstScope = "<empty>";
        if (!data.getScope().isEmpty()) {
            firstScope = data.getScope().get(0);
        }

        // Check if there are weak types
        boolean isLeftWeak = resultType.isWeakType();
        boolean isRightWeak = rightHandType.isWeakType();

        String baseMessage = "Type promotion for variable '" + resultName + "' on " + firstScope + ", line "
                + data.getLineNumber() + ":" + resultType + " <- " + rightHandType + ".";

        // If weakness is opposite of each other, warn that it can be addressed in the future
        if (isLeftWeak ^ isRightWeak) {
            String weakType = "on the left";
            if (isRightWeak) {
                weakType = "on the right";
            }

            String message = baseMessage
                    + "\n - However, one of the types ("
                    + weakType
                    + ") is a 'weak type' and this can be automatically address in the future. For now, define type in aspect file.";
            SpecsLogs.msgInfo(message);
            return;
        }

        // If both types are weak, message to developer
        if (isLeftWeak && isRightWeak) {
            // Replace left type with right type - not working right now, because C code is already being generated and
            // previous code can become stale
            // data.setVariableType(resultName, rightHandType);

            SpecsLogs
                    .msgInfo(baseMessage
                            + "\n !Both types are weak, check what should be done in this case:\n 1. do nothing, type is decided later;\n 2. Replace type of left hand with righr hand");
            return;
        }

        // Multiple definitions of output. Warn the user
        // LoggingUtils.msgInfo("Type promotion for variable '" + resultName + "' on " + firstScope + ", line "
        // + data.getLineNumber() + ":" + resultType + "->" + rightHandType + ". Define type in aspect file.");
        SpecsLogs.msgInfo(baseMessage + "\n - Define type in aspect file.");

    }

    /**
     * @param data2
     * @param normalizedArg
     * @return
     */
    public static boolean isNumericClassString(MatlabNode stringToken, MatlabToCFunctionData data) {
        if (stringToken == null) {
            return false;
        }

        // If a MatlabString, return type
        if (stringToken instanceof MatlabCharArrayNode) {
            String string = ((MatlabCharArrayNode) stringToken).getString();
            return (NumericClassName.getNumericClassName(string) != null);
        }

        // If an access call with name 'class', return true
        if (stringToken instanceof AccessCallNode) {
            String accessCallName = ((AccessCallNode) stringToken).getName();

            if (accessCallName.equals("class")) {
                return true;
            }
        }

        if (stringToken instanceof IdentifierNode) {
            String idName = ((IdentifierNode) stringToken).getName();

            // Get type
            VariableType type = data.getVariableType(idName);

            // Return true if of type string
            if (type instanceof StringType) {
                return true;
            }
        }

        return false;

    }

    /**
     * Updates the names of the inputs in the FunctionCall arguments.
     * 
     * @param outputNames
     * @param functionTypes
     * @param data
     */
    static void updateOutputAsInputArgs(FunctionCallNode functionCall, List<CNode> outputs,
            MatlabToCFunctionData data) {

        FunctionInstance fImpl = functionCall.getFunctionInstance();
        FunctionType fTypes = fImpl.getFunctionType();

        // Get Function Inputs
        List<CNode> inputArgs = new ArrayList<>(functionCall.getInputTokens());

        // Discover index of first out-as-in
        int numOutsAsIns = fTypes.getNumOutsAsIns();
        int indexFirstOutAsInt = inputArgs.size() - numOutsAsIns;

        // Replace the CTokens corresponding to outs-as-ins
        for (int i = 0; i < numOutsAsIns; i++) {
            // Output token
            CNode outputToken = outputs.get(i);

            // Replace CToken
            inputArgs.set(i + indexFirstOutAsInt, outputToken);
        }

        // Replace FunctionInputs token in FunctionCall
        functionCall.setInputTokens(inputArgs);
    }

    /**
     * Updates the types of the outputs in the Symbol table.
     * 
     * @param rightHandExpression
     * @param outputNames
     * @param data
     */
    public void updateResultTypes(List<String> outputNames, List<VariableType> outputTypes, CNode rightHandExpression) {

        // Check sizes
        if (outputNames.size() != outputTypes.size()) {
            throw new MatlabToCException("Number of output names (" + outputNames.size()
                    + ") different than number of right hand output types (" + outputTypes.size() + "). For "
                    + rightHandExpression, data);
        }

        for (int i = 0; i < outputNames.size(); i++) {
            updateResultType(outputNames.get(i), outputTypes.get(i), data);
        }
    }

    static boolean isAssignable(CNode leftHand, CNode rightHand) {

        // Check if return type of right hand is void
        if (VoidTypeUtils.isVoid(rightHand.getVariableType())) {
            return false;
        }

        // Check if type of left hand is declared matrix
        if (MatrixUtils.isStaticMatrix(leftHand.getVariableType())) {
            return false;
        }

        // Check if type of left hand is allocated matrix
        if (MatrixUtils.usesDynamicAllocation(leftHand.getVariableType())) {
            return false;
        }

        return true;
    }

    /*
    static CNode buildUnassignableInstruction(CNode rightHand, List<String> outputNames, MatlabToCFunctionData data) {
    
    // For most cases, the right hand should be a function call
    CNode functionCall = CNodeUtils.getToken(rightHand, CNodeType.FunctionCall);
    
    // Build instruction for function call
    if (functionCall != null) {
        return CNodeFactory.newInstruction(InstructionType.FunctionCall, functionCall);
    }
    
    LoggingUtils.msgWarn("Could not determine the type of this instructions:\n" + rightHand);
    
    // Build an Undefined instruction
    return CNodeFactory.newInstruction(InstructionType.Undefined, rightHand);
    }
    */

    public List<VariableType> getResultTypes(MatlabNode leftHand, CNode rightHandExpression, List<String> outputNames) {

        List<VariableType> rhOutputTypes = getOutputTypes(rightHandExpression);

        // Check special case of ArrayAccess
        // HACK
        if (rhOutputTypes.size() == 1) {

            Optional<AccessCallNode> accessCall = leftHand.to(AccessCallNode.class);
            // if (accessCall != null) {
            if (accessCall.isPresent()) {

                // Get type from symbol table
                VariableType variableType = data.getVariableType(outputNames.get(0));

                // Check if output variable has been initialized
                if (variableType == null) {

                    throw new RuntimeException("Variable '" + outputNames.get(0) + "' in function '"
                            + data.getFunctionName() + "' has not been initialized.");
                }

                // Type has to be matrix
                if (!MatrixUtils.isMatrix(variableType)) {
                    SpecsLogs.msgInfo("Array access to variable. It should be a matrix, instead it is a '"
                            + variableType + "'.");
                }

                return Arrays.asList(variableType);
            }
        }

        return rhOutputTypes;
    }

    /**
     * Returns the output types of the expression, taking into account the case of functions that use outputs as inputs.
     * 
     * <p>
     * If any of the output types is a PointerToNumeric, returns the type of the pointer (e.g., NumericType.CInt) and
     * not the pointer itself.
     * 
     * @param rightHand
     * @return
     */
    static List<VariableType> getOutputTypes(CNode token) {
        Optional<FunctionCallNode> functionCall = token.cast(FunctionCallNode.class);
        // CNode functionCall = CNodeUtils.getToken(token, CNodeType.FunctionCall);

        // If not a function call, return the type of the token as default
        if (!functionCall.isPresent()) {
            // if (functionCall == null) {
            VariableType outputType = token.getVariableType();

            // Remove pointer information
            outputType = ReferenceUtils.getType(outputType, false);

            List<VariableType> expressionType = SpecsFactory.newArrayList();
            expressionType.add(outputType);

            return expressionType;
        }

        // Get types of the function
        FunctionInstance fImplementation = functionCall.get().getFunctionInstance();

        FunctionType types = fImplementation.getFunctionType();

        // If function does not have outputs as inputs, return the C return type, without pointer
        List<VariableType> returnTypes = SpecsFactory.newArrayList();

        if (!VoidTypeUtils.isVoid(types.getCReturnType())) {
            returnTypes.add(ReferenceUtils.getType(types.getCReturnType(), false));
        } else {
            for (VariableType outputType : types.getOutputAsInputTypesNormalized()) {
                returnTypes.add(outputType);
            }
        }

        // Return outputs as inputs types
        return returnTypes;
    }

    /**
     * If left hand is a simple identifier, uses the given output type, preserving constant information. Otherwise,
     * reverts to the ExpressionRules.
     * 
     * <p>
     * This is done so that we can propagate constants in simple assignments.
     * 
     * @param leftHand
     * @param outputTypes
     * @param data
     * @return
     */
    public static CNode convertLeftHand(MatlabNode leftHand, List<VariableType> outputTypes,
            MatlabToCFunctionData data) {

        // MatlabNode identifier = MatlabTokenUtils.getToken(leftHand, MType.Identifier);
        Optional<IdentifierNode> identifier = leftHand.to(IdentifierNode.class);

        // If left hand is not a variable, use MATLAB to C rules to convert
        // token
        // if (identifier == null) {
        if (!identifier.isPresent()) {
            return TokenRules.convertTokenExpr(leftHand, data);
        }

        // String varName = MatlabTokenContent.getIdentifierName(identifier);
        String varName = identifier.get().getName();

        // We are inside an expression, variable must exist from
        // previous assignment, or as function input
        VariableType leftHandType = data.getVariableType(varName);

        // If CType
        return CNodeFactory.newVariable(varName, leftHandType);
    }

    /**
     * If left hand is a simple identifier, uses the given output type, preserving constant information. Otherwise,
     * reverts to the ExpressionRules.
     * 
     * <p>
     * This is done so that we can propagate constants in simple assignments.
     * 
     * @param leftHand
     * @param outputTypes
     * @param data
     * @return
     */
    public List<CNode> convertLeftHand2(MatlabNode leftHand, List<VariableType> outputTypes) {

        List<CNode> cLeftHand = SpecsFactory.newArrayList();

        // Set flag "left hand assignmet"
        data.setLeftHandAssignment(true);
        // If left hand is of type outputs, convert each of the children
        if (leftHand instanceof OutputsNode) {
            data.setForcePointerOutput(true);
            for (MatlabNode output : leftHand.getChildren()) {
                CNode outputToken = TokenRules.convertTokenExpr(output, data);
                cLeftHand.add(outputToken);
            }
            data.setForcePointerOutput(false);
        } else {

            // Convert left hand directly
            CNode outputToken = TokenRules.convertTokenExpr(leftHand, data);
            cLeftHand.add(outputToken);
        }

        // Unset flag "left hand assignmet"
        data.setLeftHandAssignment(false);

        return cLeftHand;
    }

    /**
     * Return true if given token represents an AccessCall.
     * 
     * @param leftHand
     * @return
     */
    /*
    public static MatlabNode getAccessCall(MatlabNode leftHand) {
    // MatlabToken call = TokenUtils.getToken(leftHand, MTokenType.AccessCall, EnumSet.of(MTokenType.Parenthesis));
    MatlabNode call = TokenUtils.getToken(leftHand, MType.AccessCall, MType.Parenthesis);
    
    return call;
    }
    */

    /**
     * When the left hand of an assignment is an accessCall, replace with a set function.
     * 
     * @param accessCall
     * @param rightHand
     * @param data
     * @return
     */
    public CNode parseLeftHandAccessCall(AccessCallNode accessCall, MatlabNode rightHand, CNode rightHandC) {

        // Get name
        String accessCallName = accessCall.getName();

        // Get VariableType
        MatrixType arrayType = (MatrixType) data.getVariableType(accessCallName);

        if (arrayType == null) {
            throw new MatlabToCException("Array '" + accessCallName + "' is being set without "
                    + "having been initiallized before (e.g., with a zeros function)", data);
        }

        // Check if variable is a matrix
        if (!MatrixUtils.isMatrix(arrayType)) {
            SpecsLogs.msgInfo(" -> " + data.getErrorMessage());
            SpecsLogs.warn(" Array set to variable " + accessCallName + " of type " + arrayType
                    + ", which was not inferred as an array.\n"
                    + " Check if there is enough information to infer an array "
                    + "(e.g., input vector that generates an array, declaration in aspect file).");
            return CNodeFactory.newLiteral("NULL");
        }

        // Get indexes
        List<MatlabNode> mIndexes = accessCall.getArguments();

        // Left-hand colon accesses should have been eliminated at this point
        MatlabNode operator = MatlabToCRulesUtils.isSimpleColonAccess(mIndexes);
        if (operator != null) {
            SpecsLogs.msgInfo(" ! Left-hand not eliminated at this point, check this");
            return parseColonArraySet(accessCallName, arrayType, operator, rightHandC, data);
        }

        // Check if there is colon notation, or colons in the left hand
        // UPDATE: hasColon might never be true, since there is a transformation being done previously which removes
        // colons from the left hand of an assignment
        boolean hasColon = MatlabToCRulesUtils.hasColon(accessCall);
        if (hasColon) {
            // Check if there is an access call on the right hand side
            rightHand = rightHand.normalize();

            // If right hand is not and identifier, transform it into and identifier
            if (!(rightHand instanceof IdentifierNode)) {
                String tempName = data.nextTempVarName();
                MatlabNode tempMat = MatlabNodeFactory.newIdentifier(tempName);

                // - create matlab for first statement
                AssignmentSt firstAssign = StatementFactory.newAssignment(tempMat, rightHand);

                // - call converter
                CNode firstInst = CodeBuilderUtils.matlabToC(firstAssign, data);

                AssignmentSt secondAssign = StatementFactory.newAssignment(accessCall, tempMat);

                // - call converter (recursively). The execution path will pass on this function again,
                // but right hand will be an identifier
                CNode secondInst = CodeBuilderUtils.matlabToC(secondAssign, data);

                CNode block = CNodeFactory.newBlock(firstInst, secondInst);
                if (block != null) {
                    return block;
                }
            }

            assert rightHand instanceof IdentifierNode;

            CNode block = new SetWithColon(data).newInstance(accessCall, (IdentifierNode) rightHand);
            if (block != null) {
                return block;
            }
        }

        // This is here just for compatibility, this function is VERY slow
        Collection<Integer> indexesWithColonNotation = MatlabToCRulesUtils.getIndexesWithColonNotation(mIndexes);
        if (!indexesWithColonNotation.isEmpty()) {
            SpecsLogs.warn("USING WILDCARD SET:" + accessCall.getCode());
            return parseWildcardArraySet(accessCallName, arrayType, mIndexes, indexesWithColonNotation, rightHandC,
                    data);
        }

        // Convert to CTokens
        List<CNode> cIndexes = SpecsFactory.newArrayList();
        for (MatlabNode mIndex : mIndexes) {
            CNode cIndex = TokenRules.convertTokenExpr(mIndex, data);
            cIndexes.add(cIndex);
        }

        // If right hand is a matrix, get the first element
        // HACK: Rethink how arrays are being accessed
        // Check if indexes are matrixes

        boolean isMatrix = false;
        for (CNode index : cIndexes) {
            if (MatrixUtils.isMatrix(index.getVariableType())) {
                isMatrix = true;
                break;
            }
        }

        if (isMatrix) {
            boolean isScalar = !MatrixUtils.isMatrix(rightHandC.getVariableType());

            SetMultiple setMultiple = new SetMultiple(cIndexes.size(), isScalar, MFileProvider.getEngine());

            List<CNode> args = SpecsFactory.newArrayList();
            args.add(CNodeFactory.newVariable(accessCallName, arrayType));
            args.addAll(cIndexes);
            args.add(rightHandC);

            CNode fcall = MFileProvider.getFunctionCall(setMultiple, args, data.getProviderData().newInstance());

            return fcall;
        }
        // Use single array access
        CNode node = parseSingleArraySet(accessCallName, arrayType, cIndexes, rightHandC, data);

        return node;

    }

    /**
     * @param accessCallName
     * @param arrayType
     * @param mIndexes
     * @param indexesWithColon
     * @param value
     * @param data
     * @return
     */
    private static CNode parseWildcardArraySet(String accessCallName, VariableType arrayType,
            List<MatlabNode> mIndexes, Collection<Integer> indexesWithColon, CNode value, MatlabToCFunctionData data) {

        int totalIndexes = mIndexes.size();

        // Build token for matrix to set
        CNode matrixVar = CNodeFactory.newVariable(accessCallName, arrayType);

        // Convert indexes to CTokens
        List<CNode> cIndexes = SpecsFactory.newArrayList();
        for (int i = 0; i < mIndexes.size(); i++) {

            // Skip in its a colon notation index
            if (indexesWithColon.contains(i)) {
                continue;
            }

            // Convert index
            MatlabNode mIndex = mIndexes.get(i);
            CNode cIndex = TokenRules.convertTokenExpr(mIndex, data);

            cIndexes.add(cIndex);
        }

        // Build input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        inputTypes.add(arrayType);
        for (CNode cIndex : cIndexes) {
            inputTypes.add(cIndex.getVariableType());
        }
        inputTypes.add(value.getVariableType());

        ProviderData dataInput = ProviderData.newInstance(data.getProviderData().newInstance(), inputTypes);

        // Build function call
        FunctionInstance getInstance = SetWildcard.newSetWildcard(indexesWithColon, totalIndexes, dataInput);

        List<CNode> args = SpecsFactory.newArrayList();
        args.add(matrixVar);
        args.addAll(cIndexes);
        args.add(value);

        return FunctionInstanceUtils.getFunctionCall(getInstance, args);

    }

    /**
     * @param rightHandC
     * @param data
     * @param accessCallName
     * @param arrayType
     * @param cIndexes
     * @return
     */
    private CNode parseSingleArraySet(String matrixName, MatrixType matrixType, List<CNode> indexesCToken,
            CNode rightHandC, MatlabToCFunctionData data) {

        VariableType rightHandType = rightHandC.getVariableType();
        if (rightHandType instanceof MatrixType) {
            MatrixType rightHandMatrixType = (MatrixType) rightHandType;
            TypeShape rightHandShape = rightHandMatrixType.matrix().getShape();
            if (rightHandShape.isFullyDefined()) {
                if (rightHandShape.getNumElements() != 1) {
                    throw new CodeGenerationException(
                            "Found assignment with matrix with more than one element on the right side. This case is not yet supported.");
                }
            } else {
                // TODO: What about partially defined matrices, such as [2, -1]? Maybe we could check for those too
                throw new RuntimeException("Needs a matrix with a single element, or a scalar");
                // LoggingUtils.msgWarn("Assuming matrix has single element");
            }

            CNode zeroNode = CNodeFactory.newCNumber(0);
            rightHandC = cnodes.newFunctionCall(rightHandMatrixType.matrix().functions().get(), rightHandC, zeroNode);
        }

        CNode matrixVarToken = CNodeFactory.newVariable(matrixName, matrixType);

        // Build inputs
        List<CNode> inputs = SpecsFactory.newArrayList();
        inputs.add(matrixVarToken);

        // Subtract one from the index, to convert from MATLAB to C index
        CNode numberOne = CNodeFactory.newCNumber(1);
        for (CNode arg : indexesCToken) {
            List<CNode> args = Arrays.asList(arg, numberOne);
            CNode minusOp = cnodes.newFunctionCall(MatlabOp.Subtraction.getMatlabFunction(), args);
            inputs.add(minusOp);
        }

        inputs.add(rightHandC);

        // Get function instance set(variable, index(es), value)
        // MatrixImplementation mImpl = MatrixUtilsV2.getImplementation(matrixType);

        // InstanceProvider setProvider = MatrixFunction.SET.getProvider(mImpl);
        InstanceProvider setProvider = matrixType.matrix().functions().set();
        CNode newNode = cnodes.newFunctionCall(setProvider, inputs);

        return newNode;
    }

    /**
     * @param accessCallName
     * @param arrayType
     * @param operator
     * @param data
     * @return
     */
    private CNode parseColonArraySet(String matrixName, MatrixType matrixType, MatlabNode operator, CNode values,
            MatlabToCFunctionData data) {

        MatlabToCRulesUtils rulesUtils = new MatlabToCRulesUtils(data);

        // MatrixImplementation impl = MatrixUtils.getImplementation(matrixType);

        // Build Variable
        CNode arrayVar = CNodeFactory.newVariable(matrixName, matrixType);

        List<CNode> offLen = rulesUtils.parseOffsetAndLength(matrixType.usesDynamicAllocation(), operator);

        List<CNode> args = Arrays.asList(arrayVar, offLen.get(0), offLen.get(1), values);

        // return cnodes.newFunctionCall(MatrixIndependentProvider.SET_ROW, args);
        return cnodes.newFunctionCall(pdata -> new SetRow(pdata).create(), args);
    }

    /**
     * Tries to build a FunctionCall instruction. Returns null if it could not.
     * 
     * @param cLeftHandList
     * @param rhExpression
     * @param data
     * @return
     */
    public static CNode buildFunctionCallAssign(List<CNode> cLeftHandList, CNode rhExpression,
            MatlabToCFunctionData data) {

        // Check if return type of right hand is a view
        VariableType rhType = rhExpression.getVariableType();
        if (MatrixUtils.usesDynamicAllocation(rhType)) {
            if (MatrixUtils.isView(rhType)) {
                return null;
            }
        }

        // Get function call
        // CNode functionCall = CNodeUtils.getToken(rhExpression, CNodeType.FunctionCall);
        Optional<FunctionCallNode> functionCallTry = rhExpression.cast(FunctionCallNode.class);

        // if (functionCall == null) {
        if (!functionCallTry.isPresent()) {
            return null;
        }

        FunctionCallNode functionCall = functionCallTry.get();

        // Get number of outs-as-ins
        // FunctionInstance instance = CTokenContent.getFunctionInstance(functionCall);
        FunctionInstance instance = functionCall.getFunctionInstance();

        if (instance.getFunctionType().getNumOutsAsIns() == 0) {
            return null;
        }

        // Update function outputs-as-inputs (in case right hand is a function call)
        AssignUtils.updateOutputAsInputArgs(functionCall, cLeftHandList, data);

        // Create FunctionCall instruction
        return CNodeFactory.newInstruction(InstructionType.FunctionCall, functionCall);
    }

    public static TokenTester newFunctionAssignmentTester() {
        return new TokenTester() {

            @Override
            public boolean test(TreeNode<?> token) {
                // Check if token is a token with parent
                if (!(token instanceof MatlabNode)) {
                    return false;
                }

                MatlabNode mToken = (MatlabNode) token;

                // If parenthesis, run the test in the parent
                if (mToken instanceof ParenthesisNode) {
                    return test(mToken.getParent());
                }

                if (!(mToken instanceof AssignmentSt)) {
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * Builds the assignment token, minding conversions between types on both hands.
     * 
     * 
     * @param leftHand
     * @param rightHand
     * @return
     */
    public CNode buildAssignment(CNode leftHand, CNode rightHand) {

        // Add cast
        CNode newRightHand = rightHand.getVariableType().conversion()
                .to(rightHand, leftHand.getVariableType());
        assert newRightHand != null : "Can't cast " + rightHand.getCode() + " (" + rightHand.getVariableType() + ") to "
                + leftHand.getVariableType();

        // Build assignment
        return CNodeFactory.newAssignment(leftHand, newRightHand);
    }

}
