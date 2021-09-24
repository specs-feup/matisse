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

package org.specs.matisselib.typeinference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class InferenceRuleList implements Iterable<TypeInferenceRule> {
    public static InferenceRuleList EMPTY = new InferenceRuleList(Collections.emptyList());

    private final List<TypeInferenceRule> rules;

    public InferenceRuleList(List<TypeInferenceRule> rules) {
	this.rules = new ArrayList<>(rules);
    }

    @Override
    public Iterator<TypeInferenceRule> iterator() {
	return this.rules.iterator();
    }

    public Stream<TypeInferenceRule> stream() {
	return this.rules.stream();
    }
}
