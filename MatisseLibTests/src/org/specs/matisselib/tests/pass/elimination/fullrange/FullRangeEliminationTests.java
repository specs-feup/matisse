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

package org.specs.matisselib.tests.pass.elimination.fullrange;

import static org.specs.matisselib.ssa.instructions.RangeInstruction.fullRange;
import static org.specs.matisselib.ssa.instructions.RangeInstruction.partialRange;
import static org.specs.matisselib.ssa.instructions.RangeInstruction.variable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.posttype.FullRangeEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.RangeGetInstruction;
import org.specs.matisselib.ssa.instructions.RangeSetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class FullRangeEliminationTests extends TestSkeleton {
    @Test
    public void testGet() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("$A", 0));
        block.addInstruction(new ArgumentInstruction("$B", 1));
        block.addInstruction(new ArgumentInstruction("$C", 2));
        block.addInstruction(new ArgumentInstruction("$x1", 3));
        block.addInstruction(new ArgumentInstruction("$x2", 4));

        RangeGetInstruction get = new RangeGetInstruction("$D", "$A",
                variable("$B"),
                variable("$C"),
                partialRange("$x1", "$x2"),
                fullRange());
        block.addComment(get.toString());
        block.addInstruction(get);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$A", intMatrixType);
        types.put("$B", intType);
        types.put("$C", intMatrixType);
        types.put("$D", intMatrixType);
        types.put("$x1", intType);
        types.put("$x2", intType);
        applyPass(body, types, functions);

        FullRangeEliminationResource resource = FullRangeEliminationResource.GET;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("$A", 0));
        block.addInstruction(new ArgumentInstruction("$B", 1));
        block.addInstruction(new ArgumentInstruction("$C", 2));
        block.addInstruction(new ArgumentInstruction("$value", 3));

        RangeSetInstruction set = new RangeSetInstruction("$D", "$A",
                Arrays.asList(variable("$B"),
                        variable("$C"),
                        fullRange(),
                        fullRange()),
                "$value");
        block.addComment(set.toString());
        block.addInstruction(set);

        NumericFactory numerics = MatlabToCOptionUtils.newDefaultNumerics();

        VariableType intType = numerics.newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$A", intMatrixType);
        types.put("$B", intType);
        types.put("$C", intMatrixType);
        types.put("$D", intMatrixType);
        types.put("$value", intMatrixType);
        applyPass(body, types, functions);

        FullRangeEliminationResource resource = FullRangeEliminationResource.SET;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));

        Assert.assertEquals("INT", types.get("$value_value$1").toString());
    }

    @Test
    public void testSimpleSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("$A", 0));
        block.addInstruction(new ArgumentInstruction("$B", 1));
        block.addAssignment("$1", 1);
        block.addInstruction(new EndInstruction("$2", "$A", 1, 3));
        block.addInstruction(new EndInstruction("$3", "$A", 2, 3));
        block.addInstruction(
                new UntypedFunctionCallInstruction("zeros", Arrays.asList("$value"), Arrays.asList("$1", "$2", "$3")));

        RangeSetInstruction set = new RangeSetInstruction("$D", "$A",
                Arrays.asList(variable("$B"),
                        fullRange(),
                        fullRange()),
                "$value");
        block.addComment(set.toString());
        block.addInstruction(set);

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newDimsShape(3));
        VariableType intMatrix1Type = DynamicMatrixType.newInstance(intType, TypeShape.newInstance(1, -1, -1));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$A", intMatrixType);
        types.put("$B", intType);
        types.put("$D", intMatrixType);
        types.put("$1", int1Type);
        types.put("$2", intType);
        types.put("$3", intType);
        types.put("$value", intMatrix1Type);

        applyPass(body, types, functions);

        FullRangeEliminationResource resource = FullRangeEliminationResource.FASTSET;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));

        Assert.assertEquals("INT", types.get("$value_value$1").toString());
    }

    @Test
    public void testScalarSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("$A", 0));
        block.addInstruction(new ArgumentInstruction("$B", 1));
        block.addInstruction(new ArgumentInstruction("$value", 2));
        block.addAssignment("$1", 1);
        block.addInstruction(new EndInstruction("$2", "$A", 1, 3));
        block.addInstruction(new EndInstruction("$3", "$A", 2, 3));

        RangeSetInstruction set = new RangeSetInstruction("$D", "$A",
                Arrays.asList(variable("$B"),
                        fullRange(),
                        fullRange()),
                "$value");
        block.addComment(set.toString());
        block.addInstruction(set);

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newDimsShape(3));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$A", intMatrixType);
        types.put("$B", intType);
        types.put("$D", intMatrixType);
        types.put("$1", int1Type);
        types.put("$2", intType);
        types.put("$3", intType);
        types.put("$value", intType);

        applyPass(body, types, functions);

        FullRangeEliminationResource resource = FullRangeEliminationResource.SETSCALAR;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("horzcat", MatlabBuiltin.HORZCAT.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("matisse_max_or_zero", MathFunction.MAX.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("plus", MatlabOp.Addition.getMatlabFunction());
        functions.put("minus", MatlabOp.Subtraction.getMatlabFunction());
        for (MatlabOp op : MatlabOp.values()) {
            functions.put(op.getName(), op.getMatlabFunction());
        }
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new FullRangeEliminationPass(), body, types, functions);
    }
}
