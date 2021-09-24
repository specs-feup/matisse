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

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.ATypes.Scalar.Functions.IsInf;
import org.specs.CIR.Types.ATypes.Scalar.Functions.IsNan;
import org.specs.CIRFunctions.LibraryFunctions.CMathFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdlibFunction;
import org.specs.MatlabToC.Functions.MathFunctions.MathScalarBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.General.Dot;
import org.specs.MatlabToC.Functions.MathFunctions.General.Fix;
import org.specs.MatlabToC.Functions.MathFunctions.General.GeneralBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.General.Mean;
import org.specs.MatlabToC.Functions.MathFunctions.General.Min;
import org.specs.MatlabToC.Functions.MathFunctions.General.Prod;
import org.specs.MatlabToC.Functions.MathFunctions.General.Sum;
import org.specs.MatlabToC.Functions.MathFunctions.Static.bitshift.BitshiftDoubleDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.bitshift.BitshiftIntegerDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.mean.MeanDecMatrixProvider;
import org.specs.MatlabToC.Functions.MathFunctions.Static.mean.MeanVectorBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMax;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxDefaultDimDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxDimDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxHigherDimDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxMatricesDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxMatrixScalarDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxScalarDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxScalarsDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxVectorDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecHigherDimBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecScalarBuilder;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecVectorBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseScalarBuilder;
import org.specs.MatlabToC.InstanceProviders.ScalarOperator;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public enum MathFunction implements MatlabFunctionProviderEnum {

    ISNAN("isnan") {
	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Provider for scalar integer input
	    builders.add(IsNan.getProvider());

	    return builders;
	}
    },

    ISINF("isinf") {
	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Provider for scalar integer input
	    builders.add(IsInf.getProvider());

	    return builders;
	}
    },

    ABS("abs") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Builder for scalar integer input
	    builders.add(CStdlibFunction.ABS);

	    // Builder for scalar double input
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.FABS, CMathFunction.FABSF));

	    // Builder for matrix input (then calls the appropriate version for
	    // int or double)
	    // builders.add(new ElementWiseDecBuilder(ABS, 1));
	    builders.add(new ElementWiseBuilder(ABS, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    ROUND("round") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Builder for scalar input
	    // builders.add(new
	    // CLibraryInstanceProvider(CMathFunction.ROUND.getCLibrary()));
	    // builders.add(CMathFunction.ROUND);
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.LROUND, CMathFunction.LROUNDF));
	    // Builder for matrix input
	    // builders.add(new ElementWiseDecBuilder(ROUND, 1));
	    builders.add(new ElementWiseBuilder(ROUND, 1));

	    return builders;
	}

    },

    ASIN("asin") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(GeneralBuilders.newSinNumericBuilder());
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.ASIN, CMathFunction.ASINF));

	    // Operations with one matrix input
	    // builders.add(new ElementWiseDecBuilder(SIN, 1));
	    builders.add(new ElementWiseBuilder(ASIN, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    ACOS("acos") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(GeneralBuilders.newSinNumericBuilder());
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.ACOS, CMathFunction.ACOSF));

	    // Operations with one matrix input
	    // builders.add(new ElementWiseDecBuilder(SIN, 1));
	    builders.add(new ElementWiseBuilder(ACOS, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    SIN("sin") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(GeneralBuilders.newSinNumericBuilder());
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.SIN, CMathFunction.SINF));

	    // Operations with one matrix input
	    // builders.add(new ElementWiseDecBuilder(SIN, 1));
	    builders.add(new ElementWiseBuilder(SIN, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    COS("cos") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(GeneralBuilders.newCosNumericBuilder());
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.COS, CMathFunction.COSF));

	    // Operations with one matrix input
	    builders.add(new ElementWiseBuilder(COS, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    LOG("log") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(GeneralBuilders.newLogNumericBuilder());
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.LOG, CMathFunction.LOGF));

	    // Operations with one matrix input
	    builders.add(new ElementWiseBuilder(LOG, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    SQRT("sqrt") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(CMathFunction.SQRT);
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.SQRT, CMathFunction.SQRTF));

	    // Operations with one matrix input
	    builders.add(new ElementWiseBuilder(SQRT, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}

    },

    EXP("exp") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with one scalar input
	    // builders.add(CMathFunction.EXP);
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.EXP, CMathFunction.EXPF));

	    // Operations with one matrix input
	    builders.add(new ElementWiseBuilder(EXP, 1));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}

    },

    PROD("prod") {

	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Builder for matrices
	    builders.add(Prod.newVectorBuilder());

	    // Builder for

	    return builders;
	}
    },

    /**
     * Modulus after division.
     * 
     * <p>
     * mod(x,y) is x - n.*y where n = floor(x./y) if y ~= 0. If y is not an integer and the quotient x./y is within
     * roundoff error of an integer, then n is that integer. The inputs x and y must be real arrays of the same size, or
     * real scalars.
     * 
     * <p>
     * The statement "x and y are congruent mod m" means mod(x,m) == mod(y,m).
     * 
     * <p>
     * By convention:<br>
     * - mod(x,0) is x.<br>
     * - mod(x,x) is 0.<br>
     * - mod(x,y), for x~=y and y~=0, has the same sign as y.<br>
     * 
     * <p>
     * Note: REM(x,y), for x~=y and y~=0, has the same sign as x. mod(x,y) and REM(x,y) are equal if x and y have the
     * same sign, but differ by y if x and y have different signs.
     * 
     */
    MOD("mod") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with two scalar inputs of type integer
	    // builders.add(GeneralBuilders.newIntegerModBuilder());
	    // Operations with two scalar inputs
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.FMOD, CMathFunction.FMODF));

	    // Operations with two matrix inputs
	    builders.add(new ElementWiseBuilder(MOD, 2));

	    return builders;
	}

    },

    /**
     * Remainder after division
     * 
     * This MATLAB function returns the remainder after division of X by Y.
     * 
     * R = rem(X,Y)
     * 
     */
    REM("rem") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with two scalar inputs of type integer
	    builders.add(GeneralBuilders.newIntegerModBuilder());

	    // Operations with two matrix inputs of type integer
	    builders.add(new ElementWiseBuilder(GeneralBuilders.newIntegerModBuilder(), 2));

	    return builders;
	}

    },

    /**
     * Sum of elements.
     * 
     * <p>
     * S = sum(X) is the sum of the elements of the vector X. If X is a matrix, S is a row vector with the sum over each
     * column. For N-D arrays, sum(X) operates along the first non-singleton dimension. If X is floating point, that is
     * double or single, S is accumulated natively, that is in the same class as X, and S has the same class as X. If X
     * is not floating point, S is accumulated in double and S has class double.
     * 
     * <p>
     * S = sum(X,DIM) sums along the dimension DIM.
     * 
     * <p>
     * S = sum(X,'double') and S = sum(X,DIM,'double') accumulate S in double and S has class double, even if X is
     * single.
     * 
     * <p>
     * S = sum(X,'native') and S = sum(X,DIM,'native') accumulate S natively and S has the same class as X.
     * 
     * <p>
     * Examples: If X = [0 1 2; 3 4 5]
     * 
     * <p>
     * then sum(X,1) is [3 5 7] and sum(X,2) is [3; 12];
     * 
     * <p>
     * If X = int8(1:20) then sum(X) accumulates in double and the result is double(210) while sum(X,'native')
     * accumulates in int8, but overflows and saturates to int8(127).
     */
    SUM("sum") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // When the input is a scalar;
	    // Can receive 1, 2 or 3 inputs

	    builders.add(new SumDecScalarBuilder());

	    // When no DIM is specified and the input is a vector ( Mx1 or 1xN matrix );
	    // Can receive 1 or 2 inputs
	    // builders.add(new SumDecVectorBuilder());
	    builders.add(new SumDecVectorBuilder());

	    // When the input is matrix and the value of DIM is not greater than the number of dimensions in the input;
	    // Can receive 1, 2 or 3 inputs
	    builders.add(new SumDecBuilder());

	    // When the input is a matrix and the value of DIM is greater than the number of dimensions of the input;
	    // Can receive 2 or 3 inputs
	    builders.add(new SumDecHigherDimBuilder());

	    // Builder for static matrices
	    // builders.add(Sum.newStaticBuilder());

	    // LoggingUtils.msgWarn("EXPERIMENT, SHOULD COMPARE PERFORMANCE OF THESE TWO STATIC CODES");

	    // Builder for matrices with one dimension
	    builders.add(Sum.newOneDimBuilder());

	    // Builder for allocated matrices
	    builders.add(Sum.newDynamicBuilder());

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    /**
     * Signum function.
     * 
     * <p>
     * For each element of X, sign(X) returns 1 if the element is greater than zero, 0 if it equals zero and -1 if it is
     * less than zero. For the nonzero elements of complex X, sign(X) = X ./ ABS(X).
     * 
     */
    SIGN("sign") {
	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // When input is a numeric
	    builders.add(GeneralBuilders.newSignNumericBuilder());

	    // When input is a matrix
	    // builders.add(GeneralAllocBuilders.newNumelBuilder());

	    return builders;
	}
    },

    BITSHIFT("bitshift") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // When the first input is a scalar and the second is a constant from a literal
	    builders.add(MathScalarBuilders.bitshiftScalarLiteral());

	    // When the first input is either an integer scalar or a declared matrix of integers
	    builders.add(new BitshiftIntegerDecBuilder());

	    // When the first input is either a double scalar or a declared matrix of doubles
	    builders.add(new BitshiftDoubleDecBuilder());

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}

    },

    /*
    PI("pi") {
    
    @Override
    public List<InstanceProvider> getBuilders() {
    
        List<InstanceProvider> builders = FactoryUtils.newArrayList();
    
        // When the first input is either an integer scalar or a declared matrix of integers
        // builders.add(new BitshiftIntegerDecBuilder());
    
        return builders;
    }
    },
    */

    MIN("min") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    builders.add(Min.newMin3Builder());

	    // min for scalars
	    builders.add(new MinMaxScalarDecBuilder(MinMax.MIN));

	    // min for vector inputs
	    builders.add(new MinMaxVectorDecBuilder(MinMax.MIN));

	    // min for scalar inputs
	    builders.add(new MinMaxScalarsDecBuilder(MinMax.MIN));

	    // min for matrices
	    builders.add(new MinMaxMatricesDecBuilder(MinMax.MIN));

	    // min for mixed inputs, a matrix and a scalar
	    builders.add(new MinMaxMatrixScalarDecBuilder(MinMax.MIN));

	    // min for a matrix input and no DIM ( m = max(A) )
	    builders.add(new MinMaxDefaultDimDecBuilder(MinMax.MIN));

	    // min for a matrix input and a dimension greater than the number of dimensions of the input
	    builders.add(new MinMaxHigherDimDecBuilder(MinMax.MIN));

	    // min for a matrix input and a specified dimension
	    builders.add(new MinMaxDimDecBuilder(MinMax.MIN));

	    // min for a single input, scalar, vector or matrix (dynamic)
	    builders.add(Min.newDynamicBuilder());

	    builders.add(new ElementWiseBuilder(MIN, 2));

	    builders.add(new ElementWiseScalarBuilder(MIN, 2));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}

    },

    MAX("max") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // max for scalars
	    builders.add(new MinMaxScalarDecBuilder(MinMax.MAX));

	    // max for vector inputs
	    builders.add(new MinMaxVectorDecBuilder(MinMax.MAX));

	    // max for scalar inputs
	    builders.add(new MinMaxScalarsDecBuilder(MinMax.MAX));

	    // max for matrices
	    builders.add(new MinMaxMatricesDecBuilder(MinMax.MAX));

	    // max for mixed inputs, a matrix and a scalar
	    builders.add(new MinMaxMatrixScalarDecBuilder(MinMax.MAX));

	    // max for a matrix input and no DIM ( m = max(A) )
	    builders.add(new MinMaxDefaultDimDecBuilder(MinMax.MAX));

	    // max for a matrix input and a dimension greater than the number of dimensions of the input
	    builders.add(new MinMaxHigherDimDecBuilder(MinMax.MAX));

	    // max for a matrix input and a specified dimension
	    builders.add(new MinMaxDimDecBuilder(MinMax.MAX));

	    builders.add(new ElementWiseBuilder(MAX, 2));

	    builders.add(new ElementWiseScalarBuilder(MAX, 2));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}

    },

    BITXOR("bitxor") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with Numeric types and two inputs
	    builders.add(ScalarOperator.create(COperator.BitwiseXor));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    BITAND("bitand") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with Numeric types and two inputs
	    builders.add(ScalarOperator.create(COperator.BitwiseAnd));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    BITOR("bitor") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Operations with Numeric types and two inputs
	    builders.add(ScalarOperator.create(COperator.BitwiseOr));

	    return builders;
	}

	@Override
	public boolean isOutputTypeEqualToInput() {
	    return true;
	}
    },

    FIX("fix") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    builders.add(new ElementWiseBuilder(FIX, 1));

	    // One scalar input
	    builders.add(Fix.newScalarBuilder());

	    return builders;
	}
    },

    FLOOR("floor") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    builders.add(new ElementWiseBuilder(FLOOR, 1));

	    // Operations with Numeric types and two inputs
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.FLOOR, CMathFunction.FLOORF));

	    return builders;
	}
    },

    CEIL("ceil") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    builders.add(new ElementWiseBuilder(CEIL, 1));

	    // Operations with Numeric types and two inputs
	    builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.CEIL, CMathFunction.CEILF));

	    return builders;
	}
    },

    MEAN("mean") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // TODO: Compare there two codes?

	    // Builder for 1 input, a declared numeric vector
	    builders.add(new MeanVectorBuilder());

	    // Builder for 1 input, a declare numeric matrix
	    builders.add(new MeanDecMatrixProvider());

	    // Builder for two inputs, static
	    builders.add(Mean.newStaticBuilder());

	    // Builder for one or two inputs, dynamic
	    builders.add(Mean.newDynamicBuilder());

	    return builders;
	}
    },

    DOT("dot") {

	@Override
	public List<InstanceProvider> getProviders() {

	    List<InstanceProvider> builders = SpecsFactory.newArrayList();

	    // Both sides are scalars
	    builders.add(ScalarOperator.create(COperator.Multiplication));

	    // A dot B, where A and B are row or column matrices.
	    builders.add(Dot.new1DImplementation());

	    return builders;

	}

    };
    /*
        INT32("int32") {
    
    	@Override
    	public List<InstanceProvider> getBuilders() {
    
    	    List<InstanceProvider> builders = FactoryUtils.newArrayList();
    
    	    // Builder for 1 input, a numeric
    	    builders.add(GeneralBuilders.newCastNumericBuilder(NumericClassName.INT32));
    
    	    return builders;
    	}
        },
        
        DOUBLE("double") {
    	
    	@Override
    	public List<InstanceProvider> getBuilders() {
    	    
    	    List<InstanceProvider> builders = FactoryUtils.newArrayList();
    	    
    	    // Builder for 1 input, a numeric
    	    builders.add(GeneralBuilders.newCastNumericBuilder(NumericClassName.DOUBLE));
    	    
    	    return builders;
    	}
        },
        
        UINT32("uint32") {
    	
    	@Override
    	public List<InstanceProvider> getBuilders() {
    	    
    	    List<InstanceProvider> builders = FactoryUtils.newArrayList();
    	    
    	    // Builder for 1 input, a numeric
    	    builders.add(GeneralBuilders.newCastNumericBuilder(NumericClassName.UINT32));
    	    
    	    return builders;
    	}
        };
        */

    private final String matlabFunctionName;

    MathFunction(String matlabFunctionName) {
	this.matlabFunctionName = matlabFunctionName;
    }

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    @Override
    public String getName() {
	return this.matlabFunctionName;
    }

}
