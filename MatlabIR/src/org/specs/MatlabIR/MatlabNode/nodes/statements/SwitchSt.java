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
import java.util.Collections;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * A Switch statement.
 * 
 * <p>
 * The first child is the reserved keyword Switch, the second child is the expression
 * 
 * @author JoaoBispo
 *
 */
public class SwitchSt extends StatementNode {

    SwitchSt(StatementData data, Collection<MatlabNode> children) {
	this(data.getLine(), children);
    }

    SwitchSt(int lineNumber, Collection<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new SwitchSt(getData(), Collections.emptyList());
    }

    public MatlabNode getExpression() {
	return getChild(1);
    }

    @Override
    public boolean isBlockIndented() {
	return true;
    }

    @Override
    public String getStatementCode() {
	return ReservedWord.Switch.getLiteral() + " " + getExpression().getCode();
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }
}
