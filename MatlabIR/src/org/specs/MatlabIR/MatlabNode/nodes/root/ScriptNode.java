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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * The root node of a MatLab script. Represents a list of statements.
 * 
 * <p>
 * Each children of a 'script' node is either a 'statement' or a 'Block'.
 * 
 * @author JoaoBispo
 *
 */
public class ScriptNode extends MatlabUnitNode {

    private ScriptNode(Collection<? extends MatlabNode> statementsOrBlocks) {
        super(statementsOrBlocks);
    }

    static ScriptNode newInstance(Collection<StatementNode> statements) {
        return new ScriptNode(statements);
    }

    /**
     * For compatibility with TOM.
     * 
     * @param content
     * @param children
     */
    public ScriptNode(Object content, Collection<MatlabNode> children) {
        this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new ScriptNode(Collections.emptyList());
    }

    /*
    public List<StatementNode> getStatements() {
    return getChildren(StatementNode.class);
    }
    */

    @Override
    public String getUnitName() {
        // Get the filename of the parent FileNode
        assert getParent() instanceof FileNode;
        return ((FileNode) getParent()).getFilename();
    }

    @Override
    public String getCode() {

        StringJoiner joiner = new StringJoiner("");
        getChildren().forEach(child -> joiner.add(child.getCode()));
        return joiner.toString();
    }

    /**
     * Scripts are considered to always have 0 inputs.
     */
    @Override
    public int getNumInputs() {
        return 0;
    }

    /**
     * Always returns -1.
     */
    @Override
    public int getLine() {
        return -1;
    }
}
