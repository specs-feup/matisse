/**
 * Copyright 2013 SPeCS Research Group.
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

package org.suikasoft.CMainFunction.Builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * @author Joao Bispo
 * 
 */
public enum MainFunctionTarget {

    MicroblazeSimulator,
    Linux,
    Windows,
    MultiTarget;

    public boolean includesWindows() {
	return this == Windows || this == MultiTarget;
    }

    public boolean includesLinux() {
	return this == Linux || this == MultiTarget;
    }

    /* (non-Javadoc)
     * @see org.specs.CIRFunctions.Main.TimeMeasurer#getPrologue()
     */
    public List<CNode> getPrologue(FunctionInstance printf) {
	switch (this) {
	case MicroblazeSimulator:
	    return buildMicroBlazePrologue(printf);
	case Linux:
	    return buildLinuxPrologue();
	case Windows:
	    return buildWindowsPrologue();
	case MultiTarget:
	    return Arrays.asList(CNodeFactory.newLiteral(SpecsIo.getResource(MultiResource.TIME_PROLOGUE)));
	default:
	    throw new RuntimeException("Case not implemented: '" + this + "'");
	}
    }

    /* (non-Javadoc)
     * @see org.specs.CIRFunctions.Main.TimeMeasurer#getEpilogue()
     */
    public List<CNode> getEpilogue(FunctionInstance printf) {
	switch (this) {
	case MicroblazeSimulator:
	    return buildMicroBlazeEpilogue(printf);
	case Linux:
	    return buildLinuxEpilogue();
	case Windows:
	    return buildWindowsEpilogue();
	case MultiTarget:
	    return Arrays.asList(CNodeFactory.newLiteral(SpecsIo.getResource(MultiResource.TIME_EPILOGUE)));
	default:
	    throw new RuntimeException("Case not implemented: '" + this + "'");
	}
    }

    /* (non-Javadoc)
     * @see org.specs.CIRFunctions.Main.TimeMeasurer#getLiteralDeclaration()
     */
    public List<String> getLiteralDeclaration() {
	switch (this) {
	case MicroblazeSimulator:
	    return Collections.emptyList();
	case Linux:
	    return SpecsFactory.newArrayList(Arrays.asList("double timeElapsed;", "struct timespec start, end;"));
	case Windows:
	    return SpecsFactory.newArrayList(Arrays.asList("double timeElapsed;", "double start, end;"));
	case MultiTarget:
	    return StringLines.getLines(SpecsIo.getResource(MultiResource.LITERAL_DEC));
	default:
	    throw new RuntimeException("Case not implemented: '" + this + "'");
	}
    }

    /* (non-Javadoc)
     * @see org.specs.CIRFunctions.Main.TimeMeasurer#getImplementationIncludes()
     */
    public List<String> getImplementationIncludes() {
	switch (this) {
	case MicroblazeSimulator:
	    return Collections.emptyList();
	case Linux:
	    // List<String> lIncludes = Arrays.asList(SystemInclude.Time.getIncludeName(),
	    // SystemInclude.Stdio.getIncludeName());
	    List<String> lIncludes = SpecsCollections.getKeyList(Arrays.asList(SystemInclude.Time, SystemInclude.Stdio));

	    return SpecsFactory.newArrayList(lIncludes);
	case Windows:
	case MultiTarget:
	    return Collections.emptyList();
	default:
	    throw new RuntimeException("Case not implemented: '" + this + "'");
	}
    }

    private static List<CNode> buildMicroBlazePrologue(FunctionInstance printf) {
	CInstructionList instList = new CInstructionList();

	// Calibration
	instList.addComment("Calibration");
	instList.addFunctionCall(printf, CNodeFactory.newLiteral("\"X\\n\""));
	instList.addFunctionCall(printf, CNodeFactory.newLiteral("\"Z\\n\""));
	instList.addLiteralInstruction("");
	instList.addComment("Start measuring cycles");
	instList.addFunctionCall(printf, CNodeFactory.newLiteral("\"X\\n\""));

	return instList.get();
    }

    private static List<CNode> buildMicroBlazeEpilogue(FunctionInstance printf) {
	CInstructionList instList = new CInstructionList();

	instList.addLiteralInstruction("");
	instList.addComment("Stop measuring cycles");
	instList.addFunctionCall(printf, CNodeFactory.newLiteral("\"Y\\n\""));

	return instList.get();
    }

    private static List<CNode> buildLinuxPrologue() {
	CInstructionList instList = new CInstructionList();

	instList.addComment("Start measuring");
	instList.addLiteralInstruction("clock_gettime(CLOCK_MONOTONIC, &start);");
	instList.addLiteralInstruction("\n");

	return instList.get();
    }

    private static List<CNode> buildLinuxEpilogue() {
	CInstructionList instList = new CInstructionList();

	instList.addLiteralInstruction("\n");
	instList.addComment("Stop measuring");
	instList.addLiteralInstruction("clock_gettime(CLOCK_MONOTONIC, &end);");

	instList.addLiteralInstruction("timeElapsed = (end.tv_sec + ((double) end.tv_nsec / 1000000000)) - "
		+ "(start.tv_sec + ((double) start.tv_nsec / 1000000000));");

	// instList.addLiteralInstruction("printf(\"TIME: %f\\n\", timeElapsed);");
	instList.addLiteralInstruction("printf(\"" + getTimePrefix() + "%e\\n\", timeElapsed);");

	return instList.get();
    }

    /**
     * @return
     */
    private static List<CNode> buildWindowsPrologue() {
	CInstructionList instList = new CInstructionList();

	instList.addComment("Start measuring");
	instList.addLiteralInstruction("start = get_time_in_seconds();");
	instList.addLiteralInstruction("\n");

	return instList.get();
    }

    private static List<CNode> buildWindowsEpilogue() {
	CInstructionList instList = new CInstructionList();

	instList.addLiteralInstruction("\n");
	instList.addComment("Stop measuring");
	instList.addLiteralInstruction("end = get_time_in_seconds();");

	instList.addLiteralInstruction("timeElapsed = end - start;");

	// instList.addLiteralInstruction("printf(\"TIME: %f\\n\", timeElapsed);");
	instList.addLiteralInstruction("printf(\"" + getTimePrefix() + "%e\\n\", timeElapsed);");

	return instList.get();
    }

    public static String getTimePrefix() {
	return "TIME: ";
    }

    public static double decodeTimeMeasure(String timeMeasure) {
	assert timeMeasure != null;
	// Preconditions.checkArgument(timeMeasure != null, "Input string cannot be null");

	if (!timeMeasure.startsWith(getTimePrefix())) {
	    throw new RuntimeException("Given string '" + timeMeasure
		    + "' does not represent a time measure. Shoud start with '" + getTimePrefix() + "'.");
	    // return null;
	}

	String value = timeMeasure.substring(getTimePrefix().length());
	value = value.trim();

	// return ParseUtils.parseDouble(value);
	return Double.parseDouble(value);
    }
}
