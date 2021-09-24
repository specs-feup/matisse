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

package org.specs.matisselib;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;

import com.google.common.base.Preconditions;

public final class MatlabRecipeBuilder {
    private final List<MatlabNodePass> passes = new ArrayList<>();

    public MatlabRecipeBuilder() {
    }

    public void addPass(MatlabNodePass pass) {
	Preconditions.checkArgument(pass != null, "pass must not be null");

	passes.add(pass);
    }

    public MatlabRecipe getRecipe() {
	return new MatlabRecipe(passes);
    }
}
