package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsIo;

public class FcloseProvider implements InstanceProvider {
    public static InstanceProvider getProvider() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .isInteger(0);

        return new GenericInstanceProvider(checker, new FcloseProvider());
    }

    private FcloseProvider() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder
                .newSimple()
                .addInput("res", data.getNumerics().newInt())
                .returningVoid()
                .withSideEffects()
                .build();

        String body = SpecsIo.getResource(FileResources.FCLOSE);

        LiteralInstance instance = new LiteralInstance(functionType, "fclose_1", FopenProvider.FILE,
                body);
        instance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);
        instance.addInstance(InitializeFileResourcesProvider.newInstance(data.create()));
        return instance;
    }
}
