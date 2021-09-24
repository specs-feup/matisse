package org.specs.MatlabToC.Functions.Builtin;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.IndirectionInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.matisselib.types.CellArrayType;

public class BuiltinCellBuilders {
    public static InstanceProvider newNumelBuilder() {
        MatisseChecker checker = new MatisseChecker().numOfInputs(1)
                .ofType(CellArrayType.class, 0);

        IndirectionInstanceProvider provider = data -> {
            CellArrayType cellType = (CellArrayType) data.getInputTypes().get(0);
            return cellType.cell().functions().numel();
        };

        return new GenericInstanceProvider(checker, provider);

    }
}
