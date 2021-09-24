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

package org.specs.matisselib.services.reporting;

import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.reporting.MessageType;

public class CommonInstructionReportingService implements InstructionReportingService {
    private final DefaultReportService reporter;

    public CommonInstructionReportingService(DefaultReportService reporter) {

	this.reporter = reporter;
    }

    @Override
    public void emitMessage(FunctionBody body, SsaInstruction source, MessageType type, String message,
	    Exception internalException) {
	Preconditions.checkArgument(body != null);
	Preconditions.checkArgument(source != null);
	Preconditions.checkArgument(type != null);
	Preconditions.checkArgument(message != null);

	int line = getLine(body, source);
	reporter.withLineNumber(line).emitMessage(type, message);
    }

    private static int getLine(FunctionBody body, SsaInstruction source) {
	return getLineFromBlock(body, source, 0, -1);
    }

    private static int getLineFromBlock(FunctionBody body, SsaInstruction source, int currentBlock, int lastLine) {
	return body.getLineFromBlock(source, currentBlock, lastLine);
    }
}
