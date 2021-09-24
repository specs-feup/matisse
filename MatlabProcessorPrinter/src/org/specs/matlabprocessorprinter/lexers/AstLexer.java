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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jsyntaxpane.TokenType;
import jsyntaxpane.lexers.SimpleRegexLexer;

import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Functions.Probe;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

public class AstLexer extends SimpleRegexLexer {

    private static final List<String> KEYWORDS = new ArrayList<>();
    private static final List<String> BUILTINS = new ArrayList<>();
    private static final Map<TokenType, String> PROPERTIES = new HashMap<>();

    static {
	KEYWORDS.add("line");

	KEYWORDS.add("FunctionFile");
	KEYWORDS.add("ScriptFile");

	KEYWORDS.add("Function(Inputs|Declaration)?");
	KEYWORDS.add("Script");

	KEYWORDS.add("Statement");
	KEYWORDS.add("Assignment");
	KEYWORDS.add("Expression");
	KEYWORDS.add("Identifier");
	KEYWORDS.add("Operator");

	KEYWORDS.add("Comment");
	KEYWORDS.add("Outputs");

	KEYWORDS.add("MatlabNumber");
	KEYWORDS.add("AccessCall");
	KEYWORDS.add("ReservedWord");
	KEYWORDS.add("ColonNotation");

	loadBuiltins(MatlabBuiltin.values());
	loadBuiltins(MatlabOp.values());
	loadBuiltins(MatissePrimitive.values());
	loadBuiltins(MathFunction.values());
	loadBuiltins(Probe.values());

	BUILTINS.add("false");
	BUILTINS.add("i");
	BUILTINS.add("pi");
	BUILTINS.add("true");

	PROPERTIES.put(TokenType.COMMENT, "Comment: .*");
	PROPERTIES.put(TokenType.NUMBER, "[0-9]+(\\.[0-9]+)?");
	PROPERTIES.put(TokenType.KEYWORD, KEYWORDS.stream().collect(Collectors.joining("|")));
	PROPERTIES.put(TokenType.KEYWORD2,
		BUILTINS.stream().collect(Collectors.joining("|")));
	PROPERTIES.put(TokenType.IDENTIFIER, "[a-zA-Z][a-zA-Z0-9_]*");
	PROPERTIES.put(TokenType.STRING, "MatlabString: .*");
    }

    private static void loadBuiltins(MatlabFunctionProviderEnum[] functions) {
	for (MatlabFunctionProviderEnum builtin : functions) {
	    BUILTINS.add(builtin.getName());
	}
    }

    public AstLexer() {
	super(PROPERTIES);
    }
}
