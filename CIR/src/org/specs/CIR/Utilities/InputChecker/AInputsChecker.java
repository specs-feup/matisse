/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Utilities.InputChecker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRTypes.Types.String.StringTypeUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Base class to create InputChecker classes.
 * 
 * <p>
 * Methods that ask for specific indexes, if the index does not exist return true by default.
 * 
 * @author Joao Bispo
 *
 * @param <T>
 */
public abstract class AInputsChecker<T extends AInputsChecker<T>> implements Checker {

    private final ProviderData data;
    private final List<Check> checks;

    private boolean invertCheck;
    private Integer indexCheck;

    private Integer rangeStart;
    private Integer rangeEnd;

    protected AInputsChecker(ProviderData data, List<Check> checks) {
        this.data = data;
        this.checks = checks;

        invertCheck = false;
        indexCheck = null;

        rangeStart = null;
        rangeEnd = null;
    }

    public AInputsChecker(List<Check> checks) {
        this(null, checks);
    }

    public AInputsChecker() {
        this(null, Lists.newArrayList());
    }

    public AInputsChecker(ProviderData data) {
        this(data, Lists.newArrayList());
        // this.data = data;
        // this.checks = Lists.newArrayList();
    }

    /**
     * Creates a shallow, immutable copy of the InputsChecker with the given ProviderData.
     * 
     * @param data
     * @return
     */
    @Override
    public abstract T create(ProviderData data);

    protected void addCheckPrivate(Check check) {
        // If invert check, encapsulate around inverter
        // This should be done to original check
        if (invertCheck) {
            check = new CheckInverter(check);
            invertCheck = false;
        }

        // If checkIndex is set, add check chain to return true if index is not present
        if (indexCheck != null) {
            // Put in int, so that value is copied, instead of passing the reference of 'indexCheck'
            final int indexInt = indexCheck;
            /*
            	    Check checkIndex = data -> {
            		return !(indexInt < data.getInputTypes().size());
            	    };
            	    check = new CheckChain(checkIndex, check);
            	    */
            check = new CheckChain(data -> !(indexInt < data.getInputTypes().size()), check);
            indexCheck = null;
        }

        // If range defined, encapsulate around range
        // Applied after possible checkIndex, to apply index to range.
        if (rangeStart != null) {
            check = new CheckRange(check, rangeStart, rangeEnd);

            rangeStart = null;
            rangeEnd = null;
        }

        checks.add(check);
    }

    /**
     * Verifies all given rules.
     * 
     * @return true if passes all checks, false otherwise
     */
    @Override
    public boolean check() {
        // Only process if ProviderData is present
        if (data == null) {
            return false;
        }

        for (Check check : checks) {
            boolean pass = check.check(data);

            if (!pass) {
                return false;
            }
        }

        return true;
    }

    /**
     * Inverts the next check.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public T not() {
        invertCheck = true;
        return (T) this;
    }

    /**
     * Applies the next check over this range of inputs
     * 
     * @param startIndex
     *            inclusive
     * @param endIndex
     *            exclusive
     * @return
     */
    @SuppressWarnings("unchecked")
    public T range(int startIndex, int endIndex) {
        rangeStart = startIndex;
        rangeEnd = endIndex;
        return (T) this;
    }

    /**
     * Helper method, which sets the end of the range as the end of the inputs list.
     * 
     * @param startIndex
     * @return
     */
    public T range(int startIndex) {
        return range(startIndex, -1);
    }

