/**
 * Copyright 2016 SPeCS.
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

package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AAssignment;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.utils.Action;
import pt.up.fe.specs.matisse.weaver.utils.When;
import pt.up.fe.specs.util.SpecsCollections;

public class MAssignment extends AAssignment {

    private final AssignmentSt assignment;

    public MAssignment(AssignmentSt assignment, AMWeaverJoinPoint parent) {
        super(new MStatement(assignment, parent));
        initMWeaverJP(parent);

        this.assignment = assignment;
    }

    @Override
    public List<? extends AExpression> selectLhs() {
        return Arrays.asList(MJoinpointUtils.fromExpression(assignment.getLeftHand(), getParentImpl()));
    }

    @Override
    public List<? extends AExpression> selectRhs() {
        return Arrays.asList(MJoinpointUtils.fromExpression(assignment.getRightHand(), getParentImpl()));
    }

    @Override
    public void appendOutputImpl(String code) {
        MatlabNode leftHand = assignment.getLeftHand();

        if (leftHand instanceof OutputsNode) {
            OutputsNode outputs = (OutputsNode) leftHand;

            Action.insert(SpecsCollections.last(outputs.getNodes()), When.after.name(), code);
        } else {
            Action.insert(leftHand, When.replace.name(), "[" + leftHand.getCode() + ", " + code + "]");
        }
    }

    @Override
    public void prependOutputImpl(String code) {
        MatlabNode leftHand = assignment.getLeftHand();

        if (leftHand instanceof OutputsNode) {
            OutputsNode outputs = (OutputsNode) leftHand;

            Action.insert(outputs.getNodes().get(0), When.before.name(), code);
        } else {
            Action.insert(leftHand, When.replace.name(), "[" + code + ", " + leftHand.getCode() + "]");
        }
    }

    @Override
    public MatlabNode getNode() {
        return assignment;
    }

    @Override
    public String toString() {
        return "Assignment(" + assignment.getCode() + ")";
    }

}
