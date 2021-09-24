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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRTypes.Types.Void.VoidType;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Represents the types used to implement a function. Corresponds to the types expected in a FunctionCall of the C tree.
 * 
 * <p>
 * FuntionTypes works as the bridge between the FunctionCall of the generic function in the C tree, and the function
 * call of the implementation in C. FunctionTypes are created in Builders, and are tied to specific
 * FunctionImplementations. The format of a FunctionType of an implementation should be well-documented.
 * 
 * <p>
 * Bear in mind that these are the inputs/outputs use to implement a function. After implementation, the inputs use to
 * call the C function may be different (e.g., when constant inputs are used and the implementation specializes to those
 * inputs)
 * 
 * <p>
 * Enforces the C semantics of several inputs, one output.
 * 
 * <p>
 * When FunctionTypes is built, all types must be already defined.
 * 
 * <p>
 * This class is <strong>thread-safe</strong> since it is immutable once constructed.
 * 
 * @author Joao Bispo
 * 
 */
public final class FunctionType {

    private final List<String> inputNames;
    private final List<VariableType> inputTypes;
    private final List<Boolean> isInputReference;

    private final List<String> outputAsInputNames;
    private final List<VariableType> outputAsInputTypes;
    private final List<String> outputDisplayNames;

    // Lazily constructed. Every use should be in "synchronized" methods.
    private List<VariableType> outputAsInputTypesNormalized;

    private List<Object> annotations = new ArrayList<>();
    private final String cReturnName;
    private final VariableType cReturnType;
    private final String cReturnDisplayName;

    private final String prefix;

    private final boolean dependsOnGlobals;
    private final boolean canHaveSideEffects;
    private final boolean isElementWise;
    private final boolean isNoOp;

    /**
     * @param inputs
     * @param outputs
     */
    private FunctionType(List<String> inputNames,
            List<VariableType> inputTypes,
            List<Boolean> isInputReference,
            List<String> outputAsInputNames,
            List<VariableType> outputAsInputTypes,
            List<String> outputAsInputDisplayNames,
            String cReturnName,
            VariableType cReturnType,
            String cReturnDisplayName,
            String prefix,
            boolean dependsOnGlobals, boolean canHaveSideEffects, boolean isElementWise, boolean isNoOp) {

        this.inputNames = parseList(inputNames);
        this.inputTypes = parseList(inputTypes);
        this.isInputReference = parseList(isInputReference);

        if (this.inputNames.size() != 0 && this.inputNames.size() != this.inputTypes.size()) {
            SpecsLogs.warn("Number of input names (" + this.inputNames.size() + ") is not the same as " +
                    "the number of input types (" + this.inputTypes.size() + "):\n"
                    + inputNames + " " + inputTypes);
        }
        if (this.isInputReference.size() != 0 && this.isInputReference.size() != this.inputTypes.size()) {
            SpecsLogs.warn("Number of input types (" + this.inputTypes.size() + ") is not the same as " +
                    "the number of by_reference flags (" + this.inputTypes.size() + "):\n"
                    + inputNames + " " + inputTypes);
        }

        this.outputAsInputNames = parseList(outputAsInputNames);
        this.outputAsInputTypes = parseList(outputAsInputTypes);
        this.outputDisplayNames = parseList(outputAsInputDisplayNames);

        this.cReturnName = cReturnName;
        this.cReturnType = cReturnType.pointer().getType(false);
        this.cReturnDisplayName = cReturnDisplayName;

        this.prefix = prefix;

        if (!this.cReturnType.isReturnType()) {
            throw new IllegalArgumentException("Type '" + this.cReturnType
                    + "' cannot be used as return type of a C function.");
        }

        this.outputAsInputTypesNormalized = null;
        this.dependsOnGlobals = dependsOnGlobals;
        this.canHaveSideEffects = canHaveSideEffects;
        this.isElementWise = isElementWise;
        this.isNoOp = isNoOp;
    }

