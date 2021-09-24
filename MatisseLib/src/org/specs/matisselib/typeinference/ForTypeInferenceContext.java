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

package org.specs.matisselib.typeinference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;

public class ForTypeInferenceContext extends ChildTypeInferenceContext {

    private final LoopInformationSink loopSink;
    private final Map<String, VariableType> variableTypes = new HashMap<>();
    private boolean reachedEnd;
    private final String start, interval;

    private boolean endReachable;

    public ForTypeInferenceContext(
	    TypeInferenceContext parentContext,
	    String start, String interval,
	    LoopInformationSink loopSink,
	    Map<String, VariableType> importedVariables) {

	super(parentContext);

	this.start = start;
	this.interval = interval;

	this.loopSink = loopSink;
	this.variableTypes.putAll(importedVariables);

	this.endReachable = true;
    }

    @Override
    public void addVariable(String variableName, VariableType variableType) {
	this.variableTypes.put(variableName, variableType);
    }

    @Override
    public Optional<VariableType> getVariableType(String variableName) {
	VariableType type = this.variableTypes.get(variableName);
	if (type != null) {
	    return Optional.of(type);
	}
	return super.getVariableType(variableName);
    }

    @Override
    public boolean isInterrupted() {
	return !this.endReachable;
    }

    @Override
    public void doBreak() {
	this.loopSink.doBreak(this.variableTypes);
    }

    @Override
    public void doContinue(int blockId) {
	this.loopSink.doContinue(blockId, this.variableTypes, false);
    }

    @Override
    public void markUnreachable() {
	this.endReachable = false;
    }

    @Override
    public void reachEndOfBlock(int blockId) {
	if (!this.reachedEnd) {
	    this.reachedEnd = true;
	    doContinue(blockId);
	}
    }

    @Override
    public int getSourceBlock() {
	return getParentContext().getSourceBlock();
    }

    @Override
    public Optional<String> getForLoopStartName() {
	return Optional.of(start);
    }

    @Override
    public Optional<String> getForLoopIntervalName() {
	return Optional.of(interval);
    }
}
