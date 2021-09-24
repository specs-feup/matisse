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

import java.io.PrintStream;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.matisselib.services.TokenReportingService;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.reporting.MessageType;

public class NodeReportService implements TokenReportingService {

    private final PrintStream stream;
    private final String fileName;
    private final StringProvider fileProvider;

    public NodeReportService(PrintStream stream, String fileName, StringProvider fileProvider) {
	Preconditions.checkArgument(stream != null);
	Preconditions.checkArgument(fileProvider != null);

	this.stream = stream;
	this.fileName = fileName;
	this.fileProvider = fileProvider;
    }

    private NodeReporter get(MatlabNode source) {
	return new NodeReporter(null, source, stream, fileName, fileProvider);
    }

    @Override
    public void emitMessage(MatlabNode source, MessageType type, String message) {
	get(source).emitMessage(type, message);
    }

    @Override
    public RuntimeException emitError(MatlabNode source, MessageType type, String message) {
	return get(source).emitError(type, message);
    }

}
