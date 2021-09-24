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

package org.specs.MatlabProcessor.Utils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Stores FileNodes by their main unit name.
 * 
 * @author JoaoBispo
 *
 */
public class FileNodeMap {

    /**
     * Maps a M-file name to the functions inside the M-file.
     */
    private final Map<String, FileNode> fileNodes;

    /**
     * @param mfileFunctions
     * @param userFunctions
     * @param chachedPrototypes
     */
    public FileNodeMap() {
	fileNodes = SpecsFactory.newHashMap();
    }

    /**
     * Considering the added M-files, returns the MatlabToken corresponding to the given 'functionName' inside the
     * M-file 'mfileName'.
     * 
     * <p>
     * If 'mfilename' is null, it only considers main functions (i.e., excludes sub-functions). If 'mfilename' is not
     * null, first tries to find using the mfileName, but if not token is found, then tries with 'mfilename' as null.
     * 
     * <p>
     * If no function is found, returns null.
     * 
     * @param mfileName
     * @param functionName
     * @return
     */
    public Optional<MatlabUnitNode> getMatlabUnit(String mfileName, String functionName) {

	// Try using mfileName as a key
	if (mfileName != null) {
	    // Get a MFilePrototypes from the table with the M-files
	    FileNode fileNode = fileNodes.get(mfileName);

	    // Get function inside the given M-file
	    if (fileNode != null) {

		Optional<MatlabUnitNode> function = fileNode.getUnit(functionName);
		if (function.isPresent()) {
		    return function;
		}
		/*
		MatlabUnitNode function = fileNode.getUnit(functionName);
		if (function != null) {
		    // This commented code was an experiment, where the tree was not cached but instead always
		    // regenerated
		    // Some quick runs indicated a slow down of more than 2x for monte_carlo example
		    // FileNode copy = new MatlabParser()
		    // .parse(SimpleFile.newInstance(fileNode.getFilename(), fileNode.getCode()));
		    // return copy.getUnit(functionName);
		    return Optional.of(function);
		}
		*/
	    }

	}

	// Try using functionName as a key
	FileNode fileNode = fileNodes.get(functionName);
	if (fileNode == null) {
	    return Optional.empty();
	}

	// Did not find function as main unit, try as sub-unit

	// This commented code was an experiment, where the tree was not cached but instead always regenerated
	// Some quick runs indicated a slow down of more than 2x for monte_carlo example
	// FileNode copy = new MatlabParser()
	// .parse(SimpleFile.newInstance(fileNode.getFilename(), fileNode.getCode()));
	// return copy.getUnit(functionName);
	return fileNode.getUnit(functionName);
    }

    /**
     * Adds a MATLAB file. Returns the name of the main function.
     * 
     * @param matlabFile
     * @return the name of the matlab function, which is used as key
     */
    public String addMatlabFile(File matlabFile) {
	return addMatlabFile(new MatlabParser().parse(matlabFile));
    }

    public String addMatlabFile(ResourceProvider resource) {
	return addMatlabFile(new MatlabParser().parse(resource));
    }

    public String addMatlabFile(SimpleFile matlabFile) {

	FileNode fileNode = new MatlabParser().parse(matlabFile);

	return addMatlabFile(fileNode);
    }

    private String addMatlabFile(FileNode fileNode) {
	String mainFunctionName = fileNode.getMainUnitName();

	// Add file
	fileNodes.put(mainFunctionName, fileNode);

	return mainFunctionName;
    }

    /**
     * 
     * @return the names of the main MATLAB units currently in the map
     */
    public Collection<String> getMainUnitNames() {
	return fileNodes.keySet();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	List<String> filenames = SpecsFactory.newArrayList(fileNodes.keySet());
	Collections.sort(filenames);

	for (String filename : filenames) {
	    FileNode fileNode = fileNodes.get(filename);

	    builder.append("File '" + filename + "'\n");
	    builder.append(fileNode);
	}

	return builder.toString();
    }

    /**
     * @param mfileName
     * @param functionName
     * @return
     */
    public boolean isMainFunction(String mfileName, String functionName) {

	// Try using mfileName as a key
	if (mfileName != null) {
	    // Get a MFilePrototypes from the table with the M-files
	    FileNode fileNode = fileNodes.get(mfileName);

	    // Check if function name is the same as the main function name
	    if (fileNode.getMainUnitName().equals(functionName)) {
		return true;
	    }

	    // Check if function name equals any of the subfunction names
	    if (fileNode.hasUnit(functionName)) {
		return false;
	    }
	}

	// If function could not be found inside M-file, check if it is a main function
	FileNode fileNode = fileNodes.get(functionName);

	if (fileNode == null) {
	    throw new RuntimeException(
		    "Could not determine if main or sub function. Did you gave a subfunction from another M-file?");
	}

	return true;
    }

}
