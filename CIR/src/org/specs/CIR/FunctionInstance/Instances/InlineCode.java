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

package org.specs.CIR.FunctionInstance.Instances;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;

/**
 * Companion interface to class InlinedInstance. Specifies the code that will appear instead of the function call.
 * 
 * @author Joao Bispo
 * 
 */
public interface InlineCode {

    /**
     * The code that appears instead of the function call, when an InlineInstance function is called, , according to the
     * input arguments.
     * 
     * @param arguments
     * @return
     */
    String getInlineCode(List<CNode> arguments);

    static InlineCode newInstance(CInstructionList instructions) {
	return args -> {
	    return instructions.get().stream()
		    .map(node -> node.getCode())
		    .collect(Collectors.joining("\n"));
	};
    }

}
