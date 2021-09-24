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

package org.specs.matisselib.ssa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.specs.matisselib.functionproperties.FunctionProperty;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

public class FunctionBody {
    private final String functionName;
    private final int firstLine;
    private List<SsaBlock> blocks = new ArrayList<>();
    private final List<String> argumentNames = new ArrayList<>();
    private final List<String> outputNames = new ArrayList<>();
    private final Set<String> byRefVariables = new HashSet<>();
    private final List<FunctionProperty> properties = new ArrayList<>();

    private final Map<String, Integer> temporaries = new HashMap<>();

    public FunctionBody(String functionName, int firstLine, List<String> argumentNames, List<String> outputNames) {
        this.functionName = functionName;
        this.firstLine = firstLine;
        this.argumentNames.addAll(argumentNames);
        this.outputNames.addAll(outputNames);
    }

    public FunctionBody(String functionName, int firstLine) {
        this(functionName, firstLine, Collections.emptyList(), Collections.emptyList());
    }

    public FunctionBody() {
        this(null, -1);
    }

    public FunctionBody copy() {
        FunctionBody body = new FunctionBody(functionName, firstLine, argumentNames, outputNames);

        body.byRefVariables.addAll(byRefVariables);

        for (FunctionProperty property : properties) {
            body.addProperty(property);
        }

        for (SsaBlock block : getBlocks()) {
            body.addBlock(block.copy());
        }

        return body;
    }

