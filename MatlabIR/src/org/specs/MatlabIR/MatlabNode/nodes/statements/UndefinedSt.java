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
import java.util.List;
import java.util.StringJoiner;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * For statements which are not defined.
 * 
 * @author JoaoBispo
 *
 */
public class UndefinedSt extends StatementNode {
    // public class UndefinedSt extends TemporaryStatement {

    UndefinedSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new UndefinedSt(getData(), Collections.emptyList());
    }

    public UndefinedSt(int line, boolean display, List<MatlabNode> children) {
	this(new StatementData(line, display), children);
    }

    /**
     * Appends each children, with a space between.
     */
    @Override
    public String getStatementCode() {
	StringJoiner joiner = new StringJoiner(" ");

	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }
}
