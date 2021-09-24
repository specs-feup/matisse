/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIR.FunctionInstance.InstanceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Exceptions.CirInvalidTypesException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.CirNodes;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.CIRFunctions.CLibrary.CFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * An InstanceHelper augmented with methods that retrieve CIR-related information.
 * 
 * @author Joao Bispo
 * 
 */
public abstract class AInstanceBuilder implements InstanceBuilder {

    private final ProviderData pdata;

    private final CirNodes nodes;

    private CFunctions cfunctions;

    /**
     * Skeleton for InstanceHelper to build functions.
     * 
     * <p>
     * Constructor invokes getChecker() on given parameters, if input types do not pass the check, throws an Exception.
     * 
     * @param data
     */
    public AInstanceBuilder(ProviderData data) {
        // Check if input types are ok for this builder
        if (!check(data)) {
            throw new CirInvalidTypesException(getClass(), data);
        }

        // Using data directly
        pdata = data;

        nodes = new CirNodes(data.getSettings());

        cfunctions = null;
    }

    /**
     * 
     * @return true if the given ProviderData passes the Checker of this class, false otherwise
     */
    private boolean check(ProviderData data) {
        return getCheckerPrivate().create(data).check();
    }

    @Override
    public CFunctions getFunctions() {
        if (cfunctions == null) {
            cfunctions = new CFunctions(pdata.getNumerics());
        }

        return cfunctions;
    }

    /**
     * @return the pdata
     */
    @Override
    public ProviderData getData() {
        return pdata;
    }

    @Override
    public DataStore getSettings() {
        return pdata.getSettings();
    }

    public CirNodes getNodes() {
        return nodes;
    }

    @Override
    public NumericFactory getNumerics() {
        return pdata.getNumerics();
    }

    protected <T extends VariableType> T getTypeAtIndex(Class<T> aClass, int index) {

        List<VariableType> inputs = getData().getInputTypes();

        Preconditions.checkArgument(index < inputs.size(),
                "Index (" + index + ") is too big for inputs (" + inputs.size() + ")");

        VariableType type = inputs.get(index);

        Preconditions.checkArgument(aClass.isInstance(type),
                "Input must be of type '" + aClass.getSimpleName() + "', got " + type);

        return aClass.cast(type);

    }

    /**
     * Creates the FunctionInstance.
     * 
     * @return
     */
    @Override
    public abstract FunctionInstance create();

    /**
     * Returns the InputsChecker.
     * 
     * @return
     */
    protected Checker getCheckerPrivate() {
        // Checker that always returns true
        return () -> true;
    }

    public String getFunctionName(String baseName, List<VariableType> inputs) {
        return baseName + FunctionInstanceUtils.getTypesSuffix(inputs);
    }

    public String getFunctionName(String baseName, VariableType... inputs) {
        return getFunctionName(baseName, Arrays.asList(inputs));
    }

    /**
     * Tries to build a matrix shape from a list of input types that represent the dimension of the matrix, using the
     * following rules:
     * 
     * <p>
     * - If it is present only one input type, it considers it is a 2D square matrix;<br>
     * - If inputs have constants, the value is used in the matrix shape.<br>
     * 
     * 
     * 
     * @param inputTypes
     * @return
     */
    protected TypeShape getMatrixShape(List<ScalarType> inputTypes) {
        // Square matrix case
        if (inputTypes.size() == 1) {
            Number constant = inputTypes.get(0).scalar().getConstant();
            if (constant == null) {
                return TypeShape.newUndefinedSquare();
            }

            return TypeShape.newInstance(constant.intValue(), constant.intValue());
        }

        // General case
        List<Integer> dims = new ArrayList<>();
        for (ScalarType type : inputTypes) {
            Number dim = type.scalar().getConstant();

            if (dim == null) {
                dims.add(-1);
            } else {
                dims.add(dim.intValue());
            }
        }

        return TypeShape.newInstance(dims);
    }

}
