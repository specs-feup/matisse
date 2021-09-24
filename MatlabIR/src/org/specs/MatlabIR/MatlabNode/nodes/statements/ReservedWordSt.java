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

import java.util.Collection;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * Represents statements which are formed by a single ReservedWord.
 * 
 * @author JoaoBispo
 *
 */
public abstract class ReservedWordSt extends StatementNode {

    ReservedWordSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    public abstract ReservedWord getReservedWord();

    @Override
    public String getStatementCode() {
	return getReservedWord().getLiteral();
    }

}
