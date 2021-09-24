package org.specs.matisselib.functions.dynamiccell;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.types.DynamicCellStruct;
import org.specs.matisselib.types.DynamicCellType;

public class CellGet extends AInstanceBuilder {
    public static final String FILE_NAME = "lib/dynamic_cell";

    private CellGet(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return data -> new CellGet(data).create();
    }

    @Override
    public FunctionInstance create() {
        if (getData().getNumInputs() != 2) {
            throw getData().getReportService().emitError(PassMessage.NOT_YET_IMPLEMENTED,
                    "Cell array gets where the index is not a single scalar.");
        }

        DynamicCellType cellArrayType = getData().getInputType(DynamicCellType.class, 0);
        VariableType underlyingType = cellArrayType.getUnderlyingType();

        FunctionType functionType = FunctionTypeBuilder
                .newAuto()
                .addInput("cell", cellArrayType)
                .addInput("index", getNumerics().newInt())
                .addOutputAsInput("out", underlyingType)
                .build();

        CInstructionList body = new CInstructionList(functionType);

        CNode indexNode = CNodeFactory.newVariable("index", getNumerics().newInt());
        CNode outNode = CNodeFactory.newVariable("out", underlyingType);
        String dataAccess = "cell->" + DynamicCellStruct.CELL_DATA + "["
                + indexNode.getCodeForContent(PrecedenceLevel.ArrayAccess) + "]";
        CNode dataAccessNode = CNodeFactory.newLiteral(dataAccess, underlyingType, PrecedenceLevel.ArrayAccess);

        CNode tmpVar = CNodeFactory.newVariable("tmpVar", underlyingType);
        body.addAssignment(tmpVar, dataAccessNode);

        body.addInstruction(AssignmentUtils.buildAssignmentNode(outNode, tmpVar, getData()));

        body.addReturn(outNode);

        return new InstructionsInstance(getFunctionName("cell_get", getData().getInputTypes()),
                FILE_NAME, body);
    }
}
