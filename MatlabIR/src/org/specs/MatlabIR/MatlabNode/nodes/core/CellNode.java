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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Represents a Cell. Curly braces are used in cell array assignment statements. For example, A(2,1) = {[1 2 3; 4 5 6]},
 * or A{2,2} = ('str').
 * 
 * <p>
 * The children are always of type 'row'.
 * 
 * @author JoaoBispo
 *
 */
public class CellNode extends MatlabNode {

    CellNode(Collection<MatlabNode> children) {
        super(children);
    }

    CellNode(Object content, Collection<MatlabNode> children) {
        this(children);
    }

    public List<RowNode> getRows() {
        return getChildren(RowNode.class);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new CellNode(Collections.emptyList());
    }

    @Override
    public String getCode() {
        if (!hasChildren()) {
            return "{}";

        }

        StringJoiner joiner = new StringJoiner("; ", "{", "}");
        getChildren().forEach(child -> joiner.add(child.getCode()));

        return joiner.toString();
    }

}
