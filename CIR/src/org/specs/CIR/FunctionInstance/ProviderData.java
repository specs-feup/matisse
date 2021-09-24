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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Contains the information necessary for building a FunctionInstance.
 * 
 * <p>
 * Currently contains the input types and function settings.
 * 
 * @author Joao Bispo
 * 
 */
public class ProviderData {

    private final List<VariableType> inputArgumentsTypes;
    private final List<VariableType> originalTypes;

    private DataStore settings;
    private final NumericFactory numerics;

    private int functionCallLevel;
    private boolean propagateConstants;
    private List<OutputData> outputData;
    private List<Boolean> isInputAVariable;

    private Reporter reportService;

    private ProviderData(List<VariableType> inputTypes, DataStore setup) {
        this(null, inputTypes, setup);
    }

    /**
     * Receives input types and out-as-in types.
     * 
     * @param inputTypes
     * @param outputTypes
     */
    private ProviderData(Reporter reportService, List<VariableType> inputTypes, DataStore settings) {
        this.reportService = reportService;

        inputArgumentsTypes = processInputTypes(inputTypes);
        // this.originalTypes = new ArrayList<>(inputTypes);
        originalTypes = inputTypes;

        outputData = null;

        this.settings = settings;
        numerics = new NumericFactory(settings.get(CirKeys.C_BIT_SIZES));

        propagateConstants = false;
        functionCallLevel = 0;
        isInputAVariable = Collections.emptyList();

    }

    private ProviderData(List<VariableType> inputTypes, ProviderData data) {
        reportService = data.reportService;

        inputArgumentsTypes = processInputTypes(inputTypes);
        originalTypes = inputTypes;

        outputData = data.outputData;

        // Create CirSetup
        settings = data.getSettings();
        numerics = new NumericFactory(settings.get(CirKeys.C_BIT_SIZES));

        propagateConstants = data.propagateConstants;
        functionCallLevel = data.functionCallLevel;
        isInputAVariable = data.isInputAVariable;
    }

    /**
     * @param inputTypes
     * @return
     */
    private static List<VariableType> processInputTypes(List<VariableType> inputTypes) {
        if (inputTypes == null) {
            return null;
        }

        // Normalize input types
        List<VariableType> normalizedTypes = new ArrayList<>();
        inputTypes.forEach(type -> normalizedTypes.add(type.normalize()));

        // return inputTypes;
        return normalizedTypes;
    }

    /**
     * @return the propagateConstants
     */
    public boolean isPropagateConstants() {
        return propagateConstants;
    }

    /**
     * @param propagateConstants
     *            the propagateConstants to set
     */
    public void setPropagateConstants(boolean propagateConstants) {
        this.propagateConstants = propagateConstants;
    }

    public static ProviderData newInstance(DataStore setup) {
        return new ProviderData(null, setup);
    }

    public static ProviderData newInstance(String setupName) {
        return newInstance(DataStore.newInstance(setupName));
    }

    public static ProviderData newInstance(List<VariableType> inputTypes, DataStore setup) {
        return new ProviderData(inputTypes, setup);
    }

    public static ProviderData newInstance(ProviderData baseData, VariableType... newInputTypes) {
        return newInstance(baseData, Arrays.asList(newInputTypes));
    }

    /**
     * @param baseData
     * @param newInputTypes
     * @return
     */
    public static ProviderData newInstance(ProviderData baseData, List<VariableType> newInputTypes) {
        return baseData.createWithContext(newInputTypes);
        /*
        ProviderData newData = new ProviderData(newInputTypes, baseData.setup);
        
        // Set other info that is not final
        newData.nargout = baseData.nargout;
        newData.functionCallLevel = baseData.functionCallLevel;
        newData.propagateConstants = baseData.propagateConstants;
        newData.outputType = baseData.outputType;
        
        return newData;
        */
    }

    public ProviderData withReportService(Reporter reportService) {
        Preconditions.checkArgument(reportService != null, "reportService must not be null");

        ProviderData providerData = new ProviderData(inputArgumentsTypes, this);
        providerData.reportService = reportService;

        return providerData;
    }

    public ProviderData withSettings(DataStore settings) {
        Preconditions.checkArgument(settings != null, "settings must not be null");

        ProviderData providerData = new ProviderData(inputArgumentsTypes, this);
        providerData.settings = settings;

        return providerData;
    }

    public Reporter getReportService() {
        return reportService;
    }

    /**
     * @return the inputTypes
     */
    public List<VariableType> getInputTypes() {
        return inputArgumentsTypes;
    }

    public <T extends VariableType> List<T> getInputTypes(Class<T> type) {
        return getInputTypes(Collections.emptyList(), type);
    }

    /**
     * @param inputNames
     *            The names of the parameters. May be null, this is just to improve error messages.
     * @return the inputTypes
     */
    public <T extends VariableType> List<T> getInputTypes(List<String> inputNames, Class<T> type) {
        List<T> newList = new ArrayList<>();

        assert inputNames.isEmpty() || inputNames.size() == inputArgumentsTypes.size();

        for (int i = 0; i < inputArgumentsTypes.size(); i++) {
            VariableType input = inputArgumentsTypes.get(i);
            if (!type.isInstance(input)) {
                String inputName = inputNames.size() == 0 ? "" : inputNames.get(i);

                String message = "Expected input types of class " + type + ", but argument " + inputName
                        + " is of type " + input;
                throw new RuntimeException(message);
            }

            newList.add(type.cast(input));
        }

        return newList;
    }

    /**
     * 
     * @return the input types, before normalization
     */
    public List<VariableType> getOriginalInputTypes() {
        return originalTypes;
    }

