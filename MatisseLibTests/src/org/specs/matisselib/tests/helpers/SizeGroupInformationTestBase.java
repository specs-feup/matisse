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

package org.specs.matisselib.tests.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;

public abstract class SizeGroupInformationTestBase extends TestSkeleton {
    protected final ScalarValueInformationBuilderService scalarValueBuilder;

    protected SizeGroupInformationTestBase(ScalarValueInformationBuilderService scalarValueBuilder) {
        this.scalarValueBuilder = scalarValueBuilder;
    }

    @Test
    public void testUnrelated() {
        Map<String, VariableType> types = new HashMap<>();
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {

            Assert.assertFalse(info.areSameValue("$1", "$2"));
        }
    }

    @Test
    public void testSameValue() {
        Map<String, VariableType> types = new HashMap<>();
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {

            Assert.assertTrue(info.areSameValue("$1", "$1"));
        }
    }

    @Test
    public void testAssignment() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(AssignmentInstruction.fromVariable("$2", "$1"));

            Assert.assertTrue(info.areSameValue("$1", "$2"));
        }
    }

    @Test
    public void testConstruction() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", numerics.newInt(1));
        types.put("$1", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt()));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$one", "$1")));

            Assert.assertTrue(info.areSameValue("$1", info.getNumelResult("A$1")));
        }
    }

    @Test
    public void testRange() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", numerics.newInt(1));
        types.put("$1", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt()));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$one", "$1")));

            Assert.assertTrue(info.inRangeOfMatrix(Arrays.asList("$1"), "A$1"));
        }
    }

    @Test
    public void testRange2D() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1", "$2")));

            Assert.assertTrue(info.inRangeOfMatrix(Arrays.asList("$1", "$2"), "A$1"));
        }
    }

    @Test
    public void testDeferred() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        types.put("A$2", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {

            info.addInstructionInformation(AssignmentInstruction.fromVariable("A$2", "A$1"));
            info.buildSize("A$1", 0, "$1");
            info.buildSize("A$2", 0, "$2");

            Assert.assertTrue(info.areSameValue("$1", "$2"));
        }
    }

    @Test
    public void testRange2DUndefinedShape() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt()));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1", "$2")));

            Assert.assertFalse(info.inRangeOfMatrix(Arrays.asList("$1", "$2"), "A$1"));
        }
    }

    @Test
    public void testSize() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        types.put("$3", numerics.newInt(1));
        types.put("$4", numerics.newInt());
        types.put("$5", numerics.newInt(2));
        types.put("$6", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1", "$2")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("size", Arrays.asList("$4"), Arrays.asList("A$1", "$3")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("size", Arrays.asList("$6"), Arrays.asList("A$1", "$5")));

            Assert.assertTrue(info.inRangeOfMatrix(Arrays.asList("$4", "$6"), "A$1"));
        }
    }

    @Test
    public void testSizeShape() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        types.put("A$2", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        types.put("size$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newRow()));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1", "$2")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("size", Arrays.asList("size$1"), Arrays.asList("A$1")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$2"), Arrays.asList("size$1")));

            Assert.assertTrue(info.areSameSize("A$1", "A$2"));
        }
    }

    @Test
    public void testZerosSingleArgument() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("$2", numerics.newInt(1));
        types.put("$3", numerics.newInt());
        types.put("$4", numerics.newInt(2));
        types.put("$5", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("size", Arrays.asList("$3"), Arrays.asList("A$1", "$2")));
            info.addInstructionInformation(
                    new UntypedFunctionCallInstruction("size", Arrays.asList("$5"), Arrays.asList("A$1", "$4")));

            Assert.assertTrue(info.areSameValue("$1", "$3"));
            Assert.assertTrue(info.areSameValue("$1", "$5"));
        }
    }

    @Test
    public void testNewInRange() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt());
        types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt()));
        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {

            Assert.assertFalse(info.inRangeOfMatrix(Arrays.asList("$1"), "A$1"));
        }
    }

    @Test
    public void testConstant() {
        NumericFactory numerics = getNumerics();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", numerics.newInt(1));
        types.put("$2", numerics.newInt(1));

        try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), scalarValueBuilder)) {

            Assert.assertTrue(info.areSameValue("$1", "$2"));
        }
    }

    // protected static NumericFactory getNumerics() {
    // return new NumericFactory(DataStore.newInstance("size-group-tests"));
    // }

    protected static Function<String, Optional<VariableType>> fromTypes(Map<String, VariableType> types) {
        return name -> Optional.ofNullable(types.get(name));
    }
}
