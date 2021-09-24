package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsIo;

public class FopenProvider implements InstanceProvider {
    public static final String FILE = "lib/io";

    public static InstanceProvider getProvider() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .isString(0);

        return new GenericInstanceProvider(checker, new FopenProvider());
    }

    private FopenProvider() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        StringType input1 = data.getInputType(StringType.class, 0);
        FunctionType functionType = FunctionTypeBuilder
                .newSimple()
                .addInput("in", input1)
                .returning(data.getNumerics().newInt())
                .withSideEffects()
                .build();

        String body = SpecsIo.getResource(FileResources.FOPEN1);

        LiteralInstance instance = new LiteralInstance(functionType, "fopen_" + input1.getSmallId() + "_1", FILE, body);
        instance.setCustomImplementationIncludes(SystemInclude.Stdio);
        instance.addInstance(RegisterFileResourceProvider.newInstance(data));
        return instance;
    }
}
