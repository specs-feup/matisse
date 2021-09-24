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
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.Exceptions.MatlabNodeException;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FunctionInputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;

/**
 * MatLab statement representing a function declaration.
 * 
 * <p>
 * Always has three children: an Outputs, an Identifier and a FunctionInputs.
 * 
 * <p>
 * Outputs represents the outputs, the Identifier contains the name of the function and FunctionInputs, the inputs.
 * 
 * @author JoaoBispo
 *
 */
public class FunctionDeclarationSt extends StatementNode {

    /**
     * Constructor for copyPrivate() and StatementType.
     * 
     * @param data
     * @param children
     */
    FunctionDeclarationSt(StatementData data, Collection<MatlabNode> children) {
        super(data, children);
    }

    FunctionDeclarationSt(int lineNumber, OutputsNode outputs, IdentifierNode name,
            FunctionInputsNode inputs) {

        this(new StatementData(lineNumber, true), Arrays.asList(outputs, name, inputs));
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new FunctionDeclarationSt(getData(), Collections.emptyList());
    }

    public OutputsNode getOutputs() {
        return getFirstChild(OutputsNode.class);
    }

    public List<String> getOutputNames() {
        return getOutputs().getNames();
    }

    public IdentifierNode getNameNode() {
        return getFirstChild(IdentifierNode.class);
    }

    public String getFunctionName() {
        return getNameNode().getName();
    }

    public IdentifierNode replaceNameNode(IdentifierNode node) {
        // Name is the first child that is an IdentifierNode
        MatlabNodeIterator iterator = getChildrenIterator();
        Optional<IdentifierNode> currentNode = iterator.next(IdentifierNode.class);

        if (!currentNode.isPresent()) {
            throw new MatlabNodeException("Could not find an IdentifierNode", this);
        }

        iterator.set(node);

        return currentNode.get();
    }

    public FunctionInputsNode getInputs() {
        return getFirstChild(FunctionInputsNode.class);
    }

    @Override
    public String getStatementCode() {
        StringBuilder builder = new StringBuilder();

        // Write 'function'
        builder.append(ReservedWord.Function.getLiteral());
        builder.append(" ");

        // Write outputs
        OutputsNode outputs = getOutputs();
        if (outputs.hasChildren()) {
            builder.append(getOutputs().getCode());
            builder.append(" = ");
        }

        // Write function name
        builder.append(getNameNode().getCode());
        // Write inputs
        builder.append(getInputs().getCode());

        return builder.toString();
    }

    @Override
    public boolean isBlockHeader() {
        return true;
        // return false;
    }

    public List<String> getInputNames() {
        return getInputs().getNames();
    }

    public List<String> getInputNames(String placeholderForUnused) {
        return getInputs().getNames(placeholderForUnused);
    }
}
