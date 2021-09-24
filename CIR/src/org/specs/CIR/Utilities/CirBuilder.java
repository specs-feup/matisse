/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Utilities;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

public abstract class CirBuilder {

    private final DataStore settings;
    private final NumericFactory numerics;
    private final CNodeFactory cnodes;

    public CirBuilder(DataStore settings) {
        this.settings = settings;
        numerics = new NumericFactory(settings);
        cnodes = new CNodeFactory(settings);
    }

    /**
     * Helper method which accepts a ProviderData.
     * 
     * @param setup
     */
    public CirBuilder(ProviderData data) {
        this(data.getSettings());
    }

    public DataStore getSettings() {
        return settings;
    }

    public NumericFactory getNumerics() {
        return numerics;
    }

    public FunctionCallNode getFunctionCall(InstanceProvider provider, CNode... inputs) {
        return cnodes.newFunctionCall(provider, inputs);
    }

    public FunctionCallNode getFunctionCall(InstanceProvider provider, List<CNode> inputs) {
        return cnodes.newFunctionCall(provider, inputs);
    }
}
