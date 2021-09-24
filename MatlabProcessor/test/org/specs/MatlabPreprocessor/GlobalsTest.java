/**
 * Copyright 2017 SPeCS.
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

package org.specs.MatlabPreprocessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class GlobalsTest {
    @Test
    public void testGlobals() {
        FileNode file = new MatlabParser().parse("global a b c;");
        List<StatementNode> stmts = file.getScript().getStatements();

        Assert.assertEquals(1, stmts.size());

        StatementNode stmt = stmts.get(0);
        Assert.assertTrue("Expected GlobalSt, got " + stmt.getClass(), stmt instanceof GlobalSt);

        GlobalSt global = (GlobalSt) stmt;
        Assert.assertEquals(Arrays.asList("a", "b", "c"), global.getIdentifiers());
    }

    @Test
    public void testPrintGlobals() {
        FileNode file = new MatlabParser().parse("global a b c;");
        List<StatementNode> stmts = file.getScript().getStatements();

        Assert.assertEquals(1, stmts.size());

        StatementNode stmt = stmts.get(0);
        Assert.assertTrue("Expected GlobalSt, got " + stmt.getClass(), stmt instanceof GlobalSt);

        GlobalSt global = (GlobalSt) stmt;
        Assert.assertEquals("global a b c;", global.getCode().trim());
    }

    @Test
    public void testGlobalsInOctaveMode() {
        FileNode file = new MatlabParser(LanguageMode.OCTAVE).parse("global a\n");
        List<StatementNode> stmts = file.getScript().getStatements();

        Assert.assertEquals(1, stmts.size());

        StatementNode stmt = stmts.get(0);
        Assert.assertTrue("Expected GlobalSt, got " + stmt.getClass(), stmt instanceof GlobalSt);

        GlobalSt global = (GlobalSt) stmt;
        Assert.assertEquals(Arrays.asList("a"), global.getIdentifiers());
    }

    @Test
    public void testInvalidSymbol() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream messageStream = new PrintStream(out);
        try {
            new MatlabParser(messageStream).parse("global 'a';");

            Assert.fail("Expected exception");
        } catch (RuntimeException e) {
            String result = new String(out.toByteArray(), Charset.forName("utf-8"));

            Assert.assertNotEquals("", result);
        }
    }

    @Test
    public void testEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream messageStream = new PrintStream(out);
        try {
            new MatlabParser(messageStream).parse("global;");

            Assert.fail("Expected exception");
        } catch (RuntimeException e) {
            String result = new String(out.toByteArray(), Charset.forName("utf-8"));

            Assert.assertNotEquals("", result);
        }
    }
}
