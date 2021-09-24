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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseTemplate;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;

import pt.up.fe.specs.util.SpecsIo;

public class NewFromMatrixTemplate extends MatlabTemplate {

    private final Number number;
    private final VariableType numericType;
    private final NumericClassName className;

    public NewFromMatrixTemplate(Number number, VariableType numericType) {
	this.number = number;
	this.numericType = numericType;

	className = MatlabToCTypesUtils.getNumericClass(numericType);
    }

    @Override
    public String getMCode() {
	MatlabNumber mNumber = MatlabNumber.getMatlabNumber(number.toString());

	String newArray = SpecsIo.getResource(BaseTemplate.NEW_FROM_MATRIX);
	newArray = newArray.replace("<CLASS>", className.getMatlabString());
	newArray = newArray.replace("<VALUE>", mNumber.toMatlabString());
	newArray = newArray.replace("<FUNCTION_NAME>", getName());

	return newArray;
    }

    @Override
    public String getName() {
	String functionName = "new_from_matrix_" + className.getMatlabString() + "_" + number.intValue() + "_"
		+ numericType.getSmallId();

	return functionName;
    }

}
