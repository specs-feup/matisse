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

package org.specs.matisselib.servicesv2;

import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.passmanager.PreTypeContext;
import org.specs.matisselib.passmanager.data.PassManagerData;
import org.specs.matisselib.services.UserFileProviderService;
import org.suikasoft.jOptions.Datakey.DataKey;

import com.google.common.base.Preconditions;

public class ScopeService {

    private final String filename;
    private final PassManagerData managerData;
    private final DataKey<PreTypeContext> contextKey;

    /**
     * Needs a PassManagerData so that it can retrieve updated units.
     * 
     * @param filename
     * @param managerData
     * @param contextKey
     */
    public ScopeService(String filename, PassManagerData managerData, DataKey<PreTypeContext> contextKey) {
	this.filename = filename;
	this.managerData = managerData;
	this.contextKey = contextKey;
    }

    public Optional<FunctionIdentification> getUserFunction(String name) {
	Preconditions.checkArgument(name != null);

	UserFileProviderService projectFiles = managerData.get(MatisseInit.MFILES_SERVICE);
	// Check if there is a function with the given name in the current file
	Optional<FileNode> fileNode = projectFiles.getFileNode(filename);
	if (fileNode.isPresent() && fileNode.get().getUnitsMap().containsKey(name)) {
	    return Optional.of(new FunctionIdentification(filename, name));
	}

	// Look for a main function within the a file with the same name
	fileNode = projectFiles.getFileNode(name + ".m");
	if (fileNode.isPresent()) {
	    return Optional.of(new FunctionIdentification(name + ".m", name));
	}

	// Nothing was found
	return Optional.empty();
    }

    /**
     * Gets an updated MatlabNode that contains the function definition.
     * 
     * @param functionIdentification
     *            The identification of the function
     * @return The function node, or empty if none was found
     */
    public MatlabUnitNode getUnit(FunctionIdentification functionIdentification) {
	return new PassManager(contextKey, "scope_service_manager") {

	}.getAndUpdateUnit(functionIdentification, managerData);
    }
}
