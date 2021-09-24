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

package org.specs.MatlabToC.Functions.Probes;

import java.util.StringJoiner;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Tree.CNode;
import org.specs.MatlabToC.Functions.Probes.ProbeProvider.Probe;
import org.specs.MatlabToC.Reporting.CommonInfoType;

/**
 * @author Joao Bispo
 * 
 */
public class ProbeProviders {

    public static InstanceProvider newProbeType() {

	Probe probe = (providerData, args) -> {

	    // Build string
	    StringJoiner joiner = new StringJoiner(", ", "Type is ", "");

	    for (CNode arg : args) {
		joiner.add(arg.getCode() + " -> " + arg.getVariableType());
	    }

	    // Show string
	    providerData.getReportService().emitMessage(CommonInfoType.PROBE, joiner.toString());
	};

	return new ProbeProvider(probe);
    }
}
