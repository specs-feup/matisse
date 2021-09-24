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

package org.specs.matlabtocl.v2.functions.matlab;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.functions.builtins.CLUnaryOperator;
import org.specs.matlabtocl.v2.functions.matlab.math.DivideOperation;

public enum MatlabOperator implements MatlabFunctionProviderEnum {
    PLUS("plus") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.ADDITION);

            return providers;
        }
    },
    MINUS("minus") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.SUBTRACTION);

            return providers;
        }
    },
    MTIMES("mtimes") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.MULTIPLICATION);

            return providers;
        }
    },
    TIMES("times") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.MULTIPLICATION);

            return providers;
        }
    },
    MRDIVIDE("mrdivide") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(DivideOperation.getProvider());

            return providers;
        }
    },
    MOD("mod") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            // STUB
            providers.add(CLBinaryOperator.MODULO);

            return providers;
        }
    },
    EQ("eq") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.EQUAL);

            return providers;
        }
    },
    LE("le") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.LESS_OR_EQUAL_TO);

            return providers;
        }
    },
    LT("lt") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.LESS_THAN);

            return providers;
        }
    },
    GT("gt") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.GREATER_THAN);

            return providers;
        }
    },
    GE("ge") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLBinaryOperator.GREATER_OR_EQUAL_TO);

            return providers;
        }
    },
    NOT("not") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLUnaryOperator.LOGICAL_NOT);

            return providers;
        }
    },
    UMINUS("uminus") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLUnaryOperator.UNARY_MINUS);

            return providers;
        }
    },
    UPLUS("uplus") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> providers = new ArrayList<>();

            providers.add(CLUnaryOperator.UNARY_PLUS);

            return providers;
        }
    };

    private final String functionName;

    private MatlabOperator(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public String getName() {
        return this.functionName;
    }
}
