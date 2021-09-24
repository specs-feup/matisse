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

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getInputName;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class CreateCellFromDims extends AInstanceBuilder {
    private static final ThreadSafeLazy<String> newCellBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(CellCreationResource.NEW_CELL_BODY));

    private CreateCellFromDims(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return data -> new CreateCellFromDims(data).create();
    }

    @Override
    public FunctionInstance create() {
        if (getData().getNargouts().orElse(-1) != 1) {
            throw getData().getReportService().emitError(PassMessage.INTERNAL_ERROR,
                    "CreateCellFromDims requires one output, got " + getData());
        }

        DynamicCellType outputType = (DynamicCellType) getData().getOutputType();
        int numDims = getData().getNumInputs();

        String functionName = "new_cell_" + outputType.getSmallId() + "_" + numDims;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            String inputName = getInputName(i);
            inputNames.add(inputName);
        }

        // Input types
        VariableType intType = getNumerics().newInt();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            inputTypes.add(intType);
        }

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "cellArray",
                outputType);

        // Build body
        String cBody = newCellBodyResource.getValue();

        cBody = cBody.replace("<NUM_DIMS>", Integer.toString(numDims));
        // cBody = cBody.replace("<DIM_NAMES>", getDimNames(numDims));
        cBody = cBody.replace("<SHAPE_INIT>", getShapeInit(numDims));

        FunctionInstance cellHelper = CreateDynamicCellHelper.getProvider(outputType).newCInstance(getData());
        cBody = cBody.replace("<CALL_NEW_HELPER>", cellHelper.getCName());

        LiteralInstance newEmptyCell = new LiteralInstance(fTypes, functionName, CellGet.FILE_NAME, cBody);

        // Set instance
        newEmptyCell.getCustomImplementationInstances().add(cellHelper);

        return newEmptyCell;
    }

    private static String getShapeInit(int numDims) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < numDims; i++) {
            // shape[0] = dim_1;
            if (i != 0) {
                builder.append("\t");
            }
            String dimVariableName = "dim_" + (i + 1);
            builder.append("shape[" + i + "] = " + dimVariableName + " > 0 ? " + dimVariableName + " : 0;\n");

        }

        return builder.toString();
    }
}
