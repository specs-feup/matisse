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

package org.specs.matisselib;

import org.specs.matisselib.functionproperties.DisableOptimizationProperty;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class PassUtils {
    public static boolean approveIn(DataService<?> key, DataService<?>... options) {
        for (DataService<?> service : options) {
            if (service.getName().equals(key.getName())) {
                return true;
            }
        }

        return false;
    }

    public static <T> T getData(DataStore dataStore, DataService<T> dataType) {
        return dataStore.get(ProjectPassServices.DATA_PROVIDER).buildData(dataType);
    }

    public static boolean skipPass(FunctionBody source, String optimizationId) {
        return source.getPropertyStream(DisableOptimizationProperty.class)
                .anyMatch(property -> property.getOptimizationId().equals(optimizationId));
    }

    public static boolean skipPass(TypedInstance instance, String optimizationId) {
        return skipPass(instance.getFunctionBody(), optimizationId);
    }

    public static Logger getLogger(DataStore passData, String passName) {
        return passData.get(PreTypeInferenceServices.LOG)
                .getLogger(passName);
    }
}
