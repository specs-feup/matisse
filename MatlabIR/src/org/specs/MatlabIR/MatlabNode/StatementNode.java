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

package org.specs.MatlabIR.MatlabNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;

/**
 * A MatLab statement.
 * 
 * <p>
 * The children tokens represent the statement.
 * 
 * <p>
 * Has as content the line number, and if it the results of the statement should be displayed.
 * 
 * @author JoaoBispo
 *
 */
public abstract class StatementNode extends MatlabNode {

    private StatementData data;

    protected StatementNode(StatementData data, Collection<? extends MatlabNode> children) {
	super(children);

	this.data = data;
    }

    public StatementData getData() {
	return data;
    }

    public String getStatementType() {
	String nodeName = getNodeName();

	if (nodeName.endsWith("St")) {
	    return nodeName.substring(0, nodeName.length() - "St".length());
	}

	return nodeName;
    }

    public int getLine() {
	return getData().getLine();
    }

    public void setLine(int lineNumber) {
	data = data.setLineNumber(lineNumber);
    }

    public boolean isDisplay() {
	return data.isDisplay();
    }

    /**
     * To customize statement code, extend 'getStatementCode'.
     */
    @Override
    final public String getCode() {

	String code = getStatementCode();

	// If results should not be display, add suffix ';'
	if (!getData().isDisplay()) {
	    code += ";";
	}

	// Append a newline after each statement.
	return code + "\n";

    }

    /**
     * The code corresponding to the statement, without new line or ;
     * 
     * @return
     */
    public abstract String getStatementCode();

    /*
    public String getStatementCode() {
    throw new MatlabNodeException("Not yet implemented", this);
    }
    */

    /**
     * 
     * @return true, if the statement should not have indentation when inside a block.
     */
    public boolean isBlockIndented() {
	if (isBlockHeader()) {
	    return true;
	}

	return false;
    }

    /**
     * 
     * @return true, if the statement can be the header of a block (e.g.,
     */
    public boolean isBlockHeader() {
	return false;
    }

    /**
     * 
     * @return if this statement is a block header, returns all statements of the for block after this statement
     */
    public List<StatementNode> getBodyStatements() {
	if (!isBlockHeader()) {
	    return Collections.emptyList();
	}

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

    public String getContentString() {
	return getStatementType() + " (" + getData() + ")";
    }

    @Override
    public String toNodeString() {
	return "Statement: " + getContentString();
    }

}