    public <T extends VariableType> T getInputType(Class<T> aClass, int index) {
        return aClass.cast(getInputTypes().get(index));
    }

    /**
     * @param outputType
     *            the outputType to set
     */
    public void setOutputType(List<VariableType> outputType) {
        if (outputType == null) {
            outputData = null;
        } else {
            outputData = outputType.stream()
                    .map(type -> type == null ? null : new OutputData(type))
                    .collect(Collectors.toList());
        }
    }

    /**
     * @param outputType
     *            the outputType to set
     */
    public void setOutputType(VariableType outputType) {
        setOutputType(Arrays.asList(outputType));
    }

    public void setOutputData(List<OutputData> outputData) {
        this.outputData = new ArrayList<>(outputData);
    }

    /**
     * The output types of the assignment, if there is an assignment.
     * 
     * <p>
     * TODO: Change to list of Optionals TODO: Assignment always put as many Optionals as the numbers of outputs, they
     * might be empty if not defined
     * 
     * @return the outputType
     */
    public List<VariableType> getOutputTypes() {
        return outputData == null ? null : outputData.stream()
                .map(out -> out == null ? null : out.getVariableType())
                .collect(Collectors.toList());
    }

    public boolean hasOutputTypes() {
        if (outputData == null) {
            return false;
        }

        if (outputData.isEmpty()) {
            return false;
        }

        // TODO: Should do any other check? (if it is used, if content is null...)
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Input types:\n");
        builder.append(inputArgumentsTypes);
        builder.append("\nTotal inputs: ");
        builder.append(inputArgumentsTypes.size());
        builder.append("\nOutputs: ");
        builder.append(getNargouts());

        return builder.toString();
    }

    public NumericFactory getNumerics() {
        return numerics;
    }

    public DataStore getSettings() {
        return settings;
    }

    /**
     * 
     * @return ProviderData instance without information about types, just the settings in the DataStore.
     */
    public ProviderData newInstance() {
        return ProviderData.newInstance(settings);
    }

    /**
     * @param nargout
     */
    public void setNargouts(Integer nargout) {
        if (nargout == null) {
            outputData = null;
            return;
        }

        outputData = new ArrayList<>();
        for (int i = 0; i < nargout; ++i) {
            outputData.add(null);
        }
    }

    /**
     * In the SSA system, there is always an assignment for function calls, even when they appear as part of a composite
     * expression such as f(x) + 1. For that reason, in the new pass system, getNargouts() is null if and only if there
     * is an optional output (meaning it can both mean 0 or 1), such as the case of statements where the results are
     * printed.
     * 
     * @return the number of output arguments of the assignment, if there is an assignment
     */
    public Optional<Integer> getNargouts() {
        return Optional
                .ofNullable(outputData)
                .map(data -> data.size());
    }

    /**
     * Returns the current function call level.
     * 
     * <p>
     * Certain transformations might be disabled if the level is greater than one, for instance, using inlined functions
     * which span several statements.
     * 
     * @return
     */
    public int getFunctionCallLevel() {
        return functionCallLevel;
    }

    public void setFunctionCallLevel(int functionCallLevel) {
        this.functionCallLevel = functionCallLevel;
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param inputTypes
     * @return
     */
    public ProviderData create(VariableType... inputTypes) {
        return create(Arrays.asList(inputTypes));
    }

    /**
     * Helper method when we want to create a new instance with the same setup but different inputs.
     * 
     * @param staticMatrix
     * @return
     */
    public ProviderData createWithContext(List<VariableType> inputTypes) {
        // return new ProviderData(inputTypes, this.setup);
        ProviderData data = new ProviderData(inputTypes, this);
        return data;
    }

    public ProviderData create(List<VariableType> inputTypes) {
        ProviderData newData = new ProviderData(reportService, inputTypes, settings);

        // Set propagate constants
        // newData.propagateConstants = this.propagateConstants;

        return newData;
    }

    /**
     * Helper method which receives CNodes and has variadic inputs.
     * 
     * @param inputs
     * @return
     */
    public ProviderData createFromNodes(CNode... inputs) {
        return createFromNodes(Arrays.asList(inputs));
    }

    /**
     * Helper method which receives CNodes.
     * 
     * @param inputs
     * @return
     */
    public ProviderData createFromNodes(List<CNode> inputs) {
        return create(CNodeUtils.getVariableTypes(inputs));
    }

    /**
     * TODO: Change to Optional
     * 
     * @return the first output type, or null if it does not exist
     */
    public VariableType getOutputType() {
        if (getOutputTypes() == null) {
            return null;
        }

        if (getOutputTypes().isEmpty()) {
            return null;
        }

        return getOutputTypes().get(0);
    }

    public void setIsInputAVariable(List<Boolean> isInputAVariable) {
        this.isInputAVariable = isInputAVariable;
    }

    public List<Boolean> getIsInputAVariable() {
        return isInputAVariable;
    }

    /**
     * The number of input types.
     * 
     * @return
     */
    public int getNumInputs() {
        return getInputTypes().size();
    }

    /**
     * Collects all input types that are instance of the given class.
     * 
     * @param class1
     * @return
     */
    public <T extends VariableType> List<T> getInputTypesOf(Class<T> aClass) {
        return getInputTypes().stream()
                .filter(type -> aClass.isInstance(type))
                .map(type -> aClass.cast(type))
                .collect(Collectors.toList());
    }

    public List<OutputData> getOutputData() {
        return outputData == null ? null : Collections.unmodifiableList(outputData);
    }

    public MemoryLayout getMemoryLayout() {
        return CirKeys.getMemoryLayout(getSettings());
    }

}
