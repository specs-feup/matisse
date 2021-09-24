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

package org.specs.MatlabToC.CodeBuilder.MatlabToC;

// import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment.AssignUtils;

public class AssignmentTest {

    @Test
    public void test() {

        AssignUtils utils = new AssignUtils(MatlabToCFunctionData.newInstance(LanguageMode.MATLAB));

        CNode pointerInt = CNodeFactory.newVariable("pint",
                utils.getData().getNumerics().newInt().pointer().getType(true));
        CNode cleanInt = CNodeFactory.newVariable("anint", utils.getData().getNumerics().newInt());

        assertEquals("*pint = anint", utils.buildAssignment(pointerInt, cleanInt).getCode());
        assertEquals("anint = anint", utils.buildAssignment(cleanInt, cleanInt).getCode());
        assertEquals("anint = *pint", utils.buildAssignment(cleanInt, pointerInt).getCode());
        assertEquals("*pint = *pint", utils.buildAssignment(pointerInt, pointerInt).getCode());

    }
}
