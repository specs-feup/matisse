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

import java.util.Collections;
import java.util.List;

import org.specs.CIR.Tree.CNode;

/**
 * @author Joao Bispo
 *
 */
public class FunctionInputsNode extends CNode {

    /**
     * @param type
     * @param content
     * @param children
     */
    FunctionInputsNode(List<CNode> inputs) {
        super(inputs);
    }

    /**
     * Empty constructor
     */
    private FunctionInputsNode() {
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#newEmptyInstance()
     */
    @Override
    protected CNode copyPrivate() {
        // return super.newEmptyInstance();
        return new FunctionInputsNode();
    }

    public List<CNode> getInputs() {
        return Collections.unmodifiableList(getChildren());
    }

    public void setInput(int i, CNode newToken) {
        setChild(i, newToken);
    }

    /**
     * Adds an input to the end of the list of inputs.
     * 
     * @param input
     */
    public void addInput(CNode input) {
        addChild(input);
    }

    @Override
    public String toReadableString() {
        return "function inputs";
    }

    @Override
    public int hashCode() {
        return getChildren().hashCode();
    }

    /*
    @Override
    public String getCode() {
    return getChildrenStream()
    	.map(node -> node.getCode())
    	.collect(Collectors.joining(", "));
    }
    */
}
