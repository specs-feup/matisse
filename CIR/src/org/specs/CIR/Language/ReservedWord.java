/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.CIR.Language;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reserved words in C.
 * 
 * @author Joao Bispo
 *
 */
public enum ReservedWord {

    Auto,
    Break,
    Case,
    Char,
    Continue,
    Default,
    Do,
    Double,
    Else,
    Entry,
    Extern,
    Float,
    For,
    Goto,
    If,
    Int,
    Long,
    Register,
    Return,
    Short,
    Sizeof,
    Static,
    Struct,
    Switch,
    Typedef,
    Union,
    Unsigned,
    While;
    
    
    private final String literal;
    
    private static final Map<String, ReservedWord> stringMap;
    static {
		Map<String, ReservedWord> tempStringMap = new HashMap<>();
		
		for(ReservedWord word : ReservedWord.values()) {
		    tempStringMap.put(word.literal, word);
		}
		
		stringMap = Collections.unmodifiableMap(tempStringMap);
    }

    /**
     * The reserved word by itself represents a statement.
     * 
     */
    /*
    private static final Set<ReservedWord> statementWords;
    static {
	statementWords = EnumSet.noneOf(ReservedWord.class);
	
	statementWords.add(Else);
    }
    */
    
    /**
     * The reserved word by itself represents a statement (e.g., else).
     * 
     * @return
     */
    /*
    public boolean isStatementWord() {
	return statementWords.contains(this);
    }
    */
    
    /**
     * @return the literal
     */
    public String getLiteral() {
	return literal;
    }
    
    /**
     * @return the stringmap
     */
    public static Map<String, ReservedWord> getStringmap() {
	return stringMap;
    }
    
    public static ReservedWord getReservedWord(String name) {
    	ReservedWord word = stringMap.get(name);
    	return word;
    }
    
    /**
     * 
     */
    private ReservedWord() {
	literal = name().toLowerCase();
    }
    
    /**
     * 
     */
    private ReservedWord(String literal) {
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
