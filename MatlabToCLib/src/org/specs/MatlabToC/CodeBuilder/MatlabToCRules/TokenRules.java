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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilderUtils;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilderUtils.TypeProperty;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIR.TypesOld.CMatrixUtils.CMatrix;
import org.specs.CIR.Utilities.ConstantUtils;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeUtils;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.AssignUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.Functions.MathFunctions.General.MatlabConstant;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Rules which can be applied to MatlabToken objects which are part of expressions.
 * 
 * @author Joao Bispo
 * 
 */
public class TokenRules {

    // private static final EnumMap<MType, BiFunction<MatlabNode, MatlabToCFunctionData, CNode>> opConversionRules;
    private static final BiFunctionClassMap<MatlabNode, MatlabToCFunctionData, CNode> opConversionRules;

    static {
        opConversionRules = new BiFunctionClassMap<>();

        TokenRules.opConversionRules.put(ParenthesisNode.class, TokenRules::parseParenthesis);
        TokenRules.opConversionRules.put(ExpressionNode.class, TokenRules::parseExpression);
        TokenRules.opConversionRules.put(OperatorNode.class, TokenRules::parseOperator);
        TokenRules.opConversionRules.put(IdentifierNode.class, TokenRules::parseIdentifier);
        TokenRules.opConversionRules.put(AccessCallNode.class, TokenRules::parseAccessCall);
        TokenRules.opConversionRules.put(MatlabNumberNode.class, TokenRules::parseMatlabNumber);
        TokenRules.opConversionRules.put(MatrixNode.class, TokenRules::parseMatrix);
        TokenRules.opConversionRules.put(MatlabCharArrayNode.class, TokenRules::parseMatlabString);

    }

    /**
     * @return the opConversionRules
     */
    public static BiFunctionClassMap<MatlabNode, MatlabToCFunctionData, CNode> getOpconversionrules() {
        return TokenRules.opConversionRules;
    }

    /**
     * @return
     */
    private static CNode parseMatlabString(MatlabCharArrayNode token, MatlabToCFunctionData data)
            throws MatlabToCException {

        return data.helper().newString(token.getString());

    }

    private static CNode parseExpression(ExpressionNode token, MatlabToCFunctionData data) throws MatlabToCException {

        CNode exprToken = convertTokenExpr(token.getSingleChild(), data);

        return exprToken;
    }

    /**
     * 
     * 
     * @return
     */
    private static CNode parseMatrix(MatrixNode token, MatlabToCFunctionData data) throws MatlabToCException {

        // Increment function call count
        data.incrementFunctionCallLevel();

        CNode matrixCreatorFunction = workMatrix(token, data);

        // Decrement function call count
        data.decrementFunctionCallLevel();

        return matrixCreatorFunction;
    }

    /**
     * @param token
     * @param data
     * @return
     */
    private static CNode workMatrix(MatrixNode token, MatlabToCFunctionData data) {
        // Matrix is made of rows. For each row, convert its elements
        // Parse Matlab Matrix into temporary CMatrix
        CMatrix cMatrix = MatlabToCRulesUtils.convertMatrix(token, data);

        // If CMatrix could not be created, given token does not
        // represent
        // a valid matrix
        if (cMatrix == null) {
            String msg = "Given token does not represent a valid Matlab matrix\n:"
                    + token.getCode();
            throw new RuntimeException(msg);
        }

        return ExpressionUtils.buildMatrixCreatorFunction(data, cMatrix);

    }

    /**
     * Converts a MatLab token to a CToken, in the context of an expression.
     * 
     * @param token
     * @param data
     * @return
     * @throws MatlabToCException
     */
    public static CNode convertTokenExpr(MatlabNode token, MatlabToCFunctionData data) throws MatlabToCException {
        return getOpconversionrules().apply(token, data);
    }

    /**
     * @return
     */
    private static CNode parseMatlabNumber(MatlabNumberNode token, MatlabToCFunctionData data)
            throws MatlabToCException {

        MatlabNumber matlabNumber = token.getNumber();

        // Check if number is not complex
        if (matlabNumber.isComplex()) {
            SpecsLogs.warn("Complex numbers not supported.");
            return null;
        }

        CNativeType defaultReal = getDefaultReal(data.getSettings());

        Number aNumber = SpecsStrings.parseNumber(matlabNumber.toMatlabString(), false);
        CNativeType numberType = data.getNumerics().newNumeric(aNumber, defaultReal);

        CNumber cnumber = CLiteral.newInstance(aNumber, numberType);

        CNode number = CNodeFactory.newCNumber(cnumber);

        return number;
    }

