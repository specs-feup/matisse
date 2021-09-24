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

package org.specs.matisselib.passmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatlabRecipe;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.collections.AccumulatorMap;

public class PreTypeContext {

    private final MatlabRecipe recipe;

    private final List<FunctionIdentification> parsedIds;
    // The pass each function id is currently in
    // TODO: Rename to completedPasses
    private final AccumulatorMap<FunctionIdentification> currentPasses;
    private final Map<FunctionIdentification, MatlabUnitNode> parsedUnits;

    // The main pass we are currently in
    private int numAppliedPasses;

    public PreTypeContext(MatlabRecipe recipe) {
	this.recipe = recipe;

	parsedIds = new ArrayList<>();
	currentPasses = new AccumulatorMap<>();
	parsedUnits = new HashMap<>();

	numAppliedPasses = 0;
    }

    public boolean hasUnit(FunctionIdentification functionId) {
	return parsedUnits.containsKey(functionId);
    }

    public MatlabUnitNode getUnit(FunctionIdentification functionId) {
	MatlabUnitNode functionNode = parsedUnits.get(functionId);

	if (functionNode == null) {
	    throw new RuntimeException("Could not find a function node for the id '" + functionId + "'");
	}

	assert parsedIds.contains(functionId);
	return functionNode;
    }

    public void addUnit(FunctionIdentification functionId, MatlabUnitNode unit) {
	Preconditions.checkArgument(!hasUnit(functionId), "Already added a unit of id '" + functionId + "'");

	parsedIds.add(functionId);
	parsedUnits.put(functionId, unit);
    }

    /**
     * The number of completely applied passes by the manager.
     * 
     * @return
     */
    public int getNumAppliedPasses() {
	return numAppliedPasses;
    }

    /**
     * Increments the master number of applied passes.
     */
    public void incrementAppliedPasses() {
	numAppliedPasses++;
    }

    /**
     * Increments the number of applied passes on the given function id.
     * 
     * @param id
     */
    int incrementAppliedPassed(FunctionIdentification id) {
	return currentPasses.add(id);
    }

    public MatlabRecipe getRecipe() {
	return recipe;
    }

    /**
     * @return the number of units currently being processed
     */
    public int getNumIds() {
	int numIds = parsedIds.size();
	assert numIds == parsedUnits.size();

	return numIds;
    }

    /**
     * A list with the ids of the functions being currently processed.
     * 
     * @return
     */
    public List<FunctionIdentification> getCurrentIds() {
	return Collections.unmodifiableList(parsedIds);
    }

    /**
     * 
     * @param id
     * @return the number of completed passes for the given function
     */
    public int getCompletedPasses(FunctionIdentification id) {
	return currentPasses.getCount(id);
    }

}
