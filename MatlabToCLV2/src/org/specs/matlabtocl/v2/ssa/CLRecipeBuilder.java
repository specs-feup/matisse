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

package org.specs.matlabtocl.v2.ssa;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public final class CLRecipeBuilder {
    private final List<CLPass> passes = new ArrayList<>();

    public CLRecipeBuilder() {
    }

    public void addPass(CLPass pass) {
	Preconditions.checkArgument(pass != null, "pass must not be null");

	this.passes.add(pass);
    }

    public CLRecipe getRecipe() {
	return new CLRecipe(this.passes);
    }
}
