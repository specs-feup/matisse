/**
 * Copyright 2014 SPeCS.
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

package org.specs.matisselib.tests.ast.returnremover;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum ReturnRemoverTestFile implements ResourceProvider {
    BASIC_SEQUENCE("basic_sequence.m"),
    BASIC_SEQUENCE_NO_RETURN("basic_sequence_no_return.m"),
    IF_RETURN("if_return.m"),
    IF_RETURN_NO_RETURN("if_return_no_return.m"),
    FOR_RETURN("for_return.m"),
    FOR_RETURN_NO_RETURN("for_return_no_return.m"),
    IF_ELSE_RETURN("if_else_return.m"),
    IF_ELSE_RETURN_NO_RETURN("if_else_return_no_return.m"),
    SWITCH_RETURN("switch_return.m"),
    SWITCH_RETURN_NO_RETURN("switch_return_no_return.m"),
    ROOT_COMMENT("root_comment.m"),
    ROOT_COMMENT_NO_RETURN("root_comment_no_return.m"),
    IF_IN_FOR_RETURN("if_in_for_return.m"),
    IF_IN_FOR_NO_RETURN("if_in_for_no_return.m");

    private static final String folder = "ast/returnremover";

    private final String resource;

    private ReturnRemoverTestFile(String resource) {
	this.resource = ReturnRemoverTestFile.folder + "/" + resource;
    }

    @Override
    public String getResource() {
	return this.resource;
    }
}
