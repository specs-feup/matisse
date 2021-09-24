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
import org.specs.MatlabToC.Functions.Probes.ProbeProviders;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Contains functions that provide basic functionality, but that are not functions that we find in MATLAB.
 * 
 * @author Joao Bispo
 * 
 */
public enum Probe implements MatlabFunctionProviderEnum {

    /**
     * Prints the type of the given variable.
     * 
     * <p>
     * MATISSE_probe(VAR...), where VAR... is any number of variables.
     */
    PROBE_TYPE("MATISSE_probe") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Builder for single variable
	    builders.add(ProbeProviders.newProbeType());

	    return builders;
	}

    };

    private final String name;

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    /**
     * Constructor.
     * 
     * @param matlabFunctionName
     */
    // private MatissePrimitive(String matlabFunctionName) {
    private Probe(String name) {
	this.name = name;
    }

    @Override
    public String getName() {
	return name;
    }

}
