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
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;

/**
 * An If statement
 * <p>
 * The first child is the reserved word "if", the second child is an expression representing the if condition.
 * 
 * @author JoaoBispo
 *
 */
public class IfSt extends StatementNode implements ConditionalStatement {

    IfSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new IfSt(getData(), Collections.emptyList());
    }

    public IfSt(int lineNumber, Collection<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    public IfSt(int lineNumber, MatlabNode condition) {
	this(lineNumber, buildChildren(condition));
	// this(new StatementData(lineNumber, true, MStatementType.If), buildChildren(condition));
    }

    private static Collection<MatlabNode> buildChildren(MatlabNode condition) {
	ReservedWordNode ifWord = MatlabNodeFactory.newReservedWord(ReservedWord.If);

	return Arrays.asList(ifWord, condition);
    }

    @Override
    public MatlabNode getExpression() {
	return getChild(1);
    }

    @Override
    public boolean isBlockIndented() {
	return true;
    }

    @Override
    public String getStatementCode() {
	return ReservedWord.If.getLiteral() + " " + getExpression().getCode();
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }

    @Override
    public List<StatementNode> getBodyStatements() {

	List<StatementNode> statements = super.getBodyStatements();

	// Find index of first block indented
	int index = 0;
	for (StatementNode statement : statements) {
	    if (statement.isBlockIndented()) {
		break;
	    }

	    index++;
	}

	return statements.subList(0, index);
    }
}
