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

package org.specs.CIR.FunctionInstance;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

/**
 * A helper class to create FunctionType instances.
 * 
 * <p>
 * This class is <strong>not thread-safe</strong>, as it assumes that it is only modified by one thread at a time.
 * 
 * @author Luís Reis
 *
 */
public final class FunctionTypeBuilder {
    private static enum BuilderType {
        AUTO,
        SIMPLE,
        INLINE,
        OUTPUTS_AS_INPUTS,
        SINGLE_OUTPUT_AS_INPUT
    }

    private final BuilderType type;
    private boolean isFinal;
    private final List<String> inputNames;
    private final List<VariableType> inputTypes;
    private final List<Boolean> inputIsReference;
    private final List<String> outputAsInputNames;
    private final List<VariableType> outputAsInputTypes;
    private final List<Object> annotations;
    private List<String> outputDisplayNames;
    private String cReturnName;
    private List<String> prefixes = new ArrayList<>();
    private VariableType cReturnType;
    private boolean dependsOnGlobals;
    private boolean hasSideEffects;
    private boolean isElementWise;
    private boolean isNoOp;

    private FunctionTypeBuilder(BuilderType type) {
        this.type = type;

        this.isFinal = false;
        this.inputNames = new ArrayList<>();
        this.inputTypes = new ArrayList<>();
        this.inputIsReference = new ArrayList<>();

        this.outputAsInputNames = new ArrayList<>();
        this.outputAsInputTypes = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    /**
     * Used to create C instances with a single output (not as input), which is potentially void.
     * 
     * @return A new builder
     */
    public static FunctionTypeBuilder newSimple() {
        return new FunctionTypeBuilder(BuilderType.SIMPLE);
    }

    /**
     * Used to create inline instances (not implementable)
     * 
     * @return A new builder
     */
    public static FunctionTypeBuilder newInline() {
        return new FunctionTypeBuilder(BuilderType.INLINE);
    }

    /**
     * Used to create C instances with outputs-as-inputs.
     * 
     * @return A new builder
     */
    public static FunctionTypeBuilder newWithOutputsAsInputs() {
        return new FunctionTypeBuilder(BuilderType.OUTPUTS_AS_INPUTS);
    }

    /**
     * Used to create C instances with outputs-as-inputs.
     * 
     * @return A new builder
     */
    public static FunctionTypeBuilder newWithSingleOutputAsInput() {
        return new FunctionTypeBuilder(BuilderType.SINGLE_OUTPUT_AS_INPUT);
    }

    /**
     * Used to create non-inline instances that automatically figure out which type category they should be.
     */
    public static FunctionTypeBuilder newAuto() {
        return new FunctionTypeBuilder(BuilderType.AUTO);
    }

    private void validateChange() {
        if (this.isFinal) {
            throw new IllegalStateException("Can't change FunctionTypeBuilder after calling build()");
        }
    }

    /**
     * Adds an input to the function type
     * 
     * @param inputName
     *            The input name. May only be null if the builder is inline.
     * @param inputType
     *            The input type.
     * @return This builder instance.
     * @see #addInlineInput(VariableType)
     * @see #addReferenceInput(String, VariableType)
     */
    public FunctionTypeBuilder addInput(String inputName, VariableType inputType) {
        Preconditions.checkArgument(this.type == BuilderType.INLINE || inputName != null,
                "inputName can only be null if the instance is inline (not implementable)");
        Preconditions.checkArgument(inputType != null);
        validateChange();

        this.inputNames.add(inputName);
        this.inputTypes.add(inputType);
        this.inputIsReference.add(false);

        return this;
    }

    public FunctionTypeBuilder addInput(Variable variable) {
        Preconditions.checkArgument(variable != null);
        validateChange();

        return addInput(variable.getName(), variable.getType());
    }

    /**
     * Adds a reference input to the function type.
     * 
     * @param inputName
     *            The input name. May only be null if the builder is inline.
     * @param inputType
     *            The input type.
     * @return This builder instance.
     * @see #addInput(String, VariableType)
     */
    public FunctionTypeBuilder addReferenceInput(String inputName, VariableType inputType) {
        Preconditions.checkArgument(this.type != BuilderType.INLINE,
                "Inline functions don't currently support reference inputs.");
        Preconditions.checkArgument(inputName != null);
        Preconditions.checkArgument(inputType != null);
        validateChange();

        this.inputNames.add(inputName);
        this.inputTypes.add(inputType);
        this.inputIsReference.add(true);

        return this;
    }

    /**
     * Adds an input to the function type. Meant to be used with inline function types, which ignore the input names.
     * 
     * @param inputType
     *            The input type.
     * @return This builder instance.
     * @see #addInput(String, VariableType)
     */
    public FunctionTypeBuilder addInput(VariableType inputType) {
        return addInput(null, inputType);
    }

    /**
     * Adds inputs to the function type.
     * 
     * @param inputNames
     *            The input names.
     * @param inputTypes
     *            The input types.
     * @return This builder instance
     * @see #addInput(String, VariableType)
     * @see #addInputs(List)
     */
    public FunctionTypeBuilder addInputs(List<String> inputNames, List<? extends VariableType> inputTypes) {
        Preconditions.checkArgument(inputNames != null);
        Preconditions.checkArgument(inputTypes != null);
        Preconditions.checkArgument(inputNames.size() == inputTypes.size());

        for (int i = 0; i < inputNames.size(); ++i) {
            addInput(inputNames.get(i), inputTypes.get(i));
        }

        return this;
    }

    /**
     * Adds num inputs of type inputType
     * 
     * @param inputType
     * @param num
     * @return
     */
    public FunctionTypeBuilder addInputs(VariableType inputType, int num) {
        Preconditions.checkArgument(num >= 0);

        for (int i = 0; i < num; ++i) {
            addInput(inputType);
        }

        return this;
    }

    /**
     * Adds inputs to the function type. Meant to be used with inline function types, which ignore the input names.
     * 
     * @param inputTypes
     *            The input types.
     * @return This builder instance.
     * @see #addInput(VariableType)
     * @see #addInputs(List, List)
     */
    public FunctionTypeBuilder addInputs(List<? extends VariableType> inputTypes) {
        Preconditions.checkArgument(inputTypes != null);

        for (VariableType inputType : inputTypes) {
            addInput(inputType);
        }

        return this;
    }

    /**
     * Adds an output-as-input to the function type. Can only be used with output-as-input instances.
     * 
     * @param outputName
     *            The name of the output.
     * @param outputType
     *            The type of the output.
     * @return This builder instance.
     * @see #returning(String, VariableType)
     */
    public FunctionTypeBuilder addOutputAsInput(String outputName, VariableType outputType) {
        Preconditions.checkArgument(outputName != null);
        Preconditions.checkArgument(outputType != null);
        Preconditions.checkState(
                this.type == BuilderType.AUTO || this.type == BuilderType.OUTPUTS_AS_INPUTS
                        || this.type == BuilderType.SINGLE_OUTPUT_AS_INPUT,
                "addOutputAsInput is only valid for automatic or outputs-as-inputs instances");
        Preconditions.checkState(this.type != BuilderType.SINGLE_OUTPUT_AS_INPUT || this.outputAsInputTypes.isEmpty(),
                "Calling addOutputAsInput multiple times for builder with single output as input");

        this.outputAsInputNames.add(outputName);
        this.outputAsInputTypes.add(outputType);

        return this;
    }

    public FunctionTypeBuilder addOutputAsInput(Variable variable) {
        return addOutputAsInput(variable.getName(), variable.getType());
    }

    public int getOutputsSoFar() {
        return outputAsInputTypes.size();
    }

    public FunctionTypeBuilder withDisplayNames(List<String> outputDisplayNames) {
        Preconditions.checkArgument(outputDisplayNames != null);
        Preconditions.checkState(this.outputDisplayNames == null);

        this.outputDisplayNames = outputDisplayNames;

        return this;
    }

    /**
     * Indicates that the function returns the given type. Only valid for simple and inline builders.
     * 
     * @param outputName
     *            The name of the return variable. May be null.
     * @param returnType
     *            The type to return
     * @return This builder instance.
     * @see #addOutputAsInput(String, VariableType)
     * @see #returningVoid()
     */
    public FunctionTypeBuilder returning(String returnName, VariableType returnType) {
        Preconditions.checkArgument(returnType != null);
        Preconditions.checkState(this.type == BuilderType.SIMPLE || this.type == BuilderType.INLINE,
                "C return type is implicit for instances with outputs-as-inputs.");
        Preconditions.checkState(this.cReturnType == null, "Can only call returnType once");
        validateChange();

        this.cReturnName = returnName;
        this.cReturnType = returnType;

        return this;
    }

    /**
     * Indicates that the function returns the given type, but specify no name for the output. Only valid for simple and
     * inline builders.
     * 
     * @param returnType
     *            The type to return.
     * @return This builder instance.
     * @see #returning(String, VariableType)
     * @see #returningVoid()
     */
    public FunctionTypeBuilder returning(VariableType returnType) {
        return returning(null, returnType);
    }

    /**
     * Indicates that the given instance has the void return type.
     * 
     * @return This builder instance
     * @see #returning(String, VariableType)
     */
    public FunctionTypeBuilder returningVoid() {
        return returning(null, VoidType.newInstance());
    }

    /**
     * Indicates that the function has the specified prefix.
     * <p>
     * This is used in OpenCL, where functions begin with the "kernel".
     * <p>
     * Also used to implement __declspec(dllexport).
     * 
     * @param prefix
     *            The prefix
     * @return This function instance.
     */
    public FunctionTypeBuilder withPrefix(String prefix) {
        Preconditions.checkArgument(prefix != null);

        if (this.type == BuilderType.INLINE) {
            throw new NotImplementedException("Prefix is not supported for inline functions.");
        }

        this.prefixes.add(prefix);

        return this;
    }

    /**
     * Indicates that the function may have side effects.
     * 
     * @return This builder instance.
     */
    public FunctionTypeBuilder withSideEffects() {
        Preconditions.checkState(!this.hasSideEffects,
                "withSideEffects has already been called for the given instance");
        validateChange();

        this.hasSideEffects = true;

        return this;
    }

    /**
     * Indicates that the function may have side effects if the condition is true.
     * 
     * @param condition
     *            If true, specifies that the function can have side effects. If false, nothing happens.
     * @return This builder instance.
     */
    public FunctionTypeBuilder withSideEffectsIf(boolean condition) {
        Preconditions.checkState(!this.hasSideEffects,
                "withSideEffects has already been called for the given instance");
        validateChange();

        if (condition) {
            withSideEffects();
        }

        return this;
    }

    /**
     * Indicates that the function is element-wise.
     * 
     * @return This builder instance.
     */
    public FunctionTypeBuilder elementWise() {
        Preconditions.checkState(!this.isElementWise, "elementWise has already been called for the given instance");
        Preconditions.checkState(this.type != BuilderType.SIMPLE, "elementWise is invalid for simple functions.");
        validateChange();

        this.isElementWise = true;

        return this;
    }

    /**
     * Indicates that the function has no effect and can be safely removed without any consequence what-so-ever. The
     * code remains valid.
     * 
     * @return
     */
    public FunctionTypeBuilder noOp() {
        Preconditions.checkState(!this.isNoOp, "noOp has already been called for the given instance");

        validateChange();

        this.isNoOp = true;

        return this;
    }

    public FunctionTypeBuilder noOpIf(boolean condition) {
        Preconditions.checkState(!this.isNoOp, "noOp has already been called for the given instance");
        validateChange();

        if (condition) {
            noOp();
        }

        return this;
    }

    public FunctionTypeBuilder withGlobalStateDependency() {
        Preconditions.checkState(!this.dependsOnGlobals,
                "withGlobalStateDependency has already called for the given instance");

        validateChange();

        this.dependsOnGlobals = true;

        return this;
    }

    public FunctionTypeBuilder addAnnotation(Object annotation) {
        this.annotations.add(annotation);

        return this;
    }

    /**
     * Creates a new function type instance.
     * 
     * @return The created function type.
     */
    public FunctionType build() {
        FunctionType type = buildBase();

        for (Object annotation : annotations) {
            type.addAnnotation(annotation);
        }

        return type;
    }

    private FunctionType buildBase() {
        this.isFinal = true;

        // FIXME
        List<String> outputDisplayNames = new ArrayList<>();
        if (this.type == BuilderType.SIMPLE) {
            if (VoidTypeUtils.isVoid(cReturnType)) {
                if (this.outputDisplayNames != null && this.outputDisplayNames.size() != 0) {
                    throw new RuntimeException("Can't have output display names without outputs.");
                }
            } else {
                if (this.outputDisplayNames != null) {
                    if (this.outputDisplayNames.size() != 1) {
                        throw new RuntimeException("Mismatch between display names and number of outputs.");
                    }

                    outputDisplayNames.add(outputDisplayNames.get(0));
                } else {
                    outputDisplayNames.add(cReturnName);
                }
            }
        } else if (this.type != BuilderType.INLINE) {
            if (this.outputDisplayNames != null) {
                if (this.outputDisplayNames.size() != outputAsInputNames.size()) {
                    throw new RuntimeException("Mismatch between display names and number of outputs.");
                }

                outputDisplayNames.addAll(this.outputDisplayNames);
            } else {
                for (int i = 0; i < outputAsInputNames.size(); ++i) {
                    outputDisplayNames.add(outputAsInputNames.get(i));
                }
            }
        }

        String prefix = prefixes.isEmpty() ? "" : String.join("\n", prefixes) + " ";

        final BuilderType actualType;

        if (this.type == BuilderType.AUTO) {
            if (outputAsInputTypes.size() == 0) {
                actualType = BuilderType.SIMPLE;
                cReturnName = null;
                cReturnType = VoidType.newInstance();
            } else if (outputAsInputTypes.size() == 1) {
                if (outputAsInputTypes.get(0) instanceof ScalarType) {
                    actualType = BuilderType.SIMPLE;
                    cReturnName = outputAsInputNames.get(0);
                    cReturnType = outputAsInputTypes.get(0);
                } else {
                    actualType = BuilderType.SINGLE_OUTPUT_AS_INPUT;
                }
            } else {
                actualType = BuilderType.OUTPUTS_AS_INPUTS;
            }
        } else {
            actualType = type;
        }

        switch (actualType) {
        case SIMPLE:
            if (this.cReturnType == null) {
                throw new IllegalStateException("build called, but no return type specified");
            }

            return FunctionType.newInstance(this.inputNames, this.inputTypes,
                    this.inputIsReference,
                    this.cReturnName, this.cReturnType,
                    outputDisplayNames.size() == 0 ? null : outputDisplayNames.get(0),
                    this.dependsOnGlobals,
                    this.hasSideEffects,
                    this.isElementWise,
                    this.isNoOp, prefix);
        case INLINE:
            if (this.cReturnType == null) {
                throw new IllegalStateException("build called, but no return type specified");
            }

            for (boolean isRef : inputIsReference) {
                if (isRef) {
                    throw new IllegalStateException("Inline functions don't currently support reference inputs.");
                }
            }

            return FunctionType.newInstanceNotImplementable(this.inputTypes,
                    this.cReturnType,
                    this.dependsOnGlobals,
                    this.hasSideEffects,
                    this.isElementWise,
                    this.isNoOp);
        case OUTPUTS_AS_INPUTS:
            if (this.outputAsInputTypes.size() == 0) {
                throw new IllegalStateException("build called, but no outputs-as-inputs specified.");
            }

            return FunctionType.newInstanceWithOutputsAsInputs(this.inputNames,
                    this.inputTypes,
                    this.inputIsReference,
                    this.outputAsInputNames,
                    this.outputAsInputTypes,
                    outputDisplayNames,
                    this.dependsOnGlobals,
                    this.hasSideEffects,
                    this.isElementWise,
                    this.isNoOp,
                    prefix);
        case SINGLE_OUTPUT_AS_INPUT:
            if (this.outputAsInputTypes.size() == 0) {
                throw new IllegalStateException("build called, but no outputs-as-inputs specified.");
            }

            return FunctionType.newInstanceWithOutputsAsInputs(this.inputNames,
                    this.inputTypes,
                    this.inputIsReference,
                    this.outputAsInputNames.get(0),
                    this.outputAsInputTypes.get(0),
                    outputDisplayNames.get(0),
                    this.dependsOnGlobals,
                    this.hasSideEffects,
                    this.isElementWise,
                    this.isNoOp,
                    prefix);
        default:
            // This is not supposed to happen.
            throw new NotImplementedException("Unrecognized type: " + type);
        }
    }
}
