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

package org.specs.MatlabProcessor.MatlabParser.Rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.ClassdefNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.RootNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;
import org.specs.MatlabProcessor.Exceptions.ParserErrorException;
import org.specs.MatlabProcessor.Reporting.ProcessorReportService;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.reporting.ReporterUtils;

/**
 * Rules to be applied to the root token as a whole.
 * 
 * @author JoaoBispo
 *
 */
public class FileRules {

    private final String filename;
    private final StringProvider code;
    private final ProcessorReportService reportService;

    public FileRules(String filename, StringProvider code, ProcessorReportService reportService) {
	this.filename = filename;
	this.code = code;
	this.reportService = reportService;
    }

    /**
     * Creates a FileNode from a list of Statements.
     * 
     * @param mfileStatements
     * @return
     */
    public FileNode parse(List<StatementNode> mfileStatements) {
	// Create temporary script node
	ScriptNode scriptNode = RootNodeFactory.newScript(mfileStatements);

	// Create blocks for stray 'FunctionDeclarations'. Due to MATLAB syntax,
	// not all functions need to have an 'end'
	// statement, after blocks for all 'end' statements have been found,
	// build remaining FunctionDeclaration.
	// Also, FunctionDeclaration blocks need to include preceding comments.
	createFunctionDeclarationBlocks(scriptNode);

	// If there is any kind of statement that is not a BlockSt with a FuncDec or Classdef, insert inside next block
	includeCommentsInsideBlocks(scriptNode);

	// Create appropriate nodes (Script if a script, Class is a class, list
	// of Functions if Functions)
	return buildFile(scriptNode);
    }

    private static void includeCommentsInsideBlocks(ScriptNode scriptNode) {

	// Get indexes of valid blocks
	List<StatementNode> statements = scriptNode.getChildren(StatementNode.class);
	List<Integer> validBlockIndexes = getValidIndexes(statements);

	// If Script, there are no indexes
	if (validBlockIndexes.isEmpty()) {
	    return;
	}

	// Iterate over the n-1 first blocks
	int startIndex = 0;
	for (int i = 0; i < validBlockIndexes.size(); i++) {
	    // Check if block index is larger than start index
	    int blockIndex = validBlockIndexes.get(i);

	    // If the same, advance
	    if (startIndex == blockIndex) {
		startIndex = blockIndex + 1;
		continue;
	    }

	    // Otherwise, add all statements until block index at the head of the block
	    MatlabNode block = statements.get(blockIndex);
	    for (int j = blockIndex - 1; j >= startIndex; j--) {
		block.addChild(0, statements.get(j));
	    }

	    startIndex = blockIndex + 1;
	}

	// Last block is special, can include statements before and after
	if (validBlockIndexes.get(validBlockIndexes.size() - 1) < statements.size() - 1) {
	    startIndex = validBlockIndexes.get(validBlockIndexes.size() - 1) + 1;
	    MatlabNode block = statements.get(validBlockIndexes.get(validBlockIndexes.size() - 1));
	    for (int i = startIndex; i < statements.size(); i++) {
		block.addChild(statements.get(i));
	    }
	}

	// Add only blocks to file
	List<MatlabNode> children = new ArrayList<>();
	for (Integer index : validBlockIndexes) {
	    children.add(statements.get(index));
	}

	scriptNode.setChildren(children);
    }

    private static List<Integer> getValidIndexes(List<StatementNode> statements) {
	List<Integer> validIndexes = new ArrayList<>();

	for (int i = 0; i < statements.size(); i++) {
	    StatementNode node = statements.get(i);

	    if (!(node instanceof BlockSt)) {
		continue;
	    }

	    // Check if a block of interest
	    for (StatementNode statement : node.getChildren(StatementNode.class)) {

		if (statement instanceof FunctionDeclarationSt
			|| statement instanceof ClassdefSt) {

		    validIndexes.add(i);
		    continue;
		}
	    }
	}

	return validIndexes;
    }

    private FileNode buildFile(ScriptNode scriptNode) {
	// At this point, there are three possibilities:
	// 1) There is a Class Block and node should be a Class;
	// 2) There is one or more Function Block and node should be a Function;
	// 3) Neither of the above, and node should be a Script;

	// Get first block that is either a Classdef or a FunctionDec
	Optional<StatementNode> firstBlockSt = getFirstDefiningBlockSt(scriptNode);

	// If no block found, it is script, return node as-is
	if (!firstBlockSt.isPresent()) {
	    return RootNodeFactory.newFile(Arrays.asList(scriptNode), filename, code);
	}

	// If statement is a classdef, build a ClassNode
	// TODO: Loosing comments before classdef
	if (firstBlockSt.get() instanceof ClassdefSt) {
	    ClassdefNode classdefNode = RootNodeFactory.newClass(scriptNode.getStatements());
	    return RootNodeFactory.newFile(Arrays.asList(classdefNode), filename, code);
	}

	if (firstBlockSt.get() instanceof FunctionDeclarationSt) {
	    // At this point should only have blocks, an each block correspond to a function
	    List<FunctionNode> functions = new ArrayList<>();

	    // System.out.println("CHILDREN:" + scriptNode.numChildren());
	    for (MatlabNode functionBlock : scriptNode.getChildren()) {
		assert functionBlock instanceof BlockSt;
		int line = ((BlockSt) functionBlock).getLine();
		// System.out.println("FBLOCK:" + functionBlock.getNodeName());
		List<StatementNode> statements = functionBlock.getChildren(StatementNode.class);
		FunctionNode function = RootNodeFactory.newFunction(line, statements);
		functions.add(function);
	    }

	    // Check that the name of the first function is the same as the filename
	    checkFunctions(functions);

	    return RootNodeFactory.newFile(functions, filename, code);
	}

	throw new ParserErrorException("Case not defined:" + firstBlockSt.get().getNodeName());
    }

