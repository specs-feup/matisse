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
import org.specs.MatlabIR.MatlabNode.StatementNode;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.reporting.ReporterUtils;

public class NodeReporter implements Reporter {

    private final Reporter parentReporter;
    private final MatlabNode source;
    private final PrintStream stream;
    private final String fileName;
    private final StringProvider fileProvider;

    public NodeReporter(Reporter parentReporter, MatlabNode source, PrintStream stream, String fileName,
	    StringProvider fileProvider) {
	Preconditions.checkArgument(stream != null);
	Preconditions.checkArgument(fileProvider != null);

	this.parentReporter = parentReporter;
	this.source = source;
	this.stream = stream;
	this.fileName = fileName;
	this.fileProvider = fileProvider;

    }

    @Override
    public void emitMessage(MessageType type, String message) {
	Preconditions.checkArgument(this.source != null);
	Preconditions.checkArgument(type != null);
	Preconditions.checkArgument(message != null);

	this.stream.println(ReporterUtils.formatMessage(type.getName(), message));
	printStackTrace(this.stream);

	if (type.getMessageCategory() == ReportCategory.ERROR) {
	    throw new RuntimeException();
	}
    }

    @Override
    public void printStackTrace(PrintStream reportStream) {
	int line = getLine(this.source);

	String code = this.fileProvider.getString();
	String codeLine = ReporterUtils.getErrorLine(code, line);

	this.stream.println(ReporterUtils.formatFileStackLine(this.fileName, line, codeLine));

	if (this.parentReporter != null) {
	    this.parentReporter.printStackTrace(reportStream);
	} else {
	    reportStream.print(ReporterUtils.stackEnd());
	}
    }

    private static int getLine(MatlabNode node) {
	if (node == null) {
	    // TODO
	    throw new UnsupportedOperationException();
	}
	if (node instanceof StatementNode) {
	    return ((StatementNode) node).getLine();
	}
	return getLine(node.getParent());
    }

    @Override
    public PrintStream getReportStream() {
	return this.stream;
    }
}
