/**
 * Copyright 2018 SPeCS.
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

package org.specs.MatlabPreprocessor.tests;

import org.junit.Test;
import org.specs.MatlabPreprocessor.MatlabParserTester;

import pt.up.fe.specs.util.providers.ResourceProvider;

public class MatlabSyntaxTest {

    private static final String BASE_PACKAGE = "pt/up/fe/specs/matlab/parser/test/syntax/";

    // @BeforeClass
    // public static void setup() throws Exception {
    // MatlabParserTester.clear();
    // }
    //
    // @After
    // public void tearDown() throws Exception {
    // MatlabParserTester.clear();
    // }

    private static MatlabParserTester getTester(String testFile) {
        ResourceProvider provider = () -> BASE_PACKAGE + testFile;
        return new MatlabParserTester(provider);
    }

    @Test
    public void testSwitch() {
        // getTester("case_test.m").showAst().showCode().test();
        getTester("case_test.m").test();
    }

}
