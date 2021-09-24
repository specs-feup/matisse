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

package org.specs.matisselib.passes.posttype;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class SolverInformationPrinterPass extends TypeTransparentSsaPass {

    @Override
    public void apply(FunctionBody body, ProviderData providerData, Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary, DataStore passData) {

        SizeGroupInformation sizes = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        System.out.print("[solver_information_printer] ");
        System.out.println(sizes);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return true;
    }
}
