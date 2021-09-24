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

package org.specs.MatlabToC.Functions.Probes;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;

public class ProbeProvider implements InstanceProvider {

    interface Probe {
	void probe(ProviderData data, List<CNode> arguments);
    }

    private final Probe probe;

    public ProbeProvider(Probe probeData) {
	this.probe = probeData;
    }

    /**
     * Executes the ProbeData interface. Always returns a InlinedInstance that does nothing.
     */
    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	// Execute probe
	// probe.probe(data);

	// Create "empty" function instance
	FunctionType type = getType(data);
	String name = "probe_" + FunctionInstanceUtils.getTypesSuffix(data.getInputTypes());
	InlineCode inlinedCode = args -> {
	    // Execute probe
	    probe.probe(data, args);
	    return "";
	};

	InlinedInstance instance = new InlinedInstance(type, name, inlinedCode);

	return instance;
    }

    @Override
    public FunctionType getType(ProviderData data) {
	return FunctionTypeBuilder.newInline()
		.addInputs(data.getInputTypes())
		.returningVoid()
		.withSideEffects()
		.build();
    }
}
