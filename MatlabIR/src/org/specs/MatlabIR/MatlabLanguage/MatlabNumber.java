/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabIR.MatlabLanguage;

import java.math.BigInteger;

import org.specs.MatlabIR.MatlabParsingUtils;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * Represents a number in MatLab.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabNumber {

    // private final Long leftHand;
    private final String leftHand;
    private final String rightHand;
    private final String exponent;
    private final boolean isComplex;
    private final boolean hasDot;
    private final boolean isReal;

    private boolean capitalE;
    private boolean complexj;

    /**
     * @param leftHand
     * @param rightHand
     * @param exponent
     * @param isComplex
     * @param isReal
     */
    private MatlabNumber(String leftHand, String rightHand, String exponent, boolean isComplex,
	    boolean hasDot, boolean isReal) {

	this.leftHand = leftHand;
	this.rightHand = rightHand;
	this.exponent = exponent;
	this.isComplex = isComplex;
	this.hasDot = hasDot;
	this.isReal = isReal;
    }

    /**
     * @param capitalE
     *            the capitalE to set
     */
    public void setCapitalE(boolean capitalE) {
	this.capitalE = capitalE;
    }

    /**
     * @param complexj
     *            the complexj to set
     */
    public void setComplexj(boolean complexj) {
	this.complexj = complexj;
    }

    /**
     * @return the rightHand
     */
    // public Long getRightHand() {
    public String getRightHand() {
	return rightHand;
    }

    /**
     * @return the exponent
     */
    // public Long getExponent() {
    public String getExponent() {
	return exponent;
    }

    /**
     * Checks for: - Signal - Number prefix - Dot - Number suffix - Scientific notation - j/i
     * 
     * TODO: make small functions with prefix, dot, j, etc...
     * 
     * @param currentLine
     * @return null if could not parse a number
     */
    public static MatlabNumber getMatlabNumber(String line) {
	return getMatlabNumber(new StringSlice(line)).number;
    }

    /**
     * Tuple for the result of MatlabNumber parser.
     * 
     * @author JoaoBispo
     *
     */
    public static class MatlabNumberResult {
	public final int numParsedChars;
	public final MatlabNumber number;

	public MatlabNumberResult(int numParsedChars, MatlabNumber number) {
	    this.numParsedChars = numParsedChars;
	    this.number = number;
	}

    }

    /**
     * Checks for: - Signal - Number prefix - Dot - Number suffix - Scientific notation - j/i
     * 
     * TODO: make small functions with prefix, dot, j, etc...
     * 
     * @param currentLine
     * @return null if could not parse a number
     */
    public static MatlabNumberResult getMatlabNumber(StringSlice line) {
	Preconditions.checkArgument(line != null);
	Preconditions.checkArgument(line.charAt(0) != '-', "MatlabNumber does not support negative values.");

	StringSlice prefix = null;
	StringSlice workline = line;

	boolean isReal = false;

	// Number prefix
	prefix = getPrefixNumber(workline);
	// Long leftPart = null;
	String leftPart = null;
	if (prefix.length() > 0) {
	    BigInteger bigInteger = new BigInteger(prefix.toString());
	    // TODO: 1) Make lefthand String instead of Long; if greater than 64, force number to be a float (add .)
	    // If greater than 64, force number to be a float (add . if not present)
	    if (bigInteger.bitLength() + 1 > 64) {
		// Check if there is a '.'
		if (workline.toString().indexOf('.') == -1) {
		    isReal = true;
		    // workline = new StringSlice(workline.toString() + ".0");
		}
		// throw new RuntimeException(
		// "Compiler does not support textual integer numbers bigger than signed 64 bits. Given number: "
		// + prefix + ".");
	    }

	    // leftPart = bigInteger.longValue();
	    leftPart = bigInteger.toString();
	}

	// builder.append(prefix);
	workline = workline.substring(prefix.length());

	// .
	boolean hasDot = false;
	if (workline.startsWith(".")) {
	    hasDot = true;
	    isReal = true;
	    // builder.append(".");
	    workline = workline.substring(".".length());
	}

	// Number suffix
	prefix = getPrefixNumber(workline);
	StringSlice rightPart = prefix;

	workline = workline.substring(prefix.length());

	// e1
	// Long exponent = null;
	StringSlice exponent = null;
	boolean capitalE = false;
	if (workline.startsWith("e") || workline.startsWith("E")) {
	    if (workline.startsWith("E")) {
		capitalE = true;
	    }
	    // builder.append(workline.charAt(0));
	    workline = workline.substring("e".length());
	    // prefix = getPrefixNumber(workline);
	    prefix = new StringSlice(getMatlabExponentNumber(workline));

	    exponent = prefix;
	    workline = workline.substring(prefix.length());
	}
	// i/j
	// Get identifier
	boolean isComplex = false;
	StringSlice identifier = MatlabParsingUtils.getPrefixWord(workline);
	if (identifier == null) {
	    identifier = new StringSlice("");
	}

	String identifierString = identifier.toString();
	boolean complexj = false;
	if (identifierString.equals("i") || identifierString.equals("j")) {
	    isComplex = true;
	    if (identifierString.equals("j")) {
		complexj = true;
	    }

	    workline = workline.substring(identifier.length());
	}
	// Check if there is a number
	if (leftPart == null && rightPart == null) {
	    SpecsLogs.warn("No number?:" + line);
	    return null;
	}

	MatlabNumber number = new MatlabNumber(leftPart,
		StringSlice.toString(rightPart),
		StringSlice.toString(exponent),
		isComplex, hasDot, isReal);
	number.setCapitalE(capitalE);
	number.setComplexj(complexj);

	int numChars = line.length() - workline.length();

	return new MatlabNumberResult(numChars, number);
    }

    private static String getMatlabExponentNumber(StringSlice line) {
	StringBuilder builder = new StringBuilder();
	StringSlice prefix = null;

	// Signal
	if (line.startsWith("+") || line.startsWith("-")) {
	    builder.append(line.charAt(0));
	    line = line.substring(1);
	}

	// Number prefix
	prefix = getPrefixNumber(line);
	builder.append(prefix);

	return builder.toString();
    }

    /**
     * Gets the number prefix of the string. For instance, "12d4" returns "12".
     * 
     * @param string
     * @return the number prefixing the string. If no number is prefixing the string, returns an empty string.
     */
    private static StringSlice getPrefixNumber(StringSlice string) {
	for (int i = 0; i < string.length(); ++i) {
	    char aChar = string.charAt(i);
	    if (!Character.isDigit(aChar)) {
		return string.substring(0, i);
	    }
	}

	return string;
    }

    public String toMatlabString() {
	StringBuilder builder = new StringBuilder();

	// Number prefix
	if (leftHand != null) {
	    builder.append(leftHand.toString());
	}

	if (hasDot) {
	    builder.append(".");
	}

	if (rightHand != null) {
	    if (!rightHand.isEmpty()) {

		builder.append(rightHand.toString());
	    }
	}

	if (exponent != null) {
	    if (capitalE) {
		builder.append("E");
	    } else {
		builder.append("e");
	    }

	    builder.append(exponent.toString());
	}

	if (isComplex) {
	    if (complexj) {
		builder.append("j");
	    } else {
		builder.append("i");
	    }
	}

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return toMatlabString();
    }

    /**
     * @return the isComplex
     */
    public boolean isComplex() {
	return isComplex;
    }

    public boolean isInteger() {
	return !isReal;
	// return rightHand == null;
    }

    public boolean isFloat() {
	return rightHand != null;
    }

    public Long getIntegerValue() {
	if (!isInteger()) {
	    SpecsLogs.warn("Number '" + toString() + "' does not represent an integer.");
	    // return null;
	}

	// long result = leftHand;
	long result = Long.parseLong(leftHand);

	if (exponent != null) {
	    result = result * (long) Math.pow(10, Long.parseLong(exponent));
	}

	return result;
    }

    public double getFloatValue() {
	String doubleString = "";
	if (leftHand != null) {
	    doubleString += leftHand.toString();
	}

	if (rightHand != null) {
	    doubleString += "." + rightHand.toString();
	}

	double result = Double.parseDouble(doubleString);

	if (exponent != null) {
	    result = result * Math.pow(10, Long.parseLong(exponent));
	}

	return result;
    }
}
