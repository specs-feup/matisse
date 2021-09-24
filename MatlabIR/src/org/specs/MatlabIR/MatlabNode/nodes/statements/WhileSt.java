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

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

import com.google.common.base.Preconditions;

/**
 * A While statement.
 * <p>
 * The first child is "while" reserved word;<br>
 * The second child is the while condition;
 * 
 * @author JoaoBispo
 *
 */
public class WhileSt extends LoopSt implements ConditionalStatement {

    WhileSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    WhileSt(int lineNumber, Collection<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    WhileSt(int lineNumber, MatlabNode expression) {
	this(lineNumber, buildChildren(expression));
    }

    private static Collection<MatlabNode> buildChildren(MatlabNode expression) {
	return Arrays.asList(MatlabNodeFactory.newReservedWord(ReservedWord.While), expression);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new WhileSt(getData(), Collections.emptyList());
    }

    /**
     * @return the condition of the while statement
     */
    public MatlabNode getCondition() {
	return getChild(1);
    }

    public void setCondition(MatlabNode condition) {
	Preconditions.checkArgument(condition != null);

	setChild(1, condition);
    }

    /**
     * 
     * @return
     */
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
	return ReservedWord.While.getLiteral() + " " + getCondition().getCode();
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }
}
