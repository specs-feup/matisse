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

package org.specs.matisselib;

import java.io.PrintStream;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.reporting.ReporterUtils;

/**
 * A simple reporting service that merely prints the warnings and errors to the command line.
 * 
 * @author Luís Reis
 *
 */
public class DefaultReportService implements Reporter {

    private final Reporter parent;
    private final PrintStream reportStream;
    private final boolean isInternal;
    private final FunctionIdentification function;
    private int currentLine = -1;
    private final StringProvider originalCode;

    public DefaultReportService(Reporter parent,
	    PrintStream reportStream,
	    boolean isInternal,
	    FunctionIdentification function,
	    StringProvider originalCode) {

	this.parent = parent;
	this.reportStream = reportStream;
	this.isInternal = isInternal;
	this.function = function;
	this.originalCode = originalCode;
    }

    @Override
    public void emitMessage(MessageType type, String message) {
	Preconditions.checkArgument(type != null, "type must not be null");
	Preconditions.checkArgument(message != null, "message must not be null");

	this.reportStream.println(ReporterUtils.formatMessage(type.getName(), message));
	printStackTrace(this.reportStream);

	if (type.getMessageCategory() == ReportCategory.ERROR) {
	    throw new CodeGenerationException("Got error " + message);
	}
    }

    @Override
    public void printStackTrace(PrintStream reportStream) {
	String stackTraceLine = ReporterUtils.formatFunctionStackLine(this.function.getName(), this.function.getFile(),
		getLineNumber(), getErrorLine());
	reportStream.println(stackTraceLine);

	if (this.parent != null) {
	    this.parent.printStackTrace(reportStream);
	} else {
	    reportStream.print(ReporterUtils.stackEnd());
	}
    }

    private int getLineNumber() {
	return this.currentLine;
    }

    public DefaultReportService withLineNumber(int currentLine) {
	DefaultReportService reportService = new DefaultReportService(this.parent, this.reportStream, this.isInternal,
		this.function,
		this.originalCode);
	reportService.currentLine = currentLine;
	return reportService;
    }

    private String getErrorLine() {
	String code = this.originalCode.getString();
	if (code == null) {
	    return "No code available";
	}
	int lineNumber = getLineNumber();
	if (lineNumber < 1) {
	    return "No code available";
	}
	String[] fileContent = code.split("\n");
	assert lineNumber - 1 < fileContent.length : "Attempting to access line " + lineNumber + " of file "
		+ this.function.getFile()
		+ ":\n" + code;
	return fileContent[lineNumber - 1];
    }

    @Override
    public PrintStream getReportStream() {
	return this.reportStream;
    }

    @Override
    public String toString() {
	return "[DefaultReportService: " + this.function + ":" + this.currentLine + "]";
    }

}
