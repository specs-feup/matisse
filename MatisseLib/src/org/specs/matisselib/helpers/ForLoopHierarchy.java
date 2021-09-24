package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;

import pt.up.fe.specs.util.SpecsCollections;

public final class ForLoopHierarchy {
    public static final class BlockData {
        private final int blockId;
        private final List<Integer> nesting;

        BlockData(int blockId, List<Integer> nesting) {
            assert blockId >= 0;
            assert nesting != null;

            this.blockId = blockId;
            this.nesting = nesting;
        }

        public int getBlockId() {
            return blockId;
        }

        public List<Integer> getNesting() {
            return Collections.unmodifiableList(nesting);
        }

        @Override
        public String toString() {
            return "{ #" + this.blockId + " @ " + this.nesting + "}";
        }
    }

    private List<BlockData> forLoops = new ArrayList<>();

    public List<BlockData> getForLoops() {
        return Collections.unmodifiableList(forLoops);
    }

    public static ForLoopHierarchy identifyLoops(FunctionBody body) {
        return identifyLoops(body, 0);
    }

    public static ForLoopHierarchy identifyLoops(FunctionBody body, int outerLoopId) {
        // Identify nested for loops.
        ForLoopHierarchy hierarchy = new ForLoopHierarchy();

        Queue<BlockData> pendingBlocks = new LinkedList<>();
        pendingBlocks.add(new BlockData(outerLoopId, Collections.emptyList()));

        while (!pendingBlocks.isEmpty()) {
            BlockData blockData = pendingBlocks.poll();
            SsaBlock block = body.getBlock(blockData.blockId);

            block.getEndingInstruction().ifPresent(end -> {
                if (end instanceof ForInstruction) {
                    ForInstruction xfor = (ForInstruction) end;

                    List<Integer> nesting = new ArrayList<>(blockData.nesting);
                    nesting.add(blockData.blockId);
                    pendingBlocks.add(new BlockData(xfor.getLoopBlock(), nesting));
                    pendingBlocks.add(new BlockData(xfor.getEndBlock(), blockData.nesting));

                    // We add inner loops before outer loops
                    hierarchy.forLoops.add(blockData);
                } else {
                    int endBlock = end.tryGetEndBlock().orElse(-1);
                    if (endBlock >= 0) {
                        pendingBlocks.add(new BlockData(endBlock, blockData.nesting));
                    }
                    for (int ownedBlock : end.getOwnedBlocks()) {
                        if (ownedBlock != endBlock) {
                            pendingBlocks.add(new BlockData(ownedBlock, Collections.emptyList()));
                        }
                    }
                }
            });
        }

        return hierarchy;
    }

    public boolean anyParentIn(int blockId, Set<Integer> blocksSoFar) {
        for (BlockData blockData : forLoops) {
            if (blockData.blockId == blockId) {
                for (int ascendent : blockData.nesting) {
                    if (blocksSoFar.contains(ascendent)) {
                        return true;
                    }
                }
                return false;
            }
        }
        throw new RuntimeException("Could not find block " + blockId + " in list.");
    }

    public List<Integer> getDirectChildLoops(int blockId) {
        List<Integer> innerLoops = new ArrayList<>();

        for (BlockData blockData : forLoops) {
            assert blockData != null;
            assert blockData.nesting != null;

            if (blockData.nesting.isEmpty()) {
                continue;
            }

            if (SpecsCollections.last(blockData.nesting) == blockId) {
                innerLoops.add(blockData.blockId);
            }
        }

        return innerLoops;
    }

    public Optional<BlockData> getBlockData(int blockId) {
        return forLoops.stream()
                .filter(data -> data.getBlockId() == blockId)
                .findFirst();
    }

    @Override
    public String toString() {
        return "[ForLoopHierarchy " + forLoops + "]";
    }
}
