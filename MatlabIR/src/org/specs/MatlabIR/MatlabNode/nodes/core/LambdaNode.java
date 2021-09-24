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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Represents an anonymous function.
 * 
 * <p>
 * Has two children, a FunctionInputs and an expression.
 * 
 * @author JoaoBispo
 *
 */
public class LambdaNode extends MatlabNode {

    /**
     * Base constructor.
     * 
     * @param children
     */
    private LambdaNode(Collection<MatlabNode> children) {
	super(children);
    }

    // LambdaNode(Collection<MatlabNode> children) {
    LambdaNode(Collection<MatlabNode> inputs, MatlabNode expression) {
	this(Arrays.asList(MatlabNodeFactory.newFunctionInputs(inputs), expression));
    }

    /**
     * TOM Compatibility.
     * 
     * @param content
     * @param children
     */
    LambdaNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new LambdaNode(Collections.emptyList());
    }

    public FunctionInputsNode getInputs() {
	return getChild(FunctionInputsNode.class, 0);
    }

    public MatlabNode getExpression() {
	return getChild(1);
    }

    @Override
    public String getCode() {
	// return "@" + joinCode(", ", "(", ")", getChildren());
	return "@" + getInputs().getCode() + " " + getExpression().getCode();
    }
}
