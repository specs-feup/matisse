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

package org.specs.CIR.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIRTypes.Types.Undefined.UndefinedTypeUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Utility methods related to CTokens.
 * 
 * @author Joao Bispo
 * 
 */
public class CNodeUtils {

    public static final String TAB = "   ";

    /**
     * @return the tab
     */
    public static String getDefaultTab() {
        return CNodeUtils.TAB;
    }

    /**
     * Recursively returns all Variables accessible by the given 'token'.
     * 
     * @param token
     * @return
     */
    public static List<Variable> getLocalVariables(CNode token) {
        List<Variable> vars = new ArrayList<>();
        getLocalVariables(token, vars);
        return vars;
    }

    private static void getLocalVariables(CNode token, List<Variable> vars) {
        // Check if token contains a variable
        if (token instanceof VariableNode) {
            Variable var = ((VariableNode) token).getVariable();
            if (var.isGlobal()) {
                return;
            }

            vars.add(var);
        }

        if (token instanceof FunctionCallNode) {

            FunctionInstance instance = ((FunctionCallNode) token).getFunctionInstance();

            vars.addAll(instance.getCallVars());
        }

        if (token.hasChildren()) {
            for (CNode child : token.getChildren()) {
                getLocalVariables(child, vars);
            }
        }
    }

    public static void getVariablesWithScope(CNode token, Map<Variable, CNode> map, CNode parent) {
        if (token instanceof VariableNode) {
            Variable var = ((VariableNode) token).getVariable();

            updateVariableReferences(token, map, parent, var);
        }

        if (token instanceof FunctionCallNode) {

            FunctionInstance instance = ((FunctionCallNode) token).getFunctionInstance();

            for (Variable var : instance.getCallVars()) {

                updateVariableReferences(token, map, parent, var);
            }
        }

        if (token.hasChildren()) {
            if (token instanceof BlockNode) {
                CNode innerParent = parent;
                for (CNode child : token.getChildren()) {
                    if (!(child instanceof InstructionNode)) {
                        assert false : child.getCode() + " not an instruction node";
                    }

                    if (InstructionType.isInstructionBlockStart(((InstructionNode) child).getInstructionType())) {
                        getVariablesWithScope(child, map, parent);
                        innerParent = child;
                    } else {
                        getVariablesWithScope(child, map, innerParent);
                    }
                }
            } else {
                for (CNode child : token.getChildren()) {
                    getVariablesWithScope(child, map, parent);
                }
            }

        }
    }

    private static void updateVariableReferences(CNode token, Map<Variable, CNode> map, CNode parent, Variable var) {
        if (!map.containsKey(var)) {

            assert parent != null;
            map.put(var, parent);
        } else {
            CNode previous = map.get(var);
            assert previous != null;

            // If the previous node is already the container block (usual case), then don't
            // update anything.
            if (!previous.getDescendantsAndSelfStream().anyMatch(token::equals)) {
                CNode commonAscendant = getCommonAscendant(token, previous).orElseThrow(
                        () -> new CodeGenerationException("Could not get root of tree"));
                assert commonAscendant != null;

                map.remove(var);
                map.put(var, commonAscendant);
            }
        }
    }

    private static Optional<CNode> getCommonAscendant(CNode current, CNode previous) {

        assert current != null;
        assert previous != null;

        for (CNode a1 : getContainerBlockHeaders(current)) {
            for (CNode a2 : getContainerBlockHeaders(previous)) {

                if (a1.equals(a2)) {
                    return Optional.of(a1);
                }
            }
        }

        return Optional.empty();
    }

    private static List<CNode> getContainerBlockHeaders(CNode current) {
        assert current != null;

        List<CNode> blockHeaders = new ArrayList<>();

        while (current.getParent() != null) {
            CNode parent = current.getParent();

            if (parent instanceof BlockNode) {
                int index = -1;
                for (int i = 0; i < parent.getNumChildren(); ++i) {
                    if (parent.getChild(i) == current) {
                        index = i;
                        break;
                    }
                }

                assert index >= 0;

                for (int i = index; i >= 0; --i) {
                    CNode child = parent.getChild(i);
                    if (InstructionType.isInstructionBlockStart(((InstructionNode) child).getInstructionType())) {
                        blockHeaders.add(child);
                        break;
                    }
                }
            }

            current = parent;
        }

        blockHeaders.add(current);

        return blockHeaders;
    }

    /**
     * Collects all FunctionImplementations in the function calls inside the token.
     * 
     * <p>
     * This method returns the implementations used only in the tree of the given CToken. It does NOT recursively
     * collect implementations used by the function calls themselves.
     * 
     * <p>
     * To collect implementations recursively, use FunctionUtils.getImplementationsRecursive().
     * 
     * TODO: Recursive test should be done here?
     * 
     * @param ctoken
     * @param instances
     */
    public static void collectInstances(CNode ctoken, Set<FunctionInstance> instances) {

        for (CNode child : ctoken.getChildren()) {
            collectInstances(child, instances);
        }

        if (ctoken instanceof FunctionCallNode) {

            FunctionInstance impl = ((FunctionCallNode) ctoken).getFunctionInstance();

            Set<FunctionInstance> neededInstances = impl.getCallInstances();
            if (neededInstances == null) {
                return;
            }

            // Add instance
            instances.addAll(neededInstances);
        }

    }

