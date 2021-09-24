package org.specs.MatlabToC.Functions.Strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.strings.MatlabStringStructInstance;
import org.specs.matisselib.types.strings.MatlabStringType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class MatlabStringFromCStringConstructor extends AInstanceBuilder {

    private static final String FUNCTION_NAME = "string_from_cstring";
    private static final String DECLARATION_FILENAME = "lib/matlabstring";
    private static final ThreadSafeLazy<String> DECLARATION_CODE = new ThreadSafeLazy<String>(
            () -> SpecsIo.getResource(MatlabStringFunctionResources.STRING_FROM_CSTRING)
                    .replace("<STRUCT_NAME>", MatlabStringStructInstance.STRUCT_NAME));

    public MatlabStringFromCStringConstructor(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        StringType inputType = getData().getInputType(StringType.class, 0);
        MatlabStringType outputType = MatlabStringType.STRING_TYPE;

        FunctionType functionType = FunctionTypeBuilder.newWithSingleOutputAsInput()
                .addInput("str", inputType)
                .addOutputAsInput("outStr", outputType)
                .build();

        LiteralInstance instance = new LiteralInstance(
                functionType,
                FUNCTION_NAME,
                DECLARATION_FILENAME,
                DECLARATION_CODE.get());
        instance.setCustomImplementationIncludes("string.h", "stdio.h", "stdlib.h");
        return instance;
    }

    public static InstanceProvider getInstanceProvider() {

        MatisseInputsChecker checker = new MatisseInputsChecker()
                .numOfOutputsAtMost(1)
                .numOfInputs(1)
                .isString(0);

        return new GenericInstanceProvider(checker, data -> new MatlabStringFromCStringConstructor(data).create());
    }
}
