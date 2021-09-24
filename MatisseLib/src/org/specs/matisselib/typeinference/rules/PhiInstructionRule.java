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

import org.specs.CIR.CirKeys;
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
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeCombiner;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class PhiInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof PhiInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        PhiInstruction phi = (PhiInstruction) instruction;
        String outputName = phi.getOutput();

        InstructionReportingService reportService = context.getInstructionReportService();

        int sourceBlock = context.getSourceBlock();
        if (sourceBlock >= 0) {
            int index = phi.getSourceBlocks().indexOf(sourceBlock);

            assert index >= 0 : "No source block #" + sourceBlock + " at " + phi + "\n"
                    + context.getInstance().getFunctionBody();
            String variable = phi.getInputVariables().get(index);

            context.getVariableType(variable).ifPresent(type -> {
                context.addVariable(outputName, type);
            });
            return;
        }

        List<VariableType> inputTypes = instruction.getInputVariables()
                .stream()
                .map(context::getVariableType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Optional<VariableType> currentCandidate = Optional.empty();
        for (VariableType inputType : inputTypes) {
            if (!currentCandidate.isPresent()) {
                currentCandidate = Optional.of(inputType);
            } else {
                // Merge types.

                VariableType type1 = currentCandidate.get();

                currentCandidate = Optional.of(
                        mergeTypes(context.getProviderData().getSettings().get(CirKeys.DEFAULT_REAL), type1, inputType,
                                phi.toString()).orElseThrow(
                                        () -> reportService.emitError(context.getInstance(), instruction,
                                                PassMessage.TYPE_INFERENCE_FAILURE,
                                                "Could not infer type of " + instruction)));
            }
        }

        if (!currentCandidate.isPresent()) {
            System.out.println("Did not infer types.");
            System.out.println(inputTypes);
        }
        currentCandidate.ifPresent(mergedType -> {

            // FIXME: Use an iterative type inference method instead?

            VariableType type = mergedType;
            if (inputTypes.size() != instruction.getInputVariables().size()) {
                // There are variables of unknown type.
                // FIXME: We'll erase the constant information but we probably should also eliminate shape
                // information here.

                if (type instanceof ScalarType) {
                    type = ((ScalarType) type).scalar().setConstant(null);
                }
            }

            context.addVariable(outputName, type);
        });

    }

    private static Optional<VariableType> mergeTypes(VariableType defaultReal, VariableType type1, VariableType type2,
            String debugInfo) {
        if (type1 instanceof ScalarType && type2 instanceof ScalarType) {
            // Merge scalars
            return TypeCombiner.getCombinedVariableType(defaultReal, Arrays.asList(type1, type2));
        }
        if (type1 instanceof MatrixType && type2 instanceof MatrixType) {
            return mergeMatrixTypes(defaultReal, (MatrixType) type1, (MatrixType) type2, debugInfo);
        }

        throw new UnsupportedOperationException("Merge types " + type1 + " with " + type2);
    }

    private static Optional<VariableType> mergeMatrixTypes(VariableType defaultReal, MatrixType type1, MatrixType type2,
            String debugInfo) {
        ScalarType underlyingType1 = type1.matrix().getElementType();
        ScalarType underlyingType2 = type2.matrix().getElementType();

        Optional<VariableType> mergedType = mergeTypes(defaultReal, underlyingType1, underlyingType2, debugInfo);

        if (!mergedType.isPresent()) {
            throw new UnsupportedOperationException(
                    "Merge matrix of " + underlyingType1 + " with matrix of " + underlyingType2 + ": " + debugInfo);
        }

        TypeShape typeShape1 = type1.getTypeShape();
        TypeShape typeShape2 = type2.getTypeShape();

        int ndims1 = typeShape1.getRawNumDims();
        int ndims2 = typeShape2.getRawNumDims();

        if (ndims1 != ndims2) {
            return Optional.of(DynamicMatrixType.newInstance(mergedType.get()));
        }

        if (typeShape1.getDims().equals(typeShape2.getDims())) {
            // Dims are the same.
            if (type1 instanceof StaticMatrixType && type2 instanceof StaticMatrixType) {
                return Optional.of(StaticMatrixTypeBuilder
                        .fromElementTypeAndShape((ScalarType) mergedType.get(), typeShape1)
                        .build());
            }
        }

        return Optional.of(DynamicMatrixType.newInstance(mergedType.get(), typeShape1.combineWith(typeShape2)));
    }

}
