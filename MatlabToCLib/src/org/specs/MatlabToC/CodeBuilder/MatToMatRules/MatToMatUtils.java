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

package org.specs.MatlabToC.CodeBuilder.MatToMatRules;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class MatToMatUtils {

    private static final List<MatlabToMatlabRule> rules;

    static {
	rules = SpecsFactory.newArrayList();

	// rules.add(new UnfoldAssignWithTemp());
	MatToMatUtils.rules.add(new FunctionCallOutliner());
    }

    public static List<StatementNode> applyRules(StatementNode statement, MatlabToCFunctionData data) {

	List<StatementNode> statements = SpecsFactory.newArrayList();
	statements.add(statement);

	// Apply each rule to list of statement
	for (MatlabToMatlabRule rule : MatToMatUtils.rules) {
	    statements = applyRule(rule, statements, data);
	}

	return statements;
    }

    /**
     * @param rule
     * @param statements
     * @return
     */
    private static List<StatementNode> applyRule(MatlabToMatlabRule rule, List<StatementNode> statements,
	    MatlabToCFunctionData data) {

	List<StatementNode> processedStatements = SpecsFactory.newArrayList();

	for (StatementNode statement : statements) {
	    // Check if rule applies
	    if (!rule.check(statement, data)) {
		processedStatements.add(statement);
		continue;
	    }

	    // Process statements
	    List<MatlabNode> newStatements = null;
	    try {
		newStatements = rule.apply(statement, data);
	    } catch (Exception e) {
		SpecsLogs.msgInfo("Could not apply MATLAB tree transformation:" + e.getMessage());
		newStatements = SpecsFactory.newArrayList(1);
		newStatements.add(statement);
	    }

	    List<StatementNode> newStatementsCasted = newStatements.stream().map(node -> (StatementNode) node)
		    .collect(Collectors.toList());
	    processedStatements.addAll(newStatementsCasted);
	}

	return processedStatements;
    }
}
