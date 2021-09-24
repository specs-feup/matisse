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

package org.specs.MatlabToC.Functions.MatissePrimitives;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.matisselib.PassMessage;

public class NewArrayFromDims extends AInstanceBuilder {

    public NewArrayFromDims(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        FunctionType functionType = getFunctionType();
        VariableType outputType = functionType.getOutputTypes().get(0);

        return ((MatrixType) outputType).functions().create().newCInstance(getData());
    }

    private FunctionType getFunctionType() {
        List<VariableType> argumentTypes = getData().getInputTypes();

        List<Integer> dims = new ArrayList<>();

        for (int i = 0; i < argumentTypes.size(); i++) {
            VariableType type = argumentTypes.get(i);

            // Check if it has a numeric element
            if (!ScalarUtils.hasScalarType(type)) {
                throw getData().getReportService()
                        .emitError(PassMessage.CORRECTNESS_ERROR,
                                "matisse_new_array_from_dims *requires* inputs to be scalars.");
            }

            ScalarType scalarType = (ScalarType) type;
            Number number = scalarType.scalar().getConstant();
            if (number == null) {
                dims.add(-1);
                continue;
            }

            double value = number.doubleValue();
            if (value < 0) {
                value = 0;
            }
            if (value != number.intValue()) {
                throw getData().getReportService()
                        .emitError(
                                PassMessage.CORRECTNESS_ERROR,
                                "In matisse_new_array_from_dims, scalar input is " + number
                                        + ", expected integer");
            }

            dims.add((int) value);
        }

        if (dims.size() == 0) {
            dims.add(1);
            dims.add(1);
        } else if (dims.size() == 1) {
            dims.add(dims.get(0));
        }

        VariableType suggestedOutputType = getData().getOutputType();
        MatrixType outputType;

        if (suggestedOutputType != null && suggestedOutputType instanceof StaticMatrixType) {
            // TODO: Check if dims are compatible?
            outputType = (MatrixType) suggestedOutputType;
        } else {
            VariableType elementType = MatlabToCTypesUtils.getElementType(getData());
            TypeShape shape = TypeShape.newInstance(dims);

            outputType = DynamicMatrixType.newInstance(elementType, shape);
        }

        List<String> argumentNames = new ArrayList<>();
        for (int i = 0; i < argumentTypes.size(); ++i) {
            argumentNames.add("arg_" + (i + 1));
        }

        ProviderData data = getData().create(getData().getInputTypes());
        data.setOutputType(outputType);
        return outputType.functions().create().getType(data);
    }

    public static InstanceProvider getProvider() {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new NewArrayFromDims(data).create();
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return new NewArrayFromDims(data).getFunctionType();
            }
        };
    }

}
