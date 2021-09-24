/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.VariableStorage;

import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdlibFunction;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.Void.VoidType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.up.fe.specs.util.SpecsIo;

public class LoadRawDataFromFile implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData providerData) {
	List<String> inputNames = Lists.newArrayList("varname", "num_elements", "out");
	List<VariableType> inputTypes = providerData.getInputTypes();
	if (inputTypes.size() != 3) {
	    return null;
	}
	FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

	MatrixType outputType = (MatrixType) inputTypes.get(2);
	String functionName = "load_raw_data_from_file_" + outputType.getSmallId();

	InstanceBuilder helper = new GenericInstanceBuilder(providerData);

	String body = SpecsIo.getResource(DataLoadTemplateFile.LOAD_FROM_FILE);
	body = body.replace("<ELEMENT_TYPE>", outputType.matrix().getElementType().code().getType());
	CNode outToken = CNodeFactory.newVariable("out", inputTypes.get(2));
	String outData = helper.getFunctionCall(MatrixFunction.GET_DATA_PTR, outToken).getCode();
	body = body.replace("<OUT_DATA>", outData);
	LiteralInstance instance = new LiteralInstance(functionType, functionName, "lib/load", body);
	Set<FunctionInstance> dependentInstances = Sets.newHashSet();

	NumericFactory numerics = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

	ProviderData exitData = ProviderData.newInstance(providerData, numerics.newInt());
	dependentInstances.add(CStdlibFunction.EXIT.newCInstance(exitData));
	VariableType stringType = StringType.create(null, 8);
	ProviderData absoluteFilenameData = ProviderData.newInstance(providerData, stringType);
	dependentInstances.add(new GetAbsoluteFilename().newCInstance(absoluteFilenameData));

	// instance.addCustomImplementationInstances(dependentInstances);
	instance.getCustomImplementationInstances().add(dependentInstances);
	instance.setCustomImplementationIncludes("stdio.h", "string.h", "errno.h");
	return instance;
    }
}
