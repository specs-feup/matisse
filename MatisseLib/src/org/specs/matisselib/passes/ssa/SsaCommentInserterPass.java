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

package org.specs.matisselib.passes.ssa;

import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Insers a comment at the beginning of a node.
 *
 */
public class SsaCommentInserterPass extends TypeNeutralSsaPass {

    private final String comment;

    public SsaCommentInserterPass(String comment) {
	Preconditions.checkArgument(comment != null);

	this.comment = comment;
    }

    @Override
    public void apply(FunctionBody source, DataStore data) {
	source.getBlock(0).prependInstruction(new CommentInstruction(comment));
    }

}
