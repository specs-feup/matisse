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

package org.specs.CIRTypes.Types.String;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.String.Functions.Copy;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class StringFunctions implements MatrixFunctions {

    StringFunctions(StringType type) {
    }

    @Override
    public InstanceProvider copy() {
        return data -> new Copy(data).create();
    }

    @Override
    public InstanceProvider numel() {
        return data -> {
            StringType stringType = data.getInputType(StringType.class, 0);
            String value = stringType.getString();
            if (value == null) {
                throw new NotImplementedException("numel(string) for non-constant strings");
            }

            FunctionType functionType = FunctionTypeBuilder.newInline()
                    .addInput(stringType)
                    .returning(data.getNumerics().newInt(value.length()))
                    .build();
            return new InlinedInstance(functionType, "$string$numel", tokens -> Integer.toString(value.length()));
        };
    }

    @Override
    public InstanceProvider get() {
        return data -> {
            if (data.getNumInputs() != 2) {
                throw new IllegalArgumentException("get(string, ...) with multiple arguments.");
            }
            StringType stringType = data.getInputType(StringType.class, 0);
            String value = stringType.getString();
            if (value == null) {
                throw new NotImplementedException("get(string, ...) with non-constant string.");
            }

            ScalarType scalarType = data.getInputType(ScalarType.class, 1);
            if (!scalarType.scalar().hasConstant()) {
                throw new NotImplementedException("get(string, ...) with non-constant index.");
            }

            int index = scalarType.scalar().getConstant().intValue();
            char ch = value.charAt(index);

            FunctionType functionType = FunctionTypeBuilder.newInline()
                    .addInput(stringType)
                    .addInput(scalarType)
                    .returning(data.getNumerics().newInt(value.length()))
                    .build();
            return new InlinedInstance(functionType, "$string$get", tokens -> Integer.toString(ch));
        };
    }
}
