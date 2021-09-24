/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.SystemInfo;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabProcessor.Utils.FileNodeMap;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * The M-files used in the project.
 * 
 * @author Joao Bispo
 * 
 */
public class ProjectMFiles {

    private final FileNodeMap mFileUser;

    /**
     * 
     */
    public ProjectMFiles() {
	mFileUser = new FileNodeMap();
    }

    /**
     * @return the mFileUser
     */
    public Map<String, FunctionNode> getMainUserFunctions() {
	Map<String, FunctionNode> mainUserFunctions = SpecsFactory.newHashMap();

	for (String mfilename : mFileUser.getMainUnitNames()) {

	    Optional<FunctionNode> mainFunction = getUserFunction(mfilename, mfilename);

	    if (!mainFunction.isPresent()) {
		throw new RuntimeException("Could not find main function in file '" + mfilename + "'");
	    }

	    mainUserFunctions.put(mfilename, mainFunction.get());
	}

	return mainUserFunctions;
    }

    /**
     * 
     * @param matlabFile
     * @return the name of the MATLAB function, which is used as key
     */
    public String addUserFile(File matlabFile) {
	// System.out.println("USER FILE (file):" + matlabFile.getName());

	// return mFileUser.addMatlabFile(matlabFile.getName(), IoUtils.read(matlabFile));
	return mFileUser.addMatlabFile(matlabFile);
    }

    public String addUserFile(ResourceProvider resource) {
	return mFileUser.addMatlabFile(resource);
    }

    public String addUserFile(SimpleFile matlabFile) {
	// System.out.println("USER FILE (simple):" + matlabFile.getFilename());
	return mFileUser.addMatlabFile(matlabFile);
    }

    /**
     * @param functionName
     * @return
     */
    public Optional<FunctionNode> getUserFunction(String functionName) {
	return getUserFunction(null, functionName);
    }

    public Optional<FunctionNode> getUserFunction(String mfilename, String functionName) {
	Optional<MatlabUnitNode> unit = mFileUser.getMatlabUnit(mfilename, functionName);

	if (!unit.isPresent()) {
	    return Optional.empty();
	}

	if (!(unit.get() instanceof FunctionNode)) {
	    // System.out.println("MFILEs:" + mFileUser.getMainUnitNames());
	    // System.out.println("mfile:" + mfilename + "; function:" + functionName);

	    throw new RuntimeException("Matlab file '" + mfilename + "' does not contain a Function but a '"
		    + unit.get().getNodeName() + "'");
	}

	return Optional.of((FunctionNode) unit.get());
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append("User functions:\n");
	builder.append(mFileUser);

	return builder.toString();
    }

    /**
     * @return the mFileUser
     */
    public FileNodeMap getUserFiles() {
	return mFileUser;
    }

}
