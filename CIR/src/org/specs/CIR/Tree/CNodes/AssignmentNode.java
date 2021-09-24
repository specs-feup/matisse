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

package org.specs.CIR.Tree.CNodes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;

/**
 * Represents an assignment.
 * 
 * @author JoaoBispo
 *
 */
public class AssignmentNode extends CNode {

    AssignmentNode(CNode leftHand, CNode rightHand) {
        super(Arrays.asList(leftHand, rightHand));
    }

    /**
     * Empty constructor
     */
    private AssignmentNode() {
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new AssignmentNode();
    }

    @Override
    public VariableType getVariableType() {
        // Get type of right hand
        return getRightHand().getVariableType();
    }

    public CNode getLeftHand() {
        // Return the first child
        return getChildren().get(0);
    }

    public CNode getRightHand() {
        return getChildren().get(1);
    }

    public void setRightHand(CNode newRightHand) {
        // Set the second child
        setChild(1, newRightHand);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {

        CNode leftHand = getLeftHand();
        CNode rightHand = getRightHand();

        if (leftHand instanceof VariableNode && rightHand instanceof FunctionCallNode) {
            String var = ((VariableNode) leftHand).getVariableName();
            FunctionCallNode rightCall = (FunctionCallNode) rightHand;

            List<CNode> rightInputs = rightCall.getFunctionInputs().getInputs();
            if (rightInputs.size() > 0 && rightInputs.get(0) instanceof VariableNode
                    && ((VariableNode) rightInputs.get(0)).getVariableName().equals(var)) {

                Optional<String> assignmentCode = rightCall.getFunctionInstance().getAssignmentCallCode(rightInputs);
                if (assignmentCode.isPresent()) {
                    return assignmentCode.get();
                }
            }
        }

        // Generate code for left hand
        String leftHandCode = leftHand.getCode();

        // Get code for the right-hand side
        String rightHandCode = rightHand.getCode();

        return leftHandCode + " = " + rightHandCode;
    }

    @Override
    public String toReadableString() {
        return getLeftHand().toReadableString() + " = " + getRightHand().toReadableString();
    }
}
