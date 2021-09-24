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

package org.specs.CIR.FunctionInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.CirUtils;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.CodeUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.MultiMap;

/**
 * Utility methods related to C functions.
 * 
 * @author Joao Bispo
 * 
 */
public class FunctionInstanceUtils {

    /**
     * Collects the includes from the input variables and the return variable.
     * 
     * <p>
     * If the given 'types' is null, returns an empty Set.
     * 
     * @param functionSignature
     * @return
     */
    public static Set<String> getIncludes(FunctionType types) {
        Set<String> includes = new HashSet<>();

        if (types == null) {
            return includes;
        }

        // Inputs
        for (VariableType inputType : types.getCInputTypes()) {
            includes.addAll(CodeUtils.getIncludes(inputType));
        }

        // Output
        includes.addAll(CodeUtils.getIncludes(types.getCReturnType()));

        return includes;
    }

    /**
     * The list of the names of the variables used in the function, excluding input variables.
     * 
     * @return
     */
    public static Collection<Variable> getFunctionVariables(CInstructionList instructions, List<String> inputNames) {

        // Set formed by the input variables
        Set<String> inputs = new HashSet<>(inputNames);

        Set<Variable> allVariables = new LinkedHashSet<>();
        // From instructions
        for (CNode token : instructions) {
            allVariables.addAll(CNodeUtils.getLocalVariables(token));
        }
        // Literal variables
        allVariables.addAll(instructions.getLiteralVariables());

        List<Variable> functionVariables = new ArrayList<>();
        for (Variable var : allVariables) {
            String variableName = var.getName();
            if (inputs.contains(variableName)) {
                continue;
            }

            functionVariables.add(var);
        }

        // Check if there are name clashes
        Set<String> variableNames = SpecsFactory.newHashSet();
        Set<String> repeatedDefinitions = SpecsFactory.newHashSet();
        Set<Variable> uniqueVars = new LinkedHashSet<>(functionVariables);
        for (Variable var : functionVariables) {
            boolean success = variableNames.add(var.getName());
            if (!success) {
                repeatedDefinitions.add(var.getName());
            } else {
                uniqueVars.add(var);
            }
        }

        if (!repeatedDefinitions.isEmpty()) {
            SpecsLogs
                    .msgInfo(
                            "Multiple definitions for the following variables. Please set their type in an aspect file:");
            for (String varName : repeatedDefinitions) {
                SpecsLogs.msgInfo("   " + varName);
            }
        }

        return uniqueVars;
    }

    public static MultiMap<CNode, Variable> getScopeVariables(CInstructionList instructions,
            List<String> inputNames) {

        // Set formed by the input variables
        Set<String> inputs = new HashSet<>(inputNames);

        Map<Variable, CNode> outermostScope = new HashMap<>();
        MultiMap<CNode, Variable> result = new MultiMap<>();

        // Variables and their outermost scope
        for (CNode token : instructions) {
            CNodeUtils.getVariablesWithScope(token, outermostScope, instructions.getRoot());
        }

        // Build the result map and ignore the variables that are inputs
        for (Variable variable : outermostScope.keySet()) {

            if (inputs.contains(variable.getName())) {

                continue;
            }
            if (variable.isGlobal()) {
                continue;
            }

            CNode scope = outermostScope.get(variable);
            result.put(scope, variable);

        }

        return result;
    }

