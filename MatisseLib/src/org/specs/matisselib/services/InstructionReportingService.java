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

package org.specs.matisselib.services;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;

public interface InstructionReportingService {
    void emitMessage(FunctionBody body, SsaInstruction source, MessageType type, String message,
	    Exception internalException);

    default RuntimeException emitError(FunctionBody body, SsaInstruction source, MessageType type, String message,
	    Exception internalException) {

	Preconditions.checkArgument(type.getMessageCategory() == ReportCategory.ERROR);

	System.err.println("[InstructionReportingService]");
	System.err.println(body);
	emitMessage(body, source, type, message, internalException);

	return new RuntimeException(message);
    }

    default RuntimeException emitError(FunctionBody body, SsaInstruction source, MessageType type, String message) {
	return emitError(body, source, type, message, null);
    }

    default void emitMessage(FunctionBody body, SsaInstruction source, MessageType type, String message) {
	emitMessage(body, source, type, message, null);
    }

    default void emitMessage(TypedInstance body, SsaInstruction source, MessageType type, String message) {
	emitMessage(body.getFunctionBody(), source, type, message, null);
    }

    default RuntimeException emitError(TypedInstance body, SsaInstruction source, MessageType type, String message,
	    Exception internalException) {

	return emitError(body.getFunctionBody(), source, type, message, internalException);
    }

    default RuntimeException emitError(TypedInstance body, SsaInstruction source, MessageType type, String message) {
	return emitError(body, source, type, message, null);
    }
}
