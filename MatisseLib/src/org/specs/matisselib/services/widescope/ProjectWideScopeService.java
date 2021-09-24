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
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.services.WideScopeService;

import com.google.common.base.Preconditions;

public class ProjectWideScopeService implements WideScopeService {
    private final MatlabAstPassManager manager;

    public ProjectWideScopeService(MatlabAstPassManager manager) {
	Preconditions.checkArgument(manager != null);

	this.manager = manager;
    }

    @Override
    public Optional<FunctionIdentification> getUserFunction(FunctionIdentification context, String name) {
	Preconditions.checkArgument(context != null);
	Preconditions.checkArgument(name != null);

	// Try to get function in same file
	Optional<FunctionIdentification> localId = manager.getFunctionsIn(context.getFile()).flatMap(functions -> {
	    for (FunctionIdentification localFunction : functions) {
		if (localFunction.getName().equals(name)) {
		    return Optional.of(localFunction);
		}
	    }
	    return Optional.empty();
	});
	if (localId.isPresent()) {
	    return localId;
	}

	// Look for a function in other files
	FunctionIdentification globalId = new FunctionIdentification(name + ".m", name);
	if (manager.hasFunctionNode(globalId)) {
	    return Optional.of(globalId);
	}

	// Nothing was found
	return Optional.empty();
    }

    @Override
    public FunctionIdentification getCurrentContext() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionIdentification) {
	Preconditions.checkArgument(functionIdentification != null);

	return manager.getFunctionNode(functionIdentification);
    }

    /*
    @Override
    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionId) {
    Preconditions.checkArgument(functionId != null);

    Optional<MatlabNode> underlyingNode = getUnderlyingFunctionNode(functionId);
    FunctionIdentification canonicalFunctionId = canonicalIds.getOrDefault(functionId, functionId);

    underlyingNode.ifPresent(node -> {
        assert currentPass.containsKey(canonicalFunctionId);

        int nextPassToApply = currentPass.get(canonicalFunctionId);
        while (nextPassToApply < currentPreTypeRecipePass) {
    	// Apply pass

    	applyPass(canonicalFunctionId, nextPassToApply++);
        }

        currentPass.put(canonicalFunctionId, nextPassToApply);
    });

    return underlyingNode;
    }
    */

    @Override
    public WideScopeService withFunctionIdentification(FunctionIdentification functionIdentification) {
	Preconditions.checkArgument(functionIdentification != null);

	return new ContextualWideScopeService(this, functionIdentification);
    }

    /*
    private Optional<MatlabNode> getUnderlyingFunctionNode(FunctionIdentification functionId) {
    assert functionId != null;

    if (parsedFunctions.containsKey(functionId)) {
        assert parsedIds.contains(functionId);

        return Optional.of(parsedFunctions.get(functionId));
    }

    Optional<FileNode> potentialFileNode = getFileNode(functionId.getFile());
    return potentialFileNode.flatMap(fileNode -> {
        if (functionId.getFile().equals(functionId.getName() + ".m")) {
    	// Calling function foo() of foo.m should return the main function,
    	// even if it is not named "foo".

    	FunctionNode function = fileNode.getMainFunction();
    	FunctionIdentification newIdentification = new FunctionIdentification(functionId.getFile(), function
    		.getFunctionName());

    	if (!newIdentification.equals(functionId)) {
    	    canonicalIds.put(functionId, newIdentification);
    	    return getUnderlyingFunctionNode(newIdentification);
    	}
        }

        Optional<MatlabNode> potentialFunction = fileNode.getFunctions().stream()
    	    .filter(f -> f.getFunctionName().equals(functionId.getName()))
    	    .map(f -> (MatlabNode) f)
    	    .findFirst();

        potentialFunction.ifPresent(function -> {
    	parsedIds.add(functionId);
    	parsedFunctions.put(functionId, function);
    	currentPass.put(functionId, 0);
        });

        return potentialFunction;
    });
    }
    */

}
