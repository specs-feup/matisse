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

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

/**
 * Else
 * 
 * @author JoaoBispo
 *
 */
public class ElseSt extends ReservedWordSt {

    ElseSt(StatementData data, Collection<MatlabNode> children) {
        super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new ElseSt(getData(), Collections.emptyList());
    }

    ElseSt(int line) {
        this(new StatementData(line, true), Arrays.asList(MatlabNodeFactory.newReservedWord(ReservedWord.Else)));
    }

    @Override
    public boolean isBlockIndented() {
        return true;
    }

    @Override
    public ReservedWord getReservedWord() {
        return ReservedWord.Else;
    }

    @Override
    public List<StatementNode> getBodyStatements() {

        // Statements of else is every sibling after this node
        int elseIndex = indexOfSelf();
        List<StatementNode> siblings = getParent().getChildren(StatementNode.class);
        return siblings.subList(elseIndex + 1, siblings.size());
    }

}
