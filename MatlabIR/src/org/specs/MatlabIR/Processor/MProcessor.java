/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabIR.Processor;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Transforms the MATLAB Tree according to certain rules
 * 
 * <p>
 * TODO: Replace with PassData
 * 
 * @author Joao Bispo
 * 
 */
public class MProcessor {

    // private final List<TreeTransformRule> statementRules;
    private final List<RuleHelper> statementRules;

    public MProcessor() {
	statementRules = SpecsFactory.newArrayList();
    }

    /**
     * Add a rule that will be applied to statements.
     * 
     * @param statementRule
     */
    // public void addStatementRule(TreeTransformRule statementRule) {
    public void addStatementRule(StatementRule statementRule) {
	RuleHelper rule = new RuleHelper(statementRule);
	addStatementRule(rule);
    }

    public void addStatementRule(RuleHelper statementRule) {
	statementRules.add(statementRule);
    }

    public MatlabNode process(MatlabNode token) {
	// First, process statements
	List<StatementNode> statements = token.getDescendantsAndSelf(StatementNode.class);
	// Do a complete passage of each rule over all statements
	for (RuleHelper rule : statementRules) {
	    statements.forEach(statement -> rule.process(statement));

	    // If rule changes the statements, "reload" them
	    if (rule.changesStatements()) {
		statements = token.getDescendantsAndSelf(StatementNode.class);
	    }

	}

	return token;

    }

}
