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

package org.specs.matisselib.passes;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public abstract class TypeTransparentSsaPass implements PostTypeInferencePass {
    @Override
    public void apply(TypedInstance instance, DataStore passData) {
	apply(instance.getFunctionBody(),
		instance.getProviderData(),
		instance::getVariableType,
		instance::makeTemporary,
		passData);

    }

    /**
     * This function is public so that unit tests can call it directly.
     */
    public abstract void apply(FunctionBody body,
	    ProviderData providerData,
	    Function<String, Optional<VariableType>> typeGetter,
	    BiFunction<String, VariableType, String> makeTemporary,
	    DataStore passData);
}