    private static CNativeType getDefaultReal(DataStore setup) {
        // Get default real
        VariableType defaultReal = setup.get(CirKeys.DEFAULT_REAL);

        // If CNativeType, return
        if (defaultReal instanceof CNativeType) {
            return (CNativeType) defaultReal;
        }

        // Return double as default real
        return new NumericFactory(setup).newDouble();
    }

    /**
     * @return
     */
    private static CNode parseAccessCall(AccessCallNode token, MatlabToCFunctionData data) throws MatlabToCException {

        // Increment function call count
        data.incrementFunctionCallLevel();

        CNode result = workAccessCall(token, data);

        // Decrement function call count
        data.decrementFunctionCallLevel();

        return result;
    }

    /**
     * @param token
     * @param data
     * @return
     */
    private static CNode workAccessCall(AccessCallNode token, MatlabToCFunctionData data) {
        MatlabToCRulesUtils rulesUtils = new MatlabToCRulesUtils(data);

        // Array access to variable
        if (!data.isFunctionCall(token.getName())) {
            return rulesUtils.parseArrayAccess(token, data.isForcePointerOutput());
        }

        // Check if parent is an assignment
        MatlabNode parent = NodeInsertUtils.getParent(token,
                AssignUtils.newFunctionAssignmentTester());
        while (parent instanceof ParenthesisNode) {
            parent = parent.getParent();
        }
        AssignmentSt assign = (AssignmentSt) parent;

        // Integer nargout = null;
        // As default, set number of outputs to 1
        Integer nargout = 1;
        if (assign != null) {
            // Get left hand
            // MatlabNode leftHand = StatementAccess.getAssignmentLeftHand(assign);
            MatlabNode leftHand = assign.getLeftHand();

            // Get output variables names
            List<String> outputNames = MatlabNodeUtils.getVariableNames(leftHand);

            nargout = outputNames.size();
        }

        // Get name
        String accessCallName = token.getName();

        // If variable with that name does not exist, assume it is a
        // function call and get arguments
        List<MatlabNode> functionArguments = token.getArguments();

        // If output type propagation is enabled, check if it should be disabled before converting input
        // arguments
        boolean oldPropagateOutputType = data.isPropagateOutputTypeToInputs();
        if (data.isPropagateOutputTypeToInputs()) {
            // Before converting arguments, check if output type should not be propagated
            InstanceProvider provider = data.getInstanceProvider(accessCallName);
            boolean propagateOutputType = provider.propagateOutputToInputs();
            if (!propagateOutputType) {
                data.setPropagateOutputTypeToInputs(false);
            }
        }

        List<CNode> args = Lists.newArrayList();
        List<VariableType> argTypes = Lists.newArrayList();
        for (MatlabNode argument : functionArguments) {
            // For each
            CNode argToken = convertTokenExpr(argument, data);

            VariableType varType = argToken.getVariableType();
            args.add(argToken);
            argTypes.add(varType);
        }

        // Propagate type to input
        // Will not work because there can be previous generated C nodes that already use the variable type
        // TODO: Make a working version of this?
        // It will need to go back the tree and change identifiers
        boolean changeVariableTypes = false;
        if (changeVariableTypes && data.isPropagateOutputTypeToInputs()) {
            FunctionType type = data.getInstanceProvider(accessCallName)
                    .getType(ProviderData.newInstance(argTypes, data.getSettings()));

            if (type.getOutputTypes().size() == 1) {
                VariableType outputType = type.getOutputTypes().get(0);
                Set<TypeProperty> outputTypeProps = new HashSet<>(
                        InstanceBuilderUtils.TypeProperty.getClass(outputType));

                // Change weak types, as long as properties are not lost
                for (int i = 0; i < args.size(); i++) {
                    VariableType argType = argTypes.get(i);
                    if (!argType.isWeakType()) {
                        continue;
                    }

                    // Check if output type preserves all properties

                    if (!outputTypeProps.containsAll(InstanceBuilderUtils.TypeProperty.getClass(argType))) {
                        continue;
                    }

                    CNode arg = args.get(i);

                    if (!(arg instanceof VariableNode)) {
                        continue;
                    }

                    String variableName = ((VariableNode) arg).getVariableName();

                    // TODO
                    // Can only change once
                    // if (data.isMarkedForChange(variableName)) {
                    // continue;
                    // }

                    data.setVariableType(variableName, outputType);

                    // Rebuild CNode
                    CNode argToken = convertTokenExpr(functionArguments.get(i), data);

                    VariableType newVarType = argToken.getVariableType();
                    args.set(i, argToken);
                    argTypes.set(i, newVarType);
                }

            }

        }

        // Restore value of output type propagation
        data.setPropagateOutputTypeToInputs(oldPropagateOutputType);

        // Set it inputs are identifiers or not

        // Set the number of output arguments
        // data.setCalledNargout(nargout);

        // Special case, function 'class'
        if (accessCallName.equals("class")) {
            return getClassString(args, data);
        }

        // Build function call
        FunctionCallNode functionCall = data.getFunctionCall(accessCallName, args, nargout);

        // Clear number of NArgouts before returning
        // data.clearNargout();

        return functionCall;
    }