    /**
     * Returns a list of CTokens representing Variables built from the given arguments.
     * 
     * <p>
     * If the lists do not have the same size, throws an exception.
     * 
     * @param inputNames
     * @param inputTypes
     * @return
     */
    public static List<CNode> buildVariableTokens(List<String> inputNames, List<VariableType> inputTypes) {

        // Check sizes
        if (inputNames.size() != inputTypes.size()) {
            throw new IllegalArgumentException("Size of names (" + inputNames.size()
                    + ") needs to be the same as size of types (" + inputTypes.size() + ").");
        }

        List<CNode> vars = SpecsFactory.newArrayList();

        for (int i = 0; i < inputTypes.size(); i++) {
            CNode var = CNodeFactory.newVariable(inputNames.get(i), inputTypes.get(i));
            vars.add(var);
        }

        return vars;
    }

    /**
     * Builds a list with the VariableTypes of the given CTokens.
     * 
     * @param stopOperands
     * @return
     */
    public static List<VariableType> getVariableTypes(List<CNode> tokens) {
        List<VariableType> types = SpecsFactory.newArrayList();

        for (CNode token : tokens) {
            VariableType variableType = token.getVariableType();
            types.add(variableType);
        }

        return types;
    }

    /**
     * Helper method.
     * 
     * @param tokens
     * @return
     */
    public static List<VariableType> getVariableTypes(CNode... tokens) {
        return getVariableTypes(Arrays.asList(tokens));
    }

    /**
     * Checks if the number of arguments is the same as the number of function inputs, and checks if the input types are
     * function-assignment compatible with the types of the function, modifying the input list of arguments with
     * conversion functions if needed.
     * 
     * @param functionName
     * @param inputTypes
     * @param inputArgs
     */
    public static void parseFunctionCallArguments(String functionName, List<VariableType> inputTypes,
            List<CNode> inputArguments) {

        // Verify types of arguments and of function
        if (inputArguments.size() != inputTypes.size()) {
            throw new CodeGenerationException("Number of function C inputs '(" + inputTypes.size()
                    + ")' do not match the number of input arguments of the tree (" + inputArguments.size()
                    + " of a total of " + inputArguments.size() + ") in call to function '" + functionName
                    + "'\nTree values:\n" + inputArguments);
        }

        for (int i = 0; i < inputArguments.size(); i++) {

            CNode arg = inputArguments.get(i);
            VariableType inputType = arg.getVariableType();

            // TODO: move this logic to convert function?
            // If argument type or function type is Undefined, ignore
            if (UndefinedTypeUtils.isUndefined(inputType)) {
                continue;
            }

            VariableType functionType = inputTypes.get(i);
            if (UndefinedTypeUtils.isUndefined(functionType)) {
                continue;
            }

            // Check if types are the same
            VariableType argType = arg.getVariableType();
            CNode parsedArg = arg;

            if (!argType.equals(functionType)) {
                parsedArg = null;

                try {

                    parsedArg = ConversionUtils.to(arg, functionType);

                } catch (Exception e) {
                    int index = i + 1;

                    String errorMsg = getErrorMsg(functionName, inputType, functionType, index) + "\nInput token:"
                            + arg;
                    SpecsLogs.warn("Exception while converting", e);

                    throw new RuntimeException(errorMsg, e);
                }

            }

            if (parsedArg == null) {
                int index = i + 1;

                throw new RuntimeException(getErrorMsg(functionName, inputType, functionType, index));
            }

            // Replace input token with conversion function
            inputArguments.set(i, parsedArg);
        }

    }

    /**
     * @param functionName
     * @param inputType
     * @param functionType
     * @param index
     * @return
     */
    private static String getErrorMsg(String functionName, VariableType inputType, VariableType functionType,
            int index) {
        return "Input '" + index + "' of type '" + inputType + "' cannot be converted to the type needed by argument #"
                + index + " of function '" + functionName + "' (needs " + functionType + ")";
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param literals
     * @return
     */
    public static List<CNode> buildLiteralTokens(String... literals) {
        return buildLiteralTokens(Arrays.asList(literals));
    }

    /**
     * Transforms a list of Strings into a list of Literal CTokens.
     * 
     * @param literals
     * @return
     */
    public static List<CNode> buildLiteralTokens(List<String> literals) {
        List<CNode> literalTokens = SpecsFactory.newArrayList();
        for (String inputArgument : literals) {
            literalTokens.add(CNodeFactory.newLiteral(inputArgument));
        }

        return literalTokens;
    }

    /**
     * Transforms a list of Integers into a list of CNumber CTokens representing integers.
     * 
     * @param shape
     * @return
     */
    public static List<CNode> buildIntegerTokens(List<Integer> integers) {
        List<CNode> tokens = SpecsFactory.newArrayList();

        for (Integer integer : integers) {
            CNode intToken = CNodeFactory.newCNumber(integer);
            tokens.add(intToken);
        }

        return tokens;
    }

}
