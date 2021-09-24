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

package org.specs.matisselib.ssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

public class SsaRecipe {
    private final List<SsaPass> passes;

    SsaRecipe(List<SsaPass> passes) {
	Preconditions.checkArgument(passes != null);

	this.passes = new ArrayList<>(passes);
    }

    public List<SsaPass> getPasses() {
	return Collections.unmodifiableList(passes);
    }

    public SsaPass get(int index) {
	return passes.get(index);
    }

    public int size() {
	return passes.size();
    }

    public static SsaRecipe empty() {
	return new SsaRecipe(Collections.emptyList());
    }

    public static SsaRecipe combine(SsaRecipe... recipes) {
	SsaRecipeBuilder builder = new SsaRecipeBuilder();

	Stream.of(recipes)
		.flatMap(recipe -> recipe.getPasses().stream())
		.forEach(builder::addPass);

	return builder.getRecipe();
    }
}
