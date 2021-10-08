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

package org.specs.matlabtocl.v2.tests.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matlabtocl.v2.codegen.CLTypeDecorator;
import org.specs.matlabtocl.v2.codegen.CLTypeDecoratorBuilder;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

public class CLTypeDecoratorBuilderTests {
    @Test
    public void testSimpleGet() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleGetInstruction("y$ret", "A$1", Arrays.asList("$1", "$2")));
        body.addBlock(block);

        CLTypeDecoratorBuilder builder = newDummyBuilder(body);
        CLTypeDecorator typeDecorator = builder.buildTypeDecorator();

        ProviderData dummyData = ProviderData.newInstance("cl-type-decorator-builder-tests");
        ScalarType intType = dummyData.getNumerics().newInt();
        SizedMatrixType decoratedType = (SizedMatrixType) typeDecorator.decorateType("A$1",
                DynamicMatrixType.newInstance(intType));

        Assert.assertFalse(decoratedType.containsNumel());
        Assert.assertEquals(1, decoratedType.containedDims());
    }

    @Test
    public void testGetOrFirst() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new GetOrFirstInstruction("y$ret", "A$1", "$1"));
        body.addBlock(block);

        CLTypeDecoratorBuilder builder = newDummyBuilder(body);
        CLTypeDecorator typeDecorator = builder.buildTypeDecorator();

        ProviderData dummyData = ProviderData.newInstance("cl-type-decorator-builder-tests");
        ScalarType intType = dummyData.getNumerics().newInt();
        SizedMatrixType decoratedType = (SizedMatrixType) typeDecorator.decorateType("A$1",
                DynamicMatrixType.newInstance(intType));

        Assert.assertTrue(decoratedType.containsNumel());
        Assert.assertEquals(0, decoratedType.containedDims());
    }

    @Test
    public void testGetSize() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("$1", 2);
        block.addInstruction(new UntypedFunctionCallInstruction("size",
                Arrays.asList("y$ret"),
                Arrays.asList("A$1", "$1")));
        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", CLNativeType.INT.setConstant(2));

        CLTypeDecoratorBuilder builder = newDummyBuilder(body, types);
        CLTypeDecorator typeDecorator = builder.buildTypeDecorator();

        ProviderData dummyData = ProviderData.newInstance("cl-type-decorator-builder-tests");
        ScalarType intType = dummyData.getNumerics().newInt();

        SizedMatrixType decoratedType = (SizedMatrixType) typeDecorator.decorateType("A$1",
                DynamicMatrixType.newInstance(intType));
        Assert.assertFalse(decoratedType.containsNumel());
        Assert.assertEquals(2, decoratedType.containedDims());
    }

    private static CLTypeDecoratorBuilder newDummyBuilder(FunctionBody body) {
        return newDummyBuilder(body, Collections.emptyMap());
    }

    private static CLTypeDecoratorBuilder newDummyBuilder(FunctionBody body, Map<String, VariableType> types) {
        VariableAllocation allocation = new VariableAllocation();
        List<String> variableNames = new ArrayList<>();

        Set<String> variables = new HashSet<>();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            variables.addAll(instruction.getInputVariables());
            variables.addAll(instruction.getOutputs());
        }

        for (String variable : variables) {
            allocation.addIsolatedVariable(variable);
            variableNames.add(variable);
        }

        return new CLTypeDecoratorBuilder(body, variableNames, allocation,
                name -> Optional.ofNullable(types.get(name)));
    }
}
