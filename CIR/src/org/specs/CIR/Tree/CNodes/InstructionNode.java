/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Tree.CNodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.CIR.CodeGenerator.CodeGeneratorRule;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.Instructions.InstructionRules;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.VariableType;

/**
 * Represents a C statement.
 * <p>
 * Content: InstructionType
 * 
 * @author Joao Bispo
 *
 */
public class InstructionNode extends CNode {

    private final InstructionType type;

    /**
     * @param type
     * @param content
     * @param children
     */
    InstructionNode(InstructionType type, CNode... ctokens) {
        // super(CNodeType.Instruction, type, ctokens);
        this(type, Arrays.asList(ctokens));
    }

    InstructionNode(InstructionType type, List<CNode> ctokens) {
        super(ctokens);

        this.type = type;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new InstructionNode(getInstructionType());
    }

    public InstructionType getInstructionType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getVariableType()
     */
    @Override
    public VariableType getVariableType() {
        InstructionType iType = getInstructionType();

        // First child of a FunctionCall instruction is a FunctionCall token
        if (iType == InstructionType.FunctionCall) {
            return getChildren().get(0).getVariableType();
        }

        throw new RuntimeException("VariableType not defined for instructions of the type '" + iType + "'");
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        // Check some special cases
        InstructionType type = getInstructionType();

        Map<InstructionType, CodeGeneratorRule> instRules = InstructionRules.getRules();
        CodeGeneratorRule rule = instRules.get(type);
        if (rule != null) {
            return rule.apply(this);
        }

        // Otherwise, just convert the tokens
        StringBuilder builder = new StringBuilder();
        getChildren().forEach(token -> builder.append(token.getCode()));
        String statement = builder.toString();

        // String statement = "";
        // if (cToken.hasChildren()) {
        // statement = CodeGeneratorUtils.tokenCode(cToken.getChildren());
        // }

        // Comments and literals do not need a ';'
        if (type == InstructionType.Comment || type == InstructionType.Literal) {
            return statement + "\n";
        }

        // If instruction has more than one line, do not append ";\n"
        if (statement.trim().contains("\n")) {
            return statement;
        }

        // If statement is empty, just ignore it
        if (statement.isEmpty()) {
            return "\n";
        }

        return statement + ";\n";
    }

    @Override
    public String toReadableString() {
        // Check some special cases
        InstructionType type = getInstructionType();

        StringBuilder builder = new StringBuilder();
        getChildren().forEach(token -> builder.append(token.toReadableString()));
        String statement = builder.toString();

        // String statement = "";
        // if (cToken.hasChildren()) {
        // statement = CodeGeneratorUtils.tokenCode(cToken.getChildren());
        // }

        // Comments and literals do not need a ';'
        if (type == InstructionType.Comment || type == InstructionType.Literal) {
            return statement + "\n";
        }

        // If instruction has more than one line, do not append ";\n"
        if (statement.trim().contains("\n")) {
            return statement;
        }

        // If statement is empty, just ignore it
        if (statement.isEmpty()) {
            return "\n";
        }

        return statement + ";\n";
    }
}
