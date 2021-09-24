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

package org.specs.matisselib.helpers;

import com.google.common.base.Preconditions;

public final class BranchBuilderResult {
    private final BlockEditorHelper ifBuilder;
    private final BlockEditorHelper elseBuilder;
    private final BlockEditorHelper endBuilder;

    public BranchBuilderResult(BlockEditorHelper ifBuilder,
	    BlockEditorHelper elseBuilder,
	    BlockEditorHelper endBuilder) {

	Preconditions.checkArgument(ifBuilder != null);
	Preconditions.checkArgument(elseBuilder != null);
	Preconditions.checkArgument(endBuilder != null);

	this.ifBuilder = ifBuilder;
	this.elseBuilder = elseBuilder;
	this.endBuilder = endBuilder;
    }

    public BlockEditorHelper getIfBuilder() {
	return ifBuilder;
    }

    public BlockEditorHelper getElseBuilder() {
	return elseBuilder;
    }

    public BlockEditorHelper getEndBuilder() {
	return endBuilder;
    }
}