    public static CNode getClassString(List<CNode> args, MatlabToCFunctionData data) {
        if (args.isEmpty()) {
            throw new RuntimeException("Function class needs at least one argument");
        }
        if (args.size() > 1) {
            SpecsLogs.msgInfo("Function 'class' only supports one argument, using first one");
        }

        // Get argument
        CNode arg = args.get(0);

        // Get numeric type
        // NumericType numericType = VariableTypeUtilsOld.getNumericType(DiscoveryUtils.getVarType(arg));
        VariableType scalarType = ScalarUtils.toScalar(arg.getVariableType());

        NumericClassName numericClass = MatlabToCTypesUtils.getNumericClass(scalarType);

        // Build CToken String
        return data.helper().newString(numericClass.getMatlabString());
    }

    /**
     * Converts directly from a Matlab type to a C type.
     * 
     * @param matlabType
     * @param cType
     * @param skipParent
     *            if true, does not create a node for the given token
     * @return
     */
    private static CNode parseParenthesis(ParenthesisNode token, MatlabToCFunctionData data) throws MatlabToCException {

        List<MatlabNode> mChildren = token.getChildren();

        if (mChildren.size() != 1) {
            throw new RuntimeException("MATLAB expression has a number of children different than one ('"
                    + mChildren.size() + "', does not know how to handle this case");
        }

        return CNodeFactory.newParenthesis(convertTokenExpr(mChildren.get(0), data));

    }

    private static CNode parseOperator(OperatorNode token, MatlabToCFunctionData data) throws MatlabToCException {

        // Increment function call count
        data.incrementFunctionCallLevel();

        CNode result = workOperator(token, data);

        // Decrement function call count
        data.decrementFunctionCallLevel();

        return result;
    }

    /**
     * @param token
     * @param data
     * @return
     */
    private static CNode workOperator(OperatorNode token, MatlabToCFunctionData data) {
        // Convert each child of the operation
        List<CNode> children = Lists.newArrayList();

        for (MatlabNode child : token.getOperands()) {
            CNode newChild = convertTokenExpr(child, data);
            children.add(newChild);
        }

        // Get GeneralFunction name that corresponds to the MatlabOperator
        String functionName = token.getOp().getFunctionName();

        // Build function call
        CNode operation = data.getFunctionCall(functionName, children);

        return CNodeFactory.newParenthesis(operation);
    }

    private static CNode parseIdentifier(IdentifierNode token, MatlabToCFunctionData data) throws MatlabToCException {

        String varName = token.getName();

        // We are inside an expression, variable must exist from
        // previous assignment, or as function input
        VariableType type = data.getVariableType(varName);

        // Get MATLAB constant corresponding to the given identifier
        MatlabConstant constant = MatlabConstant.getConstant(varName);

        // If type is null and the identifier is a Matlab constant, use the default type of the constant
        if (type == null && constant != null) {
            type = constant.getDefaultType(data.getNumerics(), data.getSettings().get(CirKeys.DEFAULT_REAL));
        }

        // If type is null, the identifier can be function 'nargin'
        if (varName.equals("nargin")) {
            int numInputs = data.getNumInputs();
            return CNodeFactory.newCNumber(numInputs);
        }

        if (varName.equals("nargout")) {
            int numOutputs = data.getOutputNames().size();
            return CNodeFactory.newCNumber(numOutputs);
        }

        // If gets here, variable is not defined
        if (type == null) {
            throw new MatlabToCException(
                    "Could not find type for variable '" + varName + "' (is it ever assigned?).",
                    data);
        }

        // If it is a constant, add initialization for the given type
        if (constant != null) {
            List<String> unparsedValues = constant.getValues();
            List<String> parsedValues = ConstantUtils.parseValues(type, unparsedValues);

            data.getInitializations().add(varName, parsedValues);
        }

        Variable var = new Variable(varName, type);

        return CNodeFactory.newVariable(var);
    }

}
