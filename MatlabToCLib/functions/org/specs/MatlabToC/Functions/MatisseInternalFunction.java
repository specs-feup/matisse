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

package org.specs.MatlabToC.Functions;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.CustomFunctions.DeleteSingleIndex;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseIsDefinedProvider;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseIsOptimizationEnabled;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseRequireStaticMatrix;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseSignature;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseTrySpecializeScalar;
import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseUnknownScalar;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

public enum MatisseInternalFunction implements MatlabFunctionProviderEnum {
    /**
     * <p>
     * y = MATISSE_unknown_scalar(x);
     * </p>
     * <p>
     * x must be a ScalarType.
     * </p>
     * 
     * Returns the value of x, except that the value of the constant is forgotten by MATISSE's type inference mechanism.
     * In other words, any optimizations that rely on knowing the value of x on compile-time can't be applied to y.
     */
    MATISSE_unknown_scalar("MATISSE_unknown_scalar") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseUnknownScalar.create());

            return providers;
        }

    },

    /**
     * <p>
     * MATISSE_require_static_matrix(x);
     * </p>
     * <p>
     * x must be a StaticMatrixType, otherwise there is a compile error.
     * </p>
     */
    MATISSE_require_static_matrix("MATISSE_require_static_matrix") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseRequireStaticMatrix.create());

            return providers;
        }
    },

    /**
     * <p>
     * y = MATISSE_try_specialize_scalar(x);
     * </p>
     * 
     * <p>
     * x must be a ScalarType.
     * </p>
     * 
     * Returns the value of x, except that if x is a constant, then it is replaced by a numeric literal in the resulting
     * assignment. This is meant to detect and debug incorrect specializations.
     */
    MATISSE_try_specialize_scalar("MATISSE_try_specialize_scalar") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseTrySpecializeScalar.create());

            return providers;
        }
    },

    MATISSE_is_defined("MATISSE_is_defined") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseIsDefinedProvider.create());

            return providers;
        }
    },

    MATISSE_is_optimization_enabled("MATISSE_is_optimization_enabled") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseIsOptimizationEnabled.getProvider());

            return providers;
        }
    },

    MATISSE_signature("MATISSE_signature") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(MatisseSignature.getProvider());

            return providers;
        }
    },

    MATISSE_delete("MATISSE_delete") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(DeleteSingleIndex.newDeleteSingleIndexMatrix());
            providers.add(DeleteSingleIndex.newDeleteSingleIndexScalar());

            return providers;
        }
    };

    private final String name;

    private MatisseInternalFunction(String name) {
        this.name = name;
    }

    @Override
    public abstract List<InstanceProvider> getProviders();

    @Override
    public String getName() {
        return this.name;
    }

}
