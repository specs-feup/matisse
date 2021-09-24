/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.Functions.BaseFunctions.Dynamic.ArrayAllocBuilders;
import org.specs.MatlabToC.Functions.BaseFunctions.Dynamic.ArrayAllocBuilders.RowCol;
import org.specs.MatlabToC.Functions.BaseFunctions.General.DynamicMatrixBuilderUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.General.EmptyAllocProvider;
import org.specs.MatlabToC.Functions.BaseFunctions.General.EmptyConcat;
import org.specs.MatlabToC.Functions.BaseFunctions.General.Find;
import org.specs.MatlabToC.Functions.BaseFunctions.General.Flip;
import org.specs.MatlabToC.Functions.BaseFunctions.General.HorzcatCols;
import org.specs.MatlabToC.Functions.BaseFunctions.General.IsEmpty;
import org.specs.MatlabToC.Functions.BaseFunctions.General.Size;
import org.specs.MatlabToC.Functions.BaseFunctions.General.SortBuilder;
import org.specs.MatlabToC.Functions.BaseFunctions.General.SpecializedPadArrayProvider;
import org.specs.MatlabToC.Functions.BaseFunctions.General.Sub2ind;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ArrayDecBuilders;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayAllocBuilder;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecBuilder;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.EyeDecBuilder;
import org.specs.MatlabToC.Functions.Builtin.BuiltinCellBuilders;
import org.specs.MatlabToC.Functions.Builtin.BuiltinMatrixBuilders;
import org.specs.MatlabToC.Functions.Builtin.BuiltinScalarBuilders;
import org.specs.MatlabToC.Functions.Builtin.RandSimple;
import org.specs.MatlabToC.Functions.Cell.DynamicCellConstructor;
import org.specs.MatlabToC.Functions.MathFunctions.Dynamic.GeneralAllocBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.General.GeneralBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.Static.GeneralDecBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.Static.LinspaceDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.NDimsBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseBuilder;
import org.specs.MatlabToC.Functions.Misc.ClassBuilder;
import org.specs.MatlabToC.Functions.StringFunctions.Str2DoubleBuilder;
import org.specs.MatlabToC.Functions.StringFunctions.StrcmpBuilder;
import org.specs.MatlabToC.Functions.StringFunctions.StrsplitConstantCharBuilder;
import org.specs.MatlabToC.Functions.Strings.MatlabStringFromCStringConstructor;
import org.specs.MatlabToC.Functions.Strings.MatlabStringFromCharMatrixConstructor;
import org.specs.MatlabToC.Functions.Strings.MatlabStringLength;
import org.specs.MatlabToC.Functions.Strings.MatlabStringZeroes;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.specs.matisselib.functions.dynamiccell.CellNumDims;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public enum MatlabBuiltin implements MatlabFunctionProviderEnum {

    /**
     * Zeros array.
     * 
     * <p>
     * zeros(..., CLASSNAME) is an array of zeros of class specified by CLASSNAME.
     * 
     * <p>
     * zeros(N, CLASSNAME) is an N-by-N matrix of zeros.
     * 
     * <p>
     * zeros(M,N, CLASSNAME) or zeros([M,N]) is an M-by-N matrix of zeros.
     * 
     * <p>
     * zeros(M,N,P,..., CLASSNAME) or zeros([M N P ...], CLASSNAME) is an M-by-N-by-P-by-... array of zeros.
     * 
     * <p>
     * zeros(SIZE(A), CLASSNAME) is the same size as A and all zeros.
     * 
     * <p>
     * zeros with no arguments is the scalar 0.
     * 
     * <p>
     * Note: The size inputs M, N, and P... should be nonnegative integers. Negative integers are treated as 0.
     * 
     * <p>
     * Example: x = zeros(2,3,'int8');
     */
    ZEROS("zeros") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabStringZeroes());

            // Builder for declared version of zeros and constant numeric inputs
            builders.add(ConstantArrayDecBuilder.createNumericInputs("zeros", 0));

            // Builder for declared version of zeros and a matrix input
            builders.add(ConstantArrayDecBuilder.createMatrixInputs("zeros", 0));

            // Builder for allocated version of zeros and numeric inputs
            builders.add(new ConstantArrayAllocBuilder("zeros", CNodeFactory.newCNumber(0)));

            // Builder for allocated version of zeros and a matrix input
            builders.add(DynamicMatrixBuilderUtils.newDynamicBuilder(0));

            return builders;
        }
    },

    /**
     * Ones array.
     * 
     * <p>
     * ones(..., CLASSNAME) is an array of ones of class specified by CLASSNAME.
     * 
     * <p>
     * ones(N, CLASSNAME) is an N-by-N matrix of ones.
     * 
     * <p>
     * ones(M,N, CLASSNAME) or ones([M,N], CLASSNAME) is an M-by-N matrix of ones.
     * 
     * <p>
     * ones(M,N,P,..., CLASSNAME) or ones([M N P ...], CLASSNAME) is an M-by-N-by-P-by-... array of ones.
     * 
     * <p>
     * ones(SIZE(A), CLASSNAME) is the same size as A and all ones.
     * 
     * <p>
     * ones with no arguments is the scalar 1.
     * 
     * <p>
     * Note: The size inputs M, N, and P... should be nonnegative integers. Negative integers are treated as 0.
     * 
     * <p>
     * Example: x = ones(2,3,'int8');
     */
    ONES("ones") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for declared version of ones and constant numeric inputs
            builders.add(ConstantArrayDecBuilder.createNumericInputs("ones", 1));

            // Builder for declared version of zeros and a matrix input
            builders.add(ConstantArrayDecBuilder.createMatrixInputs("ones", 1));

            // Builder for allocated version of zeros and numeric inputs
            builders.add(new ConstantArrayAllocBuilder("ones", CNodeFactory.newCNumber(1)));

            // Builder for allocated version of ones and a matrix input
            builders.add(DynamicMatrixBuilderUtils.newDynamicBuilder(1));

            return builders;
        }
    },

    // ASSIGN("")
    CLASS("class") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = new ArrayList<>();

            builders.add(ClassBuilder.newProvider());

            return builders;
        }

    },

    VERTCAT("vertcat") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for [] with explicit output type
            builders.add(new EmptyAllocProvider());

            // Builder for numeric inputs and declared implementation
            builders.add(ArrayDecBuilders.newColNumeric());

            // When multiple empty matrices are concatenated with a single non-empty matrix
            builders.add(EmptyConcat.newSingleNonEmpty());

            // Builder for numeric inputs and allocated implementation
            RowCol col = RowCol.COL;
            builders.add(ArrayAllocBuilders.newRowColNumeric(col));

            // Builder for matrix inputs and declared implementation
            builders.add(ArrayDecBuilders.newColMatrix());

            // Builder for matrix inputs and allocated implementation
            builders.add(ArrayAllocBuilders.newColMatrix());

            return builders;
        }

    },

    /**
     * Uniformly distributed pseudorandom numbers
     * <p>
     * This MATLAB function returns a pseudorandom scalar drawn from the standard uniform distribution on the open
     * interval (0,1).
     * 
     */
    RAND("rand") {
        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // When input is a single scalar, or a list of scalars
            builders.add(RandSimple.newProvider());

            return builders;
        }
    },

    HORZCAT("horzcat") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for [] with explicit output type
            builders.add(new EmptyAllocProvider());

            // Builder for numeric inputs and declared matrices
            builders.add(ArrayDecBuilders.newRowNumeric());

            // When multiple empty matrices are concatenated with a single non-empty matrix
            builders.add(EmptyConcat.newSingleNonEmpty());

            // Builder for numeric inputs and allocated matrices
            builders.add(ArrayAllocBuilders.newRowColNumeric(RowCol.ROW));

            // Builder for cases like [A [B C] D], where all elements are either scalar numerics or row matrices.
            builders.add(ArrayDecBuilders.newRowNumericCombine());

            builders.add(HorzcatCols.getProvider());

            return builders;
        }

    },

    EMPTY_MATRIX("empty_matrix") {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for declared matrices
            builders.add(ArrayDecBuilders.newEmptyMatrix());

            // Builder for allocated matrices
            builders.add(ArrayAllocBuilders.newEmptyMatrixBuilder());

            return builders;
        }
    },

    SIZE("size") {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for scalars
            builders.add(Size.newScalarBuilder()); // 2 inputs
            builders.add(Size.newScalarMatrixBuilder()); // 1 inputs

            builders.add(Size.newMultiOutputsBuilder());

            // Builder for matrices
            builders.add(Size.newMatrixBuilder());

            // Builder for

            return builders;
        }
    },

    FIND("find") {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for scalars
            // builders.add(Size.newScalarBuilder());

            // Builder for matrices
            // builders.add(Size.newMatrixBuilder());

            // Builder for dynamic version with one scalar
            builders.add(Find.newScalarDynamicBuilder());

            // Builder for dynamic version
            builders.add(Find.newDynamicBuilder1Arg());
            builders.add(Find.newDynamicBuilder2Args());

            return builders;
        }
    },

    ISMEMBER("ismember") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(MFileProvider.getProvider(BaseResource.ISMEMBER));

            return builders;
        }
    },

    ISEMPTY("isempty") {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for all versions
            builders.add(IsEmpty.newGeneralBuilder());

            return builders;
        }
    },

    FLIP("flip") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = new ArrayList<>();

            builders.add(Flip.newFlip1dProvider());

            return builders;
        }
    },

    SUB2IND("sub2ind") {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for scalar indexes
            builders.add(Sub2ind.newScalarIndexesBuilder());

            return builders;
        }
    },

    /**
     * Identity matrix.
     * 
     * <p>
     * eye(..., CLASSNAME) is a matrix with ones of class specified by CLASSNAME on the diagonal and zeros elsewhere.
     * 
     * <p>
     * eye(N, CLASSNAME) is the N-by-N identity matrix.
     * 
     * <p>
     * eye(M,N, CLASSNAME) or eye([M,N], CLASSNAME) is an M-by-N matrix with 1's on the diagonal and zeros elsewhere.
     * 
     * <p>
     * eye(SIZE(A), CLASSNAME) is the same size as A.
     * 
     * <p>
     * eye with no arguments is the scalar 1.
     * 
     * <p>
     * Note: The size inputs M and N should be nonnegative integers. Negative integers are treated as 0.
     * 
     * <p>
     * Example: x = eye(2,3,'int8');
     */
    EYE("eye") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for declared arrays when using numeric inputs indicating the size of the matrix, plus the
            // class-name.
            builders.add(new EyeDecBuilder());

            // Builder for allocated arrays when using numeric inputs indicating the size of the matrix, plus the
            // class-name.
            builders.add(ArrayAllocBuilders.newEye());

            return builders;
        }

    },

    FPRINTF("fprintf") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Builder for 1 input string and a variable number of non-matrix values
            builders.add(GeneralBuilders.newFprintfScalar());

            return builders;
        }
    },

    LINSPACE("linspace") {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new LinspaceDecBuilder());

            return builders;
        }

    },

    /**
     * N = ndims(X) returns the number of dimensions in the array X.
     * 
     * <p>
     * The number of dimensions in an array is always greater than or equal to 2. Trailing singleton dimensions are
     * ignored. Put simply, it is LENGTH(SIZE(X)).
     */
    NDIMS("ndims") {
        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // When input is a declared matrix
            // builders.add(GeneralDecBuilders.newNdimsBuilder());
            builders.add(NDimsBuilder.getProvider());

            // When input is an allocated matrix
            builders.add(GeneralAllocBuilders.newNdimsBuilder());

            builders.add(CellNumDims.getProvider());

            return builders;
        }
    },

    /**
     * Length of vector.
     * 
     * <p>
     * length(X) returns the length of vector X. It is equivalent to MAX(SIZE(X)) for non-empty arrays and 0 for empty
     * ones.
     */
    LENGTH("length") {
        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // When input is a declared matrix
            builders.add(GeneralDecBuilders.newLengthBuilder());

            // When input is an allocated matrix
            builders.add(GeneralAllocBuilders.newLengthBuilder());

            return builders;
        }
    },

    /**
     * Number of elements in an array or subscripted array expression.
     * 
     * <p>
     * N = numel(A) returns the number of elements, N, in array A.
     * 
     * <p>
     * N = numel(A, INDEX1, INDEX2, ...) returns in N the number of subscripted elements in array A(index1, index2,
     * ...).
     */
    NUMEL("numel") {
        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // When input is a scalar
            builders.add(BuiltinScalarBuilders.newScalarBuilder());

            // When input is a matrix
            builders.add(BuiltinMatrixBuilders.newNumelBuilder());

            // When input is a cell array
            builders.add(BuiltinCellBuilders.newNumelBuilder());

            return builders;
        }
    },

    STRLENGTH("strlength") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = new ArrayList<>();

            builders.add(new MatlabStringLength());
            builders.add(new ElementWiseBuilder(STRLENGTH, 1));

            return builders;
        }
    },

    /**
     * Compares two strings for equality.
     * 
     * <p>
     * strcmp(B, C) returns 1 if and only if the following three conditions are true:
     * <ul>
     * <li>class(B) is 'char'
     * <li>class(C) is 'char'
     * <li>B and C are equal
     * </ul>
     */
    STRCMP("strcmp") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(StrcmpBuilder.newStrcmpBuilder());

            return builders;
        }
    },

    STRSPLIT("strsplit") {
        @Override
        public List<InstanceProvider> getProviders() {
            return Arrays.asList(StrsplitConstantCharBuilder.getProvider());
        }
    },

    /**
     * Converts an array of strings, or a cell array os character arrays, into the double equivalent.
     */
    STR2DOUBLE("str2double") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(Str2DoubleBuilder.newStr2DoubleBuilder());
            builders.add(new ElementWiseBuilder(STR2DOUBLE, 1)); // array of strings

            return builders;
        }
    },

    /**
     * Sorts a matrix.
     * 
     * <p>
     * sort([1 4 2], 'descend') returns [4 2 1].
     * </p>
     */
    SORT("sort") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(SortBuilder.newSortDefaultOrderBuilderForOneOutput());
            builders.add(SortBuilder.newSortDefaultOrderBuilderForTwoOutputs());

            builders.add(SortBuilder.newSortBuilder());

            return builders;
        }
    },

    PADARRAY("padarray") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(SpecializedPadArrayProvider.getProvider());
            builders.add(MFileProvider.getProvider(BaseResource.PADARRAY));

            return builders;
        }
    },

    STRING("string") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(MatlabStringFromCStringConstructor.getInstanceProvider());
            builders.add(MatlabStringFromCharMatrixConstructor.getInstanceProvider());

            return builders;
        }
    },

    CELL("cell") {
        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(DynamicCellConstructor.getProvider());

            return builders;
        }
    };

    private final String matlabFunctionName;

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    /**
     * Constructor.
     * 
     * @param matlabFunctionName
     */
    private MatlabBuiltin(String matlabFunctionName) {
        this.matlabFunctionName = matlabFunctionName;
    }

    @Override
    public String getName() {
        return this.matlabFunctionName;
    }

}
