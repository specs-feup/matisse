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

package org.specs.matisselib.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.reporting.MessageType;

public class DeferredInstructionReportService implements InstructionReportingService {

    private List<String> messages = new ArrayList<>();

    @Override
    public void emitMessage(FunctionBody body,
	    SsaInstruction source,
	    MessageType type,
	    String message,
	    Exception internalException) {

	StringBuilder messageBuilder = new StringBuilder();

	messageBuilder.append(body.getName());
	messageBuilder.append(" ");
	messageBuilder.append(type);
	messageBuilder.append(": ");
	messageBuilder.append(message);
	messageBuilder.append("\n\t");
	messageBuilder.append(internalException.getMessage());

	messages.add(messageBuilder.toString());
    }

    public List<String> getMessages() {
	return Collections.unmodifiableList(messages);
    }
}
