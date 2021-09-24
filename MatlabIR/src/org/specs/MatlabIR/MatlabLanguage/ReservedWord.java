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
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Reserved words in MatLab, as given by the command 'iskeyword'.
 * 
 * @author Joao Bispo
 *
 */
public enum ReservedWord {

    Break,
    Case,
    Catch,
    Classdef,
    Continue,
    Else,
    Elseif,
    End,
    For,
    Function,
    Global,
    If,
    Otherwise,
    Parfor,
    Persistent,
    Return,
    Spmd,
    Switch,
    Try,
    While,

    EndIf(LanguageMode.OCTAVE),
    EndWhile(LanguageMode.OCTAVE),
    EndFor(LanguageMode.OCTAVE),
    EndFunction(LanguageMode.OCTAVE);

    private final String literal;
    private final LanguageMode languageMode;

    private static final Map<String, ReservedWord> stringMap;

    static {
        Map<String, ReservedWord> tempStringMap = SpecsFactory.newHashMap();

        for (ReservedWord word : ReservedWord.values()) {
            tempStringMap.put(word.literal, word);
        }

        stringMap = Collections.unmodifiableMap(tempStringMap);
    }

    /**
     * The reserved word by itself represents a statement.
     * 
     */
    private static final Set<ReservedWord> statementWords;

    static {
        statementWords = EnumSet.noneOf(ReservedWord.class);

        ReservedWord.statementWords.add(Else);
        ReservedWord.statementWords.add(Try);
        ReservedWord.statementWords.add(Otherwise);
    }

    /**
     * The reserved word by itself represents a statement (e.g., else).
     * 
     * <p>
     * This method is needed for cases such as "else if", where 'else' represents a whole statement, and 'if' is part of
     * another statement.
     * 
     * @return
     */
    public boolean isStatementWord() {
        return ReservedWord.statementWords.contains(this);
    }

    /**
     * @return the literal
     */
    public String getLiteral() {
        return literal;
    }

    public static ReservedWord getReservedWord(String name, LanguageMode languageMode) {
        if (name.equals("endfunction") && languageMode == LanguageMode.MATLAB) {
            throw new RuntimeException();
        }

        ReservedWord word = ReservedWord.stringMap.get(name);
        if (word == null) {
            return null;
        }
        if (word.languageMode == LanguageMode.OCTAVE && languageMode != LanguageMode.OCTAVE) {
            return null;
        }
        return word;
    }

    private ReservedWord() {
        this(LanguageMode.MATLAB);
    }

    /**
     * 
     */
    private ReservedWord(LanguageMode languageMode) {
        this.languageMode = languageMode;
        literal = name().toLowerCase();
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getLiteral();
    }

    private static final EnumSet<ReservedWord> HAS_CONDITIONAL = EnumSet.of(If, Elseif, While);
    private static final EnumSet<ReservedWord> HAS_EXPRESSION = EnumSet.of(If, Elseif, While, For, Parfor);

    /**
     * Returns true if the reserved word corresponds to a statement which has a conditional expression.
     */
    public boolean hasConditional() {
        return ReservedWord.HAS_CONDITIONAL.contains(this);
    }

    /**
     * Returns true if the reserved word corresponds to a statement which has an expression. It is a super-set of
     * hasConditional, which includes for and parfor.
     */
    public boolean hasExpression() {
        return ReservedWord.HAS_EXPRESSION.contains(this);
    }

}