    /**
     * @param inputNames2
     * @return
     */
    private static <T> List<T> parseList(List<T> aList) {
        if (aList == null) {
            return Collections.emptyList();
        }

        if (aList.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(aList);
    }

    /**
     * Should be used when the function is not implementable.
     * 
     * <p>
     * No names for inputs and outputs are needed, no outputs are passed to the function as inputs.
     * 
     * @param inputTypes
     * @param cReturnType
     * @return
     */
    public static FunctionType newInstanceNotImplementable(List<VariableType> inputTypes, VariableType cReturnType) {

        return newInstanceNotImplementable(inputTypes, cReturnType, false, false, false, false);
    }

    public static FunctionType newInstanceNotImplementable(List<VariableType> inputTypes, VariableType cReturnType,
            boolean dependsOnGlobals, boolean withSideEffects, boolean isElementWise, boolean isNoOp) {

        return new FunctionType(null, inputTypes, null, null, null, null, null, cReturnType, null, "",
                dependsOnGlobals,
                withSideEffects,
                isElementWise,
                isNoOp);
    }

    /**
     * Should be used when the function is implementable, and has outputs as inputs.
     * 
     * <p>
     * When using this function, the C return type is always 'void'.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputNames
     * @param outputAsInputTypes
     * @return
     */
    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<VariableType> inputTypes,
            List<String> outputNames, List<VariableType> outputAsInputTypes) {
        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, null,
                outputNames,
                outputAsInputTypes,
                outputNames,
                false,
                false,
                false,
                false);
    }

    /**
     * Should be used when the function is implementable, and has outputs as inputs.
     * 
     * <p>
     * When using this function, the C return type is always 'void'.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputNames
     * @param outputAsInputTypes
     * @return
     */
    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<VariableType> inputTypes,
            List<String> outputNames, List<VariableType> outputAsInputTypes, List<String> outputDisplayNames) {
        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, null, outputNames, outputAsInputTypes,
                outputDisplayNames,
                false,
                false,
                false,
                false);
    }

    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<VariableType> inputTypes,
            List<Boolean> inputIsReference,
            List<String> outputNames, List<VariableType> outputAsInputTypes, List<String> outputAsInputDisplayNames,
            boolean dependsOnGlobals,
            boolean hasSideEffects,
            boolean isElementWise, boolean isNoOp) {
        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, inputIsReference, outputNames,
                outputAsInputTypes, outputAsInputDisplayNames, dependsOnGlobals, hasSideEffects, isElementWise, isNoOp,
                "");
    }

    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<VariableType> inputTypes,
            List<Boolean> inputIsReference,
            List<String> outputNames, List<VariableType> outputAsInputTypes, List<String> outputDisplayNames,
            boolean dependsOnGlobals,
            boolean hasSideEffects,
            boolean isElementWise, boolean isNoOp, String prefix) {

        Preconditions.checkArgument(outputNames.size() == outputAsInputTypes.size());

        if (outputAsInputTypes.size() == 1) {
            return newInstanceWithOutputsAsInputs(inputNames, inputTypes, inputIsReference,
                    outputNames.get(0),
                    outputAsInputTypes.get(0),
                    outputDisplayNames.get(0), true, dependsOnGlobals, hasSideEffects, isElementWise, isNoOp, prefix);
        }

        VoidType voidType = VoidType.newInstance();

        // Convert the output types to pointers as needed
        for (int i = 0; i < outputAsInputTypes.size(); i++) {
            VariableType type = outputAsInputTypes.get(i);

            // Check if type is a pointer
            if (!ReferenceUtils.supportsPointer(type)) {
                continue;
            }

            type = ReferenceUtils.getType(type, true);
            // type = VariableTypeFactory.newPointerToNumericType(type);
            outputAsInputTypes.set(i, type);
        }

        return new FunctionType(inputNames, inputTypes, inputIsReference,
                outputNames, outputAsInputTypes, outputDisplayNames, null, voidType, null, prefix,
                dependsOnGlobals,
                hasSideEffects,
                isElementWise,
                isNoOp);
    }

    /**
     * Should be used when the function is implementable, and has outputs as inputs.
     * 
     * <p>
     * When using this function, the C return type is the same as the given outputAsInput.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputAsInputName
     * @param outputAsInputType
     * @return
     */
    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<? extends VariableType> inputTypes,
            String outputAsInputName, VariableType outputAsInputType) {

        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, null,
                outputAsInputName,
                outputAsInputType,
                outputAsInputName,
                false, false,
                false, false, "");
    }

    /**
     * Should be used when the function is implementable, and has outputs as inputs.
     * 
     * <p>
     * When using this function, the C return type is the same as the given outputAsInput.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputAsInputName
     * @param outputAsInputType
     * @return
     */
    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<? extends VariableType> inputTypes,
            String outputAsInputName, VariableType outputAsInputType, String outputAsInputDisplayName) {

        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, null,
                outputAsInputName,
                outputAsInputType,
                outputAsInputDisplayName,
                false, false,
                false, false, "");
    }

    public static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<? extends VariableType> inputTypes,
            List<Boolean> inputIsReference,
            String outputAsInputName,
            VariableType outputAsInputType,
            String outputAsInputDisplayName,
            boolean dependsOnGlobals,
            boolean hasSideEffects, boolean isElementWise, boolean isNoOp, String prefix) {

        return newInstanceWithOutputsAsInputs(inputNames, inputTypes, inputIsReference, outputAsInputName,
                outputAsInputType, outputAsInputDisplayName, false,
                dependsOnGlobals, hasSideEffects, isElementWise, isNoOp, prefix);
    }

    /**
     * Should be used when the function is implementable, and has outputs as inputs.
     * 
     * <p>
     * When using this function, if returnVoid is false, the C return type is the same as the given outputAsInput, and
     * Void otherwise.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputAsInputName
     * @param outputAsInputType
     * @param returnVoid
     * @return
     */
    private static FunctionType newInstanceWithOutputsAsInputs(List<String> inputNames,
            List<? extends VariableType> inputTypes,
            List<Boolean> inputIsReference,
            String outputAsInputName, VariableType outputAsInputType,
            String outputAsInputDisplayName,
            boolean returnVoid, boolean dependsOnGlobals, boolean hasSideEffects, boolean isElementWise, boolean isNoOp,
            String prefix) {

        // VariableType returnType = getReturnType(outputAsInputType, returnVoid);

        // Convert the output types to pointers as needed
        // Check if type is numeric
        if (ReferenceUtils.supportsPointer(outputAsInputType)) {
            outputAsInputType = ReferenceUtils.getType(outputAsInputType, true);
        }

        List<VariableType> inputTypesCast = Collections.emptyList();
        if (inputTypes != null) {
            inputTypesCast = new ArrayList<>(inputTypes);
        }

        // In case we want to return a void type
        if (returnVoid) {
            return new FunctionType(inputNames, inputTypesCast, inputIsReference, Arrays.asList(outputAsInputName),
                    Arrays.asList(outputAsInputType), Arrays.asList(outputAsInputDisplayName),
                    null,
                    VoidType.newInstance(),
                    null, prefix,
                    dependsOnGlobals,
                    hasSideEffects,
                    isElementWise, isNoOp);
        }

        // When we want to return the output-as-input type
        return new FunctionType(inputNames, inputTypesCast, inputIsReference, Arrays.asList(outputAsInputName),
                Arrays.asList(outputAsInputType), Arrays.asList(outputAsInputDisplayName),
                outputAsInputName, outputAsInputType, outputAsInputDisplayName,
                prefix, dependsOnGlobals,
                hasSideEffects,
                isElementWise, isNoOp);
    }

    /*
    private static VariableType getReturnType(VariableType outputAsInputType, boolean returnVoid) {
    if (returnVoid) {
        return VoidType.newInstance();
    }
    
    // Convert the output types to pointers as needed
    // Check if type is numeric
    if (ReferenceUtils.supportsPointer(outputAsInputType)) {
        return ReferenceUtils.getType(outputAsInputType, true);
    }
    
    return outputAsInputType;
    }
    */

    /**
     * Should be used when the function is implementable, and does not have outputs as inputs.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputName
     * @param cReturnType
     * @return
     */
    public static FunctionType newInstance(List<String> inputNames, List<VariableType> inputTypes,
            String outputName,
            VariableType cReturnType) {
        return newInstance(inputNames, inputTypes, null, outputName, cReturnType, outputName, false, false,
                false, false, "");
    }

    /**
     * Should be used when the function is implementable, and does not have outputs as inputs.
     * 
     * @param inputNames
     * @param inputTypes
     * @param outputName
     * @param cReturnType
     * @return
     */
    public static FunctionType newInstance(List<String> inputNames, List<VariableType> inputTypes,
            String outputName,
            VariableType cReturnType,
            String cReturnDisplayName) {
        return newInstance(inputNames, inputTypes, null, outputName, cReturnType, cReturnDisplayName, false, false,
                false, false, "");
    }

    public static FunctionType newInstance(List<String> inputNames,
            List<VariableType> inputTypes,
            List<Boolean> inputIsReference,
            String outputName,
            VariableType cReturnType,
            String cReturnDisplayName,
            boolean dependsOnGlobals,
            boolean withSideEffects,
            boolean isElementWise,
            boolean isNoOp,
            String prefix) {

        List<String> outputNames = SpecsFactory.newArrayList();
        outputNames.add(outputName);

        return new FunctionType(inputNames, inputTypes, inputIsReference, null, null, null,
                outputName,
                cReturnType,
                cReturnDisplayName,
                prefix,
                dependsOnGlobals,
                withSideEffects,
                isElementWise, isNoOp);
    }

    /**
     * 
     * @return true, if some of the inputs of the function are being used as outputs. False otherwise
     */
    public boolean hasOutputsAsInputs() {
        if (this.outputAsInputNames.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Returns the types of inputs when the function is implemented in C.
     * 
     * @return the inputArguments
     */
    public List<VariableType> getCInputTypes() {
        List<VariableType> inputs = SpecsFactory.newArrayList();

        // Add inputs that aren't references
        for (int i = 0; i < inputTypes.size(); ++i) {
            if (isInputReference.size() == 0 || !isInputReference.get(i)) {
                inputs.add(this.inputTypes.get(i));
            }
        }

        // If there are outputs as inputs, add them
        inputs.addAll(this.outputAsInputTypes);

        return inputs;
    }

    public List<VariableType> getFullCInputTypes() {
        List<VariableType> inputs = SpecsFactory.newArrayList();

        // Add inputs, including references
        inputs.addAll(this.inputTypes);

        // If there are outputs as inputs, add them
        inputs.addAll(this.outputAsInputTypes);

        return inputs;
    }

    public <T extends VariableType> T getInput(Class<T> typeClass, int index) {
        return typeClass.cast(this.inputTypes.get(index));
    }

    public MatrixType getOutput(Class<MatrixType> typeClass, int index) {
        return typeClass.cast(this.outputAsInputTypes.get(index));
    }

    /**
     * Returns the names of inputs when the function is implemented in C.
     * 
     * <p>
     * When the function contains outputs as inputs, returns the input names plus the names of the outputs as inputs.
     * 
     * @return the inputNames
     */
    public List<String> getCInputNames() {
        List<String> cinputNames = SpecsFactory.newArrayList();

        // Add inputs that aren't references
        for (int i = 0; i < inputNames.size(); ++i) {
            if (isInputReference.size() == 0 || !isInputReference.get(i)) {
                cinputNames.add(this.inputNames.get(i));
            }
        }

        // If there are outputs as inputs, add them
        cinputNames.addAll(this.outputAsInputNames);

        return cinputNames;
    }

    /**
     * @return the input names, or an empty list if there are no input names
     */
    public List<String> getArgumentsNames() {

        return this.inputNames;
    }

    /**
     * @return the inputTypes
     */
    public List<VariableType> getArgumentsTypes() {
        return this.inputTypes;
    }

    public List<Variable> getArguments() {
        List<Variable> arguments = new ArrayList<>();

        for (int i = 0; i < this.inputTypes.size(); ++i) {
            arguments.add(new Variable(this.inputNames.get(i), this.inputTypes.get(i)));
        }

        return arguments;
    }

    /**
     * The number of inputs when the function is implemented in C, including the outputs as inputs.
     * 
     * @return
     */
    public int getCNumInputs() {
        return getCInputTypes().size();
    }

    /**
     * The names of the outputs that are used as inputs.
     * 
     * @return
     */
    public List<String> getOutputAsInputNames() {

        return this.outputAsInputNames;
    }

    /**
     * @return the outputAsInputTypes
     */
    public List<VariableType> getOutputAsInputTypes() {
        return this.outputAsInputTypes;
    }

    public List<Variable> getOutputAsInputVariables() {
        List<Variable> outputAsInputs = new ArrayList<>();

        for (int i = 0; i < getNumOutsAsIns(); ++i) {
            outputAsInputs.add(new Variable(this.outputAsInputNames.get(i), this.outputAsInputTypes.get(i)));
        }

        return outputAsInputs;
    }

    /**
     * The types of the outputs that are used as inputs.
     * 
     * <p>
     * The 'pointer' status of the type (e.g., Numeric, MatrixAlloc) is removed before returning.
     * 
     * @return
     */
    public synchronized List<VariableType> getOutputAsInputTypesNormalized() {
        if (this.outputAsInputTypesNormalized != null) {
            return this.outputAsInputTypesNormalized;
        }

        this.outputAsInputTypesNormalized = SpecsFactory.newArrayList();

        for (VariableType outputType : this.outputAsInputTypes) {

            this.outputAsInputTypesNormalized.add(outputType.normalize());
        }

        return this.outputAsInputTypesNormalized;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Inputs:\n");
        for (int i = 0; i < this.inputTypes.size(); i++) {
            String name = this.inputNames.isEmpty() ? "<anonymous>" : this.inputNames.get(i);
            builder.append(name);
            builder.append(" -> ");
            builder.append(this.inputTypes.get(i));
            builder.append("\n");
        }

        // Outputs as inputs always has void as return type, not needed to
        // print.
        if (hasOutputsAsInputs()) {
            builder.append("\nOutputs as Inputs:\n");
            for (int i = 0; i < this.outputAsInputNames.size(); i++) {
                builder.append(this.outputAsInputNames.get(i));
                builder.append("-> ");
                builder.append(this.outputAsInputTypes.get(i));
                builder.append("\n");
            }
        }

        builder.append("\nReturn Type:\n");
        if (this.cReturnName != null) {
            builder.append(this.cReturnName);
        }

        builder.append("-> ");
        builder.append(this.cReturnType);
        builder.append("\n");

        return builder.toString();
    }

    /**
     * The type returned by the C function.
     * 
     * 
     * @return
     */
    public VariableType getCReturnType() {
        return this.cReturnType;
    }

    /**
     * The type returned by the C function.
     * 
     * 
     * @return
     */
    public <T extends VariableType> T getCReturnType(Class<T> type) {
        return type.cast(this.cReturnType);
    }

    /**
     * When the function is implementable, returns the name of the variable used as output, or null if there is no
     * output variable.
     * 
     * @return
     */
    public String getCOutputName() {
        if (this.cReturnName == null) {
            SpecsLogs.warn("The name of the return variable is not set.");
            return null;
        }

        return this.cReturnName;
    }

    /**
     * Convenience method which builds a Variable for the output type.
     * 
     * <p>
     * If the object does not have a return type and name throws a RuntimeException.
     * 
     * @return
     */
    public Variable getReturnVar() {
        if (getCOutputName() == null) {
            throw new RuntimeException("Name of return variable is not set.");
        }

        if (getCReturnType() == null) {
            throw new RuntimeException("Type of return variable is not set.");
        }

        return new Variable(getCOutputName(), getCReturnType());
    }

    public boolean hasReturnVar() {
        if (this.cReturnName == null) {
            return false;
        }

        if (this.cReturnType == null) {
            return false;
        }

        return true;
    }

    /**
     * Returns the variable corresponding to the given input name.
     * 
     * @param inputName
     * @return
     */
    public Variable getInputVar(String inputName) {
        for (int i = 0; i < getCNumInputs(); i++) {
            String currentName = getCInputNames().get(i);
            if (currentName.equals(inputName)) {
                VariableType currentType = getCInputTypes().get(i);
                return new Variable(currentName, currentType);
            }
        }

        throw new RuntimeException("C Input name '" + inputName + "' is not defined.");
    }

    /**
     * The number of outs-as-ins.
     * 
     * @return
     */
    public int getNumOutsAsIns() {
        return getOutputAsInputTypesNormalized().size();
    }

    public String getFunctionPrefix() {
        return this.prefix;
    }

    public List<VariableType> getOutputTypes() {
        if (this.outputAsInputTypes == null || this.outputAsInputTypes.isEmpty()) {
            if (this.cReturnType instanceof VoidType) {
                return Collections.emptyList();
            }
            return Arrays.asList(this.cReturnType);
        }

        return this.outputAsInputTypes.stream()
                .map(v -> v.pointer().getType(false))
                .collect(Collectors.toList());
    }

    public List<String> getOutputNames() {
        if (this.outputAsInputNames == null || this.outputAsInputNames.isEmpty()) {
            if (this.cReturnType instanceof VoidType) {
                return Collections.emptyList();
            }
            return Arrays.asList(this.cReturnName);
        }

        return Collections.unmodifiableList(this.outputAsInputNames);
    }

    public List<String> getOutputAsInputDisplayNames() {
        if (this.outputAsInputNames == null || this.outputAsInputNames.isEmpty()) {
            if (this.cReturnType instanceof VoidType) {
                return Collections.emptyList();
            }
            return Arrays.asList(this.cReturnDisplayName);
        }

        return Collections.unmodifiableList(outputDisplayNames);
    }

    public boolean dependsOnGlobalState() {
        return dependsOnGlobals;
    }

    public boolean canHaveSideEffects() {
        return this.canHaveSideEffects;
    }

    public boolean isElementWise() {
        return this.isElementWise;
    }

    public boolean isNoOp() {
        return this.isNoOp;
    }

    public boolean isInputReference(int i) {
        return isInputReference.size() != 0 && isInputReference.get(i);
    }

    public void addAnnotation(Object annotation) {
        annotations.add(annotation);
    }

    public <T> Stream<? extends T> getAnnotationsStream(Class<? extends T> cls) {
        return annotations.stream()
                .filter(cls::isInstance)
                .map(cls::cast);
    }
}