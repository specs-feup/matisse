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
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;

/**
 * A For statement.
 * <p>
 * *
 * <p>
 * - The first child is the reserved word 'for' <br>
 * - The second child is the for expression which should have the following format: <br>
 * -- The first child is the identifier <br>
 * -- The second child is the assignment token <br>
 * -- The third child is an expression <br>
 * 
 * <p>
 * TODO: Simply structure, use structure similar to http://www.mathworks.com/help/matlab/ref/for.html<br>
 * - The first child is an identifier, representing the index <br>
 * - The second child represents the values, which can be: initVal:endVal, initVal:step:endVal and valArray (a matrix,
 * which will assign a column to the index at each iteration)
 * 
 * @author JoaoBispo
 *
 */
public abstract class ForSt extends LoopSt {

    ForSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    /*
        ForSt(int lineNumber, List<MatlabNode> children) {
    	super(new StatementData(lineNumber, true, MStatementType.For), children);
        }

        ForSt(int lineNumber, MatlabNode index, MatlabNode expression) {
    	this(lineNumber, Arrays.asList(index, expression));

        }
    */
    /*
    public MatlabNode getExpression() {
    return getChild(1, 2);
    }
    */

    @Override
    public MatlabNode getExpression() {
	return getChild(1);
    }

    public MatlabNodeIterator getExpressionIterator() {
	MatlabNodeIterator it = getChildrenIterator();
	it.next();
	return it;
    }

    /*
    public IdentifierNode getIndexIdentifier() {
    // Get expression
    MatlabNode child = getChild(1);
    // Get identifier
    return child.getChild(IdentifierNode.class, 0);
    }
    */
    public IdentifierNode getIndexVar() {
	// Get identifier
	return getChild(IdentifierNode.class, 0);
    }

    @Override
    public String getStatementCode() {
	StringBuilder builder = new StringBuilder();

	builder.append(getReservedWord().getLiteral());
	builder.append(" ");
	builder.append(getIndexVar().getCode());
	builder.append(" = ");
	builder.append(getExpression().getCode());

	return builder.toString();
    }

    public abstract ReservedWord getReservedWord();

    @Override
    public boolean isBlockIndented() {
	return true;
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }

    /**
     * 
     * @return all statements of the for block after this statement
     */
    /*
    public List<StatementNode> getBodyStatements() {
    assert getParent() instanceof BlockSt;

    // Get parent block
    BlockSt blockParent = (BlockSt) getParent();

    // Skip all statements until header is found
    boolean foundHeader = false;
    List<StatementNode> body = new ArrayList<>();
    for (StatementNode statement : blockParent.getStatements()) {
        if (foundHeader) {
    	body.add(statement);
        }

        if (statement == this) {
    	foundHeader = true;
        }

    }

    return body;
    }
    */
}
