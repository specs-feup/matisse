/**
 * Copyright 2016 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabToC.Functions.MatisseInternalFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class MatisseSignature extends AInstanceBuilder {

    private static final MatisseChecker CHECKER = new MatisseChecker()
            .numOfInputs(0)
            .numOfOutputsAtMost(1);

    public MatisseSignature(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return new MatlabInstanceProviderHelper(MatisseSignature.CHECKER, data -> new MatisseSignature(data).create());
    }

    @Override
    public FunctionInstance create() {
        DataStore setup = getData().getSettings();
        StringBuilder signatureBuilder = new StringBuilder("MATISSE, ");
        if (setup.get(MatlabToCKeys.USE_PASS_SYSTEM)) {
            signatureBuilder.append("with pass system, solver=");
            signatureBuilder.append(setup.get(MatlabToCKeys.ENABLE_Z3) ? "z3" : "dummy");
        } else {
            signatureBuilder.append("classic");
        }
        String signature = signatureBuilder.toString();
        StringType stringType = StringType.create(signature, getNumerics().newChar().getBits(), true);

        FunctionType functionType = FunctionTypeBuilder
                .newInline()
                .returning(stringType)
                .build();

        InlineCode code = tokens -> {
            return "\"" + signature + "\"";
        };
        InlinedInstance instance = new InlinedInstance(
                functionType,
                "$MATISSE_signature",
                code);
        instance.setCallPrecedenceLevel(PrecedenceLevel.Atom);
        return instance;
    }
}
