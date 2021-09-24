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

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.FunctionInstance.Instances.StructInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringType;

import pt.up.fe.specs.util.SpecsIo;

public class GetAbsoluteFilename implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        List<String> inputNames = Arrays.asList("filename");
        VariableType stringType = StringType.create(null, 8);
        List<VariableType> inputTypes = Arrays.asList(stringType);
        String outputName = "absolute_filename";
        FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, outputName, stringType);

        String body = SpecsIo.getResource(DataLoadTemplateFile.GET_ABSOLUTE_FILENAME);

        String fileName = "lib/load";
        LiteralInstance instance = new LiteralInstance(functionType, "get_absolute_filename", fileName, body);
        String includeCode = SpecsIo.getResource(DataLoadTemplateFile.GET_ABSOLUTE_FILENAME_INCLUDES);
        StructInstance conditionalInclude = new StructInstance("$conditional_get_absolute_filename_includes",
                fileName,
                includeCode);
        instance.getCustomImplementationInstances().add(conditionalInclude);

        // TODO: Check if correct
        // instance.setCustomImplementationIncludes("#ifdef __linux\n" +
        // "#include <unistd.h>\n" +
        // "#endif");
        return instance;
    }

}
