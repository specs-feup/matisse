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

package org.specs.matlabtocl.v2.tests.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.services.scalarbuilderinfo.Z3ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.MatrixConstructionReductionValidator;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;

import pt.up.fe.specs.util.SpecsIo;

public class MatrixConstructionReductionValidatorTests {
    private static final ScalarValueInformationBuilderService scalarInfoBuilder = new Z3ScalarValueInformationBuilderService();

    @Test
    public void testAcceptBasicSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1"), "$value$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$iter$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.MATRIX_SET),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 1),
                        Collections.emptyList(),
                        scalarInfoBuilder,
                        types));
    }

    private Optional<ReductionType> verifyReduction(MatrixConstructionReductionValidator validator,
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
    public void testAcceptSetWithPreviousGet() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleGetInstruction("$result$1", "$2", Arrays.asList("$iter$1")));
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1"), "$result$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$iter$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.MATRIX_SET),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 2),
                        buildFlatLocations(body, 1),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testRejectGetSetWithDifferentIndexCount() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleGetInstruction("$result$1", "$2", Arrays.asList("$iter$1")));
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1", "$iter$1"), "$result$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$iter$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 2),
                        buildFlatLocations(body, 1),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testRejectSetWithGetCollision() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleGetInstruction("$result$1", "$2", Arrays.asList("$random$1")));
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1"), "$result$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$iter$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 2),
                        buildFlatLocations(body, 1),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testRejectAliasedSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$var$1"), "$value$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$one$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 1),
                        Collections.emptyList(),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testRejectMultipleSet() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1"), "$value$1"));
        block.addAssignment("$one$1", 1);
        block.addInstruction(new UntypedFunctionCallInstruction("plus", Arrays.asList("$prev_iter$1"),
                Arrays.asList("$iter$1", "$one$1")));
        block.addInstruction(new SimpleSetInstruction("$4", "$3", Arrays.asList("$prev_iter$1"), "$value$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType int1Type = providerData.getNumerics().newInt(1);
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixType);
        types.put("$2", matrixType);
        types.put("$3", matrixType);
        types.put("$4", matrixType);
        types.put("$iter$1", intType);
        types.put("$prev_iter$1", intType);
        types.put("$one$1", int1Type);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3", "$4"),
                        buildFlatLocations(body, 1, 4),
                        Collections.emptyList(),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testRejectCastSet() {
        System.out.println(SpecsIo.getWorkingDir().getAbsolutePath());

        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$iter$1"), "$value$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixIntType = DynamicMatrixType.newInstance(intType);
        MatrixType matrixDoubleType = DynamicMatrixType.newInstance(doubleType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixIntType);
        types.put("$2", matrixIntType);
        types.put("$3", matrixDoubleType);
        types.put("$4", matrixIntType);
        types.put("$iter$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.empty(),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$2", "$3"),
                        buildFlatLocations(body, 1),
                        Collections.emptyList(),
                        scalarInfoBuilder,
                        types));
    }

    @Test
    public void testAccept() {
        FunctionBody body = new FunctionBody();
        SsaBlock outerBlock = new SsaBlock();
        outerBlock.addAssignment("$out$1", 1);
        outerBlock.addInstruction(new ForInstruction("$one$1", "$one$1", "$value$1", 1, 2));
        body.addBlock(outerBlock);
        SsaBlock block = new SsaBlock();
        block.addInstruction(new SimpleSetInstruction("$3", "$2", Arrays.asList("$out$1", "$iter$1"), "$value$1"));
        body.addBlock(block);
        body.addBlock(new SsaBlock());

        ProviderData providerData = ProviderData.newInstance("test-instance");
        VariableType intType = providerData.getNumerics().newInt();
        VariableType doubleType = providerData.getNumerics().newDouble();
        MatrixType matrixIntType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$1", matrixIntType);
        types.put("$2", matrixIntType);
        types.put("$3", matrixIntType);
        types.put("$4", matrixIntType);
        types.put("$iter$1", intType);
        types.put("$out$1", intType);
        types.put("$value$1", doubleType);

        MatrixConstructionReductionValidator validator = new MatrixConstructionReductionValidator();
        Assert.assertEquals(Optional.of(ReductionType.MATRIX_SET),
                verifyReduction(validator,
                        body,
                        0,
                        Arrays.asList("$iter$1"),
                        Arrays.asList("$1", "$3"),
                        buildFlatLocations(body, 2),
                        Collections.emptyList(),
                        scalarInfoBuilder,
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
}
