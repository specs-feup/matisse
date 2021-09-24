/**
 * Copyright 2017 SPeCS.
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

public class GlobalSt extends ReservedWordSt {

    private final Set<String> currentIdentifiers;

    GlobalSt(StatementData data, Collection<MatlabNode> children) {
        super(data, children);

        this.currentIdentifiers = children.stream()
                .skip(1)
                .map(IdentifierNode.class::cast)
                .map(IdentifierNode::getName)
                .collect(Collectors.toSet());
    }

    GlobalSt(int line, boolean display, List<MatlabNode> children) {
        this(new StatementData(line, display), children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new GlobalSt(getData(), Collections.emptyList());
    }

    @Override
    public ReservedWord getReservedWord() {
        return ReservedWord.Global;
    }

    public List<IdentifierNode> getIdentifierNodes() {
        return getChildren().stream()
                .skip(1)
                .map(IdentifierNode.class::cast)
                .collect(Collectors.toList());
    }

    public List<String> getIdentifiers() {
        return getChildren().stream()
                .skip(1)
                .map(IdentifierNode.class::cast)
                .map(IdentifierNode::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getStatementCode() {
        return ReservedWord.Global.getLiteral() + " " + getIdentifiers().stream().collect(Collectors.joining(" "));
    }

    /**
     * 
     * @param name
     * @return true if it was added, false if identifier was already present
     */
    public boolean addIdentifier(String name) {
        // Identifiers are case-sensitive, do not need to lower case
        // https://www.mathworks.com/matlabcentral/answers/103146-is-matlab-case-sensitive

        if (currentIdentifiers.contains(name)) {
            return false;
        }

        addChild(MatlabNodeFactory.newIdentifier(name));
        currentIdentifiers.add(name);

        return true;
    }

}
