package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.GlobalVariableInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsIo;

public class InitializeFileResourcesProvider implements InstanceProvider {
    public static FunctionInstance newInstance(ProviderData data) {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(0);

        return new GenericInstanceProvider(checker, new InitializeFileResourcesProvider())
                .newCInstance(data);
    }

    private InitializeFileResourcesProvider() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .withSideEffects()
                .build();

        String body = SpecsIo.getResource(FileResources.INITIALIZE_FILE_RESOURCES);

        LiteralInstance instance = new LiteralInstance(functionType, "MATISSE_initialize_file_resources",
                FopenProvider.FILE, body);
        instance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);
        instance.addInstance(FileGlobals.getFileResourcesGlobal());
        instance.addInstance(
                new GlobalVariableInstance("MATISSE_file_num_available_resources", data.getNumerics().newInt()));
        return instance;
    }
}
