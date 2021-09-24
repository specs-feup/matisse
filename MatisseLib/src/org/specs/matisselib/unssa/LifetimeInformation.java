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

package org.specs.matisselib.unssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public class LifetimeInformation {

    private final int rows;
    private final List<Integer> columns;

    private Map<String, Bitmap> entryLives = new HashMap<>();
    private Map<String, Bitmap> exitLives = new HashMap<>();

    private Map<Integer, Map<String, Integer>> firstLiveBlock = new HashMap<>();
    private Map<Integer, Map<String, Integer>> lastLiveBlock = new HashMap<>();

    public LifetimeInformation(int rows, List<Integer> columns) {
        this.rows = rows;
        this.columns = new ArrayList<>(columns);
    }

    public void setLiveAtEntry(String variableName, int blockId, int instructionId) {
        Preconditions.checkArgument(variableName != null);

        Bitmap bitmap = entryLives.get(variableName);
        if (bitmap == null) {
            bitmap = new Bitmap(rows, columns);
            entryLives.put(variableName, bitmap);
        }
        bitmap.setElementAt(blockId, instructionId, true);
    }

    public void setLiveAtExit(String variableName, int blockId, int instructionId) {
        Preconditions.checkArgument(variableName != null);

        Bitmap bitmap = exitLives.get(variableName);
        if (bitmap == null) {
            bitmap = new Bitmap(rows, columns);
            exitLives.put(variableName, bitmap);
        }
        bitmap.setElementAt(blockId, instructionId, true);

        if (!lastLiveBlock.containsKey(blockId)) {
            lastLiveBlock.put(blockId, new HashMap<>());
        }
        if (!firstLiveBlock.containsKey(blockId)) {
            firstLiveBlock.put(blockId, new HashMap<>());
        }

        Map<String, Integer> lastLive = lastLiveBlock.get(blockId);
        if (lastLive.getOrDefault(variableName, -1) < instructionId) {
            lastLive.put(variableName, instructionId);
        }

        Map<String, Integer> firstLive = firstLiveBlock.get(blockId);
        if (firstLive.getOrDefault(variableName, Integer.MAX_VALUE) > instructionId) {
            firstLive.put(variableName, instructionId);
        }
    }

    public boolean isLiveAtEntry(String variableName, int blockId, int instructionId) {
        Preconditions.checkArgument(variableName != null);

        Bitmap bitmap = entryLives.get(variableName);
        return bitmap != null && bitmap.getElementAt(blockId, instructionId);
    }

    public boolean isLiveAtExit(String variableName, int blockId, int instructionId) {
        Preconditions.checkArgument(variableName != null);

        int firstLive = firstLiveBlock.getOrDefault(blockId, Collections.emptyMap())
                .getOrDefault(variableName, Integer.MAX_VALUE);
        if (firstLive > instructionId) {
            return false;
        }
        int lastLive = lastLiveBlock.getOrDefault(blockId, Collections.emptyMap())
                .getOrDefault(variableName, -1);
        if (lastLive < instructionId) {
            return false;
        }

        Bitmap bitmap = exitLives.get(variableName);
        return bitmap != null && bitmap.getElementAt(blockId, instructionId);
    }

    public Set<String> getLiveVariablesAtEntry(int blockId, int instructionId) {
        Set<String> variables = new HashSet<>();

        for (String variableName : entryLives.keySet()) {
            Bitmap bitmap = entryLives.get(variableName);

            if (bitmap.getElementAt(blockId, instructionId)) {
                variables.add(variableName);
            }
        }

        return variables;
    }

    public Set<String> getLiveVariablesAtExit(int blockId, int instructionId) {
        Set<String> variables = new HashSet<>();

        for (String variableName : exitLives.keySet()) {
            if (isLiveAtExit(variableName, blockId, instructionId)) {
                variables.add(variableName);
            }
        }

        return variables;
    }
}
