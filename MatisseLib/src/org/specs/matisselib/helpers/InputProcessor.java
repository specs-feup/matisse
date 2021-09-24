/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;

public class InputProcessor {
    public static TypeShape processDynamicMatrixInputShape(DynamicMatrixType matrix) {
	List<Integer> dims = new ArrayList<>(matrix.getTypeShape().getDims());
	for (int i = 0; i < dims.size(); ++i) {
	    if (dims.get(i) < 0) {
		dims.set(i, -1);
	    }
	    if (dims.get(i) > 1) {
		dims.set(i, -1);
	    }
	}

	TypeShape shape = TypeShape.newInstance(dims);

	return shape;
    }
}