    public void addBlock(int index, SsaBlock block) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index <= this.blocks.size());
        Preconditions.checkArgument(block != null);
        Preconditions.checkArgument(!this.blocks.contains(block));

        this.blocks.add(0, block);
    }

    public int addBlock(SsaBlock block) {
        Preconditions.checkArgument(block != null);
        Preconditions.checkArgument(!this.blocks.contains(block));

        this.blocks.add(block);

        return this.blocks.size() - 1;
    }

    public String getName() {
        return this.functionName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (this.functionName != null) {
            builder.append("Function ");
            builder.append(this.functionName);
            builder.append("\n");
        }

        for (FunctionProperty property : this.properties) {
            builder.append("[");
            builder.append(property);
            builder.append("]\n");
        }

        if (!byRefVariables.isEmpty()) {
            builder.append("By Ref: ");
            builder.append(byRefVariables);
            builder.append("\n");
        }

        buildBlockString(builder);

        return builder.toString();
    }

    public void buildBlockString(StringBuilder builder) {
        for (int i = 0; i < this.blocks.size(); ++i) {
            builder.append("block #");
            builder.append(i);
            builder.append(":\n  ");
            builder.append(this.blocks.get(i).toString().replace("\n", "\n  "));
            builder.append("\n");
        }
    }

    public List<SsaBlock> getBlocks() {
        return Collections.unmodifiableList(this.blocks);
    }

    public void setBlocks(List<SsaBlock> blocks) {
        Preconditions.checkArgument(blocks != null);

        this.blocks = new ArrayList<>(blocks);
    }

    public SsaBlock getBlock(int index) {
        return getBlocks().get(index);
    }

    public Iterable<SsaInstruction> getFlattenedInstructionsIterable() {
        return () -> new SsaFlatInstructionsIterator<>(blocks, SsaInstruction.class);
    }

    public <T extends SsaInstruction> Iterable<T> getFlattenedInstructionsIterable(Class<T> cls) {
        return () -> new SsaFlatInstructionsIterator<>(blocks, cls);
    }

    public List<SsaInstruction> getFlattenedInstructionsList() {
        return getBlocks().stream()
                .flatMap(block -> block.getInstructions().stream())
                .collect(Collectors.toList());
    }

    public Stream<SsaInstruction> getFlattenedInstructionsStream() {
        return getBlocks().stream()
                .flatMap(block -> block.getInstructions().stream());
    }

    public <T extends SsaInstruction> Stream<T> getFlattenedInstructionsOfTypeStream(Class<T> cls) {
        return getFlattenedInstructionsStream()
                .filter(cls::isInstance)
                .map(cls::cast);
    }

    public <T extends SsaInstruction> List<T> getFlattenedInstructionsOfType(Class<T> cls) {
        return getFlattenedInstructionsOfTypeStream(cls)
                .collect(Collectors.toList());
    }

    public String makeTemporary(String semantics) {
        Preconditions.checkArgument(semantics != null);

        int value = this.temporaries.getOrDefault(semantics, 0) + 1;
        this.temporaries.put(semantics, value);

        return "$" + semantics + "$" + value;
    }

    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
        Preconditions.checkArgument(oldNames != null);
        Preconditions.checkArgument(newNames != null);
        Preconditions.checkArgument(oldNames.size() == newNames.size());

        for (SsaBlock block : getBlocks()) {
            for (SsaInstruction instruction : block.getInstructions()) {
                instruction.renameBlocks(oldNames, newNames);
            }
        }
    }

    /**
     * Removes a block from the body.<br/>
     * Note: Does NOT rename the remaining blocks, so the names will become outdated.
     * 
     * @param blockId
     *            The ID of the block to remove.
     * @see renameBlocks
     */
    private void removeBlock(int blockId) {
        Preconditions.checkArgument(blockId != 0);
        Preconditions.checkArgument(blockId < this.blocks.size());

        this.blocks.remove(blockId);
    }

    public void removeBlocks(List<Integer> blockIds) {
        Preconditions.checkArgument(blockIds != null);
        Preconditions.checkArgument(blockIds.stream().distinct().count() == blockIds.size());
        Preconditions.checkArgument(blockIds.stream().allMatch(size -> size >= 0 && size < this.blocks.size()));

        int deletedSoFar = 0;

        for (int i = 0; i < this.blocks.size();) {
            if (blockIds.contains(i + deletedSoFar)) {
                // Delete it
                removeBlock(i);
                deletedSoFar++;
            } else {
                ++i;
            }
        }

        for (SsaInstruction instruction : getFlattenedInstructionsIterable()) {
            instruction.removeBlocks(blockIds);
        }
    }

    public void removeBlocks(Integer... blockIds) {
        removeBlocks(Arrays.asList(blockIds));
    }

    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        for (SsaBlock block : getBlocks()) {
            block.renameVariables(newNames);
        }
    }

    public void renameBlocks(Map<Integer, Integer> newBlockNames) {
        Preconditions.checkArgument(newBlockNames != null);

        List<Integer> oldNames = new ArrayList<>();
        List<Integer> newNames = new ArrayList<>();

        for (Integer oldName : newBlockNames.keySet()) {
            oldNames.add(oldName);
            newNames.add(newBlockNames.get(oldName));
        }

        renameBlocks(oldNames, newNames);
    }

    public void removeAndRenameBlocks(Integer... deletedBlocks) {
        removeAndRenameBlocks(Arrays.asList(deletedBlocks));
    }

    public void removeAndRenameBlocks(List<Integer> deletedBlocks) {
        Preconditions.checkArgument(deletedBlocks != null);

        List<Integer> oldNames = new ArrayList<>();
        List<Integer> newNames = new ArrayList<>();
        List<Integer> phonyNames = new ArrayList<>();
        int deletedSoFar = 0;
        for (int i = 0; i < this.blocks.size(); ++i) {
            if (deletedBlocks.contains(i)) {
                ++deletedSoFar;
            } else if (deletedSoFar > 0) {
                oldNames.add(i);
                newNames.add(i - deletedSoFar);
                phonyNames.add(-i - 1);
            }
        }

        renameBlocks(oldNames, phonyNames);
        removeBlocks(deletedBlocks);
        renameBlocks(phonyNames, newNames);
    }

    public Map<String, Integer> getLastTemporaries() {
        return new HashMap<>(this.temporaries);
    }

    /**
     * Updates all body instructions to know that the block previously known as originalBlock is now more than one: The
     * start is startBlock and the end is endBlock
     * 
     * @param originalBlock
     * @param startBlock
     * @param endBlock
     */
    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
        for (SsaInstruction instruction : getFlattenedInstructionsIterable()) {
            instruction.breakBlock(originalBlock, startBlock, endBlock);
        }
    }

    public SsaInstruction getInstructionAt(InstructionLocation location) {
        Preconditions.checkArgument(location != null);

        return getBlock(location.getBlockId())
                .getInstructions()
                .get(location.getInstructionId());
    }

    public void setInstructionAt(InstructionLocation location, SsaInstruction instruction) {
        Preconditions.checkArgument(location != null);
        Preconditions.checkArgument(instruction != null);

        getBlock(location.getBlockId())
                .getInstructions()
                .set(location.getInstructionId(), instruction);
    }

    public int getLineFromBlock(SsaInstruction source, int currentBlock, int lastLine) {
        int currentLine = lastLine;
        SsaBlock block = getBlock(currentBlock);

        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction == source) {
                return currentLine;
            }

            if (instruction instanceof LineInstruction) {
                currentLine = ((LineInstruction) instruction).getLine();
            }

            for (int targetBlock : instruction.getTargetBlocks()) {
                int line = getLineFromBlock(source, targetBlock, currentLine);
                if (line != -1) {
                    return line;
                }
            }
        }

        return -1;
    }

    public int getFirstLine() {
        return this.firstLine;
    }

    public void addProperty(FunctionProperty property) {
        this.properties.add(property);
    }

    public <T extends FunctionProperty> Stream<T> getPropertyStream(Class<T> cls) {
        return this.properties.stream()
                .filter(cls::isInstance)
                .map(cls::cast);
    }

    public <T extends FunctionProperty> boolean hasProperty(Class<T> cls) {
        return this.properties.stream()
                .anyMatch(cls::isInstance);
    }

    public void removeInstructionAt(InstructionLocation instructionLocation) {
        Preconditions.checkArgument(instructionLocation != null);

        getBlock(instructionLocation.getBlockId()).removeInstructionAt(instructionLocation.getInstructionId());
    }

    public int getNumInputs() {
        return argumentNames.size();
    }

    public Optional<String> getInputName(int i) {
        Preconditions.checkPositionIndex(i, argumentNames.size());

        return Optional.ofNullable(argumentNames.get(i));
    }

    public int getNumOutputs() {
        return outputNames.size();
    }

    public Optional<String> getOutputName(int i) {
        Preconditions.checkPositionIndex(i, outputNames.size());

        return Optional.ofNullable(outputNames.get(i));
    }

    public int indexOfInputName(String variableName) {
        Preconditions.checkArgument(variableName != null);

        return argumentNames.indexOf(variableName);
    }

    public int indexOfOutputName(String variableName) {
        Preconditions.checkArgument(variableName != null);

        return outputNames.indexOf(variableName);
    }

    public boolean isByRef(String variableName) {
        Preconditions.checkArgument(variableName != null);

        return byRefVariables.contains(variableName);
    }

    public void addByRef(String variableName) {
        Preconditions.checkArgument(variableName != null);
        Preconditions.checkState(!isByRef(variableName));

        byRefVariables.add(variableName);
    }

    /**
     * Gets the index of the last %!by_ref output. If there are no %!by_ref outputs, then this function returns -1.
     */
    public int getLastByRefOutputIndex() {
        for (int i = outputNames.size() - 1; i >= 0; --i) {
            String outputName = outputNames.get(i);
            if (byRefVariables.contains(outputName)) {
                assert outputName != null;

                return i;
            }
        }

        return -1;
    }

    public List<FunctionProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
