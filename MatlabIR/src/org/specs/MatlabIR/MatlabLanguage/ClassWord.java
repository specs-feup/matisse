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

import java.util.Collections;
import java.util.Map;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Words in MatLab used for class syntax.
 * 
 * @author Joao Bispo
 *
 */
public enum ClassWord {

    PROPERTIES,
    EVENTS,
    METHODS,
    ENUMERATION;

    private final String literal;

    private static final Map<String, ClassWord> stringMap;
    static {
	Map<String, ClassWord> tempStringMap = SpecsFactory.newHashMap();

	for (ClassWord word : ClassWord.values()) {
	    tempStringMap.put(word.literal, word);
	}

	stringMap = Collections.unmodifiableMap(tempStringMap);
    }

    /**
     * @return the literal
     */
    public String getLiteral() {
	return literal;
    }

    /**
     * @return the stringmap
     */
    public static Map<String, ClassWord> getStringmap() {
	return stringMap;
    }

    public static ClassWord getClassWord(String name) {
	ClassWord word = stringMap.get(name);
	return word;
    }

    public static boolean isClassWord(String name) {
	return stringMap.get(name) != null;
    }

    /**
     * 
     */
    private ClassWord() {
	literal = name().toLowerCase();
    }

    /**
     * 
     */
    private ClassWord(String literal) {
	this.literal = literal;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
	return getLiteral();
    }

}
