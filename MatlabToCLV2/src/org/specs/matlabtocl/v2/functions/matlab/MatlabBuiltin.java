package org.specs.matlabtocl.v2.functions.matlab;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.specs.matlabtocl.v2.functions.matlab.builtin.SizeProvider;

public enum MatlabBuiltin implements MatlabFunctionProviderEnum {
    SIZE("size") {

	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(new SizeProvider());

	    return providers;
	}
    };

    private final String functionName;

    private MatlabBuiltin(String functionName) {
	this.functionName = functionName;
    }

    @Override
    public String getName() {
	return this.functionName;
    }
}
