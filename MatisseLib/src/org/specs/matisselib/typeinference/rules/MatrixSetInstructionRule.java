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

package org.specs.matisselib.typeinference.rules;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.functionproperties.AssumeMatrixIndicesInRangeProperty;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixSetInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof MatrixSetInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        // Here's what can happen
        // 1. The normal case (Matrix replace scalar value at position)
        // 2. Multiple replacements (e.g. A(1:3) = [1 2 3])
        // 3. Insertion (e.g. A = 1; A(2) = 1;)
        // In insertion, the right side must be a scalar or a matrix with equal shape to the output.
        // As such, A(1:3) = [1 2]; is not valid.
        // Elimination (such as A(4) = []) is an entirely separate operation.

        MatrixSetInstruction set = (MatrixSetInstruction) instruction;
        String matrix = set.getInputMatrix();
        List<String> indices = set.getIndices();
        String value = set.getValue();
        String output = set.getOutput();

        Optional<VariableType> candidateMatrixType = context.getVariableType(matrix);
        Optional<VariableType> matrixOverrideType = context.getDefaultVariableType(output);

        if (!candidateMatrixType.isPresent()) {
            handleUndefinedSet(context, set, matrixOverrideType);

            return;
        }

        VariableType matrixType = candidateMatrixType.get();
        List<VariableType> indicesTypes = indices.stream()
                .map(index -> context.getVariableType(index).get())
                .collect(Collectors.toList());
        VariableType valueType = context.getVariableType(value).get();

        if (!(matrixType instanceof MatrixType)) {
            // FIXME: It is perfectly valid to convert a scalar to a matrix using a set.
            throw new NotImplementedException("Converting scalar to matrix by setting a value");
        }

        if (indicesTypes.stream().anyMatch(type -> !(type instanceof ScalarType))) {
            handleRangeSet(context, set, matrixOverrideType);

            return;
        }

        if (!(valueType instanceof ScalarType)) {
            // We are in the presence of A(scalars) = matrix;
            //
            // Not a scalar set.
            // These sets never change the size of the output matrix.
            context.addVariable(output, matrixType);
            return;
        }

        // Now, we can assume we have a scalar set.
        // We can have an insertion or a replacement.

        ScalarType underlyingType = MatrixUtils.getElementType(matrixType);
        TypeShape matrixShape = MatrixUtils.getShape(matrixType);

        if ((indices.size() == 1
                && !matrixShape.isKnownEmpty()
                && matrixShape.isFullyDefined()
                && matrixShape.getNumDims() <= 2
                && (matrixShape.getDim(0) == 1 || matrixShape.getDim(1) == 1)
                && ScalarUtils.isInteger(indicesTypes.get(0))
                && ScalarUtils.hasConstant(indicesTypes.get(0))
                && ScalarUtils.getConstant(indicesTypes.get(0)).intValue() <= matrixShape.getNumElements()) ||
                context.getPropertyStream(AssumeMatrixIndicesInRangeProperty.class).findAny().isPresent()) {

            // In range

            context.addVariable(output, matrixType, matrixOverrideType);
        } else {
            // Potentially out-of-range

            int lastRelevantIndex = getLastRelevantIndex(context, indices);

            // TODO: We can do better in determining the shape
            TypeShape newShape = TypeShape.newUndefinedShape();
            if (lastRelevantIndex == 0) {
                // Preserves num dims

                if (matrixShape.getRawNumDims() == 2 && matrixShape.getDim(0) == 1) {
                    newShape = TypeShape.newRow();
                } else if (matrixShape.isKnown1D()) {
                    newShape = TypeShape.new1D();
                } else if (matrixShape.getNumDims() >= 0) {
                    newShape = TypeShape.newDimsShape(matrixShape.getRawNumDims());
                }
            } else if (lastRelevantIndex < matrixShape.getRawNumDims()) {
                // Maybe out of range, but preserves num dims

                int numDims = -1;
                if (matrixShape.getRawNumDims() >= 0) {
                    numDims = matrixShape.getRawNumDims();
                }
                newShape = TypeShape.newDimsShape(numDims);
            } else if (matrixShape.getRawNumDims() > 0) {
                // Matrix may grow to the number of dimensions specified by the indices.

                newShape = TypeShape.newDimsShape(lastRelevantIndex + 1);
            }

            DynamicMatrixType newType = DynamicMatrixType.newInstance(underlyingType, newShape);
            context.addVariable(output, newType, matrixOverrideType);
        }
    }

    private static void handleUndefinedSet(TypeInferenceContext context,
            MatrixSetInstruction set,
            Optional<VariableType> matrixOverrideType) {

        List<String> indices = set.getIndices();
        String value = set.getValue();
        VariableType valueType = context.getVariableType(value).get();

        if (!ScalarUtils.isScalar(valueType)) {
            throw new NotImplementedException("Non-scalar matrix set of undefined matrix");
        }

        DynamicMatrixType suggestedType;
        if (indices.size() != 1) {
            TypeShape shape = indices.size() > 1 ? TypeShape.newDimsShape(indices.size())
                    : TypeShape.newUndefinedShape();
            suggestedType = DynamicMatrixType.newInstance(ScalarUtils.removeConstant(valueType),
                    shape);
        } else {

            String index = indices.get(0);
            VariableType indexType = context.getVariableType(index).get();

            int numRows;
            if (ScalarUtils.isScalar(indexType) && ScalarUtils.isInteger(indexType)
                    && ScalarUtils.hasConstant(indexType)) {
                numRows = ScalarUtils.getConstant(indexType).intValue();
                if (numRows <= 0) {
                    context.getPassData()
                            .get(TypeInferencePass.INSTRUCTION_REPORT_SERVICE)
                            .emitMessage(context.getInstance(), set, PassMessage.CORRECTNESS_ERROR,
                                    "Accessing non-positive index.");

                    numRows = -1;
                }
            } else {
                numRows = -1;
            }
            suggestedType = DynamicMatrixType.newInstance(
                    ScalarUtils.setConstant(valueType, null),
                    Arrays.asList(1, numRows));
        }

        context.addVariable(set.getOutput(), suggestedType, matrixOverrideType);
    }

    private static void handleRangeSet(TypeInferenceContext context,
            MatrixSetInstruction set,
            Optional<VariableType> matrixOverrideType) {

        String output = set.getOutput();

        // In order to determine whether a matrix set resizes a matrix, we need only look
        // at the left side of the assignment.

        String inputMatrix = set.getInputMatrix();
        MatrixType inputType = (MatrixType) context.getVariableType(inputMatrix).get();
        ScalarType elementType = inputType.matrix().getElementType();
        TypeShape inputShape = inputType.getTypeShape();

        List<String> indices = set.getIndices();
        if (indices.size() == 1) {
            String index = indices.get(0);
            VariableType indexType = context.getVariableType(index).get();
            if (inputType instanceof StaticMatrixType && indexType instanceof StaticMatrixType) {
                Number upperBound = ((StaticMatrixType) indexType).getUpperBound();
                if (inputShape.isFullyDefined() && upperBound != null
                        && upperBound.intValue() <= inputShape.getNumElements()) {
                    // Not resized

                    MatrixType outputType = StaticMatrixTypeBuilder
                            .fromElementTypeAndShape(elementType, inputShape)
                            .build();

                    context.addVariable(output, outputType, matrixOverrideType);
                    return;
                }
            }
        }

        int lastRelevantIndex = getLastRelevantIndex(context, indices);

        TypeShape newShape = TypeShape.newUndefinedShape();
        int sourceDims = inputShape.getRawNumDims();
        if (sourceDims > 0) {
            int rawNumDims = Math.max(sourceDims, lastRelevantIndex + 1);
            newShape = TypeShape.newDimsShape(rawNumDims);
        }
        DynamicMatrixType matrixType = DynamicMatrixType.newInstance(elementType, newShape);

        context.addVariable(output,
                matrixType,
                matrixOverrideType);
    }

    private static int getLastRelevantIndex(TypeInferenceContext context, List<String> indices) {
        int lastRelevantIndex = indices.size() - 1;
        while (lastRelevantIndex > 0 &&
                ConstantUtils.isConstantOne(context.getVariableType(indices.get(lastRelevantIndex)).get())) {
            --lastRelevantIndex;
        }
        return lastRelevantIndex;
    }
}
