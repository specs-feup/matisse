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

import java.util.List;

public class Bitmap {
    private final boolean[][] data;

    public Bitmap(int rows, List<Integer> columns) {
	data = new boolean[rows][];
	for (int i = 0; i < columns.size(); ++i) {
	    data[i] = new boolean[columns.get(i)];
	}
    }

    public boolean getElementAt(int row, int column) {
	return data[row][column];
    }

    public void setElementAt(int row, int column, boolean value) {
	data[row][column] = value;
    }
}
