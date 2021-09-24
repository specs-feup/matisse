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

package org.specs.matisselib.services.widescope;

import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.services.WideScopeService;

import com.google.common.base.Preconditions;

public class MockWideScopeService implements WideScopeService {

    private final FileNode file;

    public MockWideScopeService(FileNode file) {
	Preconditions.checkArgument(file != null);

	this.file = file;
    }

    @Override
    public Optional<FunctionIdentification> getUserFunction(FunctionIdentification context, String name) {
	if (file.getFunctionWithName(name).isPresent()) {
	    return Optional.of(new FunctionIdentification(context.getFile(), name));
	}

	return Optional.empty();
    }

    @Override
    public FunctionIdentification getCurrentContext() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionIdentification) {
	return file.getFunctionWithName(functionIdentification.getName()).map(f -> f);
    }

    @Override
    public WideScopeService withFunctionIdentification(FunctionIdentification functionIdentification) {
	return new ContextualWideScopeService(this, functionIdentification);
    }

}
