/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib.ssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.specs.matisselib.loopproperties.LoopProperty;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

public final class BlockContext {
    private FunctionBody ssaFunction;

    private Map<String, String> currentVariableNames = new HashMap<>();
    private final boolean isRoot;
    private final Set<String> globals;
    private final SsaBlock currentBlock;
    private final int currentBlockId;
    public List<BlockContext> breakPoints = null;
    public List<BlockContext> continuePoints = null;
    public List<LoopProperty> loopProperties = new ArrayList<>();
    private int currentLine = -1;
    private Stack<SsaBuilderEndContext> endContexts = new Stack<>();

    public BlockContext(FunctionBody ssaFunction, BlockContext parent, SsaBlock currentBlock, int currentBlockId) {
        Preconditions.checkArgument(ssaFunction != null);

        this.ssaFunction = ssaFunction;
        this.currentBlock = currentBlock;
        this.currentBlockId = currentBlockId;
        if (parent != null) {
            this.currentLine = parent.currentLine;
            this.isRoot = false;
            this.globals = parent.globals;
        } else {
            this.isRoot = true;
            this.globals = new HashSet<>();
        }
    }

    public FunctionBody getFunction() {
        return this.ssaFunction;
    }

    public SsaBlock getBlock() {
        return this.currentBlock;
    }

    public int getBlockId() {
        return this.currentBlockId;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public boolean isGlobal(String name) {
        Preconditions.checkArgument(name != null);
        Preconditions.checkArgument(!name.startsWith("^"));

        return globals.contains(name);
    }

    public void addGlobal(String name) {
        Preconditions.checkArgument(name != null);
        Preconditions.checkArgument(!name.startsWith("^"));
        Preconditions.checkState(!isGlobal(name));

        globals.add(name);
    }

    public void addInstruction(SsaInstruction instruction) {
        this.currentBlock.addInstruction(instruction);
    }

    public void setVariableNames(Map<String, String> currentVariableNames) {
        this.currentVariableNames = new HashMap<>(currentVariableNames);
    }

    public Map<String, String> getVariableNames() {
        return Collections.unmodifiableMap(this.currentVariableNames);
    }

    public Set<String> getVariables() {
        return currentVariableNames.keySet();
    }

    public String getCurrentName(String name) {
        return this.currentVariableNames.get(name);
    }

    public boolean hasVariable(String name) {
        return this.currentVariableNames.containsKey(name);
    }

    public void setCurrentName(String var, String name) {
        this.currentVariableNames.put(var, name);
    }

    public void prependInstruction(SsaInstruction instruction) {
        this.currentBlock.prependInstruction(instruction);
    }

    public boolean doBreak() {
        if (this.breakPoints == null) {
            return false;
        }

        this.breakPoints.add(this);
        return true;
    }

    public boolean doContinue() {
        if (this.continuePoints == null) {
            return false;
        }

        this.continuePoints.add(this);
        return true;
    }

    public void setLine(int line) {
        if (line < 0) {
            return;
        }
        if (this.currentLine != line) {
            this.currentLine = line;
            addInstruction(new LineInstruction(line));
        }
    }

    public void addLoopProperty(LoopProperty loopProperty) {
        loopProperties.add(loopProperty);
    }

    public void pushEndContext(String cause, String referencedSsaVariable, int index, int numIndices) {
        endContexts.push(new SsaBuilderEndContext(cause, referencedSsaVariable, index, numIndices));
    }

    public void popEndContext() {
        endContexts.pop();
    }

    public Optional<SsaBuilderEndContext> getCurrentEndContext() {
        if (endContexts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(endContexts.peek());
    }
}
