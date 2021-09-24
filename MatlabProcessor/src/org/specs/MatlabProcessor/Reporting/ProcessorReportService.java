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

package org.specs.MatlabProcessor.Reporting;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.specs.MatlabIR.Exceptions.CodeParsingException;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.reporting.ReporterUtils;

public class ProcessorReportService implements Reporter {

    private final String fileName;
    private final Reporter parentReporter;
    private int lineNumber;
    private String codeLine;
    private final PrintStream outputStream;

    public ProcessorReportService(Reporter parentReporter, String fileName, Optional<PrintStream> outputStream) {
	this(parentReporter, fileName, outputStream.orElse(new PrintStream(new ByteArrayOutputStream())));
    }

    public ProcessorReportService(Reporter parentReporter, String fileName, PrintStream outputStream) {
	this.parentReporter = parentReporter;
	this.fileName = fileName;
	this.outputStream = outputStream;
    }

    public void setCurrentLine(int lineNumber, String codeLine) {
	this.lineNumber = lineNumber;
	this.codeLine = codeLine;
    }

    public String getCodeLine() {
	if (this.codeLine == null) {
	    return "<No code available>";
	}
	return this.codeLine;
    }

    @Override
    public void emitMessage(MessageType type, String message) {
	String messageType = type.getName();

	this.outputStream.println(ReporterUtils.formatMessage(messageType, message));
	printStackTrace(this.outputStream);

	if (type.getMessageCategory() == ReportCategory.ERROR) {
	    throw new CodeParsingException(message);
	}
    }

    @Override
    public PrintStream getReportStream() {
	return this.outputStream;
    }

    @Override
    public void printStackTrace(PrintStream reportStream) {
	reportStream.println(ReporterUtils.formatFileStackLine(this.fileName, this.lineNumber, getCodeLine()));
	if (this.parentReporter != null) {
	    this.parentReporter.printStackTrace(reportStream);
	} else {
	    reportStream.print(ReporterUtils.stackEnd());
	}
    }

}
