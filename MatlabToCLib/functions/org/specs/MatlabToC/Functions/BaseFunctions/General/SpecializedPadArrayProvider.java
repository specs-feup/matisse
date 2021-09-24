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
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseTemplate;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.matisselib.PassMessage;

import pt.up.fe.specs.util.SpecsIo;

public class SpecializedPadArrayProvider {

    public static InstanceProvider getProvider() {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Optional<InstanceProvider> accepts(ProviderData data) {
		return getSpecializedProvider(data);
	    }
	};
    }

    private static Optional<InstanceProvider> getSpecializedProvider(ProviderData data) {
	if (data.getNargouts().orElse(1) > 1) {
	    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Too many outputs in call to function 'padarray'.");
	}

	if (data.getNumInputs() < 2 || data.getNumInputs() > 4) {
	    return Optional.empty();
	}

	VariableType inputType = data.getInputTypes().get(0);
	int ndims = -1;
	if (inputType instanceof MatrixType) {
	    ndims = ((MatrixType) inputType).matrix().getShape().getRawNumDims();
	}

	VariableType padType = data.getInputTypes().get(1);
	if (!(padType instanceof MatrixType)) {
	    // TODO: Scalar pads are perfectly reasonable.
	    return Optional.empty();
	}

	MatrixType padMatrixType = (MatrixType) padType;
	TypeShape padShape = padMatrixType.matrix().getShape();

	if (!padShape.isFullyDefined()) {
	    return Optional.empty();
	}

	if (padShape.getRawNumDims() != 2) {
	    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Second argument of padarray function must be a 1D matrix.");
	}

	boolean tentativePutPrefix = true;
	boolean tentativePutPostfix = true;

	int numDims;
	if (padShape.getDim(0) == 1) {
	    numDims = padShape.getDim(1);
	} else if (padShape.getDim(1) == 1) {
	    numDims = padShape.getDim(0);
	} else {
	    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Second argument of padarray function must be a 1D matrix.");
	}

	for (int i = 2; i < data.getNumInputs(); ++i) {
	    VariableType arg3type = data.getInputTypes().get(2);
	    if (!(arg3type instanceof StringType)) {
		// Custom values not currently supported

		return Optional.empty();
	    }

	    String arg3 = StringTypeUtils.getString(arg3type);
	    if (arg3.equals("pre")) {
		tentativePutPrefix = true;
		tentativePutPostfix = false;
	    } else if (arg3.equals("post")) {
		tentativePutPrefix = false;
		tentativePutPostfix = true;
	    } else if (arg3.equals("both")) {
		tentativePutPrefix = true;
		tentativePutPostfix = true;
	    } else {
		return Optional.empty();
	    }
	}

	if (numDims == 0) {
	    return Optional.empty();
	}

	int usedInputs = 2;
	boolean putPrefix = tentativePutPrefix;
	boolean putPostfix = tentativePutPostfix;
	boolean requireExtraDim = numDims < ndims;

	InstanceProvider baseProvider = MFileProvider.getProvider(new MatlabTemplate() {

	    @Override
	    public String getName() {
		return "padarray_specialized" + (putPrefix ? "_pre" : "") + (putPostfix ? "_post" : "")
			+ FunctionInstanceUtils.getTypesSuffix(data.getInputTypes()) + "_"
			+ numDims;
	    }

	    @Override
	    public String getMCode() {
		String code = SpecsIo.getResource(BaseTemplate.PADARRAY_SPECIALIZED);

		code = code.replace("<FUNCTION_NAME>", getName());
		code = code.replace("<INPUTS>", "A, pad");

		code = code.replace("<SET_VALUE>", "value = 0;");
		code = code.replace("<PRE>", putPrefix ? "1" : "0");
		code = code.replace("<POST>", putPostfix ? "1" : "0");

		StringBuilder sizesBuilder = new StringBuilder();
		StringBuilder loopBuilder = new StringBuilder();
		StringBuilder destIndices = new StringBuilder();
		StringBuilder srcIndices = new StringBuilder();
		StringBuilder endBuilder = new StringBuilder();

		int indicesToUse = numDims;
		if (requireExtraDim) {
		    ++indicesToUse;
		}

		for (int i = 0; i < indicesToUse; ++i) {
		    String indexName = getIndexName(i);
		    String sizeName = getSizeName(i);

		    if (i != 0) {
			sizesBuilder.append(", ");
			destIndices.append(", ");
			srcIndices.append(", ");
		    }

		    sizesBuilder.append(sizeName);
		    destIndices.append(indexName);
		    srcIndices.append(indexName);

		    if (putPrefix && i != numDims) {
			destIndices.append(" + pad(");
			destIndices.append(i + 1);
			destIndices.append(")");
		    }
		}

		for (int i = indicesToUse - 1; i >= 0; --i) {
		    String indexName = getIndexName(i);
		    String sizeName = getSizeName(i);

		    loopBuilder.append("for ");
		    loopBuilder.append(indexName);
		    loopBuilder.append(" = 1:");
		    loopBuilder.append(sizeName);
		    loopBuilder.append(",\n");
		    endBuilder.append("\tend\n");
		}

		code = code.replace("<SIZES>", sizesBuilder);
		code = code.replace("<LOOPS>", loopBuilder);
		code = code.replace("<DEST_INDICES>", destIndices);
		code = code.replace("<SRC_INDICES>", srcIndices);
		code = code.replace("<END_LOOPS>", endBuilder);

		return code;
	    }

	    private String getSizeName(int i) {
		return "s" + (i + 1);
	    }

	    private String getIndexName(int i) {
		return "i" + (i + 1);
	    }
	});

	return Optional.of(new MatlabInstanceProvider() {

	    @Override
	    public FunctionInstance create(ProviderData providerData) {
		return baseProvider.newCInstance(filterProviderData(data));
	    }

	    @Override
	    public FunctionType getType(ProviderData data) {
		return baseProvider.getType(filterProviderData(data));
	    }

	    private ProviderData filterProviderData(ProviderData data) {
		ProviderData newData = data.create(data.getInputTypes().subList(0, usedInputs));
		newData.setOutputData(data.getOutputData());
		return newData;
	    }

	    @Override
	    public boolean checkRule(ProviderData data) {
		return true;
	    }

	    @Override
	    public InputsFilter getInputsFilter() {
		return new InputsFilter() {

		    @Override
		    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {
			return originalArguments.subList(0, usedInputs);
		    }
		};
	    }
	});
    }

}
