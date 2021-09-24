/**
 * Copyright 2017 SPeCS.
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

import java.util.HashSet;
import java.util.Set;

import org.specs.matisselib.PassMessage;
import org.specs.matisselib.functionproperties.ExportProperty;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.typeinference.TypedInstanceContext;
import org.specs.matisselib.typeinference.TypedInstanceStateList;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.Reporter;

public class NoDuplicatedExportsValidationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        // Silently suppressing catch-up
        // This means that a SSA-to-C rule is generating new MATLAB instances.
        // None of those instances have explicit ABI names.
    }

    @Override
    public void apply(TypedInstanceStateList instances) {
        Set<String> explicitNames = new HashSet<>();

        for (TypedInstanceContext context : instances) {
            TypedInstance instance = context.instance;
            String abiName = instance.getPropertyStream(ExportProperty.class)
                    .findAny()
                    .map(prop -> prop.getAbiName().orElse(instance.getFunctionIdentification().getName()))
                    .orElse(null);
            if (abiName != null && !explicitNames.add(abiName)) {
                Reporter reportService = instance.getProviderData().getReportService();
                reportService.emitError(PassMessage.CORRECTNESS_ERROR,
                        "Multiple function instances with same explicit ABI name.");
            }
        }
    }

}
