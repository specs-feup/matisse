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

package org.specs.MatlabToC.InstanceProviders;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import com.google.common.base.Preconditions;

/**
 * Wrapper around COperator which only accepts inputs that are Scalar, instead of accepting also inputs that are
 * Scalar-convertible.
 * 
 * @author JoaoBispo
 *
 */
public class ScalarOperator implements InstanceProvider {

    private final COperator operator;

    private Checker SCALAR_CHECKER = new MatisseChecker()
	    .areScalar();

    private ScalarOperator(COperator operator) {
	this.operator = operator;
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
	if (!SCALAR_CHECKER.create(data).check()) {
	    return Optional.empty();
	}
	return operator.accepts(data);
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	return operator.newCInstance(data);
    }

    public static ScalarOperator create(COperator operator) {
	Preconditions.checkArgument(operator != null);

	return new ScalarOperator(operator);
    }
}