    /**
     * Checks if the given FunctionImplementation uses specialized functions.
     * 
     * @param mFunctionImplementation
     * @return
     */
    public static boolean hasSpecializedFunctionCallsRecursive(FunctionInstance functionInstance) {

        // If null, return false
        if (functionInstance == null) {
            return false;
        }

        if (functionInstance.isConstantSpecialized()) {
            return true;
        }

        // Get all implementations
        List<FunctionInstance> allImplementations = FunctionInstanceUtils.getInstancesRecursive(functionInstance);

        // If any of the implementations is specialized, return true
        for (FunctionInstance implementation : allImplementations) {
            if (implementation.isConstantSpecialized()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param functionImplementation
     * @return
     */
    public static List<FunctionInstance> getInstancesRecursive(FunctionInstance functionImplementation) {

        // Create set
        Set<FunctionInstance> implementations = SpecsFactory.newLinkedHashSet();

        // Use helper method to recursively collect all implementations
        getInstancesRecursive(functionImplementation, implementations);

        // Return the list with the implementations
        return SpecsFactory.newArrayList(implementations);
    }

    /**
     * @param functionImplementation
     * @param implementations
     */
    private static void getInstancesRecursive(FunctionInstance functionImplementation,
            Set<FunctionInstance> implementations) {

        // If implementation is null, return
        if (functionImplementation == null) {
            return;
        }

        // If current implementation already on the set, return
        if (implementations.contains(functionImplementation)) {
            return;
        }

        // Add FunctionImplementation to the set
        implementations.add(functionImplementation);

        // Add declaration instances
        implementations.addAll(functionImplementation.getDeclarationInstances());

        // If there are not function calls, return
        Set<FunctionInstance> functionCalls = functionImplementation.getImplementationInstances();
        if (functionCalls == null) {
            return;
        }

        // Call the function for the implementations it calls
        for (FunctionInstance functionCall : functionCalls) {
            getInstancesRecursive(functionCall, implementations);
        }
    }

    /**
     * Sanitizes function names that come from specialization from constants.
     * 
     * <p>
     * Replaces '.', ',' for '_'.
     * 
     * @param unsanitizedString
     * @return
     */
    public static String sanitizeFunctionName(String unsanitizedString) {
        String sanitizedString = unsanitizedString.replace('.', '_');
        sanitizedString = sanitizedString.replace(',', '_');
        sanitizedString = sanitizedString.replace('-', '_');

        return sanitizedString;
    }

    /**
     * Helper method for hasSpecializedFunctionCallsRecursive, but for a list of implementations.
     * 
     * @param functionInstances
     * @return
     */
    public static boolean hasSpecializedFunctionCallsRecursive(Set<FunctionInstance> functionInstances) {

        for (FunctionInstance functionInstance : functionInstances) {
            if (FunctionInstanceUtils.hasSpecializedFunctionCallsRecursive(functionInstance)) {
                return true;

            }
        }

        return false;
    }

    /**
     * Collects the includes the given FunctionInstance needs to call the instances it depends on. Can return null.
     * 
     * @param instance
     * @return
     */
    static Set<String> collectInstancesIncludes(String selfInclude, Set<FunctionInstance> dependentInstances) {

        if (dependentInstances == null) {
            return null;
        }

        Set<String> includes = SpecsFactory.newHashSet();

        // Extract includes from dependent instances
        for (FunctionInstance dependentInstance : dependentInstances) {

            Set<String> dependentIncludes = dependentInstance.getCallIncludes();
            if (dependentIncludes == null) {
                continue;
            }

            for (String dependentInclude : dependentIncludes) {

                // Check if null
                if (dependentInclude == null) {
                    continue;
                }

                // Check if the same file
                if (dependentInclude.equals(selfInclude)) {
                    continue;
                }

                includes.add(dependentInclude);
            }
        }

        return includes;
    }

    /**
     * Collects the includes needed by the tokens in the given instructions. Can return null.
     * 
     * @param instance
     * @return
     */
    static Set<String> collectInstructionsIncludes(CInstructionList instructions) {

        if (instructions == null) {
            return null;
        }

        Set<String> includes = SpecsFactory.newHashSet();

        // Extract includes from tokens
        for (CNode ctoken : instructions) {
            CirUtils.collectIncludes(ctoken, includes);
        }

        return includes;
    }

    /**
     * Builds a string based on the input types, which can be used to identify a function.
     * 
     * @return
     */
    public static String getTypesSuffix(List<VariableType> types) {
        StringBuilder builder = new StringBuilder();

        if (!types.isEmpty()) {
            builder.append("_");
        }

        for (VariableType type : types) {
            builder.append(type.getSmallId());
        }

        return builder.toString();
    }

    public static String getTypesSuffix(FunctionType type) {
        // TODO: Do we want to identify the outputs as well?
        return getTypesSuffix(type.getArgumentsTypes());
    }

    /**
     * Collects the instances needed for the function input and output types. Always returns a set.
     * 
     * @param instance
     * @return
     */
    public static Set<FunctionInstance> getFunctionTypesInstances(FunctionType functionTypes) {
        Set<FunctionInstance> codeInstances = SpecsFactory.newHashSet();

        if (functionTypes == null) {
            return codeInstances;
        }

        // Add instances of the variables present in the function signature
        for (VariableType type : functionTypes.getCInputTypes()) {
            codeInstances.addAll(CodeUtils.getInstances(type));
        }

        codeInstances.addAll(CodeUtils.getInstances(functionTypes.getCReturnType()));

        return codeInstances;
    }

    /**
     * Collects the FunctionInstances on which the instructions of the given instance depend directly.
     * 
     * @param instructions
     * @return
     */
    public static Set<FunctionInstance> getInstrucionsInstances(CInstructionList instructions) {

        if (instructions == null) {
            return null;
        }

        Set<FunctionInstance> instances = SpecsFactory.newHashSet();

        // Add instances found in instructions
        for (CNode ctoken : instructions) {
            CNodeUtils.collectInstances(ctoken, instances);
        }

        return instances;
    }

    /**
     * Helper method without InputParser and with variadic inputs.
     * 
     * @param function
     * @param inputs
     * @return
     */
    public static CNode getFunctionCall(FunctionInstance function, CNode... inputs) {
        List<CNode> cInputs = Arrays.asList(inputs);
        return getFunctionCall(function, cInputs);
    }

    /**
     * Builds a function call from the given list of inputs.
     * 
     * <p>
     * If the FunctionInstace uses outputs-as-inputs, automatically adds them to the list (i.e., using the method
     * 'TemporaryUtils.updateInputsWithOutsAsIns()').
     * 
     * <p>
     * TODO: Consider if this should be moved to MatlabToC, since the temporary outputs are due to the automatic
     * generation of C code
     * 
     * @param inputs
     * @return
     */
    public static FunctionCallNode getFunctionCall(FunctionInstance function, List<CNode> inputs) {

        // Always apply outputs-as-inputs transformation
        // inputs = TemporaryUtils.updateInputsWithOutsAsIns(function.getFunctionTypes(), inputs);

        // Create function call
        return function.newFunctionCall(inputs);
    }

    /**
     * Creates an InlineCode instance which returns always the same string, independent of the given input tokens.
     * 
     * @param name
     * @return
     */
    public static InlineCode newInlineCodeLiteral(final String literalCode) {
        InlineCode code = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                return literalCode;
            }
        };

        return code;
    }

    /**
     * Given a prefix and a number, returns a list with that number of Strings built in the following way:
     * 
     * <p>
     * prefix: index <br>
     * numArgs: 3 <br>
     * output: [index1, index2, index3]
     * 
     * @param prefix
     * @param numArgs
     * @return
     */
    public static List<String> createNameList(String prefix, int numArgs) {
        List<String> names = SpecsFactory.newArrayList();

        for (int i = 0; i < numArgs; i++) {
            names.add(prefix + (i + 1));
        }

        return names;
    }

    /**
     * Creates a list with the given type replicated numOfInputs times.
     * 
     * @param inputType
     * @param numOfInputs
     * @return
     */
    public static List<VariableType> createTypeList(VariableType inputType, int numOfInputs) {
        List<VariableType> types = new ArrayList<>();

        for (int i = 0; i < numOfInputs; i++) {
            types.add(inputType);
        }

        return types;
    }

    public static FunctionCallNode getFunctionCall(InstanceProvider provider, ProviderData baseData,
            CNode... arguments) {
        return getFunctionCall(provider, baseData, Arrays.asList(arguments));
    }

    public static FunctionCallNode getFunctionCall(InstanceProvider provider, ProviderData baseData,
            List<CNode> arguments) {

        return getFunctionCall(provider, baseData, arguments, Collections.emptyList());
    }

    public static FunctionCallNode getFunctionCall(InstanceProvider provider, ProviderData baseData,
            List<CNode> arguments,
            List<CNode> outputArguments) {

        List<CNode> allArguments = new ArrayList<>();
        allArguments.addAll(arguments);
        allArguments.addAll(outputArguments);

        // Get variable types
        List<VariableType> inputTypes = CNodeUtils.getVariableTypes(arguments);

        // Creates a provider data and increment function call level
        ProviderData newData = baseData.createWithContext(inputTypes);
        List<VariableType> outputTypes = new ArrayList<>();
        for (CNode output : outputArguments) {
            outputTypes.add(output.getVariableType());
        }
        newData.setOutputType(outputTypes);

        // ProviderData newData = ProviderData.newInstance(inputTypes, getSetup());
        newData.setFunctionCallLevel(newData.getFunctionCallLevel() + 1);

        FunctionInstance instance = provider.getCheckedInstance(newData);
        if (instance.getFunctionType().hasOutputsAsInputs()) {
            return instance.newFunctionCall(allArguments);
        }

        return instance.newFunctionCall(arguments);
    }
}
