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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixGetInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof MatrixGetInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        InstructionReportingService reportService = context.getInstructionReportService();

        MatrixGetInstruction get = (MatrixGetInstruction) instruction;
        List<String> inputs = get.getInputVariables();

        if (inputs.size() == 1) {
            // TODO: Check if this can happen for A().
            throw new UnsupportedOperationException(instruction.toString());
        }

        String matrixVariable = get.getInputMatrix();
        List<String> arguments = get.getIndices();

        VariableType matrixType = context.requireVariableType(matrixVariable);
        List<VariableType> argumentTypes = arguments.stream()
                .map(argument -> context.requireVariableType(argument))
                .collect(Collectors.toList());

        String outputVariable = get.getOutputs().get(0);

        Optional<VariableType> candidateType = context.getDefaultVariableType(outputVariable);

        if (!(matrixType instanceof MatrixType)) {
            assert matrixType instanceof ScalarType;

            // In A = 1; A(X), X must be contain only values 1.
            // The type of the result will depend on X.
            // A(1) will return the type of A, A([1 1]) will return a row matrix, etc.
            if (argumentTypes.size() == 1 && argumentTypes.get(0) instanceof ScalarType) {
                context.addVariable(outputVariable, matrixType, candidateType);
                return;
            }

            throw new NotImplementedException("Matrix get for type: " + matrixType + ", in " + get);
        }

        // A is a matrix

        MatrixType actualMatrixType = (MatrixType) matrixType;
        ScalarType underlyingType = actualMatrixType.matrix().getElementType();

        int numDims = actualMatrixType.getTypeShape().getRawNumDims();
        if (numDims != -1 && arguments.size() > numDims) {
            String baseMessage = "Accessing matrix with " + arguments.size() + " indices, but matrix has only "
                    + numDims
                    + " dimensions. ";

            List<Integer> errorDimensions = new ArrayList<>();
            boolean additionalIndexesAreAllOne = true;

            // Check every additional supplied dimension
            for (int i = numDims; i < arguments.size(); ++i) {
                VariableType argumentType = argumentTypes.get(i);
                if (argumentType instanceof ScalarType) {
                    Number constant = ((ScalarType) argumentType).scalar().getConstant();
                    if (constant != null) {
                        if (!isConstantOne(constant)) {
                            errorDimensions.add(i);
                        }
                    } else {
                        additionalIndexesAreAllOne = false;
                    }
                }
            }

            if (errorDimensions.size() == 0) {
                if (!additionalIndexesAreAllOne) {
                    String message = baseMessage + "Additional dimensions must necessarily be equal to 1.";
                    reportService.emitMessage(context.getInstance(), instruction, PassMessage.SUSPICIOUS_CASE, message);
                }
            } else {
                String message = baseMessage + "Indices " + errorDimensions + " are known to be different from 1.";
                throw reportService.emitError(context.getInstance(), instruction, PassMessage.CORRECTNESS_ERROR,
                        message);
            }
        }

        if (!argumentTypes.stream().allMatch(type -> type instanceof ScalarType)) {
            MatrixType resultType = handleMultipleGet(argumentTypes, actualMatrixType, underlyingType);

            context.addVariable(outputVariable, resultType, candidateType);

            return;
        }

        context.addVariable(outputVariable, underlyingType, candidateType);
    }

    private static MatrixType handleMultipleGet(List<VariableType> argumentTypes,
            MatrixType matrixType,
            ScalarType underlyingType) {

        if (argumentTypes.size() == 1) {

            VariableType argumentType = argumentTypes.get(0);

            TypeShape shape = TypeShape.newUndefinedShape();
            if (argumentType instanceof MatrixType) {

                MatrixType matrixArgumentType = (MatrixType) argumentType;

                TypeShape matrixShape = matrixType.getTypeShape();
                TypeShape argumentShape = matrixArgumentType.getTypeShape();

                // Combining rows/columns as "known-1d" is difficult
                // as "knownRow"/"knownColumn" may actually be scalars
                if (matrixShape.isKnownRow() && argumentShape.isKnownRow()) {
                    shape = TypeShape.newRow();
                } else if (matrixShape.isKnownColumn() && argumentShape.isKnownColumn()) {
                    shape = TypeShape.newColumn();
                } else if (argumentShape.isKnown1D()) {
                    shape = TypeShape.new1D();
                }
            }

            // TODO: Improve inference: Better shape detection, properly allocate static matrices.

            return DynamicMatrixType.newInstance(underlyingType, shape);
        }

        List<Integer> dimList = new ArrayList<>();

        for (VariableType argumentType : argumentTypes) {
            if (argumentType instanceof ScalarType) {
                dimList.add(1);
            } else if (argumentType instanceof MatrixType) {
                MatrixType argumentMatrixType = (MatrixType) argumentType;
                TypeShape argumentShape = argumentMatrixType.matrix().getShape();
                if (!argumentShape.isFullyDefined()) {
                    dimList.add(null);
                } else {
                    dimList.add(argumentShape.getNumElements());
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }

        TypeShape shape = TypeShape.newInstance(dimList);
        if (shape.isFullyDefined() && matrixType instanceof StaticMatrixType) {
            return StaticMatrixTypeBuilder
                    .fromElementTypeAndShape(underlyingType, shape)
                    .build();
        }

        return DynamicMatrixType.newInstance(underlyingType, shape);
    }

    private static boolean isConstantOne(Number constant) {
        if (constant instanceof Float) {
            return ((Float) constant) == 1;
        }
        if (constant instanceof Double) {
            return ((Double) constant) == 1;
        }
        if (constant instanceof Integer) {
            return ((Integer) constant) == 1;
        }
        throw new UnsupportedOperationException();
    }

}
