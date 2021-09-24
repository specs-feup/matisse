/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionType;

/**
 * @author Joao Bispo
 * 
 */
public class MinMaxData {
    private final FunctionType functionTypes;
    private final List<Integer> inputShape;
    private final List<Integer> outputShape;
    private final int dim;

    public MinMaxData(FunctionType functionTypes, List<Integer> inputShape,
	    List<Integer> outputShape, int dim) {
	this.functionTypes = functionTypes;
	this.inputShape = inputShape;
	this.outputShape = outputShape;
	this.dim = dim;
    }

    /**
     * @return the functionTypes
     */
    public FunctionType getFunctionTypes() {
	return functionTypes;
    }

    /**
     * @return the inputShape
     */
    public List<Integer> getInputShape() {
	return inputShape;
    }

    /**
     * @return the outputShape
     */
    public List<Integer> getOutputShape() {
	return outputShape;
    }

    /**
     * @return the dim
     */
    public int getDim() {
	return dim;
    }
}
