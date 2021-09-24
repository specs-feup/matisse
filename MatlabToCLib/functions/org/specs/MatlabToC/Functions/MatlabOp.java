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
import org.specs.CIRFunctions.LibraryFunctions.CMathFunction;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabToC.Functions.MathFunctions.General.GeneralBuilders;
import org.specs.MatlabToC.Functions.MathFunctions.Static.transpose.NumericTransposeBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseScalarBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorBuilders;
import org.specs.MatlabToC.Functions.MatlabOps.MatlabTypeOperator;
import org.specs.MatlabToC.Functions.MatlabOps.MatrixPowerPositiveIntegerDecBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ScalarDivisionBuilder;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWise.ElementBuilders;
import org.specs.MatlabToC.Functions.MatlabOpsV2.MatrixMul;
import org.specs.MatlabToC.Functions.Strings.MatlabStringEquals;
import org.specs.MatlabToC.InstanceProviders.ScalarOperator;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public enum MatlabOp implements MatlabFunctionProviderEnum {

    // Arithmetic operators
    Addition(MatlabOperator.Addition) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(Addition));

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.Addition));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(Addition, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(Addition, 2));
            return builders;
        }
    },

    Subtraction(MatlabOperator.Subtraction) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(Subtraction));

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.Subtraction));

            // FIX [ MatlabOperatorPrototype.Subtraction ] : using ElementWiseBuilder instead of ElementWiseDecBuilder
            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(Subtraction, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(Subtraction, 2));

            return builders;
        }
    },

    Multiplication(MatlabOperator.Multiplication) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(Multiplication));

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.Multiplication));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(Multiplication, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(Multiplication, 2));

            return builders;
        }
    },

    RightDivision(MatlabOperator.RightDivision) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(RightDivision));

            // Operations with Numeric types and two inputs
            builders.add(new ScalarDivisionBuilder(false));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(RightDivision, 2));

            // Operation with two inputs ( at least one is of type matrix ), static
            builders.add(new ElementWiseScalarBuilder(RightDivision, 2));

            // Operation with two inputs ( at least one is of type matrix ), dynamic
            // builders.add(new ElementWiseScalarBuilder(RightDivision, 2));

            return builders;
        }
    },

    LeftDivision(MatlabOperator.LeftDivision) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(LeftDivision));

            // Operations with Numeric types and two inputs
            builders.add(new ScalarDivisionBuilder(true));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(LeftDivision, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(LeftDivision, 2));

            return builders;
        }
    },

    MatrixRightDivision(MatlabOperator.MatrixRightDivision) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(MatlabOp.MatrixRightDivision));

            // Operations with Numeric types and two inputs
            builders.add(new ScalarDivisionBuilder(false));

            // Operations with Matrix as first type and numeric as second
            builders.add(new ElementWiseScalarBuilder(RightDivision, 2, true));

            // Operations with numeric as first type and Matrix as second
            builders.add(ElementBuilders.newScalarMatrix(RightDivision));

            return builders;
        }
    },

    MatrixLeftDivision(MatlabOperator.MatrixLeftDivision) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(MatlabOp.MatrixLeftDivision));

            // Operations with Numeric types and two inputs
            builders.add(new ScalarDivisionBuilder(true));

            return builders;
        }
    },

    MatrixMultiplication(MatlabOperator.MatrixMultiplication) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with matrix types and two inputs (static)
            // builders.add(new MatrixMultiplicationDecBuilder());

            builders.add(new MatlabTypeOperator(MatrixMultiplication));

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.Multiplication));

            // row * col effectively behaves like dot product
            builders.add(MatrixMul.newRowTimesColumn());

            // Operations with at least one matrix, where one of them might be scalar
            builders.add(new ElementWiseScalarBuilder(MatrixMultiplication, 2));

            // Operations with at least one matrix, all inputs are numeric
            builders.add(MatrixMul.newBlasEnabledProvider());

            return builders;
        }
    },

    UnaryPlus(MatlabOperator.UnaryPlus) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(MatlabOp.UnaryPlus));

            // Operations with Numeric types and one input
            builders.add(ScalarOperator.create(COperator.UnaryPlus));

            // Operations with Matrix types and one input
            builders.add(new ElementWiseBuilder(UnaryPlus, 1));

            return builders;
        }
    },

    UnaryMinus(MatlabOperator.UnaryMinus) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(new MatlabTypeOperator(MatlabOp.UnaryMinus));

            // Operations with Numeric types and one input
            builders.add(ScalarOperator.create(COperator.UnaryMinus));

            // Operations with Matrix types and one input
            builders.add(new ElementWiseBuilder(UnaryMinus, 1));

            return builders;
        }
    },

    Colon(MatlabOperator.Colon) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            builders.add(MatlabOperatorBuilders.newScalarBuilder());
            builders.add(MatlabOperatorBuilders.newColonDecBuilder());
            builders.add(MatlabOperatorBuilders.newColonAllocBuilder());

            return builders;
        }
    },

    Power(MatlabOperator.Power) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            // builders.add(CMathFunction.POW);
            builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.POW, CMathFunction.POWF));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(Power, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(Power, 2));

            return builders;
        }
    },

    MatrixPower(MatlabOperator.MatrixPower) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            // builders.add(CMathFunction.POW);
            builders.add(GeneralBuilders.newCLibraryBuilder(CMathFunction.POW, CMathFunction.POWF));

            // Operations with Numeric Types and two input (a square matrix and an integer)
            builders.add(new MatrixPowerPositiveIntegerDecBuilder());

            return builders;
        }
    },

    // Relational operators
    LessThan(MatlabOperator.LessThan) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LessThan));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(LessThan, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(LessThan, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    LessThanOrEqual(MatlabOperator.LessThanOrEqual) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LessThanOrEqual));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(LessThanOrEqual, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(LessThanOrEqual, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    GreaterThan(MatlabOperator.GreaterThan) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.GreaterThan));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(GreaterThan, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(GreaterThan, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    GreaterThanOrEqual(MatlabOperator.GreaterThanOrEqual) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.GreaterThanOrEqual));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(GreaterThanOrEqual, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(GreaterThanOrEqual, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    Equal(MatlabOperator.Equal) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(new MatlabStringEquals());

            builders.add(ScalarOperator.create(COperator.Equal));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(Equal, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(Equal, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    NotEqual(MatlabOperator.NotEqual) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.NotEqual));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(NotEqual, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(NotEqual, 2));

            return builders;
        }

        @Override
        public boolean isOutputTypeEqualToInput() {
            return false;
        }
    },

    // Logical operators
    ElementWiseAnd(MatlabOperator.ElementWiseAnd) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LogicalAnd));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(ElementWiseAnd, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(ElementWiseAnd, 2));

            return builders;
        }
    },

    ElementWiseOr(MatlabOperator.ElementWiseOr) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LogicalOr));

            // Operation with Matrix types and two inputs
            builders.add(new ElementWiseBuilder(ElementWiseOr, 2));

            // Operation with two inputs ( at least one is of type matrix )
            builders.add(new ElementWiseScalarBuilder(ElementWiseOr, 2));

            return builders;
        }
    },

    LogicalNegation(MatlabOperator.LogicalNegation) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and one input
            builders.add(ScalarOperator.create(COperator.LogicalNegation));

            // Operations with Matrix types and one input
            builders.add(new ElementWiseBuilder(LogicalNegation, 1));

            return builders;
        }
    },

    ShortCircuitAnd(MatlabOperator.ShortCircuitAnd) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LogicalAnd));

            // MATLAB does not support ShortCircuit logic with matrixes

            return builders;
        }
    },

    ShortCircuitOr(MatlabOperator.ShortCircuitOr) {

        @Override
        public List<InstanceProvider> getProviders() {
            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // Operations with Numeric types and two inputs
            builders.add(ScalarOperator.create(COperator.LogicalOr));

            // MATLAB does not support ShortCircuit logic with matrixes
            // builders.add(new ElementWiseBuilder(ShortCircuitOr, 2));
            return builders;
        }
    },

    Transpose(MatlabOperator.Transpose) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // For 1 numeric matrix
            builders.add(new NumericTransposeBuilder());

            return builders;
        }

    },

    ComplexConjugateTranspose(MatlabOperator.ComplexConjugateTranspose) {

        @Override
        public List<InstanceProvider> getProviders() {

            List<InstanceProvider> builders = SpecsFactory.newArrayList();

            // For 1 numeric matrix
            builders.add(new NumericTransposeBuilder());

            return builders;
        }

    };

    private final MatlabOperator op;

    /*
    private static final Map<MatlabOperator, String> OP_MAP;
    static {
    OP_MAP = Maps.newEnumMap(MatlabOperator.class);
    for (MatlabOp mOp : values()) {
        OP_MAP.put(mOp.op, mOp.functionName);
    }
    }
    */

    /*
    public static String getFunctionName(MatlabOperator op) {
    String functionName = OP_MAP.get(op);
    if (functionName == null) {
        throw new RuntimeException("Case not implemented:" + op);
    }
    
    return functionName;
    }
    */

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    MatlabOp(MatlabOperator op) {
        this.op = op;
    }

    public MatlabOperator getOperator() {
        return this.op;
    }

    @Override
    public String getName() {
        return this.op.getFunctionName();
    }

    @Override
    public boolean isOutputTypeEqualToInput() {
        return true;
    }

}