    /**
     * 
     * @param check
     * @return
     */
    @SuppressWarnings("unchecked")
    public T addCheck(Check check) {
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * Internal check, useful for checks that ask about a specific index.
     * 
     * <p>
     * Creates a CheckChain, where the first check is whether the index is valid. If the index is not valid, returns
     * true immediately. This is to be used when checks ask about a specific index, and we want to ignore the check if
     * the index does not exist.
     * 
     * @param inputTypes
     * @param i
     * @param success
     * @return
     */
    protected Check isIndexValid(int index, Check check) {
        indexCheck = index;
        return check;
        // return new CheckChain(data -> !(index < data.getInputTypes().size()), check);
        // return index < data.getInputTypes().size();
    }

    @SuppressWarnings("unchecked")
    public T numOfInputsRange(int start, int end) {
        Check check = (ProviderData data) -> {

            if (data.getInputTypes().size() < start) {
                return false;
            }

            if (data.getInputTypes().size() > end) {
                return false;
            }

            return true;
        };

        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param numberOfInputs
     * @return true if the number of inputs is the same as the given one
     */
    @SuppressWarnings("unchecked")
    public T numOfInputs(int numberOfInputs) {

        Check check = (ProviderData data) -> data.getInputTypes().size() == numberOfInputs;
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param inputTypes
     * @param i
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T numOfInputsAtLeast(int numberOfInputs) {
        Check check = (ProviderData data) -> data.getInputTypes().size() >= numberOfInputs;
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @return true if all the inputs are matrices
     */
    @SuppressWarnings("unchecked")
    public T areMatrices() {
        Check check = (ProviderData data) -> TypeVerification.areMatrices(data.getInputTypes());
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param numberOfOutputs
     * @return true if the number of outputs is the same as the given one
     */
    @SuppressWarnings("unchecked")
    public T numOfOutputs(int numberOfOutputs) {

        Check check = (ProviderData data) -> {
            Optional<Integer> nargouts = data.getNargouts();
            if (!nargouts.isPresent()) {
                return numberOfOutputs == 0 || numberOfOutputs == 1;
            }
            return nargouts.get() == numberOfOutputs;
        };
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param numberOfOutputs
     * @return true if the number of outputs is less or equal as the given one
     */
    @SuppressWarnings("unchecked")
    public T numOfOutputsAtMost(int numberOfOutputs) {
        Preconditions.checkArgument(numberOfOutputs > 0,
                "Function is meaningless for values below 0, and for a value of 0, use numOfOutputs instead.");
        Check check = (ProviderData data) -> data.getNargouts().orElse(0) <= numberOfOutputs;
        addCheckPrivate(check);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T numOfOutputsAtLeast(int numberOfOutputs) {
        Preconditions.checkArgument(numberOfOutputs >= 2, "Function is meaningless for values below 2");

        Check check = (ProviderData data) -> data.getNargouts().orElse(0) >= numberOfOutputs;
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param index
     * @return true if the variable type at the given index is a matrix
     */
    @SuppressWarnings("unchecked")
    public T isMatrix(int index) {
        // numOfInputsAtLeast(index + 1);

        Check check = (ProviderData data) -> MatrixUtils.isMatrix(data.getInputTypes().get(index));
        // addCheckPrivate(check);
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * @param index
     * @return true if the variable type at the given index uses dynamic allocation. If there is no variable defined at
     *         the given address, returns true
     */
    @SuppressWarnings("unchecked")
    public T usesDynamicAllocation(int index) {

        Check check = (ProviderData data) -> data.getInputTypes().get(index).usesDynamicAllocation();
        // addCheckPrivate(check);
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * @param index
     * @return true if the variable type at the given index is a matrix
     */
    @SuppressWarnings("unchecked")
    public T is1DMatrix(int index) {

        // Check check = data -> MatrixUtils.isRowVector(data.getInputTypes().get(index))
        // || MatrixUtils.isColumnVector(data.getInputTypes().get(index));
        Check check = data -> data.getInputType(MatrixType.class, index).getTypeShape().isKnown1D();
        addCheckPrivate(isIndexValid(index, check));

        return (T) this;
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T areScalar() {
        Check check = data -> {
            boolean result = TypeVerification.areScalar(data.getInputTypes());
            return result;
        };
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T areInteger() {
        Check check = data -> {
            for (VariableType type : data.getInputTypes()) {
                if (!isInteger(type)) {
                    return false;
                }
            }

            return true;

        };

        addCheckPrivate(check);
        return (T) this;

    }

    private static boolean isInteger(VariableType type) {
        if (!(type instanceof ScalarType)) {
            return false;
        }

        return ((ScalarType) type).scalar().isInteger();
    }

    /**
     * Checks if all the inputs are of the given type.
     * 
     * @param type
     * @return
     */
    // The same as ofType
    /*
    @SuppressWarnings("unchecked")
    public <V extends VariableType> T are(Class<V> type) {
    Check check = data -> {
        boolean result = TypeVerification.are(type, data.getInputTypes());
        return result;
    };
    addCheckPrivate(check);
    return (T) this;
    }
    */

    /**
     * True if the given type implements ScalarType.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T isScalar(int index) {
        // numOfInputsAtLeast(index + 1);

        Check check = (ProviderData data) -> ScalarUtils.isScalar(data.getInputTypes().get(index));
        // addCheckPrivate(check);
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * True if the given type can be cast to the type indicated by type class.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V extends VariableType> T ofType(Class<V> typeClass, int index) {

        Check check = (ProviderData data) -> typeClass.isInstance(data.getInputTypes().get(index));

        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * True if all input types can be cast to the type indicated by type class.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V extends VariableType> T ofType(Class<V> typeClass) {

        Check check = (ProviderData data) -> {

            for (VariableType type : data.getInputTypes()) {
                if (!typeClass.isInstance(type)) {
                    return false;
                }
            }

            return true;
        };

        addCheckPrivate(check);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T outputOfType(Class<? extends VariableType> typeClass, int index) {
        Check check = (ProviderData data) -> {

            return data.getOutputTypes().size() > index &&
                    typeClass.isInstance(data.getOutputTypes().get(index));
        };

        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * Returns true if the given type can be converted to scalar.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T hasScalarType(int index) {
        // numOfInputsAtLeast(index + 1);

        Check check = (ProviderData data) -> ScalarUtils.hasScalarType(data.getInputTypes().get(index));
        // addCheckPrivate(check);
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * Returns true if any of the inputs is a matrix
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    @SuppressWarnings("unchecked")
    public T hasMatrix() {
        Check check = (ProviderData data) -> {

            for (VariableType type : data.getInputTypes()) {
                if (MatrixUtils.isMatrix(type)) {
                    return true;
                }
            }

            return false;
        };

        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public T isString(int index) {
        // numOfInputsAtLeast(index + 1);

        Check check = (ProviderData data) -> StringTypeUtils.isString(data.getInputTypes().get(index));
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * An unmodifiable view of the list of checks.
     * 
     * @return
     */
    protected List<Check> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    /**
     * Checks if the input type at the given index comes from a literal
     * 
     * @param index
     */
    @SuppressWarnings("unchecked")
    public T isConstant(int index) {
        Check check = (ProviderData data) -> {
            // if (!isIndexValid(index)) {
            // return true;
            // }

            VariableType type = data.getInputTypes().get(index);
            if (!(type instanceof ScalarType)) {
                return false;
            }

            return ((ScalarType) type).scalar().hasConstant();
        };

        // addCheckPrivate(check);
        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * Checks if the input type at the given index is an integer.
     * 
     * <p>
     * If there is no type defined at the given address, returns true
     * 
     * @param index
     */
    @SuppressWarnings("unchecked")
    public T isInteger(int index) {

        Check check = (ProviderData data) -> isInteger(data.getInputTypes().get(index));

        addCheckPrivate(isIndexValid(index, check));
        return (T) this;
    }

    /**
     * Checks if the input type at the given index is a row matrix.
     * 
     */
    @SuppressWarnings("unchecked")
    public T isKnownRowMatrix(int index) {
        Check check = (ProviderData data) -> {
            VariableType type = data.getInputTypes().get(index);

            if (!MatrixUtils.isMatrix(type)) {
                return false;
            }
            TypeShape shape = MatrixUtils.getShape(type);
            return shape.isKnownRow();
        };
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * Checks if the input type at the given index is a row matrix.
     * 
     */
    @SuppressWarnings("unchecked")
    public T isKnownColumnMatrix(int index) {
        Check check = (ProviderData data) -> {
            VariableType type = data.getInputTypes().get(index);
            if (!MatrixUtils.isMatrix(type)) {
                return false;
            }

            TypeShape shape = MatrixUtils.getShape(type);
            return shape.isKnownColumn();
        };
        addCheckPrivate(check);
        return (T) this;
    }

    /**
     * Indicates that all input types starting from the specified index are scalars
     * 
     * @param start
     *            First index that should be a scalar
     */
    @SuppressWarnings("unchecked")
    public T areScalarFrom(int start) {
        Check check = (ProviderData data) -> {
            return data.getInputTypes()
                    .stream()
                    .skip(start)
                    .allMatch(ScalarType.class::isInstance);
        };
        addCheckPrivate(check);
        return (T) this;
    }
}
