package org.specs.MatlabToC.Functions.Strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.strings.MatlabStringType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class MatlabStringEquals implements MatlabInstanceProvider {

    private static final String FUNCTION_NAME = "matlab_string_equals";
    private static final String DECLARATION_FILENAME = "lib/matlabstring";
    private static final ThreadSafeLazy<String> DECLARATION_CODE = new ThreadSafeLazy<String>(
            () -> SpecsIo.getResource(MatlabStringFunctionResources.EQUALS));

    @Override
    public FunctionInstance create(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder.newSimple()
                .addInput("str1", MatlabStringType.STRING_TYPE)
                .addInput("str2", MatlabStringType.STRING_TYPE)
                .returning(data.getNumerics().newInt())
                .build();

        LiteralInstance instance = new LiteralInstance(
                functionType,
                FUNCTION_NAME,
                DECLARATION_FILENAME,
                DECLARATION_CODE.get());
        return instance;
    }

    @Override
    public boolean checkRule(ProviderData data) {
        return new MatisseInputsChecker()
                .numOfOutputsAtMost(1)
                .numOfInputs(2)
                .ofType(MatlabStringType.class, 0)
                .ofType(MatlabStringType.class, 1)
                .create(data)
                .check();
    }
}
