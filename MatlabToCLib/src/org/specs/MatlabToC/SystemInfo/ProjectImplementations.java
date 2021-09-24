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

package org.specs.MatlabToC.SystemInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.StubInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRTypes.Types.Undefined.UndefinedType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabAspects.MatlabAspects;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabProcessor.Utils.FileNodeMap;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.MFunctions.MFunctionPrototype;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matisselib.providers.MatlabFunction;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class ProjectImplementations {

    private final ImplementationData iData;
    private final Set<String> usedStubs;

    /**
     * 
     * @param implementationData
     */
    public ProjectImplementations(ImplementationData implementationData) {

        iData = implementationData;
        usedStubs = SpecsFactory.newHashSet();
    }

    /**
     * Returns a FunctionPrototype, or null if it could not be found.
     * 
     * <p>
     * Searches for prototypes, in the following order: <br>
     * - User M-files;<br>
     * - Table Built-ins;<br>
     * - M-File resources Built-ins;<br>
     * 
     * @param filename
     * @param functionName
     * @return
     */
    public Optional<InstanceProvider> getProvider(FunctionState fState, String functionName) {

        String moduleName = null;

        // Check scope size
        if (!fState.getScope().isEmpty()) {
            moduleName = fState.getScope().get(0);
        }

        Optional<InstanceProvider> implementation;

        implementation = getUserImplementation(moduleName, functionName);
        if (implementation.isPresent()) {
            return implementation;
        }

        // TODO: Add list of included M-files

        implementation = getTableBuiltIn(functionName);
        if (implementation.isPresent()) {
            return implementation;
        }

        return implementation;
    }

    /**
     * Returns a user implementation from the list of added M-files, or null if an implementation could not be built.
     * 
     * @param functionName
     * @return
     */
    public Optional<InstanceProvider> getUserImplementation(String mfileName, String functionName) {

        FileNodeMap userFiles = iData.getProjectMFiles().getUserFiles();
        Optional<MatlabUnitNode> functionTry = userFiles.getMatlabUnit(mfileName, functionName);
        if (!functionTry.isPresent()) {
            return Optional.empty();
        }

        if (!(functionTry.get() instanceof FunctionNode)) {
            throw new RuntimeException("'" + functionTry.get().getNodeName() + "' not supported only MATLAB functions");
        }

        FunctionNode function = (FunctionNode) functionTry.get();

        // If mfileName is the same as the functionName, it is a main function
        boolean isMainFunction = userFiles.isMainFunction(mfileName, functionName);

        MatlabFunction prototype = null;

        boolean isInternal = false;
        String fileName = ((FileNode) function.getRoot()).getFilename();

        if (isMainFunction) {
            prototype = MFunctionPrototype.newMainFunction(isInternal, fileName, functionName, function, iData);
        } else {
            prototype = MFunctionPrototype.newSubFunction(isInternal, fileName, functionName, mfileName,
                    function,
                    iData);
        }

        if (prototype == null) {
            return Optional.empty();
        }

        return Optional.of(prototype);
    }

    /**
     * Returns an implementation from the built-in table, or null if an implementation could not be built.
     * 
     * @param functionName
     * @return
     */
    public Optional<InstanceProvider> getTableBuiltIn(String functionName) {

        MatlabFunction prototype = iData.getBuiltInPrototypes().getPrototypes().get(functionName);

        if (prototype == null) {
            return Optional.empty();
        }

        return Optional.of(prototype);

    }

    /**
     * Returns a FunctionImplementation that corresponds to the given function name and inputs.
     * 
     * <p>
     * If no implementation for that function is found, throws an exception.
     * 
     * @param functionName
     * @param inputs
     * @return
     * @throws MatlabToCException
     */
    public InstanceProvider getImplementation(String functionName, FunctionState fState, TypesMap localVariableTypes,
            ProviderData providerData) {

        Optional<InstanceProvider> implementation = getProvider(fState, functionName);

        // If present, return provider
        if (implementation.isPresent()) {
            return implementation.get();
        }

        /*
        if (!implementation.isPresent()) {
            // Happens when Prototypes could not return a function
            printInfo(fState, "Could not find an implementation for function '" + functionName);
        }
        */
        // If stubs enabled, create a stub
        boolean enableStubs = providerData.getSettings().get(MatlabToCKeys.ENABLE_STUBS);

        if (enableStubs) {
            return createStub(functionName, fState, localVariableTypes, providerData);
        }

        // Could not create instance
        throw new RuntimeException(getInfo(fState, "Could not find an implementation for function '" + functionName));
    }

    private InstanceProvider createStub(String functionName, FunctionState fState, TypesMap localVariableTypes,
            ProviderData providerData) {
        usedStubs.add(functionName);

        // List<VariableType> types = CTokenUtils.getVariableTypes(inputs);
        List<VariableType> types = providerData.getInputTypes();

        // Try to get a type for the function. If none is found, use Undefined
        List<String> functionScope = SpecsFactory.newArrayList();
        functionScope.add(MatlabAspects.SCOPE_FUNCTION);
        for (VariableType type : types) {
            String id = type.getSmallId();
            functionScope.add(id);
        }

        VariableType vType = null;
        if (localVariableTypes.containsSymbol(functionScope, functionName)) {
            vType = localVariableTypes.getSymbol(functionScope, functionName);
            printInfo(fState, "Using type '" + vType + "' as output of stub " + functionName
                    + ", when receiving as input " + types);
        } else {
            vType = UndefinedType.newInstance();
            printInfo(fState, "Leaving output of stub '" + functionName + "' undefined, when receiving as input "
                    + types);
        }

        // If output is matrix, pass as pointer to input
        boolean outputIsPointer = false;
        if (MatrixUtils.isMatrix(vType)) {
            outputIsPointer = true;
        }

        return newStubInstance(functionName, types, vType, outputIsPointer);
    }

    private static InstanceProvider newStubInstance(String functionName, List<VariableType> inputTypes,
            VariableType outputType,
            boolean outputIsPointer) {

        FunctionInstance stubInstance = StubInstance.newInstance(functionName, inputTypes, outputType, outputIsPointer);

        // return new MatlabCachedProvider(stubInstance);
        return data -> stubInstance;
    }

    /**
     * Logs to info level a formatted MATLAB-to-C message.
     * 
     * <p>
     * Appends some white-space, the line number, and adds a newline.
     * 
     * @param lineNumber
     * 
     * @param string
     */
    public void printInfo(FunctionState fState, String message) {
        SpecsLogs.msgInfo(getInfo(fState, message));
    }

    public String getInfo(FunctionState fState, String message) {
        int lineNumber = fState.getLineNumber();
        String function = "<unknown>";
        if (!fState.getScope().isEmpty()) {
            function = fState.getScope().get(0);
        }

        return "    (" + function + ", line " + lineNumber + ") " + message + "\n";

    }
}
