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

package org.specs.matisselib.helpers.sizeinfo;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.AssumeInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.RangeGetInstruction;
import org.specs.matisselib.ssa.instructions.RangeInstruction.FullRangeIndex;
import org.specs.matisselib.ssa.instructions.RangeInstruction.Index;
import org.specs.matisselib.ssa.instructions.RangeInstruction.NormalIndex;
import org.specs.matisselib.ssa.instructions.RangeInstruction.PartialRangeIndex;
import org.specs.matisselib.ssa.instructions.RangeSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;

import com.google.common.base.Preconditions;

public final class SizeGroupInformation implements Closeable {

    private static final boolean OVERRIDE_LOG = false;

    private static final class SizeCategory {
        private String numel;
        private final Map<Integer, String> dims = new HashMap<>();
        private final Map<Integer, String> dimsSince = new HashMap<>();

        SizeCategory() {
            this.numel = null;
        }

        SizeCategory(SizeCategory category) {
            this.numel = category.numel;
            this.dims.putAll(category.dims);
            this.dimsSince.putAll(category.dimsSince);
        }

        @Override
        public String toString() {
            return "[SizeCategory " + this.numel + ", " + this.dims + ", " + this.dimsSince + "]";
        }
    }

    private final ScalarValueInformation scalarInfo;
    private final List<SizeCategory> sizeCategories = new ArrayList<>();
    private final Map<String, Integer> sizes = new HashMap<>();
    private final Map<String, Integer> sizeMatrices = new HashMap<>();
    private final Function<String, Optional<VariableType>> typeGetter;
    private final Consumer<String> logger;
    private int currentMetaName = 0;

    public SizeGroupInformation(Function<String, Optional<VariableType>> typeGetter,
            ScalarValueInformationBuilderService scalarValueBuilder) {

        this(typeGetter, scalarValueBuilder, message -> {
        });
    }

    public SizeGroupInformation(Function<String, Optional<VariableType>> typeGetter,
            ScalarValueInformationBuilderService scalarValueBuilder,
            Consumer<String> logger) {

        this.logger = logger;
        this.typeGetter = typeGetter;
        this.scalarInfo = scalarValueBuilder.build(this.typeGetter);
    }

    public SizeGroupInformation(SizeGroupInformation size) {
        Preconditions.checkArgument(size != null);

        this.logger = size.logger;
        this.scalarInfo = size.scalarInfo.copy();
        for (SizeCategory category : size.sizeCategories) {
            this.sizeCategories.add(new SizeCategory(category));
        }
        this.sizes.putAll(size.sizes);
        this.currentMetaName = size.currentMetaName;
        this.typeGetter = size.typeGetter;
    }

    @Override
    public void close() {
        this.scalarInfo.close();
    }

    private String generateMetaName() {
        return "#" + (++this.currentMetaName);
    }

    public void buildMatrixFromNumel(String matrix, String numel) {
        Preconditions.checkArgument(matrix != null);
        Preconditions.checkArgument(numel != null);

        buildNumel(matrix, numel);
    }

    public void buildMatrixWithSameSize(String outMatrix, String inMatrix) {
        Preconditions.checkArgument(outMatrix != null);
        Preconditions.checkArgument(inMatrix != null);

        int sizeGroup = getSizeGroup(inMatrix);
        this.sizes.put(outMatrix, sizeGroup);
    }

    private int getSizeGroup(String matrix) {
        Integer group = this.sizes.get(matrix);
        if (group != null) {
            return group;
        }

        SizeCategory category = new SizeCategory();
        this.sizeCategories.add(category);

        int newGroupId = this.sizeCategories.size() - 1;
        this.sizes.put(matrix, newGroupId);

        return newGroupId;
    }

