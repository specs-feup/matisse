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

package org.specs.matisselib.tests.pass.elimination.end;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.EndEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class EndEliminationTests extends TestSkeleton {

    @Test
    public void testNone() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("y$ret", 1);
        body.addBlock(block);

        applyPass(body, null, null);

        EndEliminationResource resource = EndEliminationResource.NONE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static FunctionBody buildSimpleBody(int index, int numIndices) {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new EndInstruction("y$ret", "A$1", index, numIndices));
        body.addBlock(block);

        return body;
    }

    private static void performTest(int index, int totalIndices, EndEliminationResource resource,
            TypeShape matrixShape) {

        FunctionBody body = buildSimpleBody(index, totalIndices);
        performTest(body, resource, matrixShape);
    }

    private static void performTest(FunctionBody body, EndEliminationResource resource, TypeShape matrixShape) {

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, matrixShape);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple() {
        performTest(0, 1, EndEliminationResource.SIMPLE, TypeShape.newUndefinedShape());
    }

    @Test
    public void test1of2() {
        performTest(0, 2, EndEliminationResource.I1OF2, TypeShape.newUndefinedShape());
    }

    @Test
    public void test2of2_ndims2() {
        performTest(1, 2, EndEliminationResource.I2OF2_NDIMS2, TypeShape.newDimsShape(2));
    }

    @Test
    public void test2of2_ndims3() {
        performTest(1, 2, EndEliminationResource.I2OF2_NDIMS3, TypeShape.newDimsShape(3));
    }

    @Test
    public void test3of3_ndims2() {
        performTest(2, 3, EndEliminationResource.I3OF3_NDIMS2, TypeShape.newDimsShape(2));
    }

    @Test
    public void test2of2_unknownNdims() {
        performTest(1, 2, EndEliminationResource.I2OF2_UNKNOWN_NDIMS, TypeShape.newUndefinedShape());
    }

    private static FunctionBody buildNestedBody(int index, int numIndices) {
        FunctionBody body = new FunctionBody();

        SsaBlock outerBlock = new SsaBlock();
        body.addBlock(outerBlock);
        outerBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        outerBlock.addInstruction(new ForInstruction("A$1", "A$1", "A$1", 1, 2));

        SsaBlock loopBlock = new SsaBlock();
        body.addBlock(loopBlock);
        loopBlock.addInstruction(new EndInstruction("X$1", "A$1", index, numIndices));

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("$1", "X$1"), Arrays.asList(0, 1)));

        return body;
    }

    @Test
    public void test2of2_unknownNdimsNested() {
        EndEliminationResource resource = EndEliminationResource.I2OF2_UNKNOWN_NDIMS_NESTED;
        TypeShape matrixShape = TypeShape.newUndefinedShape();
        performTest(buildNestedBody(1, 2), resource, matrixShape);
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("times", MatlabOp.Multiplication.getMatlabFunction());
        functions.put("ndims", MatlabBuiltin.NDIMS.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new EndEliminationPass(), body, types, functions);
    }
}
