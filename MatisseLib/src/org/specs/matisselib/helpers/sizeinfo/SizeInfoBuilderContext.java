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

package org.specs.matisselib.helpers.sizeinfo;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public interface SizeInfoBuilderContext {

    void log(String msg);

    FunctionBody getFunctionBody();

    String getLoopSize();

    SizeGroupInformation getCurrentInfo();

    SizeGroupInformation buildInfoFor(int blockId,
	    String loopSize,
	    SizeGroupInformation info);

    int getBlockId();

    Optional<VariableType> getVariableType(String name);

    SizeGroupInformation handleInstruction(SizeInfoBuilderContext ctx,
	    SizeGroupInformation info,
	    int loopBlockId,
	    String end,
	    SsaInstruction instruction);

    List<InstructionInformationBuilder> getBuilders();
}
