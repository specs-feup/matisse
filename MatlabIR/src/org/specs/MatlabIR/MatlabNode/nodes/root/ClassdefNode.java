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
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;

/**
 * The root node of a MatLab class. Represents a list of statements.
 * 
 * <p>
 * Each children of a 'classdef' node is either a 'statement' or a 'Block'.
 * 
 * @author JoaoBispo
 *
 */
public class ClassdefNode extends MatlabUnitNode {

    private ClassdefNode(Collection<? extends MatlabNode> statementsOrBlocks) {
        super(statementsOrBlocks);
    }

    static ClassdefNode newInstance(Collection<StatementNode> statements) {
        return new ClassdefNode(statements);
    }

    /**
     * For compatibility with TOM.
     * 
     * @param content
     * @param children
     */
    public ClassdefNode(Object content, Collection<MatlabNode> children) {
        this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new ClassdefNode(Collections.emptyList());
    }

    /*
    public List<StatementNode> getStatements() {
    return getChildren(StatementNode.class);
    }
    */

    @Override
    public String getCode() {

        StringJoiner joiner = new StringJoiner("");
        getChildren().forEach(child -> joiner.add(child.getCode()));
        return joiner.toString();
    }

    public String getClassName() {
        return getClassDefinition().getClassName();
    }

    public ClassdefSt getClassDefinition() {
        return getFirstChildRecursive(ClassdefSt.class);
    }

    @Override
    public String getUnitName() {
        return getClassName();
    }

    @Override
    public String getNodeName() {
        return "Class";
    }

    /**
     * Returns the line of the class definition
     */
    @Override
    public int getLine() {
        return getClassDefinition().getLine();
    }

}
