/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR.CodeGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.CIR.CFile;
import org.specs.CIR.CirUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.MultiMap;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * TODO: Move functions to corresponding classes (e.g., cFileCode to CFile)
 * 
 * @author Joao Bispo
 */
public class CodeGeneratorUtils {

    public static final String TAB = "   ";

    /**
     * @return the tab
     */
    public static String getTab() {
        return CodeGeneratorUtils.TAB;
    }

    /**
     * Generates the code for 'cToken', minding expression solvers.
     * 
     * <p>
     * For instance, when converting CNumbers, does not put 'f' or 'l' suffixes for float and long types.
     * 
     * @param token
     * @return
     */
    public static String expressionCode(CNode token) {

        SpecsLogs.warn("CHECK: Replaced depthIterator with getDescendantsAndSelf, check if is ok");

        // Deep copy of given token
        CNode exprToken = token.copy();

        // TokenTester cnumberTest = IteratorUtils.newTypeTest(CNumberNode.class);
        // Iterator<CNode> iterator = IteratorUtils.getDepthIterator(exprToken, cnumberTest);
        List<CNumberNode> numbers = exprToken.getDescendantsAndSelf(CNumberNode.class);
        // for (CNode cNumber : CollectionUtils.iterable(iterator)) {
        for (CNumberNode cNumber : numbers) {
            // Get number
            Number number = cNumber.getCNumber().getNumber();

            // Create literal
            CNode literal = CNodeFactory.newLiteral(number.toString());

            // Replace token with a literal
            NodeInsertUtils.set(cNumber, literal);
        }

        String code = exprToken.getCode();

        return code;
    }

    /**
     * Generates the code for a function call.
     * 
     * @param functionName
     * @param functionInputTypes
     * @param returnType
     * @param inputArgs
     * @return
     */
    public static String functionCallCode(String functionName, List<VariableType> functionInputTypes,
            List<CNode> inputArgs) {

        StringBuilder builder = new StringBuilder();

        builder.append(functionName);
        builder.append("(");

        int fTypesCounter = 0;

        // Write remaining arguments
        for (int i = 0; i < inputArgs.size(); i++) {

            CNode inputArg = inputArgs.get(i);
            // VariableType inputType = inputArg.getVariableType();

            // If not the first argument, add comma
            if (fTypesCounter != 0) {
                builder.append(", ");
            }

            // Check if pointer HACK
            assert fTypesCounter < functionInputTypes.size() : "Out of range (" + fTypesCounter + ") in "
                    + functionName + "\ninput types=" + functionInputTypes + "\nargs=" + inputArgs;
            VariableType functionType = functionInputTypes.get(fTypesCounter);

            // TODO_NORM
            fTypesCounter += 1;
            /*
            if (functionType instanceof PointerType) {
            System.out.println("FUNCTION TYPE:" + functionType);
            System.out.println("INPUT ARG:" + inputArg);
            System.out.println("CODE NORMAL:" + inputArg.getCode());
            System.out.println("CODE POINTER:" + inputArg.getCodeAsPointer());
            }
            */

            String arg = ReferenceUtils.isPointer(functionType) ? inputArg
                    .getCodeAsPointer() : inputArg.getCode();

            builder.append(arg);
        }

        builder.append(")");

        return builder.toString();
    }

    /**
     * Helper method which receives a FunctionInstance.
     * 
     * @param instance
     * @return
     */
    public static String functionDeclarationCode(FunctionInstance instance) {
        return functionDeclarationCode(instance.getComments(), instance.getFunctionType(), instance.getCName());
    }

    /**
     * Generates the function declaration to be used when building the body of a function.
     * 
     * @param instance
     * @return
     */
    public static String functionDeclarationCode(List<String> comments, FunctionType functionTypes,
            String functionName) {

        StringBuilder builder = new StringBuilder();

        // Add function comments
        // builder.append(getFunctionComments(comments));

        String prefix = functionTypes.getFunctionPrefix();
        assert prefix != null : "Null function prefix on instance " + functionName;
        builder.append(prefix);

        // Add return type
        VariableType returnType = functionTypes.getCReturnType();

        // builder.append(VariableCode.getTypeDeclaration(returnType));
        builder.append(CodeUtils.getReturnType(returnType));
        builder.append(" ");

        // Add function C name
        builder.append(functionName);

        // Append input declaration
        builder.append("(");

        List<String> names = functionTypes.getCInputNames();
        List<VariableType> types = functionTypes.getCInputTypes();

        builder.append(inputsDeclarationCode(names, types));
        builder.append(")");

        return builder.toString();
    }

