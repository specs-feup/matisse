package org.specs.MatlabToC.Functions.Strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.strings.MatlabStringType;

public class MatlabStringLength implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData data) {
        MatlabStringType input = data.getInputType(MatlabStringType.class, 0);
        FunctionType functionType = FunctionTypeBuilder
                .newInline()
                .addInput(input)
                .returning(data.getNumerics().newInt())
                .build();

        InlineCode code = tokens -> {
            return tokens.get(0).getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer) + "->length";
        };

        return new InlinedInstance(functionType, "$strlength", code);
    }

    @Override
    public boolean checkRule(ProviderData data) {
        return new MatisseInputsChecker()
                .numOfOutputsAtMost(1)
                .numOfInputs(1)
                .ofType(MatlabStringType.class, 0)
                .create(data)
                .check();
    }

}
