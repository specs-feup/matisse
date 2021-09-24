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

package org.specs.matlabtocl.v2.functions.builtins;

import java.util.List;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperatorInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public enum CLBinaryOperator implements InstanceProvider {
    LESS_THAN("<", PrecedenceLevel.LessThan, CLBinaryOperator::predicate),
    LESS_OR_EQUAL_TO("<=", PrecedenceLevel.LessThan, CLBinaryOperator::predicate),
    GREATER_THAN(">", PrecedenceLevel.GreaterThan, CLBinaryOperator::predicate),
    GREATER_OR_EQUAL_TO(">=", PrecedenceLevel.LessThan, CLBinaryOperator::predicate),
    EQUAL("==", PrecedenceLevel.Equality, CLBinaryOperator::predicate),
    NOT_EQUAL_TO("!=", PrecedenceLevel.NotEqual, CLBinaryOperator::predicate),

    BITWISE_AND("&", PrecedenceLevel.BitwiseAnd, CLBinaryOperator::combineTypes),

    LOGICAL_AND("&&", PrecedenceLevel.LogicalAnd, CLBinaryOperator::predicate),

    LEFT_SHIFT("<<", "<<=", null, PrecedenceLevel.BitShift, CLBinaryOperator::combineTypes),
    RIGHT_SHIFT(">>", ">>=", null, PrecedenceLevel.BitShift, CLBinaryOperator::combineTypes),

    SUBTRACTION("-", "-=", "--", PrecedenceLevel.Subtraction, CLBinaryOperator::combineTypes),
    ADDITION("+", "+=", "++", PrecedenceLevel.Addition, CLBinaryOperator::combineTypes),
    MULTIPLICATION("*", "*=", null, PrecedenceLevel.Multiplication, CLBinaryOperator::combineTypes),
    DIVISION("/", "/=", null, PrecedenceLevel.Multiplication, CLBinaryOperator::combineTypes),
    MODULO("%", "%=", null, PrecedenceLevel.Modulo, CLBinaryOperator::combineTypes);

    private static ScalarType predicate(List<VariableType> inputs) {
        return CLNativeType.BOOL;
    }

    private static ScalarType combineTypes(List<VariableType> inputs) {
        boolean fp = false;
        int nbits = 0;
        CLNativeType outputType = null;

        for (VariableType input : inputs) {
            CLNativeType clType;

            if (input instanceof VoidType) {
                throw new RuntimeException("Attempting to use operator with void argument. Types are: " + inputs);
            }

            if (input instanceof CLNativeType) {
                clType = (CLNativeType) input;
            } else if (input instanceof StdIntType) {
                // TODO
                clType = CLNativeType.LONG;
            } else if (input instanceof NumericTypeV2) {
                // TODO
                clType = CLNativeType.LONG;
            } else {
                throw new NotImplementedException(input.getClass().toString());
            }

            int typeNbits = clType.getBits().orElse(48);

            if (clType.isInteger()) {
                if (!fp) {
                    // TODO
                    outputType = CLNativeType.LONG;
                }
            } else {
                // clType is float or double
                if (fp) {
                    if (typeNbits > nbits) {
                        outputType = clType;
                        nbits = clType.getBits().get();
                    }
                } else {
                    nbits = typeNbits;
                    outputType = clType;
                    fp = true;
                }
            }
        }

        return outputType;
    }

    private final String symbol;
    private final String accumulationSymbol;
    private final String accumulationUnitSymbol;
    private final PrecedenceLevel precedenceLevel;
    private final Function<List<VariableType>, ScalarType> returnType;

    private CLBinaryOperator(String symbol, PrecedenceLevel precedenceLevel,
            Function<List<VariableType>, ScalarType> returnType) {
        this(symbol, null, null, precedenceLevel, returnType);
    }

    private CLBinaryOperator(String symbol,
            String accumulationSymbol,
            String accumulationUnitSymbol,
            PrecedenceLevel precedenceLevel,
            Function<List<VariableType>, ScalarType> returnType) {

        this.symbol = symbol;
        this.accumulationSymbol = accumulationSymbol;
        this.accumulationUnitSymbol = accumulationUnitSymbol;
        this.precedenceLevel = precedenceLevel;
        this.returnType = returnType;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder
                .newInline()
                .addInput(data.getInputTypes().get(0))
                .addInput(data.getInputTypes().get(1))
                .returning(this.returnType.apply(data.getInputTypes()))
                .build();

        InlineCode code = tokens -> {
            String code1 = tokens.get(0).getCodeForLeftSideOf(this.precedenceLevel);
            String code2 = tokens.get(1).getCodeForRightSideOf(this.precedenceLevel);

            return code1 + " " + this.symbol + " " + code2;
        };
        InlinedInstance instance;
        String functionName = "$op" + this.symbol;
        if (this.accumulationSymbol != null) {
            InlineCode assignmentCode = tokens -> {
                CNode left = tokens.get(0);
                CNode right = tokens.get(1);

                String code1 = left.getCodeForLeftSideOf(PrecedenceLevel.Assignment);
                String code2 = right.getCodeForRightSideOf(PrecedenceLevel.Assignment);

                if (code2.equals("1") && this.accumulationUnitSymbol != null) {
                    return this.accumulationUnitSymbol + left.getCodeForContent(PrecedenceLevel.PrefixIncrement);
                }

                return code1 + " " + this.accumulationSymbol + " " + code2;
            };

            instance = new COperatorInstance(functionType, functionName, code, assignmentCode);
        } else {
            instance = new InlinedInstance(functionType, functionName, code);
        }

        instance.setCallPrecedenceLevel(this.precedenceLevel);

        return instance;
    }
}
