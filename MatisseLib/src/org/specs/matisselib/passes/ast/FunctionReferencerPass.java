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

package org.specs.matisselib.passes.ast;

import java.util.Arrays;
import java.util.Collection;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.services.WideScopeService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Forces a reference to a function without modifying any node.
 * <p>
 * Used for testing purposes.
 */
public class FunctionReferencerPass extends AMatlabNodePass {

    private final FunctionIdentification referencedFunction;

    public FunctionReferencerPass(FunctionIdentification referencedFunction) {
	this(referencedFunction, PreTypeInferenceServices.WIDE_SCOPE);
    }

    private FunctionReferencerPass(FunctionIdentification referencedFunction, DataKey<WideScopeService> scopeKey) {
	Preconditions.checkArgument(referencedFunction != null);
	this.referencedFunction = referencedFunction;
    }

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
	WideScopeService service = data.get(PreTypeInferenceServices.WIDE_SCOPE);

	// Force the reference, but do nothing with it
	service.getFunctionNode(referencedFunction).get();

	return rootNode;
    }

    @Override
    public String getName() {
	return "FunctionReferencerPass";
    }

    @Override
    public Collection<DataKey<?>> getReadKeys() {
	return Arrays.asList(PreTypeInferenceServices.WIDE_SCOPE);
    }
}