    /**
     * Creates input declaration from the given list of Variables.
     * 
     * <p>
     * E.g., "int32_t a, int32_t b"
     * 
     * @param inputVariables
     * @return
     */
    public static String inputsDeclarationCode(List<String> inputNames, List<VariableType> inputTypes) {

        StringBuilder builder = new StringBuilder();

        if (!inputNames.isEmpty()) {
            String inputName = inputNames.get(0);
            VariableType inputType = inputTypes.get(0);
            builder.append(VariableCode.getInputsDeclaration(inputName, inputType));
        }

        for (int i = 1; i < inputNames.size(); i++) {
            String inputName = inputNames.get(i);
            VariableType inputType = inputTypes.get(i);
            builder.append(", ");
            builder.append(VariableCode.getInputsDeclaration(inputName, inputType));
        }

        return builder.toString();
    }

    /**
     * Generates the code for the H file represented by 'cModule'.
     * 
     * @param cModule
     * @return
     */
    public static String hFileCode(CFile cModule) {
        StringBuilder builder = new StringBuilder();

        // Comment
        builder.append("/* Header file for ");
        builder.append(cModule.getModuleName());
        builder.append(" */\n\n");

        // ifndef
        String defName = cModule.getIfDefName();
        builder.append("#ifndef ").append(defName).append("\n");
        builder.append("#define ").append(defName).append("\n\n");

        // Header includes
        Set<String> headerIncludes = CirUtils.getHeaderIncludes(cModule);

        String includeCode = getIncludeCode(headerIncludes, cModule.getModuleName());
        builder.append(includeCode);

        // Function declarations
        for (FunctionInstance functionData : cModule.getFunctionList()) {

            if (!functionData.hasDeclaration()) {
                continue;
            }

            // Do not declare main functions
            // if (functionData.getCName().equals("main")) {
            // continue;
            // }

            // String declaration = functionDeclarationHeader(functionData);
            String declaration = functionData.getDeclarationCode();

            builder.append(declaration).append("\n\n");
        }

        builder.append("#endif\n");

        return builder.toString();
    }

