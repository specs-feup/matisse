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

import java.util.List;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.Instructions.InstructionType;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 *
 * Represents a block of code. All children are of type 'Instruction' or 'Block'.
 * 
 * <p>
 * Content: InstructionType
 * 
 * @author Joao Bispo
 *
 */
public class BlockNode extends CNode {
    // public class BlockNode extends InstructionNode {

    /**
     * @param type
     * @param content
     * @param children
     */
    private BlockNode(List<CNode> instructions) {
        super(instructions);
    }

    /**
     * Empty constructor
     */
    private BlockNode() {
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new BlockNode();
    }

    /**
     * * Creates a new Block token with the given instructions.
     * 
     * <p>
     * Verifies if each of the given instructions is of type Instruction. If any of the tokens is not an instruction,
     * tries to infer the instruction type and builds the instruction type.
     * 
     * @return
     */
    static BlockNode create(List<CNode> blockInstructions) {
        List<CNode> insts = SpecsFactory.newArrayList();
        for (CNode token : blockInstructions) {
            if (token instanceof InstructionNode) {
                insts.add(token);
                continue;
            }

            InstructionType type = CInstructionList.getInstructionType(token);
            CNode inst = new InstructionNode(type, token);
            insts.add(inst);
        }

        return new BlockNode(insts);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        // Print all lines with a tab, except those up to block starting
        // statements, and
        // middle block statements
        StringBuilder builder = new StringBuilder();

        boolean blockStarted = false;
        for (CNode child : getChildren()) {
            String childString = child.getCode();

            // LineStream lineReader = StringLines.newInstance(childString);

            InstructionType instType = InstructionType.Undefined;
            if (child instanceof InstructionNode) {
                instType = ((InstructionNode) child).getInstructionType();
            }

            // Until a child of type block start statement appears,
            // print everything in the current indentation. Afterwards,
            // increase indentation.
            boolean isBlockStart = InstructionType.isInstructionBlockStart(instType);
            if (isBlockStart) {
                blockStarted = true;
            }

            // String line = null;
            // while ((line = lineReader.nextLine()) != null) {
            for (String line : StringLines.newInstance(childString)) {
                // Check if is middle statement
                if (!isBlockStart && blockStarted) {
                    builder.append(CNodeUtils.getDefaultTab());
                }

                builder.append(line);
                // Avoid new line on the block start to implement '{' code style
                // in line block starts
                if (!isBlockStart) {
                    builder.append("\n");
                }
            }
            // if (InstructionType.isInstructionBlockStart(instType)) {
            if (isBlockStart) {
                builder.append("{\n");
            }

        }

        if (blockStarted) {
            builder.append("}\n\n");
        }

        return builder.toString();
    }

    @Override
    public String toReadableString() {
        // Print all lines with a tab, except those up to block starting
        // statements, and
        // middle block statements
        StringBuilder builder = new StringBuilder();

        boolean blockStarted = false;
        for (CNode child : getChildren()) {
            String childString = child.toReadableString();

            // LineStream lineReader = LineStream.createLineReader(childString);

            InstructionType instType = InstructionType.Undefined;
            if (child instanceof InstructionNode) {
                instType = ((InstructionNode) child).getInstructionType();
            }

            // Until a child of type block start statement appears,
            // print everything in the current indentation. Afterwards,
            // increase indentation.
            boolean isBlockStart = InstructionType.isInstructionBlockStart(instType);
            if (isBlockStart) {
                blockStarted = true;
            }

            // String line = null;
            // while ((line = lineReader.nextLine()) != null) {
            for (String line : StringLines.newInstance(childString)) {

                // Check if is middle statement
                if (!isBlockStart && blockStarted) {
                    builder.append(CNodeUtils.getDefaultTab());
                }

                builder.append(line);
                // Avoid new line on the block start to implement '{' code style
                // in line block starts
                if (!isBlockStart) {
                    builder.append("\n");
                }
            }
            // if (InstructionType.isInstructionBlockStart(instType)) {
            if (isBlockStart) {
                builder.append("{\n");
            }

        }

        if (blockStarted) {
            builder.append("}\n\n");
        }

        return builder.toString();
    }

    public InstructionNode getHeader() {
        return getChild(InstructionNode.class, 0);
    }
}
