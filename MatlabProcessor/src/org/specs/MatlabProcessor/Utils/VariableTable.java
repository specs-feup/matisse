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

package org.specs.MatlabProcessor.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeUtils;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.collections.ScopedMap;

/**
 * Parses MATLAB trees and identifies Variables inside functions.
 * 
 * @author Joao Bispo
 * 
 */
public class VariableTable {

    private static final List<String> SCRIPT_SCOPE = Arrays.asList("_scrip_scope");

    private final ScopedMap<Set<String>> variableMap;

    public VariableTable() {
        variableMap = ScopedMap.newInstance();
    }

    public void addToken(String filename, FileNode token) {
        // Search for all the functions and add them
        List<FunctionNode> functions = token.getFunctions();

        for (FunctionNode function : functions) {
            addFunction(filename, function);
        }

        // Search for all the scripts and add them
        Optional<ScriptNode> script = token.getScriptTry();
        if (script.isPresent()) {
            addScript(filename, script.get());
        }
        /*
        List<MatlabNode> scripts = TokenUtils.getChildrenRecursively(token, MType.Script);
        for (MatlabNode script : scripts) {
        addScript(filename, script);
        }
        */

    }

    /**
     * @param filename
     * @param script
     */
    private void addScript(String filename, ScriptNode script) {
        // Build scope
        List<String> scope = buildScope(filename, null);

        // Add variables from statements
        // Add all statements recursively, including those inside blocks
        List<StatementNode> stmts = script.getDescendantsAndSelf(StatementNode.class);

        // List<StatementNode> stmts = script.getDescendantsAndSelf(StatementNode.class);
        for (StatementNode statement : stmts) {
            addStatementVars(scope, statement);
        }
    }

    /**
     * @param filename
     * @param function
     */
    private void addFunction(String filename, FunctionNode function) {

        // Get function name
        String functionName = function.getFunctionName();
        // Build scope
        List<String> scope = buildScope(filename, functionName);

        // Add function inputs
        List<String> inputNames = function.getDeclarationNode().getInputNames();
        for (String inputName : inputNames) {
            addVariable(scope, inputName);
        }

        // Add variables from statements
        // Get all statements recursively
        List<StatementNode> stmts = function.getDescendantsAndSelf(StatementNode.class);
        for (StatementNode statement : stmts) {
            addStatementVars(scope, statement);
        }

    }

    /**
     * @param filename
     * @param functionName
     * @param function
     */
    private void addStatementVars(List<String> scope, StatementNode statement) {

        if (statement instanceof AssignmentSt) {
            addAssignVars(scope, (AssignmentSt) statement);
        }
        if (statement instanceof GlobalSt) {
            addGlobalVars(scope, (GlobalSt) statement);
        }

        if (statement instanceof ForSt) {
            addForVars(scope, (ForSt) statement);
        }
    }

    /**
     * @param scope
     * @param forStatement
     */
    private void addForVars(List<String> scope, ForSt forStatement) {

        // Get indunction var id
        IdentifierNode inductionId = forStatement.getIndexVar();
        // Get name
        String inductionVarName = inductionId.getName();
        // Add name
        addVariable(scope, inductionVarName);
    }

    /**
     * @param scope
     * @param assignStatement
     */
    private void addAssignVars(List<String> scope, AssignmentSt assignStatement) {
        // Left-Hand token
        MatlabNode leftHand = assignStatement.getLeftHand();

        // Get variables names
        List<String> outputNames = MatlabNodeUtils.getVariableNames(leftHand);

        // Add names
        for (String outputName : outputNames) {
            addVariable(scope, outputName);
        }
    }

    /**
     * @param scope
     * @param assignStatement
     */
    private void addGlobalVars(List<String> scope, GlobalSt globalStatement) {
        // Add names
        for (String outputName : globalStatement.getIdentifiers()) {
            addVariable(scope, outputName);
        }
    }

    /**
     * @param scope
     * @param inputName
     */
    private void addVariable(List<String> scope, String inputName) {

        // Check if set already exists for given scope
        Set<String> variables = variableMap.getSymbol(scope);
        if (variables == null) {
            variables = SpecsFactory.newHashSet();
            variableMap.addSymbol(scope, variables);
        }

        // Add input name
        variables.add(inputName);

    }

    /**
     * @param filename
     * @param functionName
     * @return
     */
    private static List<String> buildScope(String filename, String functionName) {
        // Check if function name is null
        if (functionName == null) {
            return VariableTable.SCRIPT_SCOPE;
        }

        List<String> scope = SpecsFactory.newArrayList();

        // Remove extension from filename, if present
        filename = SpecsIo.removeExtension(filename);

        if (!filename.equals(functionName)) {
            scope.add(filename);
        }

        scope.add(functionName);

        return scope;
    }

    public boolean containsVariable(String filename, String functionName, String name) {
        List<String> scope = buildScope(filename, functionName);

        Set<String> variables = variableMap.getSymbol(scope);
        if (variables == null) {
            return false;
        }

        return variables.contains(name);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return variableMap.toString();
    }

}
