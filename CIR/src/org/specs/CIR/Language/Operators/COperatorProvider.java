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

package org.specs.CIR.Language.Operators;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;

/**
 * @author Joao Bispo
 * 
 *         TODO: This class seems to be redundant, compared with COperator <br>
 *         TODO: Can we remove inverteArguments?
 * 
 */
public class COperatorProvider implements InstanceProvider {

    private final COperator op;
    // private final boolean propagateConstants;
    private final boolean invertArguments;

    public COperatorProvider(COperator op) {
	this(op, false);
    }

    public COperatorProvider(COperator op, boolean invertArguments) {
	this.op = op;
	this.invertArguments = invertArguments;
    }

    public COperatorProvider(COperator op, boolean propagateConstants, boolean invertArguments) {
	this(op, invertArguments);
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
	if (!op.checkRule(data.getInputTypes())) {
	    return Optional.empty();
	}

	return Optional.of(this);
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	// Create implementation
	COperatorData opData = new COperatorData(op, invertArguments);
	return new COperatorBuilder(data, opData).create();
    }

    @Override
    public FunctionType getType(ProviderData data) {
	COperatorData opData = new COperatorData(op, invertArguments);
	return new COperatorBuilder(data, opData).getFunctionType();
    }

}
