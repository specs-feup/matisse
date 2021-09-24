/**
 * Copyright 2013 SPeCS Research Group.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ColonNotationNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.StatementProcessor;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Transforms a statement like:<br>
 * 
 * <pre>
 * <code>U(m:n, j1) = X';</code>
 * </pre>
 * 
 * <p>
 * Into:
 * 
 * <pre>
 * <code>
 *  X_t = X';
 * 	for i=m:n
 * 		U(i, j1) = X_t(i-m+1);
 * 	end</code>
 * </pre>
 * 
 * 
 * @author Joao Bispo
 * 
 */
public class MultipleSetToForAdvanced {

    private final MatlabToCFunctionData data;

    public MultipleSetToForAdvanced(MatlabToCFunctionData data) {
        this.data = data;
    }

    public Optional<CNode> apply(AssignmentSt token) {

        // AssignmentSt assign = (AssignmentSt) token;

        // Get access calls
        List<AccessCallNode> accessCalls = token.getDescendants(AccessCallNode.class);

        // Detect if matrix access or function call
        List<AccessCallNode> matrixAccesses = accessCalls.stream()
                .filter(accessCall -> !data.isFunctionCall(accessCall.getName()))
                .collect(Collectors.toList());

        // No matrix accesses found, return
        if (matrixAccesses.isEmpty()) {
            return Optional.empty();
        }

        // Identifiers inside accesses cannot be matrices
        for (AccessCallNode accessCall : matrixAccesses) {
            boolean hasMatrixAccess = accessCall.getArguments().stream()
                    .filter(node -> node instanceof IdentifierNode)
                    .map(node -> ((IdentifierNode) node).getName())
                    .filter(name -> data.getVariableType(name) instanceof MatrixType)
                    .findFirst().isPresent();

            if (hasMatrixAccess) {
                return Optional.empty();
            }
        }

        // Function calls must be element wise
        if (!ElementWiseTransform.areElementWise(token.getRightHand().getDescendants(AccessCallNode.class), data)) {
            // System.out.println("HAS non-elementwise FUNCTIONS:" + token.getRightHand().getCode());
            return Optional.empty();
        }

        // Get matrix accesses with colon notation (should also look for multiple set? -> e.g., a(2:N) )
        List<AccessCallNode> accessesWithColonNotation = matrixAccesses.stream()
                .filter(matrixAccess -> matrixAccess.hasArgument(ColonNotationNode.class))
                .collect(Collectors.toList());

        if (accessesWithColonNotation.isEmpty()) {
            return Optional.empty();
        }

        MatlabNode leftNode = token.getLeftHand().normalizeExpr();

        // Type on left-hand must be defined by now
        // Optimization: If not defined, add call to zeros
        if (leftNode instanceof IdentifierNode) {
            if (!data.hasType(((IdentifierNode) token.getLeftHand()).getName())) {
                return Optional.empty();
            }
        }

        // Does not support MatrixNode on right-hand

        MatlabNode rightNode = token.getRightHand().normalizeExpr();
        if (rightNode instanceof MatrixNode) {
            return Optional.empty();
        }

        // For each matrix access, create a map with the indexes where they have colon operators
        Optional<ColonMap> loopIndexesTry = ColonMap.newInstance(accessesWithColonNotation);
        if (!loopIndexesTry.isPresent()) {
            SpecsLogs.msgInfo("Can we support? -> " + token.getCode());
            return Optional.empty();
        }

        ColonMap loopIndexes = loopIndexesTry.get();
        // System.out.println("COLON MAP:" + loopIndexes);

        Map<AccessCallNode, List<Integer>> colonMap = createColonMap(accessesWithColonNotation);
        if (colonMap == null) {
            SpecsLogs.msgInfo("Can we support? -> " + token.getCode());
            return Optional.empty();
        }

        ////// Passed tests, now will transform
        // System.out.println("TRANSFORMING: " + token.getCode());

        // Create a number of for loops equal to the total number of different indexes

        StatementProcessor processor = new StatementProcessor(data.getImplementationData().getLanguageMode(),
                data.getSettings());

        StringBuilder declarationCode = new StringBuilder();
        Map<Integer, String> indexToEndVar = new HashMap<>();

        // CInstructionList instructions = new CInstructionList();
        List<CNode> instructions = new ArrayList<>();
        instructions.add(
                processor.process("% MATISSE: Multiple set to 'for' loop\n% Original code: " + token.getCode(), data));

        Map<Integer, String> loopSizes = loopIndexes.getLoopSizes();
        // for (Entry<Integer, IdentifierNode> loopEndIndex : loopEndIndexes.entrySet()) {
        for (Entry<Integer, String> loopEndIndex : loopSizes.entrySet()) {
            String loopEndName = data.nextTempVarName();
            indexToEndVar.put(loopEndIndex.getKey(), loopEndName);

            // If only one argument, use numel; otherwise, use size with the index
            // String matrixName = loopEndIndex.getValue().getName();
            // Boolean isSingleArgument = singleArgument.get(matrixName);
            // assert isSingleArgument != null;

            // String matlabRightHand = createLoopEndRightEnd(matrixName, loopEndIndex.getKey(), isSingleArgument);
            String matlabRightHand = loopEndIndex.getValue();
            String matlabAssign = loopEndName + " = " + matlabRightHand;

            declarationCode.append(matlabAssign).append("\n");
            data.addVariableType(loopEndName, data.getNumerics().newInt());

        }

        instructions.add(processor.process(declarationCode.toString(), data));

        // Now using copy
        AssignmentSt assignmentCopy = (AssignmentSt) token.copy();
        if (assignmentCopy.getLeftHand() instanceof AccessCallNode) {

            String name = ((AccessCallNode) assignmentCopy.getLeftHand()).getName();
            if (!data.hasType(name)) {
                System.out.println("NEEDS TO BE INITIALLIZED:" + name);
            } else {
                // System.out.println("TYPE: " + name + " - " + data.getVariableType(name));
            }
        }
        boolean replaceLeftHand = shouldReplaceLeftHand(assignmentCopy);

        // Create temp matrix with zeros, replace on the assignment and add type
        String matrixTempName = null;
        String leftHandIdName = null;
        if (replaceLeftHand) {
            matrixTempName = data.nextTempVarName();

            // Get sizes
            String zerosArgs = indexToEndVar.values().stream()
                    .collect(Collectors.joining(", "));

            // Add type of temporary variable
            leftHandIdName = ((IdentifierNode) assignmentCopy.getLeftHand()).getName();
            VariableType type = data.getVariableType(leftHandIdName);
            assert type != null;
            assert type instanceof MatrixType;

            // Add temp var type
            TypeShape shape = TypeShape.newDimsShape(indexToEndVar.size());
            VariableType typeCopy = ((MatrixType) type).matrix().setShape(shape);
            // Remove reference status
            typeCopy = typeCopy.pointer().getType(false);
            data.addVariableType(matrixTempName, typeCopy);

            // Add zeros call to instructions
            String zerosCall = matrixTempName + " = zeros(" + zerosArgs + ");";
            instructions.add(processor.process(zerosCall, data));

            // Replace on assignment
            MatlabNode tempId = MatlabNodeFactory.newIdentifier(matrixTempName);
            NodeInsertUtils.replace(assignmentCopy.getLeftHand(), tempId);
        }

        // Adapt statement to for
        // Replace colon notation with appropriate indexes, and matrix identifiers with access calls

        StringBuilder matlabCode = new StringBuilder();

        // Create fors, one for each index var
        List<String> endVars = new ArrayList<>(indexToEndVar.values());
        // Reverse indexes, to "perform" loop interchange
        Collections.reverse(endVars);

        // Map to get indexes from endVar
        Map<String, Integer> indexVarToIndex = new HashMap<>();
        for (Entry<Integer, String> entry : indexToEndVar.entrySet()) {
            indexVarToIndex.put(entry.getValue(), entry.getKey());
        }

        Map<Integer, String> indexToIndexVar = new HashMap<>();
        for (String endVar : endVars) {
            String indexVar = data.nextTempVarName();
            // Instead of '1', use loopStart
            String forCode = "for " + indexVar + " = 1:" + endVar;
            matlabCode.append(forCode).append("\n");

            // Add indexVar to table
            indexToIndexVar.put(indexVarToIndex.get(endVar), indexVar);

            // Add type
            data.addVariableType(indexVar, data.getNumerics().newInt());
        }

        Optional<AssignmentSt> adaptedAssignTry = adaptAssign(assignmentCopy, indexToIndexVar);
        if (!adaptedAssignTry.isPresent()) {
            return Optional.empty();
        }

        AssignmentSt adaptedAssign = adaptedAssignTry.get();

        // System.out.println("ADAPTED: " + adaptedAssign.getCode());

        matlabCode.append(adaptedAssign.getCode()).append("\n");
        for (int i = 0; i < indexToEndVar.size(); i++) {
            matlabCode.append("end\n");
        }

        // Create end loop variables
        // String data.nextTempVarName();
        // ForNodes
        // System.out.println("COLON MAP:" + colonMap);
        // System.out.println("NUMBER FORS:" + numberFors);
        // System.out.println("LOOP INDEXES:" + loopEndIndexes);
        CNode assignC = processor.process(matlabCode.toString(), data);
        // System.out.println("C CODE:\n" + assignC.getCode());
        instructions.add(assignC);

        if (replaceLeftHand) {
            String assign = leftHandIdName + " = " + matrixTempName;
            instructions.add(processor.process(assign, data));

        }

        CNode snippet = CNodeFactory.newBlock(instructions);
        // System.out.println("MATLAB:" + matlabCode.toString());
        // System.out.println("SNIPPET:" + snippet.getCode());
        // System.out.println("MULTIPLE SET ADVANCED WIP -> " + token.getCode());

        return Optional.of(snippet);

    }

