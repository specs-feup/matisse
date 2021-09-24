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

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.scalarbuilderinfo.Z3ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;

public class Z3ScalarSizeGroupInformationTests extends SizeGroupInformationTestBase {

    public Z3ScalarSizeGroupInformationTests() {
	super(new Z3ScalarValueInformationBuilderService());
    }

    @Test
    public void testMinus() {
	NumericFactory numerics = getNumerics();

	Map<String, VariableType> types = new HashMap<>();
	types.put("$one", numerics.newInt(1));
	types.put("$1", numerics.newInt());
	types.put("$2", numerics.newInt());
	types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt()));
	try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), this.scalarValueBuilder)) {
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$one", "$1")));
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("minus", Arrays.asList("$2"), Arrays.asList("$1", "$one")));

	    Assert.assertTrue(info.inRangeOfMatrix(Arrays.asList("$2"), "A$1"));
	}
    }

    @Test
    public void testMinDims() {
	NumericFactory numerics = getNumerics();

	Map<String, VariableType> types = new HashMap<>();
	types.put("$1", numerics.newInt());
	types.put("$one", numerics.newInt(1));
	types.put("$3", numerics.newInt(3));
	types.put("$three", numerics.newInt(3));
	types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(3)));
	try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), this.scalarValueBuilder)) {
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("ndims", Arrays.asList("$1"), Arrays.asList("A$1")));

	    Assert.assertFalse(info.areSameValue("$1", "$three"));
	}
    }

    @Test
    public void testMaxDims() {
	NumericFactory numerics = getNumerics();

	Map<String, VariableType> types = new HashMap<>();
	types.put("$1", numerics.newInt());
	types.put("$one", numerics.newInt(1));
	types.put("$3", numerics.newInt(3));
	types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(3)));
	try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), this.scalarValueBuilder)) {
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("ndims", Arrays.asList("$1"), Arrays.asList("A$1")));

	    Assert.assertTrue(info.isEmptyRange("$3", "$one", "$one"));
	}
    }

    @Test
    public void testDimsMismatch() {
	NumericFactory numerics = getNumerics();

	Map<String, VariableType> types = new HashMap<>();
	types.put("$1", numerics.newInt());
	types.put("$2", numerics.newInt());
	types.put("$3", numerics.newInt());
	types.put("A$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(3)));
	types.put("B$1", DynamicMatrixType.newInstance(numerics.newInt(), TypeShape.newDimsShape(2)));

	try (SizeGroupInformation info = new SizeGroupInformation(fromTypes(types), this.scalarValueBuilder)) {
	    info.addInstructionInformation(AssignmentInstruction.fromInteger("$3", 1));
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("zeros", Arrays.asList("A$1"), Arrays.asList("$1", "$2", "$3")));
	    info.addInstructionInformation(
		    new UntypedFunctionCallInstruction("zeros", Arrays.asList("B$1"), Arrays.asList("$1", "$2")));

	    Assert.assertTrue(info.areSameSize("A$1", "B$1"));
	}
    }

}
