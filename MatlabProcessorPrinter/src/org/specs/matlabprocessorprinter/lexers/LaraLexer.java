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

package org.specs.matlabprocessorprinter.lexers;

import java.util.HashMap;
import java.util.Map;

import jsyntaxpane.TokenType;
import jsyntaxpane.lexers.SimpleRegexLexer;

public class LaraLexer extends SimpleRegexLexer {

    private static final String KEYWORDS = "abstract|arguments|boolean|break" +
	    "|byte|case|catch|char|class|const|continue|debugger|default|delete|do|double"
	    + "|else|enum|eval|export|extends|false|final(ly)?|float|for|function|goto|if"
	    + "|implements|import|in(stanceof|t(erface)?|sert|itialize|put)?|let|long|native|new|null|package"
	    + "|private|protected|public|return|short|static|super|switch|synchronized|this"
	    + "|throw|throws|transient|true|try|typeof|var|void|volatile|while|with|yield"
	    + "|aspectdef|codedef|select|apply|to|condition|end|output|check|finalize|def"
	    + "|exec|run|cmd|call|before|after|around|replace|of|each";
    private static final Map<TokenType, String> PROPERTIES = new HashMap<>();

    static {
	LaraLexer.PROPERTIES.put(TokenType.COMMENT, "//.*");
	LaraLexer.PROPERTIES.put(TokenType.COMMENT2, "/\\*.*?\\*/");
	LaraLexer.PROPERTIES.put(TokenType.NUMBER, "[0-9]+(\\.[0-9]+)?");
	LaraLexer.PROPERTIES.put(TokenType.KEYWORD, LaraLexer.KEYWORDS);
	LaraLexer.PROPERTIES.put(TokenType.IDENTIFIER, "[a-zA-Z_][a-zA-Z0-9_]*");
	LaraLexer.PROPERTIES.put(TokenType.TYPE3, "\\$[a-zA-Z0-9_]+");
	LaraLexer.PROPERTIES.put(TokenType.STRING, "('.*?')");
	LaraLexer.PROPERTIES.put(TokenType.STRING2, "(\\\".*?\\\")");
    }

    public LaraLexer() {
	super(LaraLexer.PROPERTIES);
    }
}
