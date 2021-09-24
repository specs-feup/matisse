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

import org.specs.matisselib.helpers.GenericSizeGroupInformationBuilder;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matisselib.unssa.ControlFlowGraphBuilder;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CompilerDataProviders {
    public static final DataService<ControlFlowGraph> CONTROL_FLOW_GRAPH = new DataService<>("cfg", (body, data) -> {
        return ControlFlowGraphBuilder.build(body.getFunctionBody());
    });

    public static final DataService<SizeGroupInformation> SIZE_GROUP_INFORMATION = new DataService<>(
            "size_group_information",
            (TypedInstance instance, DataStore dataStore) -> {
                return GenericSizeGroupInformationBuilder.build(instance.getFunctionBody(), dataStore,
                        instance::getVariableType);
            }, (SizeGroupInformation value) -> {
                value.close();
            });
}
