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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * A MatLab assignment. The first child is the left hand, the second child is the right hand.
 * 
 * @author JoaoBispo
 *
 */
public class AssignmentSt extends StatementNode {

    AssignmentSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    public AssignmentSt(int line, boolean display, MatlabNode leftHand, MatlabNode rightHand) {
	this(new StatementData(line, display), Arrays.asList(leftHand, rightHand));
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new AssignmentSt(getData(), Collections.emptyList());
    }

    public MatlabNode getLeftHand() {
	return getChild(0);
    }

    public MatlabNode getRightHand() {
	return getChild(1);
    }

    @Override
    public String getStatementCode() {
	StringBuilder builder = new StringBuilder();

	// Always has two children
	builder.append(getLeftHand().getCode());
	builder.append(" = ");
	builder.append(getRightHand().getCode());

	return builder.toString();
    }

}
