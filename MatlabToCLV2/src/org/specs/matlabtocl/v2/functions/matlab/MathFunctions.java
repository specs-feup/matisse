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
import org.specs.matlabtocl.v2.functions.builtins.CLBuiltinMathFunction;
import org.specs.matlabtocl.v2.functions.matlab.math.CastIntegerToIntegerFunction;
import org.specs.matlabtocl.v2.functions.matlab.math.CastToFloatingPointFunction;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public enum MathFunctions implements MatlabFunctionProviderEnum {
    UINT8("uint8") {

	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    // STUB
	    providers.add(new CastIntegerToIntegerFunction(CLNativeType.UCHAR));

	    return providers;
	}
    },
    UINT32("uint32") {

	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    // STUB
	    providers.add(new CastIntegerToIntegerFunction(CLNativeType.ULONG));

	    return providers;
	}
    },
    UINT64("uint64") {

	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    // STUB
	    providers.add(new CastIntegerToIntegerFunction(CLNativeType.ULONG));

	    return providers;
	}
    },
    SINGLE("single") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    // STUB
	    providers.add(new CastToFloatingPointFunction(CLNativeType.FLOAT));

	    return providers;
	}
    },
    LOG("log") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(CLBuiltinMathFunction.LOG);

	    return providers;
	}
    },
    EXP("exp") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(CLBuiltinMathFunction.EXP);

	    return providers;
	}
    },
    SIN("sin") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(CLBuiltinMathFunction.SIN);

	    return providers;
	}
    },
    COS("cos") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(CLBuiltinMathFunction.COS);

	    return providers;
	}
    },
    SQRT("sqrt") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(CLBuiltinMathFunction.SQRT);

	    return providers;
	}
    };

    private final String functionName;

    private MathFunctions(String functionName) {
	this.functionName = functionName;
    }

    @Override
    public String getName() {
	return this.functionName;
    }
}
