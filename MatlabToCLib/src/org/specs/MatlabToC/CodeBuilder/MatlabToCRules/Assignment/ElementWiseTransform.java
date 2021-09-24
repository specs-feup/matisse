/**
 * Copyright 2014 SPeCS.
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
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.SimpleAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.TokenRules;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules.GeneralStatementRules;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.classmap.ClassSet;
import pt.up.fe.specs.util.collections.HashSetString;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Performs the following transformations:<br>
 * 1. Convert Element Wise operations into for, all operations are element-wise and there is more than one operation.
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ElementWiseTransform {

    private static final ClassSet<MatlabNode> LEGAL_SET;

    static {
        LEGAL_SET = new ClassSet<>();

        ElementWiseTransform.LEGAL_SET.add(SimpleAccessCallNode.class);
        ElementWiseTransform.LEGAL_SET.add(IdentifierNode.class);
        ElementWiseTransform.LEGAL_SET.add(MatlabNumberNode.class);

    }

    // Set<MType> legalSet = EnumSet.of(AccessCall, Identifier, MatlabNumber);
    private final MatlabNode originalStatement;
    private final MatlabNode leftHand;
    private final MatlabNode rightHand;
    private final MatlabToCFunctionData data;

    public ElementWiseTransform(MatlabNode statement, MatlabNode leftHand, MatlabNode rightHand,
            MatlabToCFunctionData data) {

        originalStatement = statement;
        this.leftHand = leftHand;
        this.rightHand = rightHand;
        this.data = data;
    }

    /**
     * 
     * @return
     */
    public Optional<CNode> apply() {
        // 1. Element-wise
        return applyElementWise();
    }

    private Optional<CNode> applyElementWise() {
        // Check left hand is an identifier
        // TODO: Only supporting the case where left-hand is identifier and not a range set
        if (!(leftHand instanceof IdentifierNode)) {
            return Optional.empty();
        }

        // Copy right hand and normalize
        MatlabNode rightHandCopy = rightHand.copy();
        rightHandCopy = Normalization.normalize(rightHandCopy);

        // Get AccessCalls and Operators
        List<AccessCallNode> accessCalls = rightHandCopy.getDescendantsAndSelf(AccessCallNode.class);

        // Check if all function calls are element wise
        if (!areElementWise(accessCalls, data)) {
            return Optional.empty();
        }

        // Check that all tokens are either other calls, operators or Identifiers
        // Set<MType> legalSet = EnumSet.of(AccessCall, Identifier, MatlabNumber);

        for (MatlabNode token : rightHandCopy.getChildren()) {
            // if (!legalSet.contains(token.getType())) {
            if (!ElementWiseTransform.LEGAL_SET.contains(token)) {
                // LoggingUtils.msgWarn("Not optimizing this case:\n" + token);
                return Optional.empty();
            }
        }

        // Check that identifier arguments of access calls are scalars
        List<IdentifierNode> ids1 = rightHandCopy.getDescendants(IdentifierNode.class);
        for (IdentifierNode id : ids1) {
            // Parent must be an access call
            if (!(id.getParent() instanceof AccessCallNode)) {
                continue;
            }

            AccessCallNode parent = (AccessCallNode) id.getParent();

            // Must not be a function call
            if (data.isFunctionCall(parent.getName())) {
                continue;
            }

            // Id must be an argument
            if (parent.getNameNode() == id) {
                continue;
            }

            VariableType type = data.getVariableType(id.getName());
            assert type != null;

            // Does not support inlining when argument of access call is a matrix
            if (!(type instanceof ScalarType)) {
                return Optional.empty();
            }

        }

        // Check if there is at least a matrix argument on the right side
        VariableType rightSideType = TokenRules.convertTokenExpr(rightHandCopy, data).getVariableType();
        if (!(rightSideType instanceof MatrixType)) {
            // System.out.println("NOT A MATRIX:" + rightHandCopy.getCode());
            return Optional.empty();
        }

        // Get Identifiers and corresponding types
        List<IdentifierNode> ids = rightHandCopy.getDescendantsAndSelf(IdentifierNode.class);
        String firstMatrixId = null;
        for (IdentifierNode id : ids) {
            String idString = id.getName();
            VariableType type = data.getVariableType(idString);
            if (type instanceof MatrixType) {
                firstMatrixId = idString;
                break;
            }
        }

        // If matrix not found, return
        if (firstMatrixId == null) {
            return Optional.empty();
        }

        // return null;

        // System.out.println("ELEMENT WISE: " + originalStatement.getCode());
        CNode transformed = transformToElementWise(rightHandCopy, firstMatrixId);
        // System.out.println("AFTER:" + transformed.getCode());

        return Optional.of(transformed);

    }

    /**
     * @param rightHandCopy
     * @param firstMatrixId
     * @return
     */
    private CNode transformToElementWise(MatlabNode rightHand, String firstMatrixId) {

        String indexName = "matisse_idx";

        assert leftHand instanceof IdentifierNode;

        String leftHandId = ((IdentifierNode) leftHand).getName();

        boolean addInitialization = !isOutputAnInput(leftHandId, rightHand);
        VariableType outputType = null;
        if (addInitialization) {
            outputType = TokenRules.convertTokenExpr(rightHand, data).getVariableType();
            // Normalize type, it will be used to indicate the type of a variable
            outputType = outputType.normalize();
        }

        // Add AccessCalls to identifiers
        rightHand = addAccessCalls(rightHand, indexName);

        // Add access call to left hand

        MatlabNode indexId = MatlabNodeFactory.newIdentifier(indexName);
        MatlabNode newLeftHand = MatlabNodeFactory.newSimpleAccessCall(leftHandId, indexId);

        String template = "for <idx>=1:numel(<id>)\n"
                + "<expr>\n"
                + "end";

        // Create expression that will be use inside the 'for'
        MatlabNode assign = StatementFactory.newAssignment(newLeftHand, rightHand);

        // Only add initialization if output id is NOT one of the inputs
        if (addInitialization) {

            // Add type of leftId to table, if not defined
            if (!data.hasType(leftHandId)) {
                // Determine type of expression

                data.addVariableType(leftHandId, outputType);
            }

            String createFunction = MatlabBuiltin.ZEROS.getName() + "(size(<id>))";

            if (MatlabToCKeys.isActive(data.getSettings(), MatisseOptimization.UseMatissePrimitives)) {
                createFunction = MatissePrimitive.NEW_ARRAY_FROM_MATRIX.getName() + "(<id>)";
            }

            // template = "<leftId> = zeros(size(<id>));\n" + template;
            template = "<leftId> = " + createFunction + ";\n" + template;
        }

        /*
        // Add dereferencing if pointer
        String matrixId = firstMatrixId;
        if (firstMatrixType.pointer().isPointer()) {
            matrixId = "(*" + matrixId + ")";
        }
        */

        Replacer forReplacer = new Replacer(template);

        forReplacer.replace("<leftId>", leftHandId);
        forReplacer.replace("<idx>", indexName);
        forReplacer.replace("<id>", firstMatrixId);
        // forReplacer.replace("<id>", matrixId);
        forReplacer.replace("<expr>", assign.getCode());

        // Transformed MATLAB
        List<StatementNode> forMatlab = new ArrayList<>();

        // Add comment with original code
        String comment = "MATISSE: inlined element-wise operations:\n"
                + originalStatement.getCode();
        forMatlab.addAll(StatementFactory.newCommentStatements(comment, data.getLineNumber()));

        // Convert to IR. Contains two children, a call to zeros, and block with the 'for'
        forMatlab.addAll(new MatlabParser().parse(forReplacer.toString()).getStatements());

        // Convert from MATLAB IR to CTokens
        List<CNode> ctokens = Lists.newArrayList();

        forMatlab.forEach(mtoken -> ctokens.add(GeneralStatementRules.convert(mtoken, data)));

        return CNodeFactory.newBlock(ctokens);
        // return GeneralStatementRules.convert(forMatlab, data);
    }

    private static boolean isOutputAnInput(String leftHandId, MatlabNode rightHand) {
        // Get all identifiers
        List<IdentifierNode> ids = rightHand.getDescendantsAndSelf(IdentifierNode.class);

        for (IdentifierNode id : ids) {

            if (id.getName().equals(leftHandId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param token
     * @param indexName
     * @return
     */
    private MatlabNode addAccessCalls(MatlabNode token, String indexName) {

        // If access call, only consider the arguments
        List<MatlabNode> children;
        if (token instanceof AccessCallNode) {
            children = ((AccessCallNode) token).getArguments();
        } else {
            children = token.getChildren();
        }

        // Add access calls to children
        children.forEach(child -> addAccessCalls(child, indexName));

        // If Identifier, transform into access call
        if (!(token instanceof IdentifierNode)) {
            return token;
        }

        String idString = ((IdentifierNode) token).getName();

        MatlabNode indexId = MatlabNodeFactory.newIdentifier(indexName);
        MatlabNode accessCall = MatlabNodeFactory.newSimpleAccessCall(idString, indexId);

        // Replace if id has parent
        if (token.hasParent()) {
            NodeInsertUtils.replace(token, accessCall);
        }

        return accessCall;
    }

    public static boolean areElementWise(List<AccessCallNode> accessCalls, MatlabToCFunctionData data) {
        HashSetString elementWiseFunctions = data.getSettings().get(MatlabToCKeys.ELEMENT_WISE_FUNCTIONS);

        for (AccessCallNode accessCall : accessCalls) {

            String name = accessCall.getName();

            // If a function, check if it is present in the element wise functions table
            if (data.isFunctionCall(name)) {

                // MTimes is a special case, it is element-wise if one of the elements is not a matrix
                /*
                if (name.equals("mtimes")) {
                boolean foundScalarType = false;
                for (MatlabNode arg : ((AccessCallNode) accessCall).getArguments()) {
                    // Identifier
                    if (arg instanceof IdentifierNode) {
                	System.out.println("ID:" + ((IdentifierNode) arg).getName());
                	System.out.println("TYPE:" + data.getVariableType(((IdentifierNode) arg).getName()));
                	VariableType idType = data.getVariableType(((IdentifierNode) arg).getName());
                
                	// If scalar type, no problem
                	if (idType instanceof ScalarType) {
                	    foundScalarType = true;
                	    break;
                	}
                
                	// If matrix type, check if
                	// If it is not a scalar, stop
                
                	continue;
                    }
                
                    // Access call
                    if (arg instanceof AccessCallNode) {
                
                    }
                
                    // Other case
                    LoggingUtils.msgWarn("Case not defined:" + type);
                }
                
                if (!foundScalarType) {
                    return false;
                }
                }
                */
                if (!elementWiseFunctions.contains(name)) {
                    return false;
                }
            }

            // If it is an element-wise function, there is no problem
            // If it is a matrix accesse, by default they are element wise. If they where more complicated (e.g.,
            // colon access) by this time they have been transformed into functions

        }

        return true;
    }
}
