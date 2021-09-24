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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CMatrixUtils.CMatrix;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationFunctions;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ColonNotationNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.GetWithColon;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Functions.CustomFunctions.AccessMatrixValues;
import org.specs.MatlabToC.Functions.CustomFunctions.GetWildcard;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;

import pt.up.fe.specs.symja.SymjaPlusUtils;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabToCRulesUtils {

    private final MatlabToCFunctionData data;
    private final GenericInstanceBuilder helper;

    public MatlabToCRulesUtils(MatlabToCFunctionData data) {
        this.data = data;
        helper = new GenericInstanceBuilder(data.getProviderData());

    }

    private static final Collection<Class<? extends MatlabNode>> OPERATOR_BYPASS_SET = Arrays.asList(
            ExpressionNode.class,
            ParenthesisNode.class);

    /**
     * Converts a Matrix Matlab token into a CMatrix.
     * 
     * @param token
     * @param data
     * @return
     * @throws MatlabToCException
     */
    public static CMatrix convertMatrix(MatrixNode token, MatlabToCFunctionData data) throws MatlabToCException {

        // Build the CMatrix
        CMatrix cMatrix = new CMatrix();

        // Get the rows
        List<RowNode> rows = token.getRows();
        for (RowNode row : rows) {

            // Build a single row
            List<CNode> cRow = SpecsFactory.newArrayList();

            for (MatlabNode element : row.getChildren()) {
                CNode cElement = TokenRules.convertTokenExpr(element, data);
                cRow.add(cElement);
            }

            cMatrix.addRow(cRow);
        }

        return cMatrix;
    }

    /**
     * Transforms array accesses into function calls.
     * 
     * <p>
     * Although MATLAB is COLUMN-MAJOR and C is ROW-MAJOR, linear indexes (when there is only one subscript) are
     * equivalent in the generated C, since the data is stored and retrieved following C's convention. In practice, this
     * means that a matrix representation in memory in C will be transposed when compared to the same representation in
     * MATLAB. However, this is transparent for the user.
     * 
     * <p>
     * Subscripts are also equivalent, although the functions that translates the subscripts to linear indexes takes
     * into account that C is ROW-MAJOR.
     * 
     * @param token
     * @param forcePointerOutput
     * @param data
     * @param setup
     *            TODO
     * @return
     */
    CNode parseArrayAccess(AccessCallNode token, boolean forcePointerOutput) {

        // Get name
        String accessCallName = token.getName();

        // Get VariableType
        VariableType arrayType = data.getVariableType(accessCallName);

        // Get indexes
        List<MatlabNode> mIndexes = token.getArguments();

        boolean hasColon = MatlabToCRulesUtils.hasColon(token);
        boolean isStaticArray = !MatrixUtils.usesDynamicAllocation(arrayType);

        // It is safe if hasColon is erroneously false, in that case it loses an optimization opportunity

        CNode optimizedColonArrayAccess = null;
        if (hasColon && !isStaticArray) {
            // Check if left hand is identifier
            // System.out.println("HAS COLON...");
            // System.out.println(token);
            // System.out.println("GET COLON NOT BEING USED NOW");

            // CNode fcall = new GetWithColon(data).newInstance(accessCallName, mIndexes);
            optimizedColonArrayAccess = new GetWithColon(data).newInstance(accessCallName, mIndexes);
            /*
            if (fcall != null) {
            return fcall;
            }
            */

        }

        if (hasColon && isStaticArray) {
            SpecsLogs.warn("OPTIMIZATION OPPORTUNITY: GetWithColon of a static array");
        }

        // If index is a colon operation, use multi-array access
        MatlabNode operator = isSimpleColonAccess(mIndexes);
        CNode colonArrayAccess = null;
        if (operator != null) {
            if (!(arrayType instanceof MatrixType)) {
                SpecsLogs.warn("NOT YET IMPLEMENTED FOR SCALARS");
            }
            // System.out.println("HERE 1:\n" + token);

            colonArrayAccess = parseColonArrayAccess(accessCallName, (MatrixType) arrayType, operator,
                    forcePointerOutput);
        }

        // If one of them is null and the other is not, return the one that is not null
        if (optimizedColonArrayAccess == null && colonArrayAccess != null) {
            return colonArrayAccess;
        }

        if (optimizedColonArrayAccess != null && colonArrayAccess == null) {
            return optimizedColonArrayAccess;
        }

        // optimizedColonArrayAccess is generally better, unless colonArrayAccess is a get_row_view_pointer
        if (optimizedColonArrayAccess != null && colonArrayAccess != null) {
            // System.out.println("OPTIMIZED:" + optimizedColonArrayAccess.getCode());
            // System.out.println("NORMAL:" + colonArrayAccess.getCode());

            if (colonArrayAccess.getCode().startsWith(TensorCreationFunctions.getRowViewPointerPrefix())) {
                return colonArrayAccess;
            }

            return optimizedColonArrayAccess;
        }

        // If any of the indexes is a ColonNotation, use getWildcard
        Collection<Integer> indexesWithColon = getIndexesWithColonNotation(mIndexes);
        if (!indexesWithColon.isEmpty()) {
            return parseWildcardArrayAccessV2(accessCallName, arrayType, mIndexes, indexesWithColon, data);
        }

        // Save current return types, and set return type to an integer, since we are going to convert indexes
        List<VariableType> previousReturnTypes = data.getAssignmentReturnTypes();
        data.setAssignmentReturnTypes(Arrays.asList(data.getNumerics().newInt()));

        // Convert to CTokens
        List<CNode> cIndexes = SpecsFactory.newArrayList();
        for (MatlabNode mIndex : mIndexes) {
            CNode cIndex = TokenRules.convertTokenExpr(mIndex, data);
            cIndexes.add(cIndex);
        }

        // Restore previous return types
        data.setAssignmentReturnTypes(previousReturnTypes);

        // Check if there is only one cIndex, and if it's type is not matrix
        if (cIndexes.size() == 1) {
            if (MatrixUtils.isMatrix(cIndexes.get(0).getVariableType())) {
                // Copy given indexes to a new array, maitaining the shape of the given index array
                System.out.println("INDEX NOT SuPORTED:\n" + cIndexes.get(0));
                throw new RuntimeException("General case for multi-access array not supported yet.");
            }
        }

        // Check if indexes are matrixes
        boolean isMatrix = false;
        for (CNode index : cIndexes) {
            if (MatrixUtils.isMatrix(index.getVariableType())) {
                isMatrix = true;
                break;
            }
        }

        if (isMatrix) {
            AccessMatrixValues accessMatrixValues = new AccessMatrixValues(cIndexes.size());

            List<CNode> args = SpecsFactory.newArrayList();
            args.add(CNodeFactory.newVariable(accessCallName, arrayType));
            args.addAll(cIndexes);

            // Set assignment return types, if set
            ProviderData pdata = data.getProviderData()
                    .createWithContext(data.getProviderData().getInputTypes());
            pdata.setOutputType(data.getAssignmentReturnTypes());
            return MFileProvider.getFunctionCall(accessMatrixValues, args, pdata);
        }

        // Use single array access
        CNode node = parseSingleArrayAccess(accessCallName, arrayType, cIndexes);

        /*
        if (MatlabProcessorUtils.toMFile(token).startsWith("X(matisse_idx)")) {
            System.out.println("RIGHT HAND M: " + MatlabProcessorUtils.toMFile(token));
            System.out.println("RIGHT HAND C SIZE: " + cIndexes.get(0).getCode());
            System.out.println("NODE C: " + node.getCode());
            System.out.println("ACCESSc: " + accessCallName);
            System.out.println("TYPE: " + arrayType);
        }
        */

        return node;
    }

    /**
     * @param mIndexes
     * @return
     */
    public static Collection<Integer> getIndexesWithColonNotation(List<MatlabNode> mIndexes) {
        Collection<Integer> colonIndexes = SpecsFactory.newLinkedHashSet();

        for (int i = 0; i < mIndexes.size(); i++) {
            MatlabNode mIndex = mIndexes.get(i);
            if (mIndex instanceof ColonNotationNode) {
                colonIndexes.add(i);
            }
        }

        return colonIndexes;
    }

    /**
     * @param accessCallName
     * @param arrayType
     * @param mIndexes
     * @param indexesWithColon
     * @param data
     * @return
     */
    private static CNode parseWildcardArrayAccessV2(String accessCallName, VariableType arrayType,
            List<MatlabNode> mIndexes, Collection<Integer> indexesWithColon, MatlabToCFunctionData data) {

        int totalIndexes = mIndexes.size();

        // Build token for input matrix
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

        // If only one index, use function that returns all values in a column vector
        FunctionInstance getInstance = null;
        // if (totalIndexes != 1) {

        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        inputTypes.add(arrayType);
        for (CNode cIndex : cIndexes) {
            inputTypes.add(cIndex.getVariableType());
        }

        ProviderData dataInput = ProviderData.newInstance(data.getProviderData(), inputTypes);

        getInstance = GetWildcard.newGetWildcard(indexesWithColon, totalIndexes, dataInput);

        List<CNode> args = SpecsFactory.newArrayList();
        args.add(matrixVar);
        args.addAll(cIndexes);

        return FunctionInstanceUtils.getFunctionCall(getInstance, args);
    }

    /**
     * @param mIndexes
     * @return
     */
    public static MatlabNode isSimpleColonAccess(List<MatlabNode> mIndexes) {
        // Check if only one index (colon operation)
        if (mIndexes.size() != 1) {
            return null;
        }

        // Check if operator
        // Optional<OperatorNode> operatorTry = TokenUtils.getToken(mIndexes.get(0), OperatorNode.class,
        // MatlabToCRulesUtils.OPERATOR_BYPASS_SET);
        MatlabNode normalizedOp = mIndexes.get(0).normalize(MatlabToCRulesUtils.OPERATOR_BYPASS_SET);
        if (!(normalizedOp instanceof OperatorNode)) {
            return null;
        }

        // if (!operatorTry.isPresent()) {
        // return null;
        // }

        // OperatorNode operator = operatorTry.get();
        OperatorNode operator = (OperatorNode) normalizedOp;

        // Check if colon operator
        if (operator.getOp() != MatlabOperator.Colon) {
            return null;
        }

        // If it has two children, return operator
        if (operator.getNumChildren() == 2) {
            return operator;
        }

        // If it has more than three children, return null
        if (operator.getNumChildren() > 3 || operator.getNumChildren() < 2) {
            return null;
        }

        // If it has three children, check if children in the middle is the constant '1'
        MatlabNode step = operator.getChildren().get(1);
        if (!(step instanceof MatlabNumberNode)) {
            return null;
        }

        MatlabNumber number = ((MatlabNumberNode) step).getNumber();
        if (number.getFloatValue() != 1.0) {
            return null;
        }

        // Build new Colon operator without middle step argument, which is 1
        MatlabNode normalizedColon = MatlabNodeFactory.newOperator(":");
        normalizedColon.addChild(operator.getChildren().get(0));
        normalizedColon.addChild(operator.getChildren().get(2));

        // return operator;
        return normalizedColon;
    }

    private CNode parseSingleArrayAccess(String accessCallName, VariableType arrayType, List<CNode> cIndexes) {

        // Build Variable
        Variable arrayVar = new Variable(accessCallName, arrayType);
        CNode arrayVarToken = CNodeFactory.newVariable(arrayVar);

        // If scalar, just return the token
        if (ScalarUtils.isScalar(arrayType)) {
            return arrayVarToken;
        }

        // If no indexes, return the matrix itself
        if (cIndexes.isEmpty()) {
            return arrayVarToken;
        }

        // Build list of inputs
        List<CNode> inputs = SpecsFactory.newArrayList();
        inputs.add(arrayVarToken);

        // Subtract one from each index
        CNode numberOne = CNodeFactory.newCNumber(1);

        // Operation is done over indexes, output type should be an integer
        List<VariableType> previousTypes = helper.getData().getOutputTypes();
        helper.getData().setOutputType(Arrays.asList(helper.getNumerics().newInt()));

        for (CNode arg : cIndexes) {
            List<CNode> args = Arrays.asList(arg, numberOne);
            CNode minusOp = helper.getFunctionCall(MatlabOp.Subtraction.getMatlabFunction(), args);
            inputs.add(minusOp);
        }

        // Restore previous type
        helper.getData().setOutputType(previousTypes);

        InstanceProvider getProvider = MatrixUtils.getMatrix(arrayType).functions().get();
        CNode fCall = helper.getFunctionCall(getProvider, inputs);

        return fCall;
    }

    /**
     * @param accessCallName
     * @param arrayType
     * @param operator
     * @param forcePointerOutput
     * @param data
     * @return
     */
    // private static CToken parseColonArrayAccess(String accessCallName, VariableType arrayType,
    // MatlabToken operator, MatlabToCFunctionData data) {

    private CNode parseColonArrayAccess(String accessCallName, MatrixType arrayType, MatlabNode operator,
            boolean forcePointerOutput) {

        // MatrixImplementation impl = MatrixUtils.getImplementation(arrayType);

        // Build Variable
        Variable arrayVar = new Variable(accessCallName, arrayType);
        CNode arrayVarToken = CNodeFactory.newVariable(arrayVar);

        List<CNode> offsetAndLength = parseOffsetAndLength(arrayType.usesDynamicAllocation(), operator);

        // List<CToken> args = Arrays.asList(arrayVarToken, offset, length);
        CNode offset = offsetAndLength.get(0);
        CNode length = offsetAndLength.get(1);

        List<CNode> args = Arrays.asList(arrayVarToken, offset, length);
        List<VariableType> argTypes = CNodeUtils.getVariableTypes(args);

        InstanceProvider provider = getRowViewProvider(accessCallName, forcePointerOutput);

        // Create new view from matrix, where data will be the first child of the colon
        // FunctionInstance getInstance = helper.getInstance(MatrixFunction.GET_ROW_VIEW.getProvider(impl), argTypes);
        FunctionInstance getInstance = helper.getInstance(provider, argTypes);

        return FunctionInstanceUtils.getFunctionCall(getInstance, args);
    }

    private InstanceProvider getRowViewProvider(String accessCallName, boolean forcePointerOutput) {

        // If left hand, always use pointer, otherwise output data will not be saved to original array, but to a copy
        if (data.isLeftHandAssignment()) {
            if (forcePointerOutput) {
                return MatrixFunction.GET_ROW_VIEW_POINTER_OUT_POINTER;
            }

            return MatrixFunction.GET_ROW_VIEW_POINTER;
        }

        // If optimization is not active, always use copy
        if (!MatlabToCKeys.isActive(data.getSettings(), MatisseOptimization.UsePointerViewsAlways)) {
            return MatrixFunction.GET_ROW_VIEW;
        }

        // Check case where access call is done on the right-hand, over a local variable
        // A case such as I = I_temp, where I is output and I_temp is local, before the function ends I_temp will be
        // freed and the values of I will be lost
        if (useCopyTest(accessCallName)) {
            // Return view by copy
            return MatrixFunction.GET_ROW_VIEW;
        }
        /*
        	//
        	if (data.getSetup().isActive(MatisseOptimization.UsePointerViewsAlways)) {
        	    return MatrixFunction.GET_ROW_VIEW_POINTER;
        
        	}
        */
        // Try to use pointer whenever possible
        // As default, return view with copy
        if (forcePointerOutput) {
            return MatrixFunction.GET_ROW_VIEW_POINTER_OUT_POINTER;
        }
        return MatrixFunction.GET_ROW_VIEW_POINTER;
    }

    /**
     * Check case where access call is done on the right-hand, over a local variable A case such as I = I_temp, where I
     * is output and I_temp is local, before the function ends I_temp will be freed and the values of I will be lost.
     * 
     * @param accessCallName
     * @param data2
     * @return
     */
    private boolean useCopyTest(String accessCallName) {
        // If left hand return
        if (data.isLeftHandAssignment()) {
            return false;
        }

        // Check if access call is a local variable
        if (!data.isLocalVariable(accessCallName)) {
            return false;
        }

        // Check if any of the ids is an output
        boolean hasOutputOnLeftHand = false;
        for (String outputName : data.getLeftHandIds()) {
            /*
            if (!data.hasType(outputName)) {
            
            // System.out.println("NAME:" + outputName);
            // System.out.println("IS FUNCTION CALL:" + data.isFunctionCall(outputName));
            LoggingUtils.msgWarn("Conservative get_row with copy:" + accessCallName + " -> "
            	+ data.getLeftHandIds());
            return true;
            }
            */

            if (data.getOutputNames().contains(outputName)) {
                hasOutputOnLeftHand = true;
                break;
            }
        }

        // Check if any of the ids in the left hand is an output
        if (!hasOutputOnLeftHand) {
            return false;
        }

        SpecsLogs.msgLib("useCopyTest: Using get_view with copy for: " + accessCallName + " -> "
                + data.getLeftHandIds());
        return true;
    }

    /**
     * @param colonOperator
     * @param data
     * @return
     */
    // public static List<CToken> parseOffsetAndLength(MatrixImplementation impl,
    public List<CNode> parseOffsetAndLength(boolean usesDynamicAllocation, MatlabNode colonOperator) {

        List<CNode> offsetAndLength = SpecsFactory.newArrayList();

        // Constant one
        CNode numberOne = CNodeFactory.newCNumber(1);

        // Convert first child, to build offset
        CNode startIndex = TokenRules.convertTokenExpr(colonOperator.getChildren().get(0), data);

        // Build offset
        // CToken offset = COperator.Subtraction.getFunctionCall(true, startIndex, numberOne);
        CNode offset = helper.getFunctionCall(COperator.Subtraction.getProvider(true), startIndex, numberOne);

        offsetAndLength.add(offset);

        // Convert second child, to build length
        CNode endIndex = TokenRules.convertTokenExpr(colonOperator.getChildren().get(1), data);

        CNode length = getLengthToken(startIndex, endIndex);

        // Parse length in case the implementation is declared, and it does not have a constant
        length = parseLength(usesDynamicAllocation, length, data);

        offsetAndLength.add(length);

        return offsetAndLength;
    }

    /**
     * (endIndex - startIndex) + 1
     * 
     * @param startIndex
     * @param endIndex
     * @param providerData
     * @return
     */
    // public static CToken getLengthToken(CToken startIndex, CToken endIndex) {
    public CNode getLengthToken(CNode startIndex, CNode endIndex) {

        // Constant one
        CNode numberOne = CNodeFactory.newCNumber(1);

        // endIndex - startIndex
        // CToken endStart = COperator.Subtraction.getFunctionCall(true, endIndex, startIndex);
        CNode endStart = helper.getFunctionCall(COperator.Subtraction.getProvider(true), endIndex, startIndex);

        // (endIndex - startIndex) + 1
        // return COperator.Addition.getFunctionCall(true, endStart, numberOne);
        CNode length = helper.getFunctionCall(COperator.Addition.getProvider(true), endStart, numberOne);

        return length;
    }

    /**
     * @param impl
     * @param length
     * @param data
     * @return
     */
    private static CNode parseLength(boolean usesDynamicAllocation, CNode length, MatlabToCFunctionData data) {

        // Check if declared implementation
        // if (impl != MatrixImplementation.DECLARED) {
        // Check if uses dynamic allocation
        if (usesDynamicAllocation) {
            return length;
        }

        // Check if does not have constant
        if (ScalarUtils.hasConstant(length.getVariableType())) {
            return length;
        }

        // Build constant table
        Map<String, String> constants = buildConstantTable(length);

        // Check if expression can be solved
        String expression = CodeGeneratorUtils.expressionCode(length);

        // String simplified = SymjaPlusUtils
        // .simplify(CodeGeneratorUtils.tokenCode(length), constants);
        // System.out.println("DASDASDAS");
        String simplified = SymjaPlusUtils.simplify(expression, constants);
        if (simplified == null) {
            throw new MatlabToCException("When using declared arrays, length of multiple access must be constant",
                    data);
        }

        // Try to parse simplified into an integer
        Number lengthNumber = SpecsStrings.parseNumber(simplified, false);
        // Integer lengthInt = ParseUtils.parseInteger(simplified);

        // if (lengthInt == null) {
        if (lengthNumber == null) {
            // return length;
            throw new MatlabToCException("Could not evaluate length of multiple access " + "to an integer ("
                    + simplified + ")", data);
        }

        return CNodeFactory.newCNumber(lengthNumber.intValue());
    }

    private static Map<String, String> buildConstantTable(CNode length) {
        Map<String, String> constants = SpecsFactory.newHashMap();

        List<VariableNode> vars = length.getDescendantsAndSelf(VariableNode.class);
        for (VariableNode cvar : vars) {
            Variable var = cvar.getVariable();

            // Skip variable, if it does not have constant
            if (!ScalarUtils.hasConstant(var.getType())) {
                continue;
            }

            // Get variable constant
            String constString = ScalarUtils.getConstantString(var.getType());

            // Add it to the table
            String previousValue = constants.put(var.getName(), constString);

            // Check if the same value
            if (previousValue == null) {
                continue;
            }

            if (!previousValue.equals(constString)) {
                SpecsLogs.warn("Variables with the same name '" + var.getName() + "' but different constants ("
                        + previousValue + " and " + constString + ")");
            }
        }

        return constants;
    }

    /**
     * TODO: Correct bug after ARRAY 2015 results (Issue #13)
     * 
     * @param accessCall
     * @return
     */
    public static boolean hasColon(MatlabNode token) {
        // Check for colon notation
        List<ColonNotationNode> colonNotation = token.getDescendantsAndSelf(ColonNotationNode.class);
        if (!colonNotation.isEmpty()) {
            return true;
        }

        List<OperatorNode> operators = token.getDescendantsAndSelf(OperatorNode.class);
        for (OperatorNode operator : operators) {
            if (operator.getOp() == MatlabOperator.Colon) {
                return true;
            }
        }

        return false;
    }

}
