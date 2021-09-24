/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import org.specs.CIR.Language.Operators.COperator;

/**
 * Represents the function to use on the MinMax implementations.
 * 
 * @author Pedro Pinto
 *
 */
public enum MinMax {

	MAX("max", ">", COperator.GreaterThan),
	MIN("min", "<", COperator.LessThan);

	private final String name;
	private final String operatorString;
	private final COperator operator;

	/**
	 * @return the name
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @return the operator string
	 */
	public String getOperatorString() {
	    return operatorString;
	}
	
	/**
	 * @return the operator
	 */
	public COperator getOperator() {
	    return operator;
	}

	private MinMax(String name, String operatorString, COperator operator) {
	    this.name = name;
	    this.operatorString = operatorString;
	    this.operator = operator;
	}

}
