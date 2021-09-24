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

package org.specs.matisselib.passes.posttype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * After type inference, we know which output variables ($ret) are used and which aren't. So we can rename those outputs
 * to something else (e.g. A$ret to A$3) so dead code elimination gets rid of it.
 * 
 * @author Luís Reis
 *
 */
public class RedundantOutputEliminationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
	List<String> outputs = instance.getFunctionType().getOutputNames();

	Map<String, String> outputsToRename = new HashMap<>();
	for (SsaInstruction instruction : instance.getFunctionBody().getFlattenedInstructionsIterable()) {
	    for (String output : instruction.getOutputs()) {
		if (output.endsWith("$ret")) {
		    String baseName = output.substring(0, output.length() - "$ret".length());

		    if (!outputs.contains(baseName)) {
			String newName = instance.makeTemporary(baseName, instance.getVariableType(output));

			outputsToRename.put(output, newName);
		    }
		}
	    }
	}

	instance.renameVariables(outputsToRename);
    }

}
