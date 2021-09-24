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

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Represents a MatLab expression with operators.
 * 
 * <p>
 * After the tree is completely generated, always contains a child which represents an expression tree which respects
 * the MatLab operator precedences.
 * 
 * @author JoaoBispo
 *
 */
public class CommentBlockStartSt extends TemporaryStatement {

    /**
     * TODO: Move to TempNodes package and make package-private
     * 
     * @param data
     * @param children
     */
    public CommentBlockStartSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    CommentBlockStartSt(int lineNumber) {
	this(new StatementData(lineNumber, true), Collections.emptyList());
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CommentBlockStartSt(getData(), Collections.emptyList());
    }

    @Override
    public String getStatementCode() {
	return "%{";
    }

}
