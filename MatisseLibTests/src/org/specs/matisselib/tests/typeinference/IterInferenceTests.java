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
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.rules.IterInstructionRule;

public class IterInferenceTests extends TestSkeleton {
    @Test
    public void testAcceptIter() {
        Assert.assertTrue(new IterInstructionRule().accepts(new IterInstruction("$out$1")));
    }

    @Test
    public void testRejectNonIter() {
        Assert.assertFalse(new IterInstructionRule().accepts(new LineInstruction(3)));
    }

    @Test
    public void testSimpleIter() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.loopStartName = "$step$1";
        dummyContext.loopIntervalName = "$step$1";
        dummyContext.variableTypes.put("$step$1", getNumerics().newInt());
        new IterInstructionRule().inferTypes(dummyContext, new InstructionLocation(1, 0),
                new IterInstruction("$out$1"));

        TestUtils.assertStrictEquals(getNumerics().newInt(), dummyContext.variableTypes.get("$out$1"));
    }

    @Test
    public void testDoubleStepIter() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.loopStartName = "$start$1";
        dummyContext.loopIntervalName = "$step$1";
        dummyContext.variableTypes.put("$start$1", getNumerics().newInt());
        dummyContext.variableTypes.put("$step$1", getNumerics().newDouble());
        new IterInstructionRule().inferTypes(dummyContext, new InstructionLocation(1, 0),
                new IterInstruction("$out$1"));

        TestUtils.assertStrictEquals(getNumerics().newDouble(), dummyContext.variableTypes.get("$out$1"));
    }

    @Test
    public void testDoubleStartIter() {
        ProviderData providerData = ProviderData.newInstance("end-tests");

        DummyTypeInferenceContext dummyContext = new DummyTypeInferenceContext(providerData);
        dummyContext.loopStartName = "$start$1";
        dummyContext.loopIntervalName = "$step$1";
        dummyContext.variableTypes.put("$start$1", getNumerics().newDouble());
        dummyContext.variableTypes.put("$step$1", getNumerics().newInt());
        new IterInstructionRule().inferTypes(dummyContext, new InstructionLocation(1, 0),
                new IterInstruction("$out$1"));

        TestUtils.assertStrictEquals(getNumerics().newDouble(), dummyContext.variableTypes.get("$out$1"));
    }
}
