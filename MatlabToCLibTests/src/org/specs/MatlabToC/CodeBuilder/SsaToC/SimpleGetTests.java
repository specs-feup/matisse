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

package org.specs.MatlabToC.CodeBuilder.SsaToC;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.VariableManager;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SimpleGetProcessor;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;

public class SimpleGetTests {
    @Test
    public void testDynamicSimpleGet1D() {
        MockSsaToCBuilderService builder = makeBuilder();

        SimpleGetInstruction get = new SimpleGetInstruction("$out", "$in", Arrays.asList("i$1"));
        SimpleGetProcessor processor = new SimpleGetProcessor();

        TestUtils.test(builder, get, processor, "out = in->data[i1 - 1];");
    }

    @Test
    public void testDynamicSimpleGet2D() {
        MockSsaToCBuilderService builder = makeBuilder();

        SimpleGetInstruction get = new SimpleGetInstruction("$out", "$in", Arrays.asList("i$1", "i$2"));
        SimpleGetProcessor processor = new SimpleGetProcessor();

        TestUtils.test(builder, get, processor, "out = in->data[(i1 - 1) + (i2 - 1) * in->shape[0]];");
    }

    private static MockSsaToCBuilderService makeBuilder() {
        final ProviderData providerData = ProviderData.newInstance("simple-get-tests");
        NumericFactory numerics = providerData.getNumerics();

        VariableType doubleType = numerics.newDouble();
        VariableType intType = numerics.newInt();
        VariableType doubleMatrixType = DynamicMatrixType.newInstance(doubleType);

        final VariableManager manager = new MockVariableManager() {
            @Override
            public String convertSsaToFinalName(String variableName) {
                return variableName.replace("$", "");
            }

            @Override
            public Optional<VariableType> getVariableTypeFromFinalName(String finalName) {
                switch (finalName) {
                case "out":
                    return Optional.of(doubleType);
                case "in":
                    return Optional.of(doubleMatrixType);
                case "i1":
                case "i2":
                    return Optional.of(intType);
                default:
                    return Optional.empty();
                }
            }
        };
        MockSsaToCBuilderService builder = new MockSsaToCBuilderService() {
            @Override
            public VariableManager getVariableManager() {
                return manager;
            }

            @Override
            public ProviderData getCurrentProvider() {
                return providerData;
            }
        };
        return builder;
    }
}
