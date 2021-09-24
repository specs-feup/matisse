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

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.rules.EndInstructionRule;

public class EndInferenceTests extends TestSkeleton {
    @Test
    public void testAcceptEnd() {
        Assert.assertTrue(new EndInstructionRule().accepts(new EndInstruction("$1", "$2", 0, 1)));
    }

    @Test
    public void testRejectNonEnd() {
        Assert.assertFalse(new EndInstructionRule().accepts(new LineInstruction(3)));
    }

    @Test
    public void inferUndefinedShape() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 0, 1);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in", DynamicMatrixType.newInstance(getNumerics().newInt()));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(), dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void inferInRangeShape() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 0, 2);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newInstance(2, 3)));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(2), dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void inferOutOfRangeShape() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 3, 4);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newInstance(2, 3)));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(1), dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void inferCombinationShape() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 1, 2);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newInstance(2, 3, -1)));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(), dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void inferLastIndexShape() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 2, 3);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newInstance(2, 3, 4)));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(4), dummyContext.variableTypes.get("$out"));
    }

    @Test
    public void inferUndefinedDim() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        EndInstruction instruction = new EndInstruction("$out", "$in", 0, 2);
        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.addVariable("$in",
                DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newInstance(-1, -1)));
        new EndInstructionRule().inferTypes(dummyContext, new InstructionLocation(0, 0), instruction);

        TestUtils.assertStrictEquals(getNumerics().newInt(), dummyContext.variableTypes.get("$out"));
    }

}
