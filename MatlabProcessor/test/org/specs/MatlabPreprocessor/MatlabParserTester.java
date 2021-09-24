/**
 * Copyright 2016 SPeCS.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.lazy.Lazy;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class MatlabParserTester {

    private final ResourceProvider resource;
    private boolean showAst = false;
    private boolean showCode = false;
    private boolean onePass = false;
    private LanguageMode languageMode = LanguageMode.MATLAB;

    public MatlabParserTester(ResourceProvider resource) {
        this.resource = resource;
    }

    public void setLanguage(LanguageMode languageMode) {
        this.languageMode = languageMode;
    }

    public MatlabParserTester showAst() {
        showAst = true;
        return this;
    }

    public MatlabParserTester showCode() {
        showCode = true;
        return this;
    }

    public MatlabParserTester onePass() {
        onePass = true;
        return this;
    }

    public void test() {
        try {
            setUp();
            testProper();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Before
    public void setUp() throws Exception {
        SpecsSystem.programStandardInit();
    }

    // @After
    public static void clear() throws Exception {

    }

    @Test
    public void testProper() {

        MatlabParser parser = new MatlabParser(languageMode);

        // Parse files
        FileNode fileNode = parser.parse(resource);

        Lazy<String> code = Lazy.newInstance(() -> fileNode.getCode());

        if (showAst) {
            SpecsLogs.msgInfo("MATLAB AST:\n" + fileNode);
        }

        if (showCode) {
            SpecsLogs.msgInfo("MATLAB Code:\n" + code.get());
        }

        if (onePass) {
            return;
        }

        // Second pass
        FileNode fileNodeV2 = parser.parse(code.get());
        String codeV2 = fileNodeV2.getCode();
        // Compare with .txt, if available

        // Get .txt resource
        String txtResource = resource.getResource() + ".txt";

        if (!SpecsIo.hasResource(txtResource)) {
            SpecsLogs.msgInfo("MatlabParserTester: no .txt check file for resource '" + resource.getResource() + "'");
            SpecsLogs.msgInfo("Contents of output:\n" + codeV2);
            return;
        }

        String txtContents = SpecsStrings.normalizeFileContents(SpecsIo.getResource(txtResource), true);
        String generatedFileContents = SpecsStrings.normalizeFileContents(codeV2, true);

        Assert.assertEquals(txtContents, generatedFileContents);

    }

}
