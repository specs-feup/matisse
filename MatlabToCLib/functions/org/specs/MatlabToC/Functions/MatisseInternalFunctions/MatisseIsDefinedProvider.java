package org.specs.MatlabToC.Functions.MatisseInternalFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRFunctions.Common.IsDefinedProvider;
import org.specs.matisselib.PassMessage;

public class MatisseIsDefinedProvider implements InstanceProvider {
    private final InstanceProvider baseProvider;

    public MatisseIsDefinedProvider() {
        baseProvider = IsDefinedProvider.create();
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        validate(data);

        return baseProvider.newCInstance(data);
    }

    @Override
    public FunctionType getType(ProviderData data) {
        validate(data);

        return baseProvider.getType(data);
    }

    private static void validate(ProviderData data) {
        if (data.getInputTypes().size() != 1) {
            throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
                    "MATISSE_is_defined requires exactly 1 input.");
        }
        VariableType type = data.getInputTypes().get(0);
        if (!type.usesDynamicAllocation()) {
            throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
                    "MATISSE_is_define requires a dynamic matrix element. Instead, got " + type);
        }
    }

    public static InstanceProvider create() {
        return new MatisseIsDefinedProvider();
    }
}
