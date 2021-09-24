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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

/**
 * @author Joao Bispo
 * 
 */
public class GetWithColon {

    private final MatlabToCFunctionData data;

    /**
     * @param data
     */
    public GetWithColon(MatlabToCFunctionData data) {
	this.data = data;
    }

    /**
     * @param token
     * @return
     */
    public CNode newInstance(String accessCallName, List<MatlabNode> mIndexes) {
	// Get variable being accessed
	VariableType matrixType = data.getVariableType(accessCallName);
	CNode getVar = CNodeFactory.newVariable(accessCallName, matrixType);

	return new FunctionMultipleGet(data).newFunctionCall(getVar, mIndexes);
    }

}
