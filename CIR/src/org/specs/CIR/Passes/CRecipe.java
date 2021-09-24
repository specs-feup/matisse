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

package org.specs.CIR.Passes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

public final class CRecipe {
    private final List<CPass> passes;

    CRecipe(List<CPass> passes) {
	Preconditions.checkArgument(passes != null);

	this.passes = new ArrayList<>(passes);
    }

    public List<CPass> getPasses() {
	return Collections.unmodifiableList(passes);
    }

    public CPass get(int index) {
	return passes.get(index);
    }

    public int size() {
	return passes.size();
    }

    public static CRecipe empty() {
	return new CRecipe(Collections.emptyList());
    }

    public static CRecipe fromSinglePass(CPass pass) {
	Preconditions.checkArgument(pass != null);

	return new CRecipe(Arrays.asList(pass));
    }
}
