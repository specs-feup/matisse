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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;

public class RootNodeFactory {

    /**
     * Builds a Class node, where all children are of the type 'statement', or 'block'.
     * 
     * @param statements
     * @return
     */
    public static ClassdefNode newClass(List<StatementNode> statements) {

        return ClassdefNode.newInstance(statements);
    }

    /**
     * Helper method with varargs.
     * 
     * @param statements
     * @return
     */
    /*
    public static FunctionNode newFunction(StatementNode... statements) {
    return RootNodeFactory.newFunction(Arrays.asList(statements));
    }
    */

    /**
     * Builds a Script node, where all children are of the type 'statement', or 'block'.
     * 
     * @param statements
     * @return
     */
    public static ScriptNode newScript(List<StatementNode> statements) {
        /*
        if (!CompatibilityUtils.areStatements(statements)) {
        throw new RuntimeException("Nodes must be statements");
        }
        */

        return ScriptNode.newInstance(statements);
    }

    public static FileNode newFile(Collection<? extends MatlabUnitNode> nodes, File mFile) {
        return RootNodeFactory.newFile(nodes, mFile.getName(), StringProvider.newInstance(mFile));
        // return RootNodeFactory.newFile(nodes, mFile, StringProvider.newInstance(mFile));
    }

    /**
     * @param nodes
     * @return a new FileNode
     */
    // public static FileNode newFile(Collection<? extends MatlabUnitNode> nodes, File file,
    // StringProvider originalCode) {
    public static FileNode newFile(Collection<? extends MatlabUnitNode> nodes, String filename,
            StringProvider originalCode) {

        assert !nodes.isEmpty();

        // TODO: Accept file also
        FileNodeContent content = new FileNodeContent(filename, originalCode);

        // Check the first type of the node
        MatlabUnitNode firstUnit = nodes.stream().findFirst().get();

        if (firstUnit instanceof ScriptNode) {

            // Throw exception if more than one
            if (nodes.size() != 1) {
                throw new CodeParsingException("Expected one ScriptNode, found " + nodes.size() + " nodes:\n" + nodes);
            }
            return RootNodeFactory.newScriptFile((ScriptNode) firstUnit, content);
        }

        if (firstUnit instanceof ClassdefNode) {

            // Throw exception if more than one
            if (nodes.size() != 1) {
                throw new CodeParsingException(
                        "Expected one ClassdefNode, found " + nodes.size() + " nodes:\n" + nodes);
            }
            return RootNodeFactory.newClassFile((ClassdefNode) firstUnit, content);
        }

        // Remaining case if a FunctionFile

        // All nodes must be FunctionNode, otherwise throw exception
        if (nodes.stream().filter(node -> !(node instanceof FunctionNode)).findFirst().isPresent()) {
            throw new CodeParsingException("Expected all nodes to be FunctionNode:\n" + nodes);
        }

        List<FunctionNode> functionNodes = nodes.stream().map(node -> (FunctionNode) node).collect(Collectors.toList());

        return RootNodeFactory.newFunctionFile(functionNodes, content);
        /*
        	// If the first type is Script, check that it is the only child
        	boolean isScript = false;
        	Optional<? extends MatlabUnitNode> firstNode = nodes.stream().findFirst();
        	if (firstNode.isPresent() && firstNode.get() instanceof ScriptNode) {
        	    if (nodes.size() != 1) {
        		throw new RuntimeException("When '" + FileNode.class.getSimpleName() + "' receives a  '"
        			+ firstNode.get().getClass().getSimpleName() + "', it can only have one child, received "
        			+ nodes.size() + " children:\n" + nodes);
        	    }
        
        	    isScript = true;
        	}
        
        	// Check if all children are of the type Function or Class
        	if (!isScript) {
        	    Set<MatlabNodeType> allowedTypes = new HashSet<>(Arrays.asList(MType.Function, MType.Class));
        	    CheckUtils.allMatch(nodes, allowedTypes);
        	}
        
        	return new FileNode(nodes, filename, wholeFile);
        
        	return FileNode.newInstance(nodes, filename, wholeFile);
        	*/

    }

    public static ClassFileNode newClassFile(ClassdefNode node, FileNodeContent content) {
        return new ClassFileNode(node, content);
    }

    public static FunctionFileNode newFunctionFile(Collection<FunctionNode> node, FileNodeContent content) {
        return new FunctionFileNode(node, content);
    }

    public static ScriptFileNode newScriptFile(ScriptNode node, FileNodeContent content) {
        return new ScriptFileNode(node, content);
    }

    /**
     * Creates a function token. Begin index is the index of a statement token of the type FunctionDeclaration. The
     * statementList is made with statements from beginIndex+1 (inclusive) to endIndex (exclusive).
     * 
     * @param statements
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static FunctionNode newFunction(int endLine, List<StatementNode> statements) {

        // Needs one FunctionDeclaration (and only one)
        long funcDecCount = statements.stream()
                .filter(node -> node instanceof FunctionDeclarationSt)
                .count();
        Preconditions.checkArgument(funcDecCount == 1, "Function needs one and only one FunctionDeclaration, found "
                + funcDecCount);

        FunctionNode node = FunctionNode.newInstance(endLine, statements);

        return node;
    }

}
