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

import java.util.List;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;

import com.google.common.base.Preconditions;

public class FunctionCallNode extends CNode {

    private final FunctionInstance functionInstance;

    FunctionCallNode(FunctionInstance functionInstance, List<CNode> functionInputs) {
        super(CNodeFactory.newFunctionInputs(functionInputs));

        Preconditions.checkNotNull(functionInstance);

        this.functionInstance = functionInstance;
    }

    private FunctionCallNode(FunctionInstance functionInstance) {
        this.functionInstance = functionInstance;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        // FunctionInstance might not be immutable, should have a copy() method
        // Until then, using potentially slower generic method
        // return new FunctionCallNode(XStreamUtils.copy(getFunctionInstance()));
        // Using the same object, for performance reasons
        return new FunctionCallNode(getFunctionInstance());
    }

    @Override
    public VariableType getVariableType() {
        FunctionType types = getFunctionInstance().getFunctionType();

        // Return the type the function returns
        return types.getCReturnType();

    }

    public FunctionInstance getFunctionInstance() {
        return functionInstance;
    }

    public FunctionInputsNode getFunctionInputs() {
        return (FunctionInputsNode) getChildren().get(0);
    }

    public List<CNode> getInputTokens() {
        return getFunctionInputs().getInputs();
    }

    public void setInputTokens(List<CNode> inputs) {
        // First child contains the inputs. Replace the children
        getChildren().get(0).setChildren(inputs);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        return getFunctionInstance().getCallCode(getInputTokens());
    }

    @Override
    public PrecedenceLevel getPrecedenceLevel() {
        return getFunctionInstance().getCallPrecedenceLevel();
    }

    @Override
    public String toNodeString() {
        return "FunctionCallNode " + getFunctionInstance().getCName();
    }

    @Override
    public String toReadableString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getFunctionInstance().getCName());
        builder.append(getInputTokens().stream()
                .map(t -> t.toReadableString())
                .collect(Collectors.joining(", ", "(", ")")));

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionCallNode)) {
            return false;
        }
        FunctionCallNode otherCall = (FunctionCallNode) obj;
        return functionInstance == otherCall.functionInstance &&
                getChildren().equals(otherCall.getChildren());
    }
}
