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

public class BytecodeLexer extends SimpleRegexLexer {

    private static final List<String> KEYWORDS = new ArrayList<>();
    private static final Map<TokenType, String> PROPERTIES = new HashMap<>();

    static {
        BytecodeLexer.KEYWORDS.add("[Ff]unction [a-zA-Z0-9_]+");
        BytecodeLexer.KEYWORDS.add("block");

        BytecodeLexer.KEYWORDS.add("arg");
        BytecodeLexer.KEYWORDS.add("assume");
        BytecodeLexer.KEYWORDS.add("branch");
        BytecodeLexer.KEYWORDS.add("break");
        BytecodeLexer.KEYWORDS.add("builtin");
        BytecodeLexer.KEYWORDS.add("call");
        BytecodeLexer.KEYWORDS.add("cell_get");
        BytecodeLexer.KEYWORDS.add("cell_make_row");
        BytecodeLexer.KEYWORDS.add("cell_set");
        BytecodeLexer.KEYWORDS.add("combine_size");
        BytecodeLexer.KEYWORDS.add("continue");
        BytecodeLexer.KEYWORDS.add("end");
        BytecodeLexer.KEYWORDS.add("for");
        BytecodeLexer.KEYWORDS.add("get(_or_first)?");
        BytecodeLexer.KEYWORDS.add("iter");
        BytecodeLexer.KEYWORDS.add("line");
        BytecodeLexer.KEYWORDS.add("matrix_set");
        BytecodeLexer.KEYWORDS.add("multi_set");
        BytecodeLexer.KEYWORDS.add("parallel_copy");
        BytecodeLexer.KEYWORDS.add("phi");
        BytecodeLexer.KEYWORDS.add("read_global");
        BytecodeLexer.KEYWORDS.add("simple_get");
        BytecodeLexer.KEYWORDS.add("simple_set");
        BytecodeLexer.KEYWORDS.add("str");
        BytecodeLexer.KEYWORDS.add("untyped_call");
        BytecodeLexer.KEYWORDS.add("validate_at_least_one_empty_matrix");
        BytecodeLexer.KEYWORDS.add("validate_equal");
        BytecodeLexer.KEYWORDS.add("validate_true");
        BytecodeLexer.KEYWORDS.add("vertical_flatten");
        BytecodeLexer.KEYWORDS.add("write_global");

        BytecodeLexer.PROPERTIES.put(TokenType.KEYWORD,
                BytecodeLexer.KEYWORDS.stream().collect(Collectors.joining("|")));
        BytecodeLexer.PROPERTIES.put(TokenType.COMMENT, "%.*");
        BytecodeLexer.PROPERTIES.put(TokenType.NUMBER, "[0-9]+(\\.[0-9]+)?");
        BytecodeLexer.PROPERTIES.put(TokenType.IDENTIFIER, "[a-zA-Z$][a-zA-Z0-9_$+]*");
        BytecodeLexer.PROPERTIES.put(TokenType.STRING, "\".*?\"");
        BytecodeLexer.PROPERTIES.put(TokenType.STRING2, "#[0-9]+");
        BytecodeLexer.PROPERTIES.put(TokenType.WARNING, "!undefined");
    }

    public BytecodeLexer() {
        super(BytecodeLexer.PROPERTIES);
    }
}
