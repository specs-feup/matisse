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

package org.specs.MatlabToC.CodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.Initializations;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.Matisse.Matlab.VariableTable;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.SystemInfo.FunctionState;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.MatlabToC.SystemInfo.ProjectImplementations;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.PassMessage;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Collects data during MatLab-to-C process.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabToCFunctionData {

    private final static String TEMP_VAR_PREFIX = "temp_var_";

    private ProviderData providerData;
    // private final SetupHelper setup;

    private final ImplementationData implementationData;

    private final InstanceBuilder helper;
    // private final NumericFactory numerics;

    private final VariableTable constantTable;
    private final Optional<Integer> numArgsOuts;

    // Variables defined externally (e.g., through aspect files)
    private final TypesMap externalVariableTypes;
    private final ProjectImplementations projectImplementations;
    private final List<String> scope;

    // private final FunctionSettings functionSettings;

    // / Information acquired during MatlabToC transformation

    // Information for building the function data object
    private final TypesMap localVariableTypes;

    // The name of outputs
    private final Collection<String> outputNames;

    // The name of the function
    private String functionName;

    private final Collection<String> inputNames;
    private final List<VariableType> inputTypes;

    private boolean foundFunctionDeclaration;
    private final List<String> functionComments;

    private int lineNumber;
    private int tempVarCounter;

    private boolean propagateConstants;
    private int propagateConstantsGuardLevel;

    private boolean propagateOutputTypeToInputs;

    private boolean isLeftHandAssignment;
    private List<String> leftHandIds;

    // The types that are going to specialize the current M-function
    private final List<VariableType> givenInputTypes;

    // Optional initializations for variables
    private final Initializations initializations;

    private final String baseCFilename;

    private List<VariableType> assignmentReturnType;

    // private final Set<String> variablesToChange = new HashSet<>();

    /**
     * Every time parsing begins for a function call, this number goes up. It goes down when it exits the function.
     */
    private int currentFunctionCallLevel;

    /**
     * HACK for when you have several output types, and need to use a pointer as return
     */
    private boolean forcePointerOutput;

    /**
     * Creates a FunctionBuilder for the given FunctionData.
     * 
     * @param varTypeTable
     * @param manager
     * @param parentFunction
     *            if the data refers to a subFunction, it is the parent main function. Otherwise, is null
     * @param givenInputTypes
     *            the variables types with which the function will be specialized. If the current object is to be used
     *            to parse a script, this argument should be null
     */
    private MatlabToCFunctionData(ProviderData providerData, ImplementationData implementationData,
            List<String> scope) {

        this.providerData = providerData;
        this.implementationData = implementationData;

        // Get variable table
        constantTable = providerData.getSettings().get(MatisseKeys.CONSTANT_VARIABLES);

        numArgsOuts = providerData.getNargouts();

        helper = new GenericInstanceBuilder(providerData);

        externalVariableTypes = implementationData.getTypesMap();

        projectImplementations = new ProjectImplementations(implementationData);
        this.scope = scope;

        localVariableTypes = new TypesMap();

        localVariableTypes.addSymbols(implementationData.getTypesMap());

        functionName = null;

        inputNames = new LinkedHashSet<>();
        inputTypes = new ArrayList<>();
        outputNames = SpecsFactory.newLinkedHashSet();

        foundFunctionDeclaration = false;
        functionComments = new ArrayList<>();

        lineNumber = -1;
        tempVarCounter = 0;

        propagateConstants = true;
        propagateConstantsGuardLevel = 0;

        propagateOutputTypeToInputs = true;

        isLeftHandAssignment = false;
        leftHandIds = new ArrayList<>();

        givenInputTypes = SpecsFactory.getUnmodifiableList(providerData.getInputTypes());

        initializations = new Initializations();

        baseCFilename = implementationData.getBaseCFilename();

        assignmentReturnType = Collections.emptyList();
        currentFunctionCallLevel = 0;

        forcePointerOutput = false;
    }

    public void setForcePointerOutput(boolean forcePointerOutput) {
        this.forcePointerOutput = forcePointerOutput;
    }

    public boolean isForcePointerOutput() {
        return forcePointerOutput;
    }

    public NumericFactory getNumerics() {
        return helper.getNumerics();
    }

    public InstanceBuilder helper() {
        return helper;
    }

    /**
     * @return the implementationData
     */
    public ImplementationData getImplementationData() {
        return implementationData;
    }

    /**
     * Creates a MatlabToCFunctionData to build MATLAB functions.
     * 
     * @param implementationData
     * @param scope
     * @param implementationSettings
     * @param givenInputTypes
     * @return
     */
    /*
    public static MatlabToCFunctionData newInstanceForFunction(ProviderData providerData,
        ImplementationData implementationData, List<String> scope) {
    
    return new MatlabToCFunctionData(providerData, implementationData, scope);
    }
    */

    public boolean isPropagateOutputTypeToInputs() {
        return propagateOutputTypeToInputs;
    }

    public void setPropagateOutputTypeToInputs(boolean propagateOutputTypeToInputs) {
        this.propagateOutputTypeToInputs = propagateOutputTypeToInputs;
    }

    /**
     * Creates a MatlabToCFunctionData to build MATLAB scripts.
     * 
     * @param implementationData
     * @param scope
     * @param implementationSettings
     * @return
     */
    public static MatlabToCFunctionData newInstance(ProviderData providerData, ImplementationData implementationData,
            List<String> scope) {

        return new MatlabToCFunctionData(providerData, implementationData, scope);
    }

    /**
     * Helper method which accepts a DataStore and a TypesMap.
     * 
     * @param setup
     * @param map
     * @return
     */
    public static MatlabToCFunctionData newInstance(LanguageMode languageMode, DataStore setup, TypesMap map) {
        ProviderData providerData = ProviderData.newInstance(setup);
        ImplementationData data = MatlabToCUtils.newImplementationData(languageMode, map, setup);
        return MatlabToCFunctionData.newInstance(providerData, data, Arrays.asList());
    }

    /**
     * Helper method which returns a new instance with default values.
     * 
     * @return
     */
    public static MatlabToCFunctionData newInstance(LanguageMode languageMode) {
        return newInstance(languageMode, MatlabToCOptionUtils.newDefaultSettings(), new TypesMap());
    }

    /**
     * @return the providerData
     */
    public ProviderData getProviderData() {
        // Update constant propagation information
        providerData.setPropagateConstants(isPropagateConstants());

        return providerData;
    }

    public DataStore getSettings() {
        return providerData.getSettings();
    }

    /**
     * @return the baseCFilename
     */
    public String getBaseCFilename() {
        return baseCFilename;
    }

    /**
     * @return the localVariableTypes
     */
    public TypesMap getLocalVariableTypes() {
        return localVariableTypes;
    }

    /**
     * @return the implementationSettings
     */
    /*
    public FunctionSettings getISettings() {
    return functionSettings;
    }
    */

    /**
     * @param lineNumber
     *            the lineNumber to set
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;

        Reporter previousReportService = providerData.getReportService();
        if (previousReportService != null && previousReportService instanceof DefaultReportService) {
            DefaultReportService newReportService = ((DefaultReportService) previousReportService)
                    .withLineNumber(lineNumber);
            providerData = providerData.withReportService(newReportService);
        }
    }

    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the functionComments
     */
    public List<String> getFunctionComments() {
        return functionComments;
    }

    /**
     * @param foundFunctionDeclaration
     *            the foundFunctionDeclaration to set
     */
    public void setFoundFunctionDeclaration(boolean foundFunctionDeclaration) {
        this.foundFunctionDeclaration = foundFunctionDeclaration;
    }

    /**
     * @return the foundFunctionDeclaration
     */
    public boolean haveFoundFunctionDeclaration() {
        return foundFunctionDeclaration;
    }

    /**
     * @return the varTable
     */
    /*
    public TypesMap getVarTypeTable() {
    return externalVariableTypes;
    }
    */

    /**
     * @return the projectFunctions
     */
    /*
    public ProjectPrototypes getProjectFunctions() {
    return projectFunctions;
    }
    */

    /**
     * @return the outputNames
     */
    public Collection<String> getOutputNames() {
        return outputNames;
        // return FactoryUtils.newArrayList(outputNames);
        // return Collections.unmodifiableList(outputNames);
    }

    /**
     * @param functionName
     *            the functionName to set
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * @return the functionName
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * @return the scope
     */
    public List<String> getScope() {
        return scope;
    }

    /**
     * @return
     */
    public Collection<String> getInputNames() {
        return inputNames;
    }

    /**
     * @return
     */
    public List<VariableType> getInputTypes() {
        return inputTypes;
    }

    /*
    public void stopConstantPropagation() {
    propagateConstants = false;
    }
    */

    public boolean isPropagateConstants() {
        return propagateConstants;
    }

    /**
     * Creates a new Variable with the given name. The type is defined as follows:
     * 
     * <p>
     * - Checks global VariableType definitions for the given name, and returns a type if found; <br>
     * - Checks local VariableType definitions for the given name, and returns a type if found; <br>
     * - Infers the type from the value in the given token, if the token is not null;
     * 
     * @param variableName
     * @param tokenWithValue
     * @return
     */
    public VariableType getVariableType(String variableName, CNode tokenWithValue) {

        VariableType var = getVariableInternal(variableName, tokenWithValue);
        return var;
    }

    /**
     * Helper method for getVariable, returns null instead of raising an exception.
     * 
     * @param variableName
     * @param tokenWithValue
     * @return
     * @throws MatlabToCException
     */
    private VariableType getVariableInternal(String variableName, CNode tokenWithValue) throws MatlabToCException {

        // Give priority to current vars
        // Checking local VariableType definition
        // When accessing local variables, no scope is needed

        if (localVariableTypes.containsSymbol(scope, variableName)) {
            VariableType type = localVariableTypes.getSymbol(scope, variableName);

            // return localVariableTypes.getSymbol(scope, variableName);
            return type;
        }

        // Infer type
        if (tokenWithValue != null) {
            VariableType varType = tokenWithValue.getVariableType();

            if (varType != null) {
                return varType;
            }
        }

        // Give priority to current vars
        // Checking local VariableType definition
        // When accessing local variables, no scope is needed
        // if (localVariableTypes.containsSymbol(scope, variableName)) {
        // return localVariableTypes.getSymbol(scope, variableName);
        // }

        // Could not determine type, return null
        return null;
    }

    /**
     * Checks if there is a type defined for the given variable name and value.
     * 
     * @param variableName
     * @param tokenWithValue
     * @return
     * @throws MatlabToCException
     */
    public boolean hasType(String variableName, CNode tokenWithValue) {

        VariableType var = null;

        try {
            var = getVariableInternal(variableName, tokenWithValue);

        } catch (MatlabToCException e) {
            SpecsLogs.warn("It should never get here, the method checks first "
                    + "if the type of the variable exists.");
            return false;
        }

        if (var == null) {
            return false;
        }

        return true;
    }

    /**
     * Maps the given type name to the given type, in the local VariableType definitions. Only adds the variable if it
     * does not exist previously.
     * 
     * @param name
     * @param type
     */
    public void addVariableType(String name, VariableType type) {

        // If variable is constant, set attribute
        // if (functionSettings.getConstantVariables().hasVariable(getScope(), name)) {

        if (constantTable.hasVariable(getScope(), name)) {
            type = type.setImmutable(true);
        }

        // Check if variable is already defined
        boolean promotionHappened = CodeBuilderUtils.checkTypePromotion(name, type, this);
        if (promotionHappened) {
            System.out.println("Local Variables:\n" + localVariableTypes);
            System.out.println("Aspect Variables:\n" + externalVariableTypes);
        }

        // Check if type is already defined locally
        if (localVariableTypes.containsSymbol(scope, name)) {
            return;
        }

        // TODO: Confirm the value of scope here, to check if it was
        // empty...?
        // Because of A uses B with result on B

        // If name is an input, remove pointer status
        /*
        if(inputNames.contains(name)) {
            type = PointerUtils.getTypeWithoutPointer(type);
            System.out.println("REMOVING POINTER FOR "+name);
        }
        */
        addLocalSymbol(scope, name, type);
        // localVariableTypes.addSymbol(scope, name, type);
    }

    /**
     * @param variableName
     * @return
     * @throws MatlabToCException
     */
    public VariableType getVariableType(String variableName) throws MatlabToCException {
        return getVariableType(variableName, null);
    }

    /**
     * @param variableName
     * @return
     */
    public boolean hasType(String variableName) {
        return hasType(variableName, null);
    }

    /**
     * 
     * @return the types of the outputs of the parsed MatLab function (including pointer information). If a type cannot
     *         be found for any of the outputs, returns null
     */
    public List<VariableType> getOutputTypes() {
        List<VariableType> types = new ArrayList<>();

        for (String name : outputNames) {
            VariableType type = getVariableType(name);

            // Check if there is a type for the current variable
            if (type == null) {
                throw new MatlabToCException("Could not determine type of output variable '" + name + "', in scope "
                        + scope);
            }

            types.add(type);
        }

        return types;
    }

    /**
     * Logs to info level a formatted MATLAB-to-C message.
     * 
     * <p>
     * Appends some white-space, the line number, and adds a newline.
     * 
     * @param string
     */
    public void printInfo(String message) {
        SpecsLogs.msgInfo("    (line " + getLineNumber() + ") " + message + "\n");

    }

    /**
     * @param inputNames
     */
    public void addInputs(List<String> inputNames) {
        // With how many inputs will the function be specialized?
        int numInputs = givenInputTypes.size();

        for (int i = 0; i < numInputs; i++) {
            addInput(inputNames.get(i));
        }
    }

    /**
     * @param inputName
     * @throws MatlabToCException
     */
    private void addInput(String inputName) {
        // Get type
        VariableType inputType = getVariableType(inputName);

        // Remove pointer status from type
        inputType = ReferenceUtils.getType(inputType, false);

        // Redefine variable
        setVariableType(inputName, inputType);

        inputNames.add(inputName);
        inputTypes.add(inputType);
    }

    /**
     * Returns true if the given variable was defined externally (e.g., through an aspect file). Otherwise, returns
     * false (e.g., if the variable was inferred).
     * 
     * @param variableName
     * @return
     */
    public boolean isVariableDefinedExternally(String variableName) {
        if (externalVariableTypes.containsSymbol(scope, variableName)) {
            return true;
        }

        return false;
    }

    /**
     * Replaces the type of an existing variable.
     * 
     * <p>
     * If variable does not already exists, outputs a warning.
     * 
     * @param varName
     * @param newOutputType
     */
    public void setVariableType(String variableName, VariableType newOutputType) {

        if (!localVariableTypes.containsSymbol(scope, variableName)) {
            SpecsLogs.warn("Variable '" + variableName + "' does not exist for scope '" + scope
                    + "'. Creating new entry.");

        }
        // Checking local VariableType definition
        // When accessing local variables, no scope is needed
        addLocalSymbol(scope, variableName, newOutputType);

        return;
    }

    private void addLocalSymbol(List<String> scope, String variableName, VariableType type) {
        /*
        	if (variableName.equals("z")) {
        	    System.out.println("TYPE:" + type);
        	    System.out.println("iMUT:" + type.isImmutable());
        	    System.out.println("LOCAL VARS:" + localVariableTypes);
        	    System.out.println("USAR VARS:" + externalVariableTypes);
        	}
        */
        localVariableTypes.addSymbol(scope, variableName, type);
    }

    /**
     * @return
     */
    public String nextTempVarName() {
        String name = MatlabToCFunctionData.TEMP_VAR_PREFIX + tempVarCounter;
        tempVarCounter += 1;

        return name;
    }

    /**
     * @param outputName
     */
    public void addOutputName(String outputName) {
        if (outputName == null) {
            SpecsLogs.warn("OutputName is null");
        }

        if (!numArgsOuts.isPresent()) {
            outputNames.add(outputName);
            return;
        }

        // Number of output arguments is not the same as the number of outputs of the function
        // Only add output names until reaching the number of arguments with which the function was called
        // int nargouts = functionSettings.getNargouts();
        // int nargouts = providerData.getNargouts();

        if (outputNames.size() >= numArgsOuts.get()) {
            return;
        }
        outputNames.add(outputName);

    }

    /**
     * Creates a function call from the given function name and input arguments.
     * 
     * <p>
     * Acquires a prototype corresponding to the function name, builds an implementation with the original input
     * arguments and returns a function call after parsing the original input arguments.
     * 
     * <p>
     * Assumes that the function has one output argument.
     * 
     * @param functionName
     * @param inputArguments
     *            can be null
     * @return
     */
    public CNode getFunctionCall(String functionName, List<CNode> inputArguments) {
        return getFunctionCall(functionName, inputArguments, null);
        // return getFunctionCall(functionName, inputArguments, 1);
    }

    public InstanceProvider getInstanceProvider(String functionName) {
        FunctionState fState = new FunctionState(scope, getLineNumber());
        Optional<InstanceProvider> provider = projectImplementations.getProvider(fState, functionName);
        if (provider.isPresent()) {
            return provider.get();
        }

        providerData.getReportService().emitMessage(PassMessage.FUNCTION_NOT_FOUND,
                "There is no provider defined for MATLAB function '"
                        + functionName + "'");
        // emitMessage always throws an exception for error messages, so this is unreachable.
        throw null;
    }

    public FunctionCallNode getFunctionCall(String functionName, List<CNode> inputArguments, Integer nargout) {
        // Increment function call count
        // incrementFunctionCallLevel();
        if (inputArguments == null) {
            inputArguments = Collections.emptyList();
        }

        // Get input types
        List<VariableType> inputTypes = CNodeUtils.getVariableTypes(inputArguments);

        // Create ProviderData
        // ProviderData providerData = ProviderData.newInstance(inputTypes, getSetup());
        ProviderData providerData = getProviderData().createWithContext(inputTypes);

        // Set number of output arguments
        providerData.setNargouts(nargout);

        // Set function call level
        providerData.setFunctionCallLevel(getCurrentFunctionCallLevel());

        // If inside assignment right-hand, set output types.
        // Only set output if current function call level is 1
        // if (getCurrentFunctionCallLevel() == 1) {
        // providerData.setOutputType(getAssignmentReturnTypes());
        // }

        if (isPropagateOutputTypeToInputs()) {
            List<VariableType> assignmentReturnTypes = getAssignmentReturnTypes();

            if (nargout == null || assignmentReturnTypes.size() != 0) {
                assert nargout == null || assignmentReturnTypes.size() == nargout : "nargout is " + nargout
                        + ", assignment return types is " + assignmentReturnTypes;
                providerData.setOutputType(assignmentReturnTypes);
            }
        }

        // Check: if function call level is 0, there might me some case that is being overlooked
        if (getCurrentFunctionCallLevel() < 1) {
            throw new MatlabToCException("Current function call level below 1, is a case overlooked? -> "
                    + getCurrentFunctionCallLevel(), this);
        }

        // Add which input arguments are variables (e.g., A), as opposed to function call, for instance
        providerData.setIsInputAVariable(getIsInputAVariable(inputArguments));

        FunctionState fState = new FunctionState(scope, getLineNumber());

        InstanceProvider output = projectImplementations.getImplementation(functionName, fState,
                localVariableTypes,
                providerData);
        /*
        if (functionName.equals("size") && providerData.getOutputTypes().size() == 2) {
            System.err.println("Trouble");
        }
        */

        Optional<InstanceProvider> instance = output.accepts(providerData);
        if (!instance.isPresent()) {
            throw new MatlabToCException("Could not implement MATLAB function '" + functionName
                    + "' inside MATLAB function '" + getFunctionName() + "' for data:\n"
                    + providerData, this);
        }

        FunctionCallNode result = instance.get()
                .newCInstance(providerData)
                .newFunctionCall(inputArguments);

        return result;
    }

    private static List<Boolean> getIsInputAVariable(List<CNode> inputArguments) {
        List<Boolean> isAVar = new ArrayList<>(inputArguments.size());

        inputArguments.forEach(arg -> isAVar.add(arg instanceof VariableNode));

        return isAVar;
    }

    /**
     * @return
     */
    public int getNumInputs() {
        return getInputNames().size();
    }

    /**
     * 
     */
    public void increasePropragationGuard() {
        propagateConstantsGuardLevel += 1;

        if (propagateConstants) {
            propagateConstants = false;
            // Update provider data
            providerData.setPropagateConstants(isPropagateConstants());
        }
    }

    /**
     * 
     */
    public void decreasePropragationGuard() {
        if (propagateConstantsGuardLevel < 1) {
            throw new RuntimeException("Guard level is already below 1.");
        }

        propagateConstantsGuardLevel -= 1;
        if (propagateConstantsGuardLevel == 0) {
            propagateConstants = true;
            // Update provider data
            providerData.setPropagateConstants(isPropagateConstants());
        }
    }

    public String getErrorMessage() {
        return "Problems on function '" + getFunctionName() + "', line " + getLineNumber();
    }

    /**
     * Checks if a given variable is an output of the function that should be a pointer.
     * 
     * @param variableName
     * @param variableName
     * @param rightHandType
     * @return
     */
    public boolean isOutputPointer(String variableName, VariableType type) {
        // If not part of function outputs, should not be pointer (local variable)
        if (!outputNames.contains(variableName)) {
            return false;
        }

        // If declared matrix, cannot be pointer
        if (MatrixUtils.isStaticMatrix(type)) {
            return false;
        }

        // If type is matrix, is output pointer
        if (MatrixUtils.isMatrix(type)) {
            return true;
        }

        // If more than one output, and is not declared matrix, is pointer
        if (outputNames.size() > 1) {
            return true;
        }

        return false;
    }

    /**
     * @return the initializations
     */
    public Initializations getInitializations() {
        return initializations;
    }

    /**
     * @return all the variable-type associations used during MATLAB-to-C conversion
     */
    public TypesMap getTypes() {
        TypesMap types = new TypesMap();

        // Add local types
        types.addSymbols(localVariableTypes);

        // Add external types
        types.addSymbols(externalVariableTypes);

        return types;
    }

    public boolean isFunctionCall(String id) {

        // Check if access to an array position
        boolean isVariable = hasType(id);

        // Array access to variable
        if (isVariable) {
            return false;
        }

        return true;
    }

    /**
     * 
     * @param accessCallName
     * @return true if the name corresponds to a variable, and is local (not and input/output)
     */
    public boolean isLocalVariable(String variableName) {
        if (isFunctionCall(variableName)) {
            return false;
        }

        if (inputNames.contains(variableName)) {
            return false;
        }

        if (outputNames.contains(variableName)) {
            return false;
        }

        return true;
    }

    /**
     * If current parsing
     * 
     * @param returnTypes
     */
    public void setAssignmentReturnTypes(List<VariableType> returnTypes) {
        assignmentReturnType = returnTypes;

        // Do not know if this can be generalized
        // Update provider data
        // providerData.setOutputType(returnTypes);
    }

    /**
     * If the list is not empty, it means that current MATLAB-to-C parsing is inside the right hand of an assignment.
     * The return types may not be known at this point, so there can be null values inside the list.
     * 
     * @return the assignmentReturnType
     */
    public List<VariableType> getAssignmentReturnTypes() {
        return assignmentReturnType;
    }

    public void incrementFunctionCallLevel() {
        currentFunctionCallLevel++;
    }

    public void decrementFunctionCallLevel() {
        currentFunctionCallLevel--;
    }

    /**
     * @return the currentFunctionCallLevel
     */
    private int getCurrentFunctionCallLevel() {
        return currentFunctionCallLevel;
    }

    public void setLeftHandAssignment(boolean bool) {
        isLeftHandAssignment = bool;
    }

    public boolean isLeftHandAssignment() {
        return isLeftHandAssignment;
    }

    public List<String> getLeftHandIds() {
        return leftHandIds;
    }

    public void setLeftHandIds(List<String> leftHandIds) {
        this.leftHandIds = leftHandIds;
    }

    // public boolean isMarkedForChange(String variableName) {
    // // TODO Auto-generated method stub
    // return false;
    // }

}
