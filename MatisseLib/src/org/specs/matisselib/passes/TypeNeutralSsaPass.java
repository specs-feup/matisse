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

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

/**
 * A pass that can be applied both before and after the type inference.
 * 
 * @author Luís Reis
 *
 */
public abstract class TypeNeutralSsaPass implements SsaPass, PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
	Preconditions.checkArgument(instance != null);
	Preconditions.checkArgument(passData != null);

	apply(instance.getFunctionBody(), passData);
    }

    @Override
    public abstract void apply(FunctionBody source, DataStore data);

    @Override
    public String getName() {
	return getClass().getSimpleName();
    }

    @Override
    public DataView getParameters() {
	return DataView.empty();
    }
}