    public void buildNumel(String matrixInput, String numelOutput) {
        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);
        String numel = sizeCategory.numel;
        if (numel == null) {
            sizeCategory.numel = numelOutput;
        } else {
            this.scalarInfo.addAlias(numel, numelOutput);
        }
    }

    public void buildSize(String matrixInput, int index, String sizeOutput) {
        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);

        String dim = sizeCategory.dims.get(index);
        if (dim == null) {
            sizeCategory.dims.put(index, sizeOutput);
        } else {
            this.scalarInfo.addAlias(dim, sizeOutput);
        }
    }

    public void buildSizeSince(String matrixInput, int index, String sizeOutput) {
        if (index == 0) {
            buildNumel(matrixInput, sizeOutput);
            return;
        }

        TypeShape shape = this.typeGetter.apply(matrixInput)
                .filter(MatrixUtils::isMatrix)
                .map(type -> type.getTypeShape())
                .orElse(TypeShape.newUndefinedShape());
        if (shape.getRawNumDims() == index + 1) {
            buildSize(matrixInput, index, sizeOutput);
            return;
        }

        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);

        String dim = sizeCategory.dimsSince.get(index);
        if (dim == null) {
            sizeCategory.dimsSince.put(index, sizeOutput);
        } else {
            this.scalarInfo.addAlias(dim, sizeOutput);
        }
    }

    public void setSizeAtLeast(String matrixInput, int index, String minimum) {
        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);

        String dim = sizeCategory.dims.get(index);
        if (dim == null) {
            sizeCategory.dims.put(index, dim = generateMetaName());
        }

        this.scalarInfo.setAtLeast(dim, minimum);
    }

    public void setSizeSinceAtLeast(String matrixInput, int index, String minimum) {
        if (index == 0) {
            setNumelAtLeast(matrixInput, minimum);
            return;
        }

        TypeShape shape = this.typeGetter.apply(matrixInput)
                .filter(MatrixUtils::isMatrix)
                .map(type -> type.getTypeShape())
                .orElse(TypeShape.newUndefinedShape());
        if (shape.getRawNumDims() == index + 1) {
            setSizeAtLeast(matrixInput, index, minimum);
            return;
        }

        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);

        String dim = sizeCategory.dimsSince.get(index);
        if (dim == null) {
            sizeCategory.dimsSince.put(index, dim = generateMetaName());
        }

        this.scalarInfo.setAtLeast(dim, minimum);
    }

    public void setNumelAtLeast(String matrixInput, String minimum) {
        int sizeGroup = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);

        String numel = sizeCategory.numel;
        if (numel == null) {
            numel = sizeCategory.numel = generateMetaName();
        }

        this.scalarInfo.setAtLeast(numel, minimum);
    }

    public String getNumelResult(String matrixInput) {
        int sizeGroupId = getSizeGroup(matrixInput);
        SizeCategory sizeCategory = this.sizeCategories.get(sizeGroupId);

        String numel = sizeCategory.numel;
        if (numel == null) {
            String metaName = generateMetaName();
            sizeCategory.numel = metaName;
            return metaName;
        }
        return numel;
    }

    public void buildSizeShape(String matrixInput, String sizeOutput) {
        int sizeGroupId = getSizeGroup(matrixInput);
        this.sizeMatrices.put(sizeOutput, sizeGroupId);
    }

    public String getSizeResult(String matrixInput, int index) {
        String size = generateMetaName();
        buildSize(matrixInput, index, size);

        this.typeGetter.apply(matrixInput).ifPresent(type -> {
            if (MatrixUtils.isMatrix(type)) {
                TypeShape shape = MatrixUtils.getShape(type);
                int dim;
                if (shape.getRawNumDims() < 0) {
                    dim = -1;
                } else if (index >= shape.getRawNumDims()) {
                    dim = 1;
                } else {
                    dim = shape.getDim(index);
                }
                if (dim >= 0) {
                    this.scalarInfo.specifyConstant(size, (double) dim);
                }

            }

        });

        return size;
    }

    public String getSizeSinceResult(String matrixInput, int index) {
        if (index == 0) {
            return getNumelResult(matrixInput);
        }

        String size = generateMetaName();
        buildSizeSince(matrixInput, index, size);

        this.typeGetter.apply(matrixInput).ifPresent(type -> {
            if (MatrixUtils.isMatrix(type)) {
                TypeShape shape = MatrixUtils.getShape(type);
                int dim;
                if (index >= shape.getRawNumDims()) {
                    dim = 1;
                } else {
                    dim = shape.getDim(index);
                }
                if (dim >= 0) {
                    this.scalarInfo.specifyConstant(size, (double) dim);
                }

            }

        });

        return size;
    }

    public boolean areSameValue(String v1, String v2) {
        return this.scalarInfo.areSameValue(v1, v2);
    }

    public boolean areSameValue(String v1, int v2) {
        return this.scalarInfo.isKnownEqual(v1, v2);
    }

    public boolean areSameSize(String v1, String v2) {
        if (v1.equals(v2)) {
            return true;
        }

        int s1 = getSizeGroup(v1);
        int s2 = getSizeGroup(v2);

        if (s1 == s2) {
            return true;
        }

        int ndims1 = getMatrixNdims(v1);
        int ndims2 = getMatrixNdims(v2);

        if (ndims1 < 0 || ndims2 < 0) {
            log("Unknown number of dimensions for " + v1 + ", " + v2);
            return false;
        }

        int commonDims = Math.min(ndims1, ndims2);
        int largestDims = Math.max(ndims1, ndims2);

        SizeCategory category1 = this.sizeCategories.get(s1);
        SizeCategory category2 = this.sizeCategories.get(s2);

        for (int i = 0; i < commonDims; ++i) {
            String dim1 = category1.dims.get(i);
            String dim2 = category2.dims.get(i);

            if (dim1 == null || dim2 == null) {
                log("Could not prove that " + v1 + " and " + v2
                        + " have same size. Missing information about dimension " + i + " (zero-based).");
                log(dim1 + ", " + dim2);
                return false;
            }
            if (!areSameValue(dim1, dim2)) {
                log(
                        "Could not prove that " + v1 + " and " + v2 + " have same size: Dimension values differ.");
                log(dim1 + " vs " + dim2);
                return false;
            }
        }
        if (commonDims != largestDims) {
            if (ndims1 == largestDims) {
                return matrixIsFlatSince(v1, commonDims);
            }

            assert ndims2 == largestDims;
            return matrixIsFlatSince(v2, commonDims);
        }

        return true;
    }

    private boolean matrixIsFlatSince(String matrix, int startDim) {
        int group = getSizeGroup(matrix);
        SizeCategory category = this.sizeCategories.get(group);
        int ndims = ((MatrixType) this.typeGetter.apply(matrix).get())
                .getTypeShape()
                .getRawNumDims();
        for (int dim = startDim; dim < ndims; ++dim) {
            String dimSince = category.dimsSince.get(dim);
            if (dimSince != null && this.scalarInfo.isKnownEqual(dimSince, 1)) {
                return true;
            }
            String dimVar = category.dims.get(dim);
            if (dimVar == null) {
                log("Missing information about matrix " + matrix + ", dimension " + dim);
                return false;
            }
            if (!this.scalarInfo.isKnownEqual(dimVar, 1)) {
                log("Size mismatch: Could not prove that " + dimVar + " == 1");
                return false;
            }
        }

        return true;
    }

    private Integer getMatrixNdims(String varName) {
        return this.typeGetter.apply(varName)
                .filter(MatrixType.class::isInstance)
                .map(MatrixType.class::cast)
                .map(type -> type.getTypeShape().getRawNumDims())
                .orElse(-1);
    }

    public boolean haveSameNumel(String v1, String v2) {
        if (v1.equals(v2)) {
            return true;
        }

        int s1 = getSizeGroup(v1);
        int s2 = getSizeGroup(v2);

        if (s1 == s2) {
            return true;
        }

        String numel1 = this.sizeCategories.get(s1).numel;
        String numel2 = this.sizeCategories.get(s2).numel;
        if (numel1 != null && numel2 != null) {
            return this.scalarInfo.areSameValue(numel1, numel2);
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SizeGroupInformation:\n\t");
        builder.append(this.scalarInfo.toString().replace("\n", "\n\t"));
        for (int i = 0; i < this.sizeCategories.size(); ++i) {
            builder.append("\n\t");
            builder.append(i);
            builder.append(": ");
            builder.append(this.sizeCategories.get(i));
        }
        builder.append("\n\t");
        builder.append(this.sizes);
        builder.append("\n\t");
        builder.append(this.sizeMatrices);
        builder.append("]");
        return builder.toString();
    }

    public void addInstructionInformation(SsaInstruction instruction) {
        if (instruction instanceof AssumeInstruction) {
            AssumeInstruction assume = (AssumeInstruction) instruction;

            this.scalarInfo.setTrue(assume.getVariable());
            return;
        }
        if (instruction instanceof FunctionCallInstruction) {
            FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

            addFunctionCallInstructionInformation(this.typeGetter, functionCall);
            return;
        }
        if (instruction instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) instruction;

            String outputName = phi.getOutput();
            Optional<VariableType> candidateOutputType = this.typeGetter.apply(outputName);
            if (!candidateOutputType.isPresent()) {
                return;
            }

            VariableType outputType = candidateOutputType.get();
            if (MatrixUtils.isMatrix(outputType)) {
                // TODO: We can significantly improve size fusion for matrices.

                setFusedSize(outputName, phi.getInputVariables());
            } else if (ScalarUtils.isScalar(outputType)) {
                String numel = null;
                for (String input : phi.getInputVariables()) {
                    if (numel == null) {
                        numel = input;
                    } else if (!areSameValue(numel, input)) {
                        // Can't fuse them
                        return;
                    }
                }
                this.scalarInfo.buildScalarCopy(outputName, numel);
            }

            return;
        }
        if (instruction instanceof AssignmentInstruction) {
            AssignmentInstruction assignment = (AssignmentInstruction) instruction;

            String outputName = assignment.getOutput();

            Input input = assignment.getInput();
            if (input instanceof NumberInput) {
                double inputValue = ((NumberInput) input).getNumber();

                this.scalarInfo.specifyConstant(outputName, inputValue);

                return;
            }

            if (!(input instanceof VariableInput)) {
                return;
            }

            String inputName = ((VariableInput) input).getName();

            Optional<VariableType> candidateOutputType = this.typeGetter.apply(outputName);
            if (!candidateOutputType.isPresent()) {
                return;
            }
            VariableType outputType = candidateOutputType.get();

            if (MatrixUtils.isMatrix(outputType)) {
                buildMatrixWithSameSize(outputName, inputName);
            } else if (ScalarUtils.isScalar(outputType)) {
                this.scalarInfo.buildScalarCopy(outputName, inputName);
            }
        }

        if (instruction instanceof SimpleSetInstruction) {
            SimpleSetInstruction set = (SimpleSetInstruction) instruction;

            String out = set.getOutput();
            String in = set.getInputMatrix();

            buildMatrixWithSameSize(out, in);
            return;
        }

        if (instruction instanceof MatrixSetInstruction) {
            MatrixSetInstruction set = (MatrixSetInstruction) instruction;

            String out = set.getOutput();
            String in = set.getInputMatrix();

            List<String> indices = set.getIndices();

            if (inRangeOfMatrix(indices, in)) {
                buildMatrixWithSameSize(out, in);
            } else {
                log(indices + " not in range of " + in);
                logAll();
            }
            return;
        }

        if (instruction instanceof VerticalFlattenInstruction) {
            VerticalFlattenInstruction flatten = (VerticalFlattenInstruction) instruction;

            String out = flatten.getOutput();
            String in = flatten.getInput();

            int sizeGroup = getSizeGroup(out);
            SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);
            sizeCategory.numel = getNumelResult(in);
            sizeCategory.dims.put(0, sizeCategory.numel);

            return;
        }
        if (instruction instanceof EndInstruction) {
            EndInstruction end = (EndInstruction) instruction;

            int index = end.getIndex();
            int numIndices = end.getNumIndices();
            String inputVariable = end.getInputVariable();
            String output = end.getOutput();

            buildEnd(inputVariable, index, numIndices, output);
            return;
        }
        if (instruction instanceof RangeGetInstruction) {
            RangeGetInstruction get = (RangeGetInstruction) instruction;

            String matrix = get.getInputMatrix();
            String output = get.getOutput();
            Optional<VariableType> potentialType = this.typeGetter.apply(matrix);
            if (!potentialType.isPresent()) {
                return;
            }

            VariableType type = potentialType.get();
            if (!(type instanceof MatrixType)) {
                return;
            }

            MatrixType matrixType = (MatrixType) type;
            TypeShape shape = matrixType.getTypeShape();

            for (int i = 0; i < get.getIndices().size(); ++i) {
                Index index = get.getIndices().get(i);

                if (index instanceof FullRangeIndex) {
                    if (i < get.getIndices().size() - 1 || i + 1 == shape.getRawNumDims()) {
                        String sizeInRange = getSizeResult(matrix, i);
                        buildSize(output, i, sizeInRange);
                    } else {
                        String sizeInRange = getSizeSinceResult(matrix, i);
                        buildSizeSince(output, i, sizeInRange);
                    }
                } else if (index instanceof PartialRangeIndex) {
                    PartialRangeIndex range = (PartialRangeIndex) index;

                    String start = range.getStart();
                    String end = range.getEnd();

                    String size = generateMetaName();
                    this.scalarInfo.setRangeSize(size, start, end);
                    if (i < get.getIndices().size() - 1 || i + 1 == shape.getRawNumDims()) {
                        buildSize(output, i, size);
                    } else {
                        buildSizeSince(output, i, size);
                    }
                }
            }

            return;
        }
        if (instruction instanceof RangeSetInstruction) {
            RangeSetInstruction set = (RangeSetInstruction) instruction;

            String out = set.getOutput();
            String in = set.getInputMatrix();

            List<Index> indices = set.getIndices();
            for (int i = 0; i < indices.size(); i++) {
                Index index = indices.get(i);

                if (index instanceof FullRangeIndex) {
                    if (i == indices.size() - 1) {
                        buildSizeSince(out, i, getSizeSinceResult(in, i));
                    } else {
                        buildSize(out, i, getSizeResult(in, i));
                    }
                } else if (index instanceof NormalIndex) {
                    String indexScalar = ((NormalIndex) index).getIndex();
                    if (i == indices.size() - 1) {
                        String size = getSizeSinceResult(in, i);
                        if (this.scalarInfo.isKnownLessOrEqualTo(indexScalar, size)) {
                            buildSizeSince(out, i, size);
                        } else {
                            setSizeAtLeast(out, i, size);
                            setSizeAtLeast(out, i, indexScalar);
                        }
                    } else {
                        String size = getSizeResult(in, i);
                        if (this.scalarInfo.isKnownLessOrEqualTo(indexScalar, size)) {
                            buildSize(out, i, size);
                        } else {
                            setSizeSinceAtLeast(out, i, size);
                            setSizeSinceAtLeast(out, i, indexScalar);
                        }
                    }
                }
            }
        }
    }

    private void setFusedSize(String outputName, List<String> inputVariables) {
        boolean allSameSize = true;
        String baseSize = inputVariables.get(0);
        for (int i = 1; i < inputVariables.size(); ++i) {
            if (!areSameSize(baseSize, inputVariables.get(i))) {
                allSameSize = false;
                break;
            }
        }

        if (allSameSize) {
            buildMatrixWithSameSize(outputName, baseSize);
            return;
        }

        // TODO
    }

    private void buildEnd(String inputVariable, int index, int numIndices, String output) {
        if (index != numIndices - 1) {
            buildSize(inputVariable, index, output);
        } else {
            buildSizeSince(inputVariable, index, output);
        }
    }

    private void addFunctionCallInstructionInformation(Function<String, Optional<VariableType>> typeGetter,
            FunctionCallInstruction functionCall) {

        String functionName = functionCall.getFunctionName();

        List<String> inputs = functionCall.getInputVariables();
        List<String> outputs = functionCall.getOutputs();
        if (outputs.isEmpty()) {
            return;
        }

        if (functionName.equals("numel")) {
            buildNumel(functionCall.getInputVariables().get(0), outputs.get(0));
            return;
        }
        if (functionName.equals("size")) {
            if (inputs.size() == 2 && outputs.size() == 1) {
                String matrix = inputs.get(0);
                String dim = inputs.get(1);
                String output = outputs.get(0);

                Optional<Number> constant = typeGetter.apply(dim)
                        .filter(ScalarType.class::isInstance)
                        .map(ScalarType.class::cast)
                        .flatMap(type -> type.scalar().getNullableConstant());
                if (constant.isPresent()) {
                    Number num = constant.get();
                    if (num.intValue() == num.doubleValue()) {
                        int index = num.intValue() - 1;

                        buildSize(matrix, index, output);
                        return;
                    }
                }
            } else if (inputs.size() == 1 && outputs.size() > 1) {
                String matrix = inputs.get(0);

                for (int i = 0; i < outputs.size(); ++i) {
                    buildEnd(matrix, i, outputs.size(), outputs.get(i));
                }
                return;
            } else if (inputs.size() == 1 && outputs.size() == 1) {
                String matrix = inputs.get(0);
                String size = outputs.get(0);

                buildSizeShape(matrix, size);
                return;
            }
        }
        if (functionName.equals("matisse_new_array_from_matrix")) {
            buildMatrixWithSameSize(outputs.get(0), inputs.get(0));
            return;
        }
        if (functionName.equals("matisse_new_array")) {
            if (inputs.size() == 1 && outputs.size() == 1) {
                String input = inputs.get(0);
                String output = outputs.get(0);

                buildMatrixFromShapeMatrix(input, output);
                return;
            }
        }
        if (functionName.equals("zeros") || functionName.equals("ones")) {
            if (inputs.size() == 1 && MatrixUtils.isMatrix(typeGetter.apply(inputs.get(0)).orElse(null))) {
                String input = inputs.get(0);
                String output = outputs.get(0);

                buildMatrixFromShapeMatrix(input, output);
                return;
            }

            allocateMatrixFromScalarArguments(inputs, outputs);
            return;
        }
        if (functionName.equals("matisse_new_array_from_dims")) {
            allocateMatrixFromScalarArguments(inputs, outputs);

            return;
        }
        if (functionName.equals("colon")) {
            if (inputs.size() == 2 && outputs.size() == 1) {
                String start = inputs.get(0);
                String end = inputs.get(1);

                String output = outputs.get(0);
                String outputSize = generateMetaName();
                buildNumel(output, outputSize);
                buildSize(output, 1, outputSize);

                this.scalarInfo.setRangeSize(outputSize, start, end);
            }

            return;
        }

        if (functionCall instanceof TypedFunctionCallInstruction) {
            TypedFunctionCallInstruction typedCall = (TypedFunctionCallInstruction) functionCall;

            if (typedCall.getFunctionType().isElementWise() && typedCall.getOutputs().size() == 1) {
                List<String> matrixArguments = new ArrayList<>();
                for (String input : typedCall.getInputVariables()) {
                    Optional<VariableType> type = typeGetter.apply(input);
                    assert type.isPresent() : "Type of " + input + " unknown.";
                    if (type.get() instanceof MatrixType) {
                        matrixArguments.add(input);
                    }
                }

                if (matrixArguments.size() > 0) {
                    buildMatrixWithSameSize(typedCall.getOutputs().get(0), matrixArguments.get(0));
                    return;
                }
            }
        }

        if (functionCall.getInputVariables().stream()
                .allMatch(input -> ScalarUtils.isScalar(typeGetter.apply(input).orElse(null)))) {

            this.scalarInfo.addScalarFunctionCallInformation(functionCall);
        }
    }

    private void buildMatrixFromShapeMatrix(String input, String output) {
        Integer size = this.sizeMatrices.get(input);
        if (size != null) {
            this.sizes.put(output, size);
            logAll();
        }
        return;
    }

    private void allocateMatrixFromScalarArguments(List<String> inputs, List<String> outputs) {
        if (outputs.size() != 1) {
            return;
        }

        log("Matrix from scalars: " + outputs + ": " + inputs);

        List<String> nonOneInputs = new ArrayList<>();
        Double constant = 1.0;
        for (String input : inputs) {
            VariableType type = this.typeGetter.apply(input)
                    .orElseThrow(() -> new RuntimeException("Missing type of " + input));
            if (!isKnownOne(type)) {
                nonOneInputs.add(input);
            }
            if (constant != null && ScalarUtils.isScalar(type) && ScalarUtils.hasConstant(type)) {
                constant *= ScalarUtils.getConstant(type).doubleValue();
            } else {
                constant = null;
            }
        }

        String output = outputs.get(0);

        if (inputs.size() == 1) {
            String input = inputs.get(0);

            buildSize(output, 0, input);
            buildSize(output, 1, input);
        } else {
            if (nonOneInputs.size() == 1) {
                buildMatrixFromNumel(output, nonOneInputs.get(0));
            }

            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);

                buildSize(output, i, input);
            }
        }

        if (constant != null) {
            int sizeGroup = getSizeGroup(output);
            SizeCategory sizeCategory = this.sizeCategories.get(sizeGroup);
            if (sizeCategory.numel == null) {
                String metaName = generateMetaName();
                sizeCategory.numel = metaName;
            }

            this.scalarInfo.specifyConstant(sizeCategory.numel, constant);
        }
    }

    private static boolean isKnownOne(VariableType type) {
        return ScalarUtils.isScalar(type)
                && ScalarUtils.hasConstant(type)
                && ScalarUtils.getConstant(type).doubleValue() == 1;
    }

    public boolean inRangeOfMatrix(List<String> sizes, String matrix) {
        if (sizes.size() != 1) {
            TypeShape shape = this.typeGetter.apply(matrix)
                    .filter(MatrixType.class::isInstance)
                    .map(MatrixType.class::cast)
                    .map(mt -> mt.getTypeShape())
                    .orElse(TypeShape.newUndefinedShape());

            for (int index = 0; index < sizes.size(); ++index) {
                String dim = sizes.get(index);

                String sizeResult;
                if (shape.getRawNumDims() > 0 && index >= shape.getRawNumDims()) {
                    log("Handling excess dimensions.");
                    if (!this.scalarInfo.isKnownEqual(dim, 1)) {
                        return false;
                    }
                    continue;
                }

                if (index == sizes.size() - 1) {
                    sizeResult = getSizeSinceResult(matrix, index);
                } else {
                    sizeResult = getSizeResult(matrix, index);
                }

                if (!this.scalarInfo.isKnownLessOrEqualTo(dim, sizeResult)) {
                    log(dim + " not in range of " + sizeResult);
                    logAll();
                    return false;
                }
            }

            return true;
        }

        String size = sizes.get(0);
        String numelResult = getNumelResult(matrix);
        return this.scalarInfo.isKnownLessOrEqualTo(size, numelResult);
    }

    private void log(String message) {
        if (SizeGroupInformation.OVERRIDE_LOG) {
            System.out.print("[size_group_information] ");
            System.out.println(message);
            return;
        }
        this.logger.accept(message);
    }

    public void logAll() {
        log(toString());
    }

    public void setUpTo(String value, String maximum) {
        this.scalarInfo.setUpTo(value, maximum);
    }

    public boolean isSingleIteration(String start, String interval, String end) {
        return this.scalarInfo.isKnownNotEqual(interval, 0) &&
                this.scalarInfo.areSameValue(start, end);
    }

    public boolean isEmptyRange(String start, String interval, String end) {
        return this.scalarInfo.isKnownEqual(interval, 0) ||
                (this.scalarInfo.isKnownPositive(interval) && this.scalarInfo.isKnownGreaterThan(start, end)) ||
                (this.scalarInfo.isKnownNegative(interval) && this.scalarInfo.isKnownLessThan(start, end));
    }
}
