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

package org.specs.MatlabIR;

import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 *
 */
public class MatlabParsingUtils {

    /**
     * Extracts the identifier that is prefixing the given line (first char only letters, then letter, numbers and _).
     * 
     * <p>
     * If no prefix is found, returns null.
     * 
     * @param line
     * @return
     */
    public static StringSlice getPrefixWord(StringSlice line) {
	if (line.isEmpty() || !Character.isLetter(line.charAt(0))) {
	    return null;
	}

	int lastValidChar = 1;
	for (int i = 1; i < line.length(); ++i) {
	    if (isIdentifierChar(line.charAt(i))) {
		++lastValidChar;
	    } else {
		break;
	    }
	}

	return line.subSequence(0, lastValidChar);
    }

    /**
     * @param currentChar
     * @return
     */
    public static boolean isIdentifierChar(char currentChar) {
	// ["_","a"-"z","A"-"Z","0"-"9"]
	// if(Character.isAlphabetic(currentChar)) {
	if (Character.isLetter(currentChar)) {
	    return true;
	}

	if (Character.isDigit(currentChar)) {
	    return true;
	}

	if (currentChar == '_') {
	    return true;
	}

	// System.out.println("Not identifier:"+currentChar);
	return false;
    }

    /**
     * Checks if the given word is prefixing the line.
     * 
     * @param line
     * @param prefixWord
     * @return
     */
    public static boolean checkPrefixWord(String line, String prefixWord) {
	if (!line.startsWith(prefixWord)) {
	    return false;
	}

	try {
	    char aChar = line.charAt(prefixWord.length());
	    boolean isIdChar = MatlabParsingUtils.isIdentifierChar(aChar);
	    // If next char is a valid identifier char, it is another word
	    // prefixing the line.
	    if (isIdChar) {
		return false;
	    }

	    return true;
	} catch (IndexOutOfBoundsException e) {
	    // If there are no more chars, we are at the end of the line, the
	    // given word is prefixing the line.
	    return true;
	}
    }

    /**
     * Converts index to subscript, COLUMN MAJOR, and considering one-based numbering
     * 
     * @param dims
     * @param index
     * @return
     */
    public static List<Integer> indexToSubscriptMatlab(List<Integer> dims, int matlabIndex) {
	int index = matlabIndex - 1;
	List<Integer> zeroSubscripts = indexToSubscript(dims, index);

	List<Integer> oneSubscripits = SpecsFactory.newArrayList();
	for (Integer subs : zeroSubscripts) {
	    oneSubscripits.add(subs + 1);
	}

	return oneSubscripits;
    }

    /**
     * Converts index to subscript, COLUMN MAJOR, and considering zero-based numbering
     * 
     * @param dims
     * @param index
     * @return
     */
    private static List<Integer> indexToSubscript(List<Integer> dims, int index) {

	// List with subscripts
	List<Integer> sub = SpecsFactory.newArrayList();

	for (int i = 0; i < dims.size(); i++) {
	    int currentSub = index;

	    for (int j = 0; j < sub.size(); j++) {
		// Subtract index from last sub
		currentSub -= sub.get(j);
		currentSub /= dims.get(j);
	    }

	    if (i < dims.size() - 1) {
		currentSub %= dims.get(i);
	    }

	    sub.add(currentSub);
	}
	return sub;
    }
}
