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

package org.specs.matisselib.tests.pass.elimination.reduction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.reductionelimination.CumulativeReductionEliminationPass;
import org.specs.matisselib.passes.posttype.reductionelimination.DotReductionEliminationPass;
import org.specs.matisselib.passes.posttype.reductionelimination.MinMax3ReductionEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ReductionEliminationTests extends TestSkeleton {
    @Test
    public void testDot() {

        VariableType doubleType = getNumerics().newDouble();
        VariableType doubleRowType = DynamicMatrixType.newInstance(doubleType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionType functionType = FunctionType.newInstance(Arrays.asList("A", "B"),
                Arrays.asList(doubleRowType, doubleRowType), "out", doubleType);

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("A$2", 1));
        block.addInstruction(new TypedFunctionCallInstruction("dot", functionType, "A$ret", "A$1", "A$2"));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", doubleRowType);
        types.put("A$2", doubleRowType);
        types.put("A$ret", doubleType);
        TestUtils.testTypeTransparentPass(new DotReductionEliminationPass(), body, types, functions);

        ReductionEliminationResource resource = ReductionEliminationResource.DOT;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSum() {

        VariableType doubleType = getNumerics().newDouble();
        VariableType doubleRowType = DynamicMatrixType.newInstance(doubleType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("sum", Arrays.asList("y$ret"), Arrays.asList("A$1")));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", doubleRowType);
        types.put("y$ret", doubleType);
        TestUtils.testTypeTransparentPass(new CumulativeReductionEliminationPass(), body, types, functions);

        ReductionEliminationResource resource = ReductionEliminationResource.SUM;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testMean() {

        VariableType doubleType = getNumerics().newDouble();
        VariableType doubleRowType = DynamicMatrixType.newInstance(doubleType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("mean", Arrays.asList("y$ret"), Arrays.asList("A$1")));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", doubleRowType);
        types.put("y$ret", doubleType);
        TestUtils.testTypeTransparentPass(new CumulativeReductionEliminationPass(), body, types, functions);

        ReductionEliminationResource resource = ReductionEliminationResource.MEAN;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testMin3() {
        int dimAlong = 2;
        ReductionEliminationResource resource = ReductionEliminationResource.MIN3;

        performMinTest(dimAlong, resource);
    }

    @Test
    public void testMin3_2() {
        int dimAlong = 3;
        ReductionEliminationResource resource = ReductionEliminationResource.MIN3_2;

        performMinTest(dimAlong, resource);
    }

    private static void performMinTest(int dimAlong, ReductionEliminationResource resource) {

        VariableType doubleType = getNumerics().newDouble();
        VariableType doubleMatrixType = DynamicMatrixType.newInstance(doubleType, TypeShape.newDimsShape(3));
        VariableType emptyMatrixType = DynamicMatrixType.newInstance(doubleType, TypeShape.newEmpty());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("$empty", 1));
        block.addInstruction(new ArgumentInstruction("$dim$1", 2));
        block.addInstruction(
                new UntypedFunctionCallInstruction("min", Arrays.asList("y$ret", "z$ret"),
                        Arrays.asList("A$1", "$empty$1", "$dim$1")));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", doubleMatrixType);
        types.put("$empty$1", emptyMatrixType);
        types.put("$dim$1", getNumerics().newInt(dimAlong));
        types.put("y$ret", doubleMatrixType);
        types.put("z$ret", doubleMatrixType);
        TestUtils.testTypeTransparentPass(new MinMax3ReductionEliminationPass(), body, types, functions);

        String bodyContent = body.toString();
        StringBuilder obtained = new StringBuilder();
        obtained.append(bodyContent);
        obtained.append("Types:\n");
        for (String name : types.keySet()) {
            obtained.append("\t");
            obtained.append(name);
            obtained.append(": ");
            obtained.append(types.get(name));
            obtained.append("\n");
        }

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained.toString()));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("min", MathFunction.MIN.getMatlabFunction());
        functions.put("plus", MatlabOp.Addition.getMatlabFunction());
        functions.put("eq", MatlabOp.Equal.getMatlabFunction());
        functions.put("lt", MatlabOp.LessThan.getMatlabFunction());
        functions.put("times", COperator.Multiplication.getProvider());
        functions.put("rdivide", MatlabOp.RightDivision.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        return functions;
    }
}
