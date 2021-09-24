package org.specs.matisselib.functions.strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.matisselib.types.strings.MatlabStringStructInstance;
import org.specs.matisselib.types.strings.MatlabStringType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class MakeEmptyMatlabStringInstance extends AInstanceBuilder {

    private static final String FUNCTION_NAME = "matlab_string_make_empty";
    private static final ThreadSafeLazy<String> CODE = new ThreadSafeLazy<String>(
            () -> SpecsIo.getResource("matisselib/matlab_string_make_empty.c")
                    .replace("<STRUCT_NAME>", MatlabStringStructInstance.STRUCT_NAME));
    private static final String DECLARATION_FILENAME = "lib/matlabstring";

    public MakeEmptyMatlabStringInstance(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        FunctionType functionType = FunctionTypeBuilder
                .newWithSingleOutputAsInput()
                .addOutputAsInput("out", MatlabStringType.STRING_TYPE)
                .build();

        LiteralInstance instance = new LiteralInstance(functionType,
                FUNCTION_NAME,
                DECLARATION_FILENAME,
                CODE.get());
        instance.setCustomImplementationIncludes("string.h", "stdlib.h");
        return instance;
    }

    public static InstanceProvider getProvider() {
        return data -> new MakeEmptyMatlabStringInstance(data).create();
    }

}
