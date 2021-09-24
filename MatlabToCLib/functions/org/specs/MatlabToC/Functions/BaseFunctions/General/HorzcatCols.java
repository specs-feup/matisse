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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;
import org.specs.matisselib.PassMessage;

public class HorzcatCols implements InstanceProvider {

    private HorzcatCols() {
    }

    private static boolean isValid(ProviderData data) {
	Integer fixedRows = null;

	for (VariableType input : data.getInputTypes()) {
	    if (!(input instanceof MatrixType)) {
		return false;
	    }

	    MatrixType matrix = (MatrixType) input;
	    TypeShape shape = matrix.getTypeShape();

	    if (!shape.isKnownColumn()) {
		return false;
	    }

	    int numRows = shape.getDim(0);
	    if (numRows > 0) {
		if (fixedRows != null) {
		    if (fixedRows != numRows) {
			throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
				"horzcat called with matrices of different number of rows.");
		    }
		}

		fixedRows = numRows;
	    }
	}

	return true;
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
	if (isValid(data)) {
	    return Optional.of(getTemplateProvider(data));
	}
	return Optional.empty();
    }

    private static InstanceProvider getTemplateProvider(ProviderData data) {
	Integer fixedRows = null;
	int numCols = data.getNumInputs();
	boolean fullyKnown = true;

	for (VariableType input : data.getInputTypes()) {
	    MatrixType matrix = (MatrixType) input;
	    TypeShape shape = matrix.getTypeShape();

	    int numRows = shape.getDim(0);
	    if (numRows > 0) {
		fixedRows = numRows;
	    } else if (numRows < 0) {
		fullyKnown = false;
	    } else {
		// numRows == 0
		--numCols;
	    }
	}

	StringBuilder code = new StringBuilder();

	String functionName = "MATISSE_horzcat_cols";
	code.append("function y = ");
	code.append(functionName);
	code.append("(");

	for (int i = 0; i < data.getNumInputs(); ++i) {
	    String inputName = getInputName(i);

	    if (i != 0) {
		code.append(", ");
	    }

	    code.append(inputName);
	}
	code.append(")\n");

	code.append("num_cols = " + numCols + ";");
	if (fixedRows != null) {
	    code.append("\nnum_rows = " + fixedRows + ";");

	    if (!fullyKnown) {
		// TODO: Validation

		List<VariableType> inputTypes = data.getInputTypes();
		for (int i = 0; i < inputTypes.size(); i++) {
		    String inputName = getInputName(i);

		    code.append("\nif isempty(" + inputName + "),");
		    code.append("\n\tnum_cols = num_cols - 1");
		    code.append("\nend");
		}
	    }
	} else {
	    code.append("\nnum_rows = 0;");

	    List<VariableType> inputTypes = data.getInputTypes();
	    for (int i = 0; i < inputTypes.size(); i++) {
		String inputName = getInputName(i);

		code.append("\nif ~isempty(" + inputName + "),");
		code.append("\n\tpartial_size = size(" + inputName + ", 1);");
		code.append("\n\t% TODO: Missing validation");
		code.append("\n\tnum_rows = partial_size;");
		code.append("\nend");
	    }
	}

	code.append("\n\n");

	code.append("y = zeros(num_rows, num_cols); % FIXME: Might not be correct for empty matrices.");

	code.append("\n\n");
	code.append("current_index = 1;");

	List<VariableType> inputTypes = data.getInputTypes();
	for (int i = 0; i < inputTypes.size(); i++) {
	    String inputName = getInputName(i);

	    code.append("\nif ~isempty(" + inputName + "),");
	    code.append("\n\ty(:, current_index) = ");
	    code.append(inputName);
	    code.append(";\n\tcurrent_index = current_index + 1;");
	    code.append("\nend");
	}

	code.append("\nend");

	return MFileProvider.getProvider(new MatlabTemplate() {

	    @Override
	    public String getName() {
		return functionName;
	    }

	    @Override
	    public String getMCode() {
		return code.toString();
	    }
	});
    }

    private static String getInputName(int i) {
	return "i" + (i + 1);
    }

    public static InstanceProvider getProvider() {
	return new HorzcatCols();
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	throw new RuntimeException("Calling newCInstance directly: Should use accepts instead.");
    }

}
