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

package org.specs.matisselib.tests.typeinference;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.rules.MatrixSetInstructionRule;

public class MatrixSetInferenceTests extends TestSkeleton {
    @Test
    public void testAcceptMatrixSet() {
        Assert.assertTrue(new MatrixSetInstructionRule()
                .accepts(new MatrixSetInstruction("$1", "$2", Arrays.asList("$3"), "$4")));
    }

    @Test
    public void testRejectEnd() {
        Assert.assertFalse(new MatrixSetInstructionRule()
                .accepts(new EndInstruction("out", "$in", 0, 1)));
    }

    @Test
    public void testInferGrowth() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        MatrixSetInstruction instruction = new MatrixSetInstruction("$out", "$in",
                Arrays.asList("$in1", "$in2", "$in3"), "$value");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        dummyContext.addVariable("$in1", getNumerics().newInt());
        dummyContext.addVariable("$in2", getNumerics().newInt());
        dummyContext.addVariable("$in3", getNumerics().newInt());
        dummyContext.addVariable("$value", getNumerics().newInt());
        new MatrixSetInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(3)),
                dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void testInferNoDimGrowth() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        MatrixSetInstruction instruction = new MatrixSetInstruction("$out", "$in",
                Arrays.asList("$in1", "$in2", "$in3"), "$value");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        dummyContext.addVariable("$in1", getNumerics().newInt());
        dummyContext.addVariable("$in2", getNumerics().newInt());
        dummyContext.addVariable("$in3", getNumerics().newInt(1));
        dummyContext.addVariable("$value", getNumerics().newInt());
        new MatrixSetInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)),
                dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void testInferRangeGrowth() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        MatrixSetInstruction instruction = new MatrixSetInstruction("$out", "$in",
                Arrays.asList("$in1", "$in2", "$in3"), "$value");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        dummyContext.addVariable("$in1", DynamicMatrixType.newInstance(getNumerics().newInt()));
        dummyContext.addVariable("$in2", getNumerics().newInt());
        dummyContext.addVariable("$in3", getNumerics().newInt());
        dummyContext.addVariable("$value", getNumerics().newInt());
        new MatrixSetInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(3)),
                dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void testInferRangeNoGrowth() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        MatrixSetInstruction instruction = new MatrixSetInstruction("$out", "$in",
                Arrays.asList("$in1", "$in2", "$in3"), "$value");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        dummyContext.addVariable("$in1", DynamicMatrixType.newInstance(getNumerics().newInt()));
        dummyContext.addVariable("$in2", getNumerics().newInt());
        dummyContext.addVariable("$in3", getNumerics().newInt(1));
        dummyContext.addVariable("$value", getNumerics().newInt());
        new MatrixSetInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)),
                dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void testUndefinedSet() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        MatrixSetInstruction instruction = new MatrixSetInstruction("$out", "$in",
                Arrays.asList("$in1", "$in2"), "$value");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in1", getNumerics().newInt());
        dummyContext.addVariable("$in2", getNumerics().newInt());
        dummyContext.addVariable("$value", getNumerics().newInt());
        new MatrixSetInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)),
                dummyContext.variableTypes.get("$out"));
    }
}