    /**
     * Returns true if left hand is an identifier, and is used on the right hand
     * 
     * @param assignmentCopy
     * @return
     */
    private static boolean shouldReplaceLeftHand(AssignmentSt assignment) {
        // Disabled, to speedup examples
        return false;
        /*
        	if (!(assignment.getLeftHand() instanceof IdentifierNode)) {
        	    return false;
        	}
        
        	String idName = ((IdentifierNode) assignment.getLeftHand()).getName();
        
        	return assignment.getRightHand().getDescendantsAndSelfStream()
        		.filter(node -> node instanceof IdentifierNode)
        		.filter(node -> ((IdentifierNode) node).getName().equals(idName))
        		.findFirst()
        		.isPresent();
        */
    }

    private Optional<AssignmentSt> adaptAssign(AssignmentSt assignment, Map<Integer, String> indexToIndexVar) {
        // System.out.println("BEFIRE:" + assignment.getCode());

        // Adapt access calls
        if (!adaptAccessCalls(assignment, indexToIndexVar)) {
            return Optional.empty();
        }

        // Adapt colon operator

        // Adapt matrix identifiers
        List<IdentifierNode> ids = assignment.getDescendants(IdentifierNode.class);
        // Check which ones are matrix ids
        for (IdentifierNode id : ids) {
            // Make sure they are not ids inside access calls
            if (id.getParent() instanceof AccessCallNode) {
                continue;
            }

            VariableType type = data.getVariableType(id.getName());
            assert type != null : "No type for var " + id.getName() + ": " + assignment.getCode();

            if (!(type instanceof MatrixType)) {
                continue;
            }

            // Replace with access call
            List<MatlabNode> children = new ArrayList<>();
            for (String varName : indexToIndexVar.values()) {
                children.add(MatlabNodeFactory.newIdentifier(varName));
            }

            IdentifierNode accessCallId = MatlabNodeFactory.newIdentifier(id.getName());
            AccessCallNode accessCall = MatlabNodeFactory.newAccessCall(accessCallId, children);
            NodeInsertUtils.replace(id, accessCall);
        }

        // System.out.println("AFTER:" + assignment.getCode());
        return Optional.of(assignment);
    }

