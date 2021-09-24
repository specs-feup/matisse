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

package org.specs.matisselib.unssa;

import java.util.List;
import java.util.function.Predicate;

import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.typeinference.TypedInstance;

public interface VariableAllocator {
    public VariableAllocation performAllocation(FunctionBody body, Predicate<List<String>> canMerge);

    public default VariableAllocation performAllocation(TypedInstance instance, GlobalTypeProvider globalTypeProvider) {
        return performAllocation(
                instance.getFunctionBody(),
                names -> instance.getCombinedVariableTypeFromVariables(names, globalTypeProvider).isPresent());
    }
}
