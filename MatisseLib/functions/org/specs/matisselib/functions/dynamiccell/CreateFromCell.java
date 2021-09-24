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

package org.specs.matisselib.functions.dynamiccell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.types.DynamicCellStruct;
import org.specs.matisselib.types.DynamicCellType;

public class CreateFromCell extends AInstanceBuilder {
    private CreateFromCell(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return data -> new CreateFromCell(data).create();
    }

    @Override
    public FunctionInstance create() {
        if (getData().getNumInputs() != 1 || getData().getNargouts().orElse(-1) != 1) {
            throw getData().getReportService().emitError(PassMessage.INTERNAL_ERROR,
                    "CreateFromCell requires one input and one output, got " + getData());
        }

        DynamicCellType type = getTypeAtIndex(DynamicCellType.class, 0);

        NumericTypeV2 intType = getNumerics().newInt();
        PointerType intPointerType = new PointerType(intType);
        ProviderData cellHelperData = getData().create(intPointerType, intType);
        cellHelperData.setOutputType(type);
        FunctionInstance cellHelper = CreateDynamicCellHelper.getProvider(type).newCInstance(cellHelperData);

        InlineCode code = args -> {

            CNode cellNode = args.get(0);
            if (!(cellNode instanceof VariableNode)) {
                throw new RuntimeException("CNode should be a VariableNode: " + cellNode);
            }

            Variable var = ((VariableNode) cellNode).getVariable();

            CNode shapeNode = CNodeFactory.newLiteral(DynamicCellStruct.getShapeCode(var.getName(), type));
            CNode dimsNode = getFunctionCall(type.cell().functions().numDims(), cellNode);

            List<CNode> inputArgs = new ArrayList<>();
            inputArgs.add(shapeNode);
            inputArgs.add(dimsNode);
            inputArgs.add(args.get(1));

            return CodeGeneratorUtils.functionCallCode(cellHelper.getCName(),
                    cellHelper.getFunctionType().getCInputTypes(), inputArgs);

        };

        String functionName = "create_from_cell$" + cellHelper.getCName();
        FunctionType fType = FunctionType.newInstanceWithOutputsAsInputs(
                Arrays.asList("in"), Arrays.asList(type),
                "out", type);

        InlinedInstance instance = new InlinedInstance(fType, functionName, code);

        instance.setCallInstances(cellHelper);

        return instance;
    }
}
