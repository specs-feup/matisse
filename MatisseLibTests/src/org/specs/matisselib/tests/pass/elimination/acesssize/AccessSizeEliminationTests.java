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

package org.specs.matisselib.tests.pass.elimination.acesssize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.posttype.AccessSizeEliminationPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AccessSizeInstruction;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class AccessSizeEliminationTests {
    @Test
    public void testGeneric() {
        TypeShape AShape = TypeShape.newUndefinedShape();
        TypeShape IShape = TypeShape.newUndefinedShape();
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.GENERIC;

        performTest(AShape, IShape, resource);
    }

    @Test
    public void testA2D() {
        TypeShape AShape = TypeShape.newDimsShape(2);
        TypeShape IShape = TypeShape.newUndefinedShape();
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A2D;

        performTest(AShape, IShape, resource);
    }

    @Test
    public void testA2DI2D() {
        TypeShape AShape = TypeShape.newDimsShape(2);
        TypeShape IShape = TypeShape.newDimsShape(2);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A2D_I2D;

        performTest(AShape, IShape, resource);
    }

    @Test
    public void testA2DI2DWithFor() {
        TypeShape AShape = TypeShape.newDimsShape(2);
        TypeShape IShape = TypeShape.newDimsShape(2);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A2D_I2D_WITH_FOR;

        performTestWithFor(AShape, IShape, resource);
    }

    @Test
    public void testA2DI1DWithFor() {
        TypeShape AShape = TypeShape.newDimsShape(2);
        TypeShape IShape = TypeShape.newInstance(1, 3);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A2D_I1D_WITH_FOR;

        performTestWithFor(AShape, IShape, resource);
    }

    @Test
    public void testA2DI1DUndefinedWithFor() {
        TypeShape AShape = TypeShape.newDimsShape(2);
        TypeShape IShape = TypeShape.newRow();
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A2D_I1D_UNDEFINED_WITH_FOR;

        performTestWithFor(AShape, IShape, resource);
    }

    @Test
    public void testARowI1D() {
        TypeShape AShape = TypeShape.newInstance(1, 3);
        TypeShape IShape = TypeShape.newInstance(1, 3);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.AROW_I1D;

        performTest(AShape, IShape, resource);
    }

    @Test
    public void testAColI1D() {
        TypeShape AShape = TypeShape.newInstance(3, 1);
        TypeShape IShape = TypeShape.newInstance(1, 3);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.ACOL_I1D;

        performTest(AShape, IShape, resource);
    }

    @Test
    public void testAUndefinedI1DWithFor() {
        TypeShape AShape = TypeShape.newUndefinedShape();
        TypeShape IShape = TypeShape.newInstance(1, 3);
        AccessSizeEliminationResource resource = AccessSizeEliminationResource.A_UNDEFINED_I1D_WITH_FOR;

        performTestWithFor(AShape, IShape, resource);
    }

    private static void performTest(TypeShape AShape, TypeShape IShape, AccessSizeEliminationResource resource) {
        NumericFactory numerics = MatlabToCOptionUtils.newDefaultNumerics();

        VariableType intType = numerics.newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        VariableType AType = DynamicMatrixType.newInstance(intType, AShape);
        VariableType IType = DynamicMatrixType.newInstance(intType, IShape);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("I$1", 1));
        block.addInstruction(new AccessSizeInstruction("y$ret", "A$1", "I$1"));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", AType);
        types.put("I$1", IType);
        types.put("y$ret", intMatrixType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void performTestWithFor(TypeShape AShape, TypeShape IShape, AccessSizeEliminationResource resource) {
        NumericFactory numerics = MatlabToCOptionUtils.newDefaultNumerics();

        VariableType intType = numerics.newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        VariableType AType = DynamicMatrixType.newInstance(intType, AShape);
        VariableType IType = DynamicMatrixType.newInstance(intType, IShape);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        SsaBlock loopBlock = new SsaBlock();
        SsaBlock endBlock = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("I$1", 1));
        block.addInstruction(new ArgumentInstruction("$1", 2));
        block.addInstruction(new AccessSizeInstruction("y$1", "A$1", "I$1"));
        block.addInstruction(new ForInstruction("$1", "$1", "$1", 1, 2));

        endBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("A$1", "I$1"), Arrays.asList(0, 1)));

        body.addBlock(block);
        body.addBlock(loopBlock);
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", AType);
        types.put("I$1", IType);
        types.put("$1", intType);
        types.put("y$1", intMatrixType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("ndims", MatlabBuiltin.NDIMS.getMatlabFunction());
        functions.put("numel", new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                throw new RuntimeException();
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return FunctionTypeBuilder.newInline()
                        .addInputs(data.getInputTypes())
                        .returning(data.getNumerics().newInt())
                        .build();
            }
        });
        functions.put("horzcat", MatlabBuiltin.HORZCAT.getMatlabFunction());
        functions.put("eq", MatlabOp.Equal.getMatlabFunction());
        functions.put("or", MatlabOp.ElementWiseOr.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new AccessSizeEliminationPass(), body, types, functions);
        new SsaValidatorPass("test-for").apply(body, new CommonPassData("data"));
    }
}
