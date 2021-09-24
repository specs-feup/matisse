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

package org.specs.MatlabProcessor.octave;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class OctaveTest extends TestCase {

    @Override
    public void setUp() {
        SpecsSystem.programStandardInit();
        SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testComment() {
        String code = new MatlabParser(LanguageMode.OCTAVE).parse(SpecsIo.getResource(OctaveTestResource.COMMENT))
                .getCode();

        Assert.assertEquals(TestUtils.clean(SpecsIo.getResource(OctaveTestResource.COMMENT.getResultResource())),
                TestUtils.clean(code));
    }

}
