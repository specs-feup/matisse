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

package org.specs.MatlabIR.MatlabNode.nodes.temporary;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

/**
 * The inputs for a lambda node.
 * 
 * @author JoaoBispo
 *
 */
public class LambdaInputsNode extends TemporaryNode {

    LambdaInputsNode(Collection<MatlabNode> inputs) {
	// super(null, inputs);
	super("", inputs);

    }

    @Override
    protected MatlabNode copyPrivate() {
	return new LambdaInputsNode(Collections.emptyList());
    }

    public List<MatlabNode> getInputs() {
	return getChildren();
    }

    @Override
    public String getCode() {
	return "@" + MatlabNodeFactory.newFunctionInputs(getChildren()).getCode();
    }

}
