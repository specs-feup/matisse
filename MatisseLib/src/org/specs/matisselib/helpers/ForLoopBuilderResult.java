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

public final class ForLoopBuilderResult {
    private final BlockEditorHelper loopBuilder;
    private final BlockEditorHelper endBuilder;

    public ForLoopBuilderResult(BlockEditorHelper loopBuilder,
	    BlockEditorHelper endBuilder) {

	Preconditions.checkArgument(loopBuilder != null);
	Preconditions.checkArgument(endBuilder != null);

	this.loopBuilder = loopBuilder;
	this.endBuilder = endBuilder;
    }

    public BlockEditorHelper getLoopBuilder() {
	return loopBuilder;
    }

    public BlockEditorHelper getEndBuilder() {
	return endBuilder;
    }
}
