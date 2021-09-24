package org.specs.MatlabToC.Functions.StringFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.matisselib.types.strings.MatlabStringType;

public class Str2DoubleBuilder implements InstanceProvider {
    private Str2DoubleBuilder() {
    }

    public static InstanceProvider newStr2DoubleBuilder() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .ofType(MatlabStringType.class, 0);

        return new GenericInstanceProvider(checker, new Str2DoubleBuilder());
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInput(MatlabStringType.STRING_TYPE)
                .returning(data.getNumerics().newDouble())
                .build();

        InlineCode code = tokens -> {
            // FIXME
            // atof is not a perfect representation of str2double, as str2double does a lot more validation
            // but it's good enough for our purposes.
            return "atof(" + tokens.get(0).getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer) + "->data)";
        };
        return new InlinedInstance(functionType, "str2double$" + data.getInputTypes().get(0).getSmallId(), code);
    }
}
