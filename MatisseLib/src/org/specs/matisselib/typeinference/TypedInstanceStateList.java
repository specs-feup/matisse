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

package org.specs.matisselib.typeinference;

import java.util.Iterator;
import java.util.List;

import org.specs.matisselib.InferenceResult;

public class TypedInstanceStateList implements Iterable<TypedInstanceContext> {
    List<InferenceResult> inferenceResults;

    public TypedInstanceStateList(List<InferenceResult> inferenceResults) {
        this.inferenceResults = inferenceResults;
    }

    @Override
    public Iterator<TypedInstanceContext> iterator() {
        return new Iterator<TypedInstanceContext>() {
            int element = 0;

            @Override
            public TypedInstanceContext next() {
                InferenceResult result = inferenceResults.get(element++);

                return new TypedInstanceContext(result.instance, result.instancedPassData);
            }

            @Override
            public boolean hasNext() {
                return element < inferenceResults.size();
            }
        };
    }
}
