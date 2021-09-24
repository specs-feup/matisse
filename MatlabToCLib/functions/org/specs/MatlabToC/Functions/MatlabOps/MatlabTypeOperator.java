/**
 * Copyright 2016 SPeCS.
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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.types.MatlabElementType;
import org.specs.matisselib.types.MatlabTypeGroup;

public class MatlabTypeOperator implements InstanceProvider {

    private final MatlabOp underlyingOperator;

    public MatlabTypeOperator(MatlabOp underlyingOperator) {
        this.underlyingOperator = underlyingOperator;
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
        int arity = underlyingOperator.getOperator().getNumOperands();

        if (data.getNumInputs() != arity) {
            return Optional.empty();
        }

        boolean hasAnyMatlabType = false;
        for (VariableType inputType : data.getInputTypes()) {
            if (inputType instanceof MatlabElementType) {
                hasAnyMatlabType = true;
            }

            if (!(inputType instanceof ScalarType)) {
                return Optional.empty();
            }
        }

        if (!hasAnyMatlabType) {
            return Optional.empty();
        }

        return Optional.of(this);
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        List<MatlabElementType> types = processTypes(data);

        MatlabElementType outputType = getOutputType(data, types);

        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInputs(types)
                .returning(outputType)
                .build();

        InlineCode code = tokens -> {
            List<CNode> cArguments = new ArrayList<>();

            for (CNode token : tokens) {
                if (token.getVariableType() instanceof MatlabElementType) {
                    cArguments.add(CNodeFactory.newLiteral(token.getCode(),
                            ((MatlabElementType) token.getVariableType())
                                    .getUnderlyingCType(data.getNumerics()),
                            token.getPrecedenceLevel()));
                } else {
                    cArguments.add(token);
                }
            }

            FunctionCallNode functionCallNode = underlyingOperator
                    .getMatlabFunction()
                    .getCheckedInstance(data.createFromNodes(cArguments))
                    .newFunctionCall(cArguments);
            return functionCallNode.getCode();

            // TODO: Cast to appropriate type.

        };
        return new InlinedInstance(functionType, "$matlab$" + underlyingOperator.getOperator().getFunctionName(),
                code);
    }

    private static MatlabElementType getOutputType(ProviderData data, List<MatlabElementType> types) {
        MatlabElementType defaultType = MatlabElementType
                .getDefaultNumberType(data.getSettings().get(CirKeys.DEFAULT_REAL));

        boolean forbidInteger = false;
        boolean forceInteger = false;
        MatlabElementType currentType = defaultType;
        for (MatlabElementType type : types) {
            if (type.equals(defaultType)) {
                continue;
            }

            if (currentType.getTypeGroup() == MatlabTypeGroup.LOGICAL
                    || currentType.getTypeGroup() == MatlabTypeGroup.CHAR) {

                if (forceInteger) {
                    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                            "Integer type values can only be combined with value of the same class, or the default number type.");
                }
                forbidInteger = true;
                continue;
            }

            if (currentType.equals(defaultType)) {
                currentType = type;

                if (type.getTypeGroup().isInteger()) {
                    forceInteger = true;
                }
                continue;
            }

            if (forbidInteger || !currentType.equals(type)) {
                throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                        "Integer type values can only be combined with value of the same class, or the default number type.");
            }
        }

        return currentType;
    }

    private static List<MatlabElementType> processTypes(ProviderData data) {
        List<MatlabElementType> processedTypes = new ArrayList<>();
        for (VariableType type : data.getInputTypes()) {
            if (type instanceof MatlabElementType) {
                processedTypes.add((MatlabElementType) type);
                continue;
            }

            processedTypes.add(MatlabElementType.getDefaultNumberType(data.getSettings().get(CirKeys.DEFAULT_REAL)));
        }

        return processedTypes;
    }
}
