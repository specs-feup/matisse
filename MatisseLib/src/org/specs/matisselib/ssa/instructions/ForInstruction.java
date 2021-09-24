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

package org.specs.matisselib.ssa.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.loopproperties.LoopProperty;
import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class ForInstruction extends SsaInstruction {

    private String start;
    private String interval;
    private String end;
    private int loopBlock;
    private int endBlock;
    private List<LoopProperty> loopProperties;

    public ForInstruction(String start, String interval, String end, int loopBlock, int endBlock) {
        this(start, interval, end, loopBlock, endBlock, Collections.emptyList());
    }

    public ForInstruction(String start, String interval, String end, int loopBlock, int endBlock,
            List<LoopProperty> loopProperties) {
        Preconditions.checkArgument(start != null);
        Preconditions.checkArgument(interval != null);
        Preconditions.checkArgument(end != null);
        Preconditions.checkArgument(loopBlock >= 0);
        Preconditions.checkArgument(endBlock >= 0);

        this.start = start;
        this.interval = interval;
        this.end = end;
        this.loopBlock = loopBlock;
        this.endBlock = endBlock;
        this.loopProperties = new ArrayList<>(loopProperties);
    }

    @Override
    public ForInstruction copy() {
        return new ForInstruction(this.start, this.interval, this.end, this.loopBlock, this.endBlock,
                this.loopProperties);
    }

    @Override
    public boolean isEndingInstruction() {
        return true;
    }

    public int getLoopBlock() {
        return this.loopBlock;
    }

    public int getEndBlock() {
        return this.endBlock;
    }

    @Override
    public Optional<Integer> tryGetEndBlock() {
        return Optional.of(getEndBlock());
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStart() {
        return this.start;
    }

    public String getInterval() {
        return this.interval;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEnd() {
        return this.end;
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(this.start, this.interval, this.end);
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.CONTROL_FLOW;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (LoopProperty property : loopProperties) {
            builder.append(property);
            builder.append(" ");
        }
        builder.append("for ");
        builder.append(this.start);
        builder.append(", ");
        builder.append(this.interval);
        builder.append(", ");
        builder.append(this.end);
        builder.append(", #");
        builder.append(this.loopBlock);
        builder.append(", #");
        builder.append(this.endBlock);
        return builder.toString();
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
        this.loopBlock = tryRenameBlock(oldNames, newNames, this.loopBlock);
        this.endBlock = tryRenameBlock(oldNames, newNames, this.endBlock);
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        this.start = newNames.getOrDefault(this.start, this.start);
        this.interval = newNames.getOrDefault(this.interval, this.interval);
        this.end = newNames.getOrDefault(this.end, this.end);
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
        if (blockIds.contains(this.loopBlock) || blockIds.contains(this.endBlock)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public List<Integer> getTargetBlocks() {
        return Arrays.asList(this.loopBlock, this.endBlock);
    }

    @Override
    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
        if (originalBlock != startBlock) {
            renameBlocks(Arrays.asList(originalBlock), Arrays.asList(startBlock));
        }
    }

    @Override
    public List<Integer> getOwnedBlocks() {
        return getTargetBlocks();
    }

    public List<LoopProperty> getLoopProperties() {
        return Collections.unmodifiableList(loopProperties);
    }

    public void replaceProperties(List<LoopProperty> newProperties) {
        this.loopProperties = new ArrayList<>(newProperties);
    }

    public boolean hasProperty(Class<? extends LoopProperty> propertyClass) {
        return loopProperties.stream()
                .anyMatch(propertyClass::isInstance);
    }

    public void addLoopProperty(LoopProperty property) {
        loopProperties.add(property);
    }
}
