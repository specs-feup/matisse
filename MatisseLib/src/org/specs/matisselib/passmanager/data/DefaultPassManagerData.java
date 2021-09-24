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

package org.specs.matisselib.passmanager.data;

import java.util.Map;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNodePass.BuilderPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.passmanager.PreTypeContext;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class DefaultPassManagerData extends APassManagerData {

    public DefaultPassManagerData(String setupName, DataKey<PreTypeContext> contextKey) {
	// super(new SimpleSetup(setupName), contextKey);
	super(DataStore.newInstance(setupName), contextKey);
    }

    private DefaultPassManagerData(DataStore setupBuilder, Map<FunctionIdentification, BuilderPassData> dataMap,
	    DataKey<PreTypeContext> contextKey, Optional<FunctionIdentification> currentId) {

	super(setupBuilder, dataMap, contextKey, currentId);
    }

    @Override
    public Optional<PassManagerData> newData(Optional<FunctionIdentification> functionId) {

	// Return empty if there is a functionId, and is not in the map
	if (functionId.isPresent() && !getDataMap().containsKey(functionId)) {
	    return Optional.empty();
	}

	return Optional.of(new DefaultPassManagerData(getSetupBuilder(), getDataMap(), getContextKey(), functionId));
    }

    /*
        public DefaultPassManagerData(PassData basePassData) {
    	super(new SimpleSetup(basePassData));
    	Preconditions.checkArgument(basePassData != null);
        }
    */
    /*
        public DefaultPassManagerData(String setupName, CleanSetup baseSetup) {
    	super(new SimpleSetup(setupName, baseSetup));
    	Preconditions.checkArgument(baseSetup != null);
        }
    */

}
