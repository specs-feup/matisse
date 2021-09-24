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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateEqualInstruction;

import pt.up.fe.specs.util.SpecsCollections;

public class ValidateEqualProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateEqualInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateEqualInstruction validation = (ValidateEqualInstruction) instruction;

        List<String> ssaInputs = validation.getInputVariables();
        List<VariableType> inputTypes = new ArrayList<>();

        for (String ssaInput : ssaInputs) {
            VariableType variableType = builder.getVariableTypeFromSsaName(ssaInput).get();
            if (!ScalarUtils.isScalar(variableType)) {
                throw builder.emitError(PassMessage.INTERNAL_ERROR,
                        "validate_equal instruction is only valid for scalar inputs");
            }
            inputTypes.add(variableType);
        }

        if (ssaInputs.size() <= 1) {
            return;
        }

        CNode previousValue = builder.generateVariableNodeForSsaName(SpecsCollections.last(ssaInputs));
        CNode condition = null;
        VariableType conditionType = null;
        for (int i = ssaInputs.size() - 2; i >= 0; --i) {
            CNode currentValue = builder.generateVariableNodeForSsaName(ssaInputs.get(i));

            ProviderData equalData = builder.getCurrentProvider()
                    .create(inputTypes.get(i), inputTypes.get(i + 1));
            FunctionInstance equalInstance = COperator.NotEqual.getCheckedInstance(equalData);
            CNode comparison = equalInstance
                    .newFunctionCall(currentValue, previousValue);
            VariableType equalReturnType = equalInstance
                    .getFunctionType()
                    .getOutputTypes()
                    .get(0);

            if (condition == null) {
                condition = comparison;
                conditionType = equalReturnType;
            } else {
                assert conditionType != null;
                ProviderData orData = builder.getCurrentProvider()
                        .create(equalReturnType, conditionType);
                FunctionInstance orInstance = COperator.LogicalOr.getCheckedInstance(orData);
                conditionType = orInstance.getFunctionType().getOutputTypes().get(0);
                condition = orInstance.newFunctionCall(comparison, condition);
            }

            previousValue = currentValue;
        }

        CLibraryProvider abortProvider = new StdlibFunctions(builder.getCurrentProvider().getNumerics())
                .abort();
        List<CNode> branchNodes = new ArrayList<>();
        branchNodes.add(CNodeFactory.newLiteral(
                "printf(\"validation failed at " + instruction + "\");"));
        for (String ssaInput : ssaInputs) {
            branchNodes.add(
                    CNodeFactory.newLiteral("printf(\"%d\", (int) " + builder.generateVariableNodeForSsaName(ssaInput)
                            .getCode() + ");"));
        }
        branchNodes.add(abortProvider.getCheckedInstance(builder.getCurrentProvider().create()).newFunctionCall());
        CNode branch = IfNodes.newIfThen(condition, branchNodes);

        currentBlock.addInstruction(branch);

        InlinedInstance inst = new InlinedInstance(FunctionTypeBuilder.newInline().returningVoid().build(), "printf",
                tokens -> "");
        inst.setCustomCallIncludes("stdio.h");
        builder.addDependency(inst);
    }
}