    private void checkFunctions(List<FunctionNode> functions) {

	// If there is no filename defined, return
	if (filename.equals(FileNode.getNoFilename())) {
	    return;
	}

	// Get main function
	FunctionNode mainFunction = functions.get(0);

	// If function name equals the filename without extension, return
	String functionName = mainFunction.getFunctionName();
	String expectedFunctionName = SpecsIo.removeExtension(filename);
	if (expectedFunctionName.equals(functionName)) {
	    return;
	}

	// Found main function in a file with a different name
	// MATLAB accepts files like this, but will refer to the function by the name of the file
	// We can safely correct the name of the function, if there is a sub-function in the same file with the same
	// name, when called it will cause an infinite recursion in MATLAB.
	// System.out.println("DEC BEFORE:" + functionNode.getDeclarationNode());
	FunctionDeclarationSt declarationNode = mainFunction.getDeclarationNode();
	declarationNode.replaceNameNode(MatlabNodeFactory.newIdentifier(expectedFunctionName));
	// System.out.println("DEC AFTER:" + functionNode.getDeclarationNode());

	String warningMsg = "Found a main function in file '" + filename
		+ "' with a different name than the file('" + functionName
		+ "'). Corrected tree, now function name is '" + expectedFunctionName + "'";

	int lineNumber = mainFunction.getDeclarationNode().getLine();
	reportService.setCurrentLine(lineNumber, ReporterUtils.getErrorLine(code.getString(), lineNumber));
	reportService.warn(warningMsg);

	// Check if there is a sub-function with the same name of the expected main function name
	for (int i = functions.size() - 1; i >= 1; i--) {
	    // Ignore function if it has the same name as the expected main function
	    FunctionNode functionNode = functions.get(i);
	    if (!functionNode.getFunctionName().equals(expectedFunctionName)) {
		continue;
	    }

	    lineNumber = functionNode.getDeclarationNode().getLine();
	    reportService.setCurrentLine(lineNumber, ReporterUtils.getErrorLine(code.getString(), lineNumber));

	    // Found sub-function with the same name as the main function, remove it
	    functions.remove(i);

	    reportService.warn("Found sub function in file '" + filename
		    + " with the same name as the file('" + functionName
		    + "')'. Removing sub-function, it is unaccessible");
	}

	return;
    }

    private static Optional<StatementNode> getFirstDefiningBlockSt(ScriptNode scriptNode) {

	MatlabNodeIterator iterator = scriptNode.getChildrenIterator();

	// Return first block that is either a class of a function declaration
	while (iterator.hasNext()) {
	    StatementNode node = (StatementNode) iterator.next();

	    // Check if block
	    if (!(node instanceof BlockSt)) {
		continue;
	    }

	    // Check if a block of interest
	    for (StatementNode statement : node.getChildren(StatementNode.class)) {

		if (statement instanceof FunctionDeclarationSt
			|| statement instanceof ClassdefSt) {

		    return Optional.of(statement);
		}
	    }

	}

	return Optional.empty();
    }

    private static void createFunctionDeclarationBlocks(ScriptNode scriptNode) {

	List<StatementNode> statements = new ArrayList<>(scriptNode.getStatements());

	// Start at the end
	int endIndex = statements.size() - 1;
	for (int i = statements.size() - 1; i >= 0; i--) {

	    // Check if current index is a stray function declaration
	    if (!(statements.get(i) instanceof FunctionDeclarationSt)) {
		continue;
	    }

	    // Found a stray Function Declaration. Get start and end indexes of the function
	    // int endIndex = statements.size() - 1;
	    int startIndex = findStartIndex(statements, i);

	    // Build block
	    int line = statements.get(endIndex).getLine();
	    BlockSt block = StatementFactory.newBlock(line, statements.subList(startIndex, endIndex + 1));

	    // Remove all block nodes except last one
	    for (int j = endIndex; j > startIndex; j--) {
		statements.remove(j);
	    }

	    // Replace first statement
	    statements.set(startIndex, block);

	    // Update i and end index
	    i = startIndex;
	    endIndex = startIndex - 1;
	}

	scriptNode.setChildren(statements);

	/*
	// Find FunctionDeclarations that are not inside blocks
	MatlabNodeIterator iterator = scriptNode.getChildrenIterator();

	Optional<FunctionDeclarationSt> funcDec = iterator.next(FunctionDeclarationSt.class);

	// While there are stray function declarations, build blocks with them
	while (funcDec.isPresent()) {

	}
	*/

	// System.out.println("SCRUP:" + scriptNode);
	// System.out.println("CROPA");
    }

    private static int findStartIndex(List<StatementNode> statements, int index) {
	// While there are comments before index, and statements go back
	int startIndex = index;

	boolean foundStartIndex = false;
	while (!foundStartIndex) {
	    // Check if there are more statements
	    if (startIndex == 0) {
		foundStartIndex = true;
		continue;
	    }

	    // Check if node before start index is a comment
	    // If not, found start index
	    StatementNode node = statements.get(startIndex - 1);
	    if (!(node instanceof CommentSt)) {
		foundStartIndex = true;
		continue;
	    }

	    // Previous node is a comment, decrement start index
	    startIndex--;
	}

	return startIndex;
    }

}
