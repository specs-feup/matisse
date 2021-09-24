/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MatlabOpsV2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.Blas.BlasMult;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;

/**
 * @author Joao Bispo
 *
 */
public class MatrixMul {

    // static private final Integer BLAS_THRESHOLD = 256;
    // static private final Integer BLAS_THRESHOLD = -1;

    private static final MatisseChecker CHECKER = new MatisseChecker().
    // Check if it has two inputs
            numOfInputs(2).
            // Check if they are matrices
            areMatrices();

    /**
     * Receives two matrices.
     * 
     * @return
     */
    public static InstanceProvider newBlasEnabledProvider() {

        InstanceProvider provider = new InstanceProvider() {
            @Override
            public Optional<InstanceProvider> accepts(ProviderData data) {
                if (useBlas(data)) {
                    return Optional.of(BlasMult.getProvider());
                }
                return Optional.of(MatrixMul.newNaiveProvider());
            }

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                throw new UnsupportedOperationException("Calling newInstance directly. Use accepts instead");
            }
        };

        return new GenericInstanceProvider(MatrixMul.CHECKER, provider);

    }

    public static InstanceProvider newNaiveProvider() {
        return new InstanceProvider() {
            @Override
            public FunctionInstance newCInstance(ProviderData data) {

                // Set output type if matrix1 and matrix2 have defined dimensions
                MatrixType outputMatrix = getOutputType(data);

                // Set output type
                if (outputMatrix != null) {
                    data.setOutputType(outputMatrix);
                }

                return MFileProvider.getInstance(OpsResource.MATRIX_MULV3, data);
            }

            @Override
            public FunctionType getType(ProviderData data) {

                MatrixType outputMatrix = getOutputType(data);

                // Set output type
                if (outputMatrix != null) {
                    data.setOutputType(outputMatrix);
                }

                return MFileProvider.getFunctionType(OpsResource.MATRIX_MULV3, data);
            }
        };
    }

    private static MatrixType getOutputType(ProviderData data) {
        // Check if dim 1 is defined
        MatrixType matrix1 = data.getInputType(MatrixType.class, 0);
        if (matrix1.getTypeShape().getNumDims() < 1) {
            return null;
        }

        Integer dim1 = matrix1.getTypeShape().getDim(0);
        if (dim1 < 1) {
            return null;
        }

        MatrixType matrix2 = data.getInputType(MatrixType.class, 1);
        if (matrix2.getTypeShape().getNumDims() < 2) {
            return null;
        }

        Integer dim2 = matrix2.getTypeShape().getDim(1);
        if (dim2 < 1) {
            return null;
        }

        // Select the output matrix type and copy it
        MatrixType outputMatrix = getOutputMatrix(Arrays.asList(matrix1, matrix2)).copy();

        // Remove constant information
        outputMatrix.matrix().getElementType().scalar().setConstantString(null);

        // Build and set shape

        outputMatrix = outputMatrix.matrix().setShape(TypeShape.newInstance(dim1, dim2));
        return outputMatrix;
    }

    /**
     * Gives priority to matrices which do no use dynamically allocated memory. Returns the type of first matrix that
     * does not use dynamic allocation. Otherwise, just returns the first matrix type.
     * 
     * @param asList
     * @return
     */
    private static MatrixType getOutputMatrix(List<MatrixType> matrixTypes) {
        for (MatrixType type : matrixTypes) {
            if (!type.matrix().usesDynamicAllocation()) {
                return type;
            }
        }

        // No type found that uses static allocation, return first type
        return matrixTypes.get(0);
    }

    private static boolean useBlas(ProviderData data) {

        if (!MatlabToCUtils.isActive(data.getSettings(), MatisseOptimization.UseBlas)) {
            return false;
        }

        // Get both matrices
        MatrixType m1 = data.getInputType(MatrixType.class, 0);
        MatrixType m2 = data.getInputType(MatrixType.class, 1);
        // Check if matrix elements implement CNative
        if (!(m1.matrix().getElementType() instanceof CNativeType)) {
            return false;
        }

        if (!(m2.matrix().getElementType() instanceof CNativeType)) {
            return false;
        }

        // If numDims is more than two, do not use BLAS
        TypeShape m1Shape = m1.matrix().getShape();
        TypeShape m2Shape = m2.matrix().getShape();
        if (m1Shape.getNumDims() != -1 && m1Shape.getNumDims() > 2) {
            return false;
        }

        if (m2Shape.getNumDims() != -1 && m2Shape.getNumDims() > 2) {
            return false;
        }

        // If both matrices are fully defined, check the maximum number of elements
        if (m1Shape.isFullyDefined() && m2Shape.isFullyDefined()) {
            int maxElements = Math.max(m1Shape.getNumElements(), m2Shape.getNumElements());
            int blasThreshold = data.getSettings().get(MatlabToCKeys.BLAS_THRESHOLD);
            if (maxElements < blasThreshold) {
                return false;
            }
        }
        // LoggingUtils.msgInfo("TEMP: Not checking if matrix has 2 dimensions, due to example 'redes'.");
        /*
        // Matrices must have 2 dimensions
        if (m1.getMatrixShape().getNumDims() != 2) {
            return false;
        }
        System.out.println("2");
        if (m2.getMatrixShape().getNumDims() != 2) {
            return false;
        }
        */
        CNativeType element1 = (CNativeType) m1.matrix().getElementType();
        CNativeType element2 = (CNativeType) m2.matrix().getElementType();

        // Check if element types are the same
        if (!element1.equals(element2)) {
            return false;
        }

        // Check if they are float (they are the same type, one check is enough)
        if (element1.scalar().isInteger()) {
            return false;
        }

        // Get base type (float or double)
        /*
        Optional<Boolean> isDouble = isDouble(element1);
        if (!isDouble.isPresent()) {
            return false;
        }
        */

        return true;

    }

    public static InstanceProvider newRowTimesColumn() {
        Checker checker = new MatisseChecker()
                .areMatrices()
                .isKnownRowMatrix(0)
                .isKnownColumnMatrix(1);

        return new MatlabInstanceProviderHelper(checker, MathFunction.DOT.getMatlabFunction());
    }

    /**
     * 
     * @param element1
     * @return true if scalar uses C 'double', false is uses C 'float', and empty otherwise
     */
    /*
    private static Optional<Boolean> isDouble(CNativeType scalar) {
    if (scalar.cnative().getCType() == CTypeV2.FLOAT) {
        return Optional.of(false);
    }
    
    if (scalar.cnative().getCType() == CTypeV2.DOUBLE) {
        return Optional.of(true);
    }
    
    return Optional.empty();
    }
    */
}
