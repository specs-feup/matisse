/**
 * Copyright 2015 SPeCS.
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

package org.specs.matisselib.passmanager;

import java.util.Map;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.passmanager.data.PassManagerData;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.providers.StringProvider;

public class SimplePassManager {

    private final PassManagerData managerData;
    private final PassManager manager;

    private SimplePassManager(PassManager manager, PassManagerData managerData) {
        this.manager = manager;
        this.managerData = managerData;
    }

    public static SimplePassManager apply(String rootFile,
            MatlabRecipe recipe,
            LanguageMode languageMode,
            Map<String, StringProvider> availableFile,
            DataView additionalServices) {

        DataStore data = new MatisseInit().newPassData(languageMode, availableFile, additionalServices);

        return apply(rootFile, recipe, data);
    }

    private static SimplePassManager apply(String rootFile, MatlabRecipe recipe, DataStore data) {

        MatlabAstPassManager manager = new PreTypeInferenceManagerV2(recipe, data);
        // PassManagerData managerData = manager.applyPasses(rootFile, recipe, data);
        manager.applyPreTypeInferencePasses(rootFile);

        // return new SimplePassManager(manager, managerData);
        return new SimplePassManager(manager.getPassManager(), manager.getPassData());
    }

    public <T> T get(DataKey<T> key) {
        return this.managerData.get(key);
    }

    public MatlabUnitNode getUnitFromFunction(String functionName) {
        return getUnit(new FunctionIdentification(functionName + ".m", functionName));
    }

    public MatlabUnitNode getUnit(String fileName) {
        return getUnit(new FunctionIdentification(fileName));
    }

    public MatlabUnitNode getUnit(FunctionIdentification functionId) {
        return this.manager.getUnit(functionId, this.managerData);
    }
}
