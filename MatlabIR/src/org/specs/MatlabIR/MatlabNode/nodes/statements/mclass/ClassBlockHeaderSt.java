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

package org.specs.MatlabIR.MatlabNode.nodes.statements.mclass;

import java.util.Collection;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * General class for block headers in MATLAB syntax for classes.
 * 
 * 
 * @author JoaoBispo
 *
 */
public abstract class ClassBlockHeaderSt extends StatementNode {

    ClassBlockHeaderSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    @Override
    public String getStatementCode() {
	throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }

}
