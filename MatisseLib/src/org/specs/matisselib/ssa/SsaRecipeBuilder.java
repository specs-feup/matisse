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
import java.util.List;

import com.google.common.base.Preconditions;

public final class SsaRecipeBuilder {
    private final List<SsaPass> passes = new ArrayList<>();

    public SsaRecipeBuilder() {
    }

    public void addPass(SsaPass pass) {
	Preconditions.checkArgument(pass != null, "pass must not be null");

	passes.add(pass);
    }

    public SsaRecipe getRecipe() {
	return new SsaRecipe(passes);
    }
}
