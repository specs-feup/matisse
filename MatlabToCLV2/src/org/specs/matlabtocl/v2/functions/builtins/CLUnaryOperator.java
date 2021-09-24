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
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public enum CLUnaryOperator implements InstanceProvider {
    LOGICAL_NOT("!", PrecedenceLevel.LogicalNot, CLUnaryOperator::predicate),
    UNARY_MINUS("-", PrecedenceLevel.UnaryMinus, CLUnaryOperator::propagate),
    UNARY_PLUS("+", PrecedenceLevel.UnaryPlus, CLUnaryOperator::propagate);

    private static VariableType predicate(List<VariableType> inputs) {
        return CLNativeType.BOOL;
    }

    private static VariableType propagate(List<VariableType> inputs) {
        return inputs.get(0);
    }

    private final String symbol;
    private final PrecedenceLevel precedenceLevel;
    private final Function<List<VariableType>, VariableType> returnType;

    private CLUnaryOperator(String symbol,
            PrecedenceLevel precedenceLevel,
            Function<List<VariableType>, VariableType> returnType) {

        this.symbol = symbol;
        this.precedenceLevel = precedenceLevel;
        this.returnType = returnType;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder
                .newInline()
                .addInput(data.getInputTypes().get(0))
                .returning(this.returnType.apply(data.getInputTypes()))
                .build();

        InlineCode code = tokens -> {
            String code1 = tokens.get(0).getCodeForContent(this.precedenceLevel);

            return this.symbol + code1;
        };
        String functionName = "$op" + this.symbol;
        InlinedInstance instance = new InlinedInstance(functionType, functionName, code);

        instance.setCallPrecedenceLevel(this.precedenceLevel);

        return instance;
    }
}
