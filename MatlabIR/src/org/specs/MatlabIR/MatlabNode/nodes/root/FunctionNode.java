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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * The root node of a MatLab function.
 * 
 * <p>
 * The children can be either 'Statement' or 'Block'.
 * 
 * <p>
 * Can only contain one statement of type 'FunctionDeclaration'.
 * 
 * @author JoaoBispo
 *
 */
public class FunctionNode extends MatlabUnitNode {

    // The number of the line that ends the function
    private final int endLine;

    private FunctionNode(int endLine, Collection<? extends MatlabNode> statOrBlockOrFuncDec) {
        super(statOrBlockOrFuncDec);

        this.endLine = endLine;
    }

    static FunctionNode newInstance(int endLine, Collection<StatementNode> statements) {
        return new FunctionNode(endLine, statements);
    }

    /**
     * For compatibility with TOM.
     * 
     * @param content
     * @param children
     */
    public FunctionNode(Object content, Collection<MatlabNode> children) {
        this(-1, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new FunctionNode(endLine, Collections.emptyList());
    }

    @Override
    public String getUnitName() {
        return getFunctionName();
    }

    /**
     * @return the FunctionDeclaration of this Function
     */
    public FunctionDeclarationSt getDeclarationNode() {
        return getFirstChild(FunctionDeclarationSt.class);
    }

    /**
     * Get statements of the function, after the FunctionDeclaration.
     * 
     * 
     * @param function
     * @return
     */
    public List<StatementNode> getFunctionStatements() {
        int indexOfFirstStmt = getIndexOfFirstStmt();
        return getChildren(StatementNode.class).subList(indexOfFirstStmt, getNumChildren());

        /*
        // Find index of the function declaration
        int functionDeclarationIndex = getChildIndex(FunctionDeclarationSt.class);
        
        if (functionDeclarationIndex == -1) {
            throw new RuntimeException("FunctionNode does not have a FunctionDeclaration statement:\n" + this);
        }
        
        // Return all statements after that
        return getChildren(StatementNode.class).subList(functionDeclarationIndex + 1, getNumChildren());
        */
    }

    public String getFunctionName() {
        return getDeclarationNode().getNameNode().getName();
    }

    @Override
    public String getCode() {
        StringBuilder builder = new StringBuilder();

        MatlabNodeIterator iterator = getChildrenIterator();

        boolean seenFunctionDeclaration = false;
        while (iterator.hasNext()) {
            MatlabNode node = iterator.next();

            if (seenFunctionDeclaration) {
                builder.append(getTab());
            }
            builder.append(node.getCode().trim().replace("\n", "\n" + getTab()) + "\n");

            if (node instanceof FunctionDeclarationSt) {
                seenFunctionDeclaration = true;
            }
        }

        // Insert 'end' at the end of function
        builder.append("end\n");

        return builder.toString();
    }

    public int getIndexOfFirstStmt() {
        // Find index of the function declaration
        int functionDeclarationIndex = getChildIndex(FunctionDeclarationSt.class);

        if (functionDeclarationIndex == -1) {
            throw new RuntimeException("FunctionNode does not have a FunctionDeclaration statement:\n" + this);
        }

        return functionDeclarationIndex + 1;
    }

    /**
     * Sometimes, after the 'end' of a function there can be additional comment statements. This function ignores those
     * comments and returns the statements up to the corresponding function end.
     * 
     * @return the statements up to the 'end' of the function.
     */
    public List<StatementNode> getBodyStatements() {
        if (endLine < 1) {
            SpecsLogs.warn("Field 'endLine' was initialized with a value lower than 1, returning all statements");
            return getStatements();
        }

        List<StatementNode> bodySt = getFunctionStatements();

        if (bodySt.isEmpty()) {
            return Collections.emptyList();
        }

        // Find the first index, starting from the last, that corresponds to a statement whose line is the same or lower
        // than the line where the function ends

        int endIndex = IntStream.iterate(bodySt.size(), i -> i - 1)
                .filter(i -> bodySt.get(i - 1).getLine() <= endLine)
                .findFirst().getAsInt();
        /*
        	int endIndex = bodySt.size();
        	for (int i = bodySt.size() - 1; i >= 0; i--) {
        	    if (bodySt.get(i).getLine() <= endLine) {
        		break;
        	    }
        
        	    endIndex--;
        	}
         */
        return bodySt.subList(0, endIndex);
    }

    public int getEndLine() {
        return endLine;
    }

    /**
     * A Function scope is a list with the function name, if it is the main function, or with the main function followed
     * by the function name, if it is a subfunction.
     * 
     * @return
     */
    public List<String> getScope() {
        String functionName = getFunctionName();

        if (!hasParent()) {
            SpecsLogs.warn("CHECK: Should it have a parent?");
            return Arrays.asList(functionName);
        }

        FunctionNode firstFunction = getParent().getFirstChild(FunctionNode.class);

        if (firstFunction == this) {
            return Arrays.asList(functionName);
        }

        String mainFunction = firstFunction.getFunctionName();

        return Arrays.asList(mainFunction, functionName);
    }

    public List<String> getInputNames() {
        return getDeclarationNode().getInputNames();
    }

    public List<String> getOutputNames() {
        return getDeclarationNode().getOutputNames();
    }

    public List<IdentifierNode> getInputs() {
        return getDeclarationNode().getInputs().getNamesNodes();
    }

    public List<MatlabNode> getOutputs() {
        return getDeclarationNode().getOutputs().getNodes();
    }

    @Override
    public int getNumInputs() {
        return getInputs().size();
    }

    /**
     * Returns the line of the FunctionDeclaration.
     */
    @Override
    public int getLine() {
        return getDeclarationNode().getLine();
    }

    public FileNode getFile() {
        MatlabNode parent = getParent();
        if (parent == null) {
            throw new RuntimeException("FunctionNode is not associated with a file");
        }

        return (FileNode) parent;
    }

    public boolean isMainFunction() {
        return getFile().getMainFunction() == this;
    }

    public String getId() {
        FileNode file = getFile();

        // Main function
        if (isMainFunction()) {
            return file.getFilename() + "%" + getFunctionName();
        }

        // Sub function
        return file.getFilename() + "%" + file.getMainFunction().getFunctionName() + "%" + getFunctionName();
    }

}
