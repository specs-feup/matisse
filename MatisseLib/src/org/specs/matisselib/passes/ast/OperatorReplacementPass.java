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

package org.specs.matisselib.passes.ast;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * A pass that converts operators such as "*" and "-" into their function counterparts.
 * <p>
 * This pass does NOT replace '&&' nor '||', since there is no function with the exact same behavior.
 * 
 * @author luiscubal
 *
 */
public class OperatorReplacementPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
	Preconditions.checkArgument(rootNode != null);
	Preconditions.checkArgument(data != null);

	if (rootNode instanceof OperatorNode) {
	    OperatorNode operatorNode = (OperatorNode) rootNode;
	    MatlabOperator operator = operatorNode.getOp();
	    if (operator != MatlabOperator.ShortCircuitAnd && operator != MatlabOperator.ShortCircuitOr) {
		List<MatlabNode> operands = new ArrayList<>();
		for (MatlabNode child : rootNode.getChildren()) {
		    operands.add(apply(child, data));
		}

		String functionName = operator.getFunctionName();
		return MatlabNodeFactory.newSimpleAccessCall(functionName, operands);
	    }
	}

	MatlabNodeIterator it = rootNode.getChildrenIterator();
	while (it.hasNext()) {
	    it.set(apply(it.next(), data));
	}

	return rootNode;
    }

    @Override
    public String getName() {
	return "OperatorReplacementPass";
    }

}
