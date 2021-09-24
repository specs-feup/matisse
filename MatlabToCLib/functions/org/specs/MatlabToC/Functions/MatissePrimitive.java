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

package org.specs.MatlabToC.Functions;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.MatissePrimitives.CompatibilityPackageResource;
import org.specs.MatlabToC.Functions.MatissePrimitives.MatissePrimitiveProviders;
import org.specs.MatlabToC.Functions.MatissePrimitives.NewArrayFromDims;
import org.specs.MatlabToC.Functions.MatissePrimitives.NewArrayFromValues;
import org.specs.MatlabToC.Functions.MatissePrimitives.NewArrayStatic;
import org.specs.MatlabToC.Functions.MatissePrimitives.ReserveCapacity;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.specs.MatlabToC.Utilities.MatlabResourceProvider;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Contains functions that provide basic functionality, but that are not functions that we find in MATLAB.
 * 
 * @author Joao Bispo
 * 
 */
public enum MatissePrimitive implements MatlabFunctionProviderEnum {

    /**
     * New array, uninitialized.
     * 
     * 
     * new_array(SHAPE, <'class'>), where SHAPE is a matrix and 'class' is the element type of the array
     *
     */
    NEW_ARRAY(CompatibilityPackageResource.NEW_ARRAY) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for declared version of zeros and constant numeric inputs
            // providers.add(new ConstantArrayDecBuilder("zeros", 0));

            // Provider when for dynamic allocation is not allowed
            providers.add(NewArrayStatic.getProvider());

            // Provider for matrix input, dynamic implementation
            providers.add(MatissePrimitiveProviders.newArrayDynamic());

            return providers;
        }

    },
    /**
     * New array, uninitialized.
     * 
     * 
     * new_array(DIM1, DIM2, <'class'>), where DIMs are the values and 'class' is the element type of the array. This
     * effectively behaves like zeros, except that the values are not necessarily initialized to zero.
     *
     */
    NEW_ARRAY_FROM_DIMS(CompatibilityPackageResource.NEW_ARRAY_FROM_DIMS) {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            providers.add(NewArrayFromDims.getProvider());

            return providers;
        }
    },

    /**
     * New array, uninitialized.
     * 
     * <p>
     * new_array(MATRIX), where MATRIX is a matrix, whose shape and type will be use to create a new matrix.
     * 
     */
    NEW_ARRAY_FROM_MATRIX(CompatibilityPackageResource.NEW_ARRAY_FROM_MATRIX) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for declared version of zeros and constant numeric inputs
            // providers.add(new ConstantArrayDecBuilder("zeros", 0));

            // Provider for matrix input, dynamic implementation
            providers.add(MatissePrimitiveProviders.newArrayFromMatrix());

            return providers;
        }

    },

    /**
     * New array, uninitialized.
     * 
     * <p>
     * new_array(matrix, element, dim1, dim2...), where it accepts:<br>
     * - A matrix, which will be used as the base of the output matrix, if present;<br>
     * - An element, which will set the element type of the matrix;<br>
     * - A variable number of integers which will determine the shape of the matrix;<br>
     * 
     */
    NEW_ARRAY_FROM_VALUES(CompatibilityPackageResource.NEW_ARRAY_FROM_VALUES) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // General provider for any matrices
            providers.add(NewArrayFromValues.getProvider());

            return providers;
        }

    },

    RESERVE_CAPACITY(CompatibilityPackageResource.RESERVE_CAPACITY) {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            providers.add(ReserveCapacity.getProvider());

            return providers;
        }
    },

    /**
     * Cast element to the default real type.
     */
    TO_REAL(CompatibilityPackageResource.TO_REAL) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for matrix input, dynamic implementation
            providers.add(MatissePrimitiveProviders.newCastToReal());

            return providers;
        }

    },

    /**
     * Changes the shape of a matrix.
     */
    CHANGE_SHAPE(CompatibilityPackageResource.CHANGE_SHAPE) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for matrix input, dynamic implementation
            providers.add(MatissePrimitiveProviders.newChangeShape());

            return providers;
        }

    },

    /**
     * Changes the shape of a matrix.
     */
    IDIVIDE(CompatibilityPackageResource.IDIVIDE) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for generic implementation
            providers.add(MatissePrimitiveProviders.newIDivide());

            return providers;
        }

    },

    MAX_OR_ZERO(CompatibilityPackageResource.MAX_OR_ZERO) {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = SpecsFactory.newArrayList();

            // Provider for generic implementation
            providers.add(MatissePrimitiveProviders.newMaxOrZero());

            return providers;
        }
    };

    // private final String matlabFunctionName;
    private final MatlabResourceProvider matlabResource;

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    /**
     * Resource that represents an M-file with an implementation of the function that can be used to execute the code in
     * pure MATLAB with equivalent functionality.
     * 
     * @return
     */
    public MatlabResourceProvider getCompatibilityMFile() {
        return matlabResource;
    }

    /**
     * Constructor.
     * 
     * @param matlabFunctionName
     */
    // private MatissePrimitive(String matlabFunctionName) {
    private MatissePrimitive(MatlabResourceProvider matlabResource) {
        // this.matlabFunctionName = matlabFunctionName;
        this.matlabResource = matlabResource;
    }

    @Override
    public String getName() {
        return matlabResource.getFunctionName();
    }

    /**
     * Builds a list of MatlabFunctions using the compatibility functions, instead of the primitives.
     * 
     * @return
     */
    /*
    public static List<MatlabFunction> buildCompatibilityFunctions() {
    MFunctionProvider builder = MFunctionProvider.newInstance(true, fileName, matlabFunction, mtoken, data);
    // TODO Auto-generated method stub
    return null;
    }
    */

}
