package org.specs.matisselib.functions.dynamiccell;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.types.DynamicCellStruct;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class CellFree extends AInstanceBuilder {
    public static final String FILE_NAME = "lib/dynamic_cell";
    private static final ThreadSafeLazy<String> cellFreeResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(CellCreationResource.DYNAMIC_CELL_FREE));

    private CellFree(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return data -> new CellFree(data).create();
    }

    @Override
    public FunctionInstance create() {
        if (getData().getNumInputs() != 1) {
            throw getData().getReportService().emitError(PassMessage.NOT_YET_IMPLEMENTED,
                    "Cell free must take a single argument.");
        }

        VariableType cellArrayType = getData()
                .getInputType(DynamicCellType.class, 0)
                .pointer()
                .getType(true);

        // Input by-ref is newer than the free system, so it's not suitable
        // to use in this case.
        FunctionType functionType = FunctionTypeBuilder
                .newAuto()
                .addInput("result", cellArrayType)
                .withSideEffects()
                .build();

        String body = cellFreeResource.get();
        body = body.replaceAll("<CELL_DATA>", DynamicCellStruct.CELL_DATA);
        body = body.replaceAll("<CELL_SHAPE>", DynamicCellStruct.CELL_SHAPE);

        return new LiteralInstance(functionType, "dynamic_cell_free_" + cellArrayType.getSmallId(), FILE_NAME, body);
    }
}
