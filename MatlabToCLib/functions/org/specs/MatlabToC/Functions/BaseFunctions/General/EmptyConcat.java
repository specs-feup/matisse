/**
 * Copyright 2017 SPeCS.
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

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;

public class EmptyConcat extends AInstanceBuilder {
    private EmptyConcat(ProviderData data) {
        super(data);
    }

    private static int getReturnTypeIndex(ProviderData data) {
        List<VariableType> inputTypes = data.getInputTypes();
        for (int i = 0; i < inputTypes.size(); i++) {
            VariableType type = inputTypes.get(i);
            if (!MatrixUtils.isKnownEmptyMatrix(type)) {
                return i;
            }
        }

        assert false;
        throw new Error("Calling getType of function that should not have passed validation");
    }

    private static VariableType getReturnType(ProviderData data) {
        return data.getInputTypes().get(getReturnTypeIndex(data));
    }

    public static FunctionType getType(ProviderData data) {
        VariableType returnType = getReturnType(data);

        List<String> inputNames = new ArrayList<>();
        for (int i = 0; i < data.getNumInputs(); ++i) {
            inputNames.add("in" + (i + 1));
        }

        return FunctionTypeBuilder.newSimple()
                .addInputs(inputNames, data.getInputTypes())
                .returning("out", returnType)
                .build();
    }

    public static InstanceProvider newSingleNonEmpty() {
        return new MatlabInstanceProvider() {
            @Override
            public boolean checkRule(ProviderData data) {
                if (data.getInputTypes().isEmpty()) {
                    return false;
                }

                if (data.getInputTypes().stream()
                        .anyMatch(type -> !ScalarUtils.isScalar(type) && !MatrixUtils.isMatrix(type))) {
                    return false;
                }

                return data.getInputTypes().stream()
                        .filter(input -> !MatrixUtils.isKnownEmptyMatrix(input))
                        .count() == 1;
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return EmptyConcat.getType(data);
            }

            @Override
            public FunctionInstance create(ProviderData providerData) {
                return new EmptyConcat(providerData).create();
            }
        };
    }

    private static class EmptyConcatTemplate extends MatlabTemplate {

        private final String name, code;

        EmptyConcatTemplate(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @Override
        public String getMCode() {
            return code;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    @Override
    public FunctionInstance create() {
        int inputIndex = getReturnTypeIndex(getData());

        String referencedInput = "in" + (inputIndex + 1);
        String output = "out";

        String functionName = "empty_concat_" + inputIndex;

        StringBuilder matlabFunction = new StringBuilder();
        matlabFunction.append("function out = ");
        matlabFunction.append(functionName);
        matlabFunction.append("(");
        for (int i = 0; i < getData().getNumInputs(); ++i) {
            if (i != 0) {
                matlabFunction.append(", ");
            }
            matlabFunction.append("in");
            matlabFunction.append(i + 1);
        }
        matlabFunction.append("),\n");

        matlabFunction.append("\t");
        matlabFunction.append(output);
        matlabFunction.append(" = ");
        matlabFunction.append(referencedInput);
        matlabFunction.append(";\n");

        matlabFunction.append("end\n");

        return MFileProvider.getInstance(new EmptyConcatTemplate(functionName, matlabFunction.toString()), getData());
    }

}
