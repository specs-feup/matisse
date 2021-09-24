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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.CodeBuilder.VariableManager;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.TypedFunctionCallProcessor;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;

import pt.up.fe.specs.util.providers.StringProvider;

public class FunctionCallTests {
    @Test
    public void testSimple() {
        final ProviderData providerData = ProviderData.newInstance("function-call-tests");
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

            @Override
            public Optional<VariableType> getOriginalSsaType(String ssaName) {
                return manager.getVariableTypeFromSsaName(ssaName);
            }
        };

        FunctionType fType = FunctionTypeBuilder.newWithOutputsAsInputs()
                .addInput("a", doubleMatrixType)
                .addInput("b", intType)
                .addOutputAsInput("out", doubleMatrixType)
                .build();

        builder.addDummyFunction("f", fType);

        TypedFunctionCallInstruction call = new TypedFunctionCallInstruction("f", fType,
                "$out", "$in", "i$1");
        TypedFunctionCallProcessor processor = new TypedFunctionCallProcessor();

        TestUtils.test(builder, call, processor, "f(in, i1, &out);");
    }

    @Test
    public void testReferenceMatchingOutputAsInput() {
        final ProviderData providerData = ProviderData.newInstance("function-call-tests");
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

            @Override
            public Optional<VariableType> getOriginalSsaType(String ssaName) {
                return manager.getVariableTypeFromSsaName(ssaName);
            }
        };

        FunctionType fType = FunctionTypeBuilder.newWithOutputsAsInputs()
                .addReferenceInput("a", doubleMatrixType)
                .addInput("b", intType)
                .addOutputAsInput("a", doubleMatrixType)
                .build();

        builder.addDummyFunction("f", fType);

        TypedFunctionCallInstruction call = new TypedFunctionCallInstruction("f", fType,
                "$in", "$in", "i$1");
        TypedFunctionCallProcessor processor = new TypedFunctionCallProcessor();

        TestUtils.test(builder, call, processor, "f(i1, &in);");
    }

    @Test
    public void testReferenceDifferentOutputAsInput() {
        final ProviderData providerData = ProviderData.newInstance("function-call-tests");
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

            @Override
            public Optional<VariableType> getOriginalSsaType(String ssaName) {
                return manager.getVariableTypeFromSsaName(ssaName);
            }
        };

        ByteArrayOutputStream warningDest = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(warningDest);

        builder.reporter = new DefaultReportService(null,
                stream,
                false,
                new FunctionIdentification("f.m"),
                StringProvider.newInstance("<code>"));

        FunctionType fType = FunctionTypeBuilder.newWithOutputsAsInputs()
                .addReferenceInput("a", doubleMatrixType)
                .addInput("b", intType)
                .addOutputAsInput("a", doubleMatrixType)
                .build();

        builder.addDummyFunction("f", fType);

        TypedFunctionCallInstruction call = new TypedFunctionCallInstruction("f", fType,
                "$out", "$in", "i$1");
        TypedFunctionCallProcessor processor = new TypedFunctionCallProcessor();

        TestUtils.test(builder, call, processor, "<Assign out = in>\n"
                + "f(i1, &out);");

        String result = new String(warningDest.toByteArray());
        result = result.substring(0, result.indexOf('\n')).trim();
        Assert.assertEquals(
                "Optimization Opportunity: Could not allocate input and output to same variable in %!by_ref call. Adding matrix copy.",
                result);
    }
}
