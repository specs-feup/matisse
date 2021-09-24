/**
 * Copyright 2016 SPeCS.
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
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssumeMatrixIndicesInRangeDirectiveInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class AddAssumeMatrixIndicesInRangeDirectivePass extends TypeNeutralSsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
        SsaBlock firstBlock = source.getBlock(0);

        firstBlock.prependInstruction(
                new AssumeMatrixIndicesInRangeDirectiveInstruction());
    }

}
