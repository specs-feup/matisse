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

package org.specs.matisselib.tests.pass.elementwise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.functionproperties.DisableOptimizationProperty;
import org.specs.matisselib.passes.posttype.ElementWisePass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ElementWiseTests extends TestSkeleton {
    enum InputType {
        SCALAR_ARGUMENT,
        SCALAR_CONSTANT,
        MATRIX_ARGUMENT,
        MATRIX_SAME_NUMEL_ARGUMENT,
    }

    @Test
    public void testPlusScalar() {
        performTest(ElementWiseResource.PLUS_SCALAR, "plus", InputType.MATRIX_ARGUMENT, InputType.SCALAR_ARGUMENT);
    }

    @Test
    public void testPlusMatrix() {
        performTest(ElementWiseResource.PLUS_MATRIX, "plus", InputType.MATRIX_ARGUMENT, InputType.MATRIX_ARGUMENT);
    }

    @Test
    public void testPlusMatrixSameNumel() {
        performTest(ElementWiseResource.PLUS_MATRIX_SAME_NUMEL, "plus", InputType.MATRIX_ARGUMENT,
                InputType.MATRIX_SAME_NUMEL_ARGUMENT);
    }

    @Test
    public void testPlusScalar2() {
        performTest(ElementWiseResource.PLUS_SCALAR2, "plus", InputType.SCALAR_ARGUMENT, InputType.MATRIX_ARGUMENT);
    }

    @Test
    public void testPlusInlineConstant() {
        performTest(ElementWiseResource.PLUS_INLINE_CONSTANT, "plus", InputType.MATRIX_ARGUMENT,
                InputType.SCALAR_CONSTANT);
    }

    @Test
    public void testUminus() {
        performTest(ElementWiseResource.UMINUS, "uminus", InputType.MATRIX_ARGUMENT);
    }

    @Test
    public void testCombine() {

        ScalarType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addAssignment("B$1", 123);
        block.addAssignment("C$1", 456);

        block.addInstruction(new LineInstruction(123));

        ProviderData providerData1 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType1 = functions.get("plus").getType(providerData1);
        ProviderData providerData2 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType2 = functions.get("minus").getType(providerData2);

        block.addInstruction(
                new TypedFunctionCallInstruction("plus", functionType1, "y$1", "A$1", "B$1"));
        block.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType2, "z$ret", "y$1", "C$1"));

        body.addBlock(block);

        types.put("A$1", intMatrixType);
        types.put("B$1", intType);
        types.put("C$1", intType);
        types.put("y$1", intMatrixType);
        types.put("z$ret", intMatrixType);

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(ElementWiseResource.COMBINED.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testDoNotCombineRedundant() {

        ScalarType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addAssignment("B$1", 123);
        block.addAssignment("C$1", 456);

        block.addInstruction(new LineInstruction(123));

        ProviderData providerData1 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType1 = functions.get("plus").getType(providerData1);
        ProviderData providerData2 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType2 = functions.get("minus").getType(providerData2);

        block.addInstruction(
                new TypedFunctionCallInstruction("plus", functionType1, "y$1", "A$1", "B$1"));
        block.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType2, "z$ret", "y$1", "C$1"));
        block.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType2, "w$ret", "y$1", "C$1"));

        body.addBlock(block);

        types.put("A$1", intMatrixType);
        types.put("B$1", intType);
        types.put("C$1", intType);
        types.put("y$1", intMatrixType);
        types.put("z$ret", intMatrixType);
        types.put("w$ret", intMatrixType);

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(ElementWiseResource.DO_NOT_COMBINE_REDUNDANT.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testAcceptCombineRedundantDirective() {

        ScalarType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        body.addProperty(new DisableOptimizationProperty("element_wise_prevent_redundancy"));
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addAssignment("B$1", 123);
        block.addAssignment("C$1", 456);

        block.addInstruction(new LineInstruction(123));

        ProviderData providerData1 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType1 = functions.get("plus").getType(providerData1);
        ProviderData providerData2 = ProviderData.newInstance("test")
                .create(intMatrixType, intType);
        FunctionType functionType2 = functions.get("minus").getType(providerData2);

        block.addInstruction(
                new TypedFunctionCallInstruction("plus", functionType1, "y$1", "A$1", "B$1"));
        block.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType2, "z$ret", "y$1", "C$1"));
        block.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType2, "w$ret", "y$1", "C$1"));

        body.addBlock(block);

        types.put("A$1", intMatrixType);
        types.put("B$1", intType);
        types.put("C$1", intType);
        types.put("y$1", intMatrixType);
        types.put("z$ret", intMatrixType);
        types.put("w$ret", intMatrixType);

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(
                        SpecsIo.getResource(ElementWiseResource.ACCEPT_COMBINE_REDUNDANT_DIRECTIVE.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void performTest(ElementWiseResource resource, String functionName, InputType... inputs) {

        ScalarType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        List<String> inputNames = new ArrayList<>();
        List<VariableType> inputTypes = new ArrayList<>();
        for (int i = 0; i < inputs.length; ++i) {
            String name = (char) ('A' + i) + "$1";
            VariableType type;

            InputType argumentType = inputs[i];
            if (argumentType == InputType.SCALAR_CONSTANT) {
                type = intType;
                block.addInstruction(AssignmentInstruction.fromInteger(name, 123));
            } else if (argumentType == InputType.SCALAR_ARGUMENT) {
                block.addInstruction(new ArgumentInstruction(name, i));
                type = intType;
            } else if (argumentType == InputType.MATRIX_ARGUMENT) {
                type = intMatrixType;
                block.addInstruction(new ArgumentInstruction(name, i));
            } else if (argumentType == InputType.MATRIX_SAME_NUMEL_ARGUMENT) {
                type = intMatrixType;
                String dim1 = body.makeTemporary("t");
                types.put(dim1, getNumerics().newInt(1));
                String dim2 = body.makeTemporary("t");
                types.put(dim2, intType);
                block.addAssignment(dim1, 1);
                block.addInstruction(
                        new UntypedFunctionCallInstruction("numel",
                                Arrays.asList(dim2),
                                Arrays.asList(inputNames.get(0))));
                block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_dims",
                        Arrays.asList(name),
                        Arrays.asList(dim1, dim2)));
            } else {
                throw new NotImplementedException(argumentType.toString());
            }

            types.put(name, type);
            inputNames.add(name);
            inputTypes.add(type);

        }

        block.addInstruction(new LineInstruction(123));

        ProviderData providerData = ProviderData.newInstance("test")
                .create(inputTypes);
        FunctionType functionType = functions.get(functionName).getType(providerData);

        block.addInstruction(
                new TypedFunctionCallInstruction(functionName, functionType, Arrays.asList("y$ret"), inputNames));

        body.addBlock(block);

        types.put("y$ret", functionType.getOutputTypes().get(0));

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("matisse_new_array", MatissePrimitive.NEW_ARRAY.getMatlabFunction());
        functions.put("matisse_new_array_from_matrix", MatissePrimitive.NEW_ARRAY_FROM_MATRIX.getMatlabFunction());
        functions.put("plus", MatlabOp.Addition.getMatlabFunction());
        functions.put("minus", MatlabOp.Subtraction.getMatlabFunction());
        functions.put("uminus", MatlabOp.UnaryMinus.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new ElementWisePass(), body, types, functions);
    }
}
