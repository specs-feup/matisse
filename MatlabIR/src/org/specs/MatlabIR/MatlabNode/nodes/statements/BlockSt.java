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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

import pt.up.fe.specs.util.utilities.StringLines;

/**
 * Represents a block of code. All children are of type 'statement' or 'Block'.
 * 
 * <p>
 * The first child is always a statement, which can be of the following types for BaseNodes: <br>
 * 'StatementType.For' <br>
 * 'StatementType.Parfor' <br>
 * 'StatementType.If' <br>
 * 'StatementType.Switch' <br>
 * 'StatementType.Try' <br>
 * 'StatementType.While'
 * 
 * <p>
 * Content is a StatementData, corresponding to the end statement that finishes the block.
 * 
 * @author JoaoBispo
 *
 */
public class BlockSt extends StatementNode {

    BlockSt(int lineNumber, Collection<StatementNode> children) {
        super(new StatementData(lineNumber, true), new ArrayList<>(children));
    }

    protected BlockSt(StatementData data, Collection<StatementNode> children) {
        super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new BlockSt(getData(), Collections.emptyList());
    }

    public List<StatementNode> getStatements() {
        return getChildrenAll(StatementNode.class);
    }

    @Override
    public List<StatementNode> getBodyStatements() {
        // Find header node
        // StatementNode header = getHeaderNode();

        List<StatementNode> allStatements = getStatements();

        // Find index of first child after header node
        // Only need to go until the before-last node
        for (int i = 0; i < allStatements.size() - 1; i++) {
            if (allStatements.get(i).isBlockHeader()) {
                return allStatements.subList(i + 1, allStatements.size());
            }
        }

        // If no node found, probably body is empty, return empty list
        return Collections.emptyList();
    }

    @Override
    public String getStatementCode() {

        // Until a header statement appears, do not add indentation.
        // After that, add indentation to all statements that are not headers.

        StringBuilder builder = new StringBuilder();
        boolean seenHeader = false;
        for (StatementNode node : getStatements()) {
            // Get node code (can span several lines)
            String nodeCode = node.getCode();

            // If header as not appear yet, or node has same indentation as block, do not add indentation
            if (!seenHeader || node.isBlockIndented()) {
                StringLines.newInstance(nodeCode).forEach(line -> builder.append(line).append("\n"));

                // If current node is header, flag
                if (node.isBlockHeader()) {
                    seenHeader = true;
                }
                continue;
            }

            // Add indentation
            StringLines.newInstance(nodeCode).forEach(line -> builder.append(getTab()).append(line).append("\n"));
            /*
            	    // Add identation if not a header, and if first header was already seen
            	    if (!node.isBlockIndented() && seenHeader) {
            		builder.append(getTab());
            	    }
            
            	    builder.append(node.getCode());
            
            	    // if (node.isBlockIndented()) {
            	    if (node.isBlockHeader()) {
            		seenHeader = true;
            	    }
            	    */
        }

        builder.append("end");

        return builder.toString();
    }

    /**
     * 
     * @return the Header node of this block
     */
    public StatementNode getHeaderNode() {
        return getHeaderNodeOptional()
                .orElseThrow(() -> new RuntimeException("Block without header: " + getCode()));
    }

    public Optional<StatementNode> getHeaderNodeOptional() {
        return getStatements().stream()
                .filter(node -> node.isBlockHeader())
                .findFirst();
    }

    /**
     * Returns the line of the block header, or -1 if the block does not have a header
     */
    /*
    @Override
    public int getLine() {
        return getHeaderNodeOptional().map(header -> header.getLine()).orElse(-1);
    }
    */
}
