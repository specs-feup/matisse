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

package org.specs.matlabtocl.v2.tests.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.SumReductionValidator;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;

public class SumReductionValidatorTests {
    @Test
    public void testAcceptScalarReduction() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$4"), Arrays.asList("$3", "$2")));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", doubleType);
        types.put("$3", doubleType);
        types.put("$4", doubleType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.SUM),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$3", "$4"),
                        buildFlatLocations(body, 0, 1),
                        Collections.emptyList(),
                        null,
                        types));
    }

    private Optional<ReductionType> verifyReduction(SumReductionValidator validator,
            FunctionBody body, int outerBlockId, List<String> iterVariables, List<String> reductionVariables,
            List<InstructionLocation> constructionInstructions, List<InstructionLocation> midUsageInstructions,
            ScalarValueInformationBuilderService scalarBuilderService,
            Map<String, VariableType> types) {

        ParallelRegionInstance parallelInstance = new ParallelRegionInstance(null, body, Collections.emptyList(),
                Collections.emptyList(), types);

        return validator.verifyReduction(parallelInstance, outerBlockId, Arrays.asList(outerBlockId), iterVariables,
                reductionVariables,
                constructionInstructions, midUsageInstructions, scalarBuilderService);
    }

    @Test
    public void testAcceptScalarMinus() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("minus", Arrays.asList("$4"), Arrays.asList("$3", "$2")));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", doubleType);
        types.put("$3", doubleType);
        types.put("$4", doubleType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.SUM),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$3", "$4"),
                        buildFlatLocations(body, 0, 1),
                        Collections.emptyList(),
                        null,
                        types));
    }

    @Test
    public void testRejectReverseScalarMinus() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("minus", Arrays.asList("$4"), Arrays.asList("$2", "$3")));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", doubleType);
        types.put("$3", doubleType);
        types.put("$4", doubleType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$3", "$4"),
                        buildFlatLocations(body, 0, 1),
                        Collections.emptyList(),
                        null,
                        types));
    }

    @Test
    public void testRejectScalarTimes() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(
                new UntypedFunctionCallInstruction("times", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$4"), Arrays.asList("$3", "$2")));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", doubleType);
        types.put("$3", doubleType);
        types.put("$4", doubleType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$3", "$4"),
                        buildFlatLocations(body, 0, 1),
                        Collections.emptyList(),
                        null,
                        types));
    }

    @Test
    public void testRejectMatrixReduction() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("plus", Arrays.asList("$4"), Arrays.asList("$3", "$2")));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType doubleMatrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleMatrixType);
        types.put("$2", doubleMatrixType);
        types.put("$3", doubleMatrixType);
        types.put("$4", doubleMatrixType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$3", "$4"),
                        buildFlatLocations(body, 0, 1),
                        Collections.emptyList(),
                        null,
                        types));
    }

    @Test
    public void testAcceptTrivialAssignment() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(AssignmentInstruction.fromVariable("$2", "$1"));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType doubleType = providerData.getNumerics().newDouble();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", doubleType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.SUM),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$2"),
                        buildFlatLocations(body, 0),
                        Collections.emptyList(),
                        null,
                        types));
    }

    @Test
    public void testRejectCastAssignment() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(AssignmentInstruction.fromVariable("$2", "$1"));
        body.addBlock(block);

        ProviderData providerData = ProviderData.newInstance("test-instance");
        NumericFactory numerics = providerData.getNumerics();
        VariableType doubleType = numerics.newDouble();
        VariableType floatType = numerics.newFloat();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", doubleType);
        types.put("$2", floatType);

        SumReductionValidator validator = new SumReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Collections.emptyList(),
                        Arrays.asList("$1", "$2"),
                        buildFlatLocations(body, 0),
                        Collections.emptyList(),
                        null,
                        types));
    }

    private static List<InstructionLocation> buildFlatLocations(FunctionBody body, int... indices) {
        List<InstructionLocation> locations = new ArrayList<>();

        int currentLocation = 0;
        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            SsaBlock block = body.getBlock(blockId);
            int numInstructions = block.getInstructions().size();

            for (int index : indices) {
                if (index >= currentLocation && index < currentLocation + numInstructions) {
                    locations.add(new InstructionLocation(blockId, index - currentLocation));
                }
            }

            currentLocation += numInstructions;
        }

        if (indices.length != locations.size()) {
            throw new RuntimeException();
        }
        return locations;
    }

    private static Function<String, Optional<VariableType>> makeTypeGetter(Map<String, VariableType> types) {
        return name -> Optional.ofNullable(types.get(name));
    }
}