    private boolean adaptAccessCalls(AssignmentSt assignment, Map<Integer, String> indexToIndexVar) {
        // Replace colon notation with appropriate indexes, and matrix identifiers with access calls
        List<AccessCallNode> accessCalls = assignment.getDescendants(AccessCallNode.class);

        // Detect if matrix access or function call
        List<AccessCallNode> matrixAccesses = accessCalls.stream()
                .filter(accessCall -> !data.isFunctionCall(accessCall.getName()))
                .collect(Collectors.toList());

        // Get matrix accesses with colon notation
        List<AccessCallNode> accessesWithColonNotation = matrixAccesses.stream()
                .filter(matrixAccess -> matrixAccess.hasArgument(ColonNotationNode.class))
                .collect(Collectors.toList());

        for (AccessCallNode accessWithColon : accessesWithColonNotation) {
            List<MatlabNode> args = accessWithColon.getArguments();
            // If single argument just replace with unique entry in map (guaranteed to be only one)
            if (indexToIndexVar.size() == 1) {
                IdentifierNode id = MatlabNodeFactory
                        .newIdentifier(indexToIndexVar.values().stream().findFirst().get());

                for (int i = 0; i < args.size(); i++) {
                    MatlabNode arg = args.get(i);
                    if (!(arg instanceof ColonNotationNode)) {
                        continue;
                    }

                    NodeInsertUtils.replace(arg, id);
                }

                // System.out.println("ACCESS SINGLE:" + accessWithColon.getCode());
                continue;
            }

            for (int i = 0; i < args.size(); i++) {
                MatlabNode arg = args.get(i).normalizeExpr();

                String tempVarIndex = indexToIndexVar.get(i);

                // No temp var for this index
                if (tempVarIndex == null) {
                    continue;
                }

                // assert tempVarIndex != null : "No VarIndex for index " + i + " in access " +
                // accessWithColon.getCode()
                // + ": " + assignment.getCode();
                IdentifierNode id = MatlabNodeFactory.newIdentifier(tempVarIndex);

                if (arg instanceof ColonNotationNode) {
                    NodeInsertUtils.replace(arg, id);
                    continue;
                }

                if (arg instanceof OperatorNode) {
                    OperatorNode opNode = (OperatorNode) arg;
                    if (opNode.getOp() == MatlabOperator.Colon) {
                        // Check that it only has two arguments
                        if (opNode.getOperands().size() != 2) {
                            throw new RuntimeException(
                                    "Does not support the case where colon has more than two operands: "
                                            + opNode.getCode());
                        }

                        // If first argument is not a number, stop
                        if (!(opNode.getOperands().get(0) instanceof MatlabNumberNode)) {
                            return false;
                            /*
                            // If first argument is not a number, use general version
                            // The start value will be the variable, plus the original start value minus 1
                            String adjustValue = opNode.getOperands().get(0).getCode() + " - 1";
                            
                            List<MatlabNode> addArgs = Arrays.asList(id, MatlabNodeFactory.newNumber(adjustValue));
                            MatlabNode indexNode = MatlabNodeFactory.newOperator(MatlabOperator.Addition, addArgs);
                            NodeInsertUtils.replace(arg, indexNode);
                            continue;
                            */
                        }

                        // Otherwise, use simpler version
                        MatlabNumber number = ((MatlabNumberNode) opNode.getOperands().get(0)).getNumber();

                        // Get integer value, it will be an index anyway
                        int originalStartValue = number.getIntegerValue().intValue();

                        // The start value will be the variable, plus the original start value minus 1
                        int adjustValue = originalStartValue - 1;

                        MatlabNode indexNode = id;
                        if (adjustValue != 0) {
                            List<MatlabNode> addArgs = Arrays.asList(id, MatlabNodeFactory.newNumber(adjustValue));
                            indexNode = MatlabNodeFactory.newOperator(MatlabOperator.Addition, addArgs);
                        }

                        NodeInsertUtils.replace(arg, indexNode);
                        continue;

                    }

                }

            }

            // System.out.println("ACCESS 2:" + accessWithColon.getCode());

        }

        return true;
    }

    private static Map<AccessCallNode, List<Integer>> createColonMap(List<AccessCallNode> accessesWithColonNotation) {
        // System.out.println("ACCESS:" + accessesWithColonNotation);
        // Using linked hash map to maintain the order
        Map<AccessCallNode, List<Integer>> map = new LinkedHashMap<>();

        // Check if all accesses have the same number of indexes, and if there is an access to a single index
        int numIndexes = -1;
        for (AccessCallNode node : accessesWithColonNotation) {
            int numArgs = node.getArguments().size();

            // First value found
            if (numIndexes == -1) {
                numIndexes = numArgs;
            }

            // Otherwise, check if it is different and they are both bigger than one
            else {
                boolean biggerThanOne = numIndexes > 1 && numArgs > 1;
                if (biggerThanOne && numIndexes != numArgs) {
                    SpecsLogs.warn("Does not support this case");
                    return null;
                }
            }

            // Get indexes with colon notation
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < numArgs; i++) {
                MatlabNode arg = node.getArguments().get(i).normalizeExpr();

                if (arg instanceof ColonNotationNode) {
                    indexes.add(i);
                }
            }

            map.put(node, indexes);
        }

        return map;
    }
}
