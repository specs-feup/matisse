package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIRTypes.Types.Literal.LiteralType;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsIo;

public class RegisterFileResourceProvider implements InstanceProvider {
    public static FunctionInstance newInstance(ProviderData data) {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1);

        return new GenericInstanceProvider(checker, new RegisterFileResourceProvider())
                .newCInstance(data);
    }

    private RegisterFileResourceProvider() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        PointerType filePtrType = new PointerType(LiteralType.newInstance("void"));
        FunctionType functionType = FunctionTypeBuilder
                .newSimple()
                .addInput("f", filePtrType)
                .returning(data.getNumerics().newInt())
                .withSideEffects()
                .build();

        String body = SpecsIo.getResource(FileResources.REGISTER_FILE_RESOURCE);

        LiteralInstance instance = new LiteralInstance(functionType, "MATISSE_register_file_resource",
                FopenProvider.FILE, body);
        instance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);
        instance.addInstance(InitializeFileResourcesProvider.newInstance(data.create()));
        return instance;
    }
}