    public static String getIncludeCode(Set<String> headerIncludes, String filename) {

        StringBuilder builder = new StringBuilder();

        // Correct includes, in case the include is in a different
        Set<String> correctedIncludes = SpecsFactory.newHashSet();
        for (String include : headerIncludes) {
            if (SystemInclude.isSystemInclude(include)) {
                correctedIncludes.add(include);
                continue;
            }

            // String correctedInclude = IoUtils.getRelativePath(include, filename, "/");
            String correctedInclude = SpecsIo.getRelativePath(new File(include), new File(filename));
            // System.out.println("ORIGINAL INCLUDE, FILENAME:"+include+", "+filename);
            // System.out.println("CORRECTED INCLUDE:"+correctedInclude);
            correctedIncludes.add(correctedInclude);
        }

        // Order includes
        List<String> orderedIncludes = SpecsFactory.newArrayList(correctedIncludes);
        Collections.sort(orderedIncludes);

        // for (String include : headerIncludes) {
        for (String include : orderedIncludes) {
            builder.append(includeCode(include)).append("\n");
        }

        // If has includes, add another newline, to separate the includes from
        // the declarations.
        if (!orderedIncludes.isEmpty()) {
            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * Generates the code for the C file represented by 'cModule'.
     * 
     * @param cModule
     * @return
     */
    public static String cFileCode(CFile cModule) {
        StringBuilder builder = new StringBuilder();

        // Comment
        builder.append("/* Implementation file for ").append(cModule.getModuleName()).append(" */\n\n");

        // Add includes
        Set<String> includes = new HashSet<>();

        // This module include
        if (cModule.hasHeaderFile()) {
            includes.add(cModule.getIncludeSelf());
            // String fullInclude = cModule.getIncludeSelf();

            // Parse include to remove any path it may contain
            // String includeName = (new File(fullInclude)).getName();
            // includes.add(includeName);
        }

        // Header includes -> Do not need, they are already in the header?
        // includes.addAll(CirUtils.getHeaderIncludes(cModule));
        // Body includes
        includes.addAll(CirUtils.getBodyIncludes(cModule));

        String includeCode = getIncludeCode(includes, cModule.getModuleName());
        builder.append(includeCode);

        // Add functions
        for (FunctionInstance functionData : cModule.getFunctionList()) {
            if (!functionData.hasImplementation()) {
                continue;
            }

            String functionImplementation = functionData.getImplementationCode();
            builder.append("\n").append(functionImplementation);
        }

        return builder.toString();
    }

    /**
     * Builds an include line based on the include name. Automatically checks if it is a common system include,
     * according to SystemInclude class.
     * 
     * <p>
     * E.g.: add.h -> #include "add.h" E.g.: stdio.h -> #include <stdio.h>
     * 
     * @param includeName
     * @return
     */
    public static String includeCode(String includeName) {
        boolean isSystemInclude = SystemInclude.isSystemInclude(includeName);
        return includeCode(includeName, isSystemInclude);
    }

    /**
     * Builds an include line based on the include name.
     * 
     * <p>
     * E.g.: add.h, false -> #include "add.h" E.g.: stdio.h, true -> #include <stdio.h>
     * 
     * @param includeName
     * @param isSystemInclude
     * @return
     */
    public static String includeCode(String includeName, boolean isSystemInclude) {
        StringBuilder builder = new StringBuilder();

        builder.append("#include ");

        if (isSystemInclude) {
            builder.append("<");
        } else {
            builder.append("\"");
        }

        builder.append(includeName);

        if (isSystemInclude) {
            builder.append(">");
        } else {
            builder.append("\"");
        }

        return builder.toString();
    }

    public static String functionImplementation(FunctionInstance instance, CInstructionList instructions) {

        List<String> functionComments = instance.getComments();
        String functionDeclarationCode = CodeGeneratorUtils.functionDeclarationCode(instance);
        List<String> inputNames = instance.getFunctionType().getCInputNames();

        String functionBody = functionBody(inputNames, instructions);

        return functionImplementation(functionComments, functionDeclarationCode, functionBody);
    }

    /**
     * A method that just returns the string representation of the function body.
     * 
     * @param instructions
     * @return
     */
    public static String functionImplementation(CInstructionList instructions) {

        List<String> emptyList = SpecsFactory.newArrayList();
        String functionBody = functionBody(emptyList, instructions);

        return functionImplementation(null, "", functionBody);
    }

    public static String getFunctionComments(List<String> comments) {

        if (comments == null) {
            comments = Collections.emptyList();
        }

        StringBuilder builder = new StringBuilder();

        // Add function comments
        builder.append("/**\n");
        for (String comment : comments) {
            builder.append(" * ");
            builder.append(comment);
            builder.append("\n");
        }
        builder.append(" */\n");

        return builder.toString();
    }

    /**
     * Builds the C body for a FunctionImplementation from a list of CTokens.
     * 
     * @param functionComments
     *            can be null
     * @param functionDeclarationCode
     * @param functionBody
     * @return
     */
    public static String functionImplementation(List<String> functionComments, String functionDeclarationCode,
            String functionBody) {

        if (functionComments == null) {
            functionComments = SpecsFactory.newArrayList();
        }

        StringBuilder builder = new StringBuilder();

        // Add function comments
        builder.append(getFunctionComments(functionComments));
        /*
        builder.append("/**\n");
        for (String comment : functionComments) {
        // builder.append("//");
        builder.append(" * ");
        builder.append(comment);
        builder.append("\n");
        }
        */
        // builder.append(" */\n");

        // Add function declaration
        builder.append(functionDeclarationCode);
        builder.append("\n");
        builder.append("{\n");

        builder.append(functionBody);

        builder.append("}\n");

        return builder.toString();
    }

    /**
     * @param inputNames
     * @param instructions
     * @param builder
     * @param tab
     */
    private static String functionBodyOld(List<String> inputNames, CInstructionList instructions) {

        StringBuilder builder = new StringBuilder();
        String tab = CodeGeneratorUtils.getTab();

        // Add variable declaration
        Collection<Variable> functionVars = FunctionInstanceUtils.getFunctionVariables(instructions, inputNames);

        for (Variable var : functionVars) {

            List<String> values = instructions.getInitializations().getValues(var.getName());
            // TODO: Initializations should return empty list
            if (values == null) {
                values = Collections.emptyList();
            }
            builder.append(tab);
            // builder.append(VariableCode.getVariableDeclaration(var.getName(), var.getType(), values));
            builder.append(CodeUtils.getDeclarationWithInputs(var.getType(), var.getName(), values));

            builder.append(";\n");
        }
        builder.append("\n");

        // Add code
        for (CNode token : instructions) {

            String cCode = token.getCode();

            for (String line : StringLines.newInstance(cCode)) {
                // String line;
                // while ((line = lineReader.nextLine()) != null) {
                builder.append(tab);
                builder.append(line);
                builder.append("\n");
            }

        }

        return builder.toString();
    }

    /**
     * Declares variables in the smallest scope possible.
     * 
     * @param inputNames
     * @param instructions
     * @return
     */
    private static String functionBody(List<String> inputNames, CInstructionList instructions) {

        StringBuilder builder = new StringBuilder();
        String tab = CodeGeneratorUtils.getTab();

        // Variables that can be declared at a smaller scope
        MultiMap<CNode, Variable> scopeVars = FunctionInstanceUtils.getScopeVariables(instructions, inputNames);

        // Variables that need to be declared at the function scope
        List<Variable> functionScopeVars = new ArrayList<>();
        for (Variable literal : instructions.getLiteralVariables()) {
            // Don't re-declare function inputs.
            if (!inputNames.contains(literal.getName())) {
                functionScopeVars.add(literal);
            }
        }

        for (CNode node : scopeVars.keySet()) {
            for (Variable variable : functionScopeVars) {
                scopeVars.get(node).remove(variable);
            }
        }

        // Add variables with scope function
        functionScopeVars.addAll(scopeVars.get(instructions.getRoot()));

        functionScopeVars.stream()
                .sorted((v1, v2) -> v1.getName().compareTo(v2.getName()))
                .forEach(var -> {

                    List<String> values = instructions.getInitializations().getValues(var.getName());
                    // TODO: Initializations should return empty list
                    if (values == null) {
                        values = Collections.emptyList();
                    }
                    builder.append(tab);
                    // builder.append(VariableCode.getVariableDeclaration(var.getName(), var.getType(), values));
                    builder.append(CodeUtils.getDeclarationWithInputs(var.getType(), var.getName(), values));

                    builder.append(";\n");
                });
        builder.append("\n");

        // Add code
        for (CNode token : instructions) {

            // Add the variables to the correct scope ******************************************

            if (token instanceof InstructionNode) {
                InstructionNode ins = (InstructionNode) token;
                if (ins.getInstructionType() == InstructionType.Block) {
                    BlockNode bn = (BlockNode) ins.getChild(0);

                    List<CNode> headers = bn.getDescendantsStream()
                            .filter(InstructionNode.class::isInstance)
                            .map(InstructionNode.class::cast)
                            .filter(node -> InstructionType.isInstructionBlockStart(node.getInstructionType()))
                            .collect(Collectors.toList());

                    for (CNode inst : headers) {
                        List<Variable> varsToAdd = scopeVars.get(inst);

                        varsToAdd
                                .stream()
                                .sorted((v1, v2) -> -v1.getName().compareTo(v2.getName()))
                                .forEach(
                                        variable -> {

                                            List<String> values = instructions.getInitializations().getValues(
                                                    variable.getName());
                                            // TODO: Initializations should return empty list
                                            if (values == null) {
                                                values = Collections.emptyList();
                                            }
                                            CNode decl = CNodeFactory.newLiteral(CodeUtils.getDeclarationWithInputs(
                                                    variable.getType(),
                                                    variable.getName(), values)
                                                    + ";");
                                            NodeInsertUtils.insertAfter(inst, decl);
                                        });
                    }
                }
            }
            // Add the variables to the correct scope ******************************************

            String cCode = token.getCode();

            for (String line : StringLines.newInstance(cCode)) {
                // String line;
                // while ((line = lineReader.nextLine()) != null) {
                builder.append(tab);
                builder.append(line);
                builder.append("\n");
            }

        }

        return builder.toString();
    }

    /**
     * Transforms all given arguments in Literal tokens (with code generation) and simplifies literals to integers, if
     * possible.
     * 
     * @param arguments
     * @return
     * @throws CodeGenerationException
     */
    public static List<CNode> getSimplifiedIndexes(List<CNode> arguments) {

        List<CNode> newArgs = SpecsFactory.newArrayList();

        boolean foundReal = false;
        for (CNode arg : arguments) {

            // Generate code for argument
            String argCode = arg.getCode();

            // Check if argument is not integer
            if (!ScalarUtils.getScalar(arg.getVariableType()).isInteger()) {
                argCode = "(int) " + (argCode);
                foundReal = true;
            }
            // Try to simplify argument
            argCode = VariableCode.simplifyIndex(argCode);

            newArgs.add(CNodeFactory.newLiteral(argCode));
        }

        if (foundReal) {
            SpecsLogs.msgInfo(" -> Found index with real numbers instead of integers, opportunity for optimization");
        }

        return newArgs;
    }

}
