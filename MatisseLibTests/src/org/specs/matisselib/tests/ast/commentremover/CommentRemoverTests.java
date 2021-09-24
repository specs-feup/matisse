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

package org.specs.matisselib.tests.ast.commentremover;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.passes.ast.CommentRemoverPass;
import org.specs.matisselib.tests.MockPassData;
import org.specs.matisselib.tests.TestUtils;

public class CommentRemoverTests extends TestCase {
    private static FileNode getProcessedTokenFromResourceFile(CommentRemoverResource file) {
	return new MatlabParser().parse(file);
    }

    private static void testFile(CommentRemoverResource result, CommentRemoverResource source) {
	FileNode token = getProcessedTokenFromResourceFile(source);
	FileNode expectedToken = getProcessedTokenFromResourceFile(result);

	new CommentRemoverPass().apply(token, new MockPassData());

	TestUtils.assertTreesEqual(expectedToken.getScript(), token.getScript(), false);
    }

    @Override
    public void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testSimple() {
	SpecsProperty.ShowStackTrace.applyProperty("true");
	testFile(CommentRemoverResource.SIMPLE_NO_COMMENTS, CommentRemoverResource.SIMPLE);
    }
}
