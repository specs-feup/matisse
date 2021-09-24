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

package org.specs.MatlabAspects;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.specs.CIR.Types.VariableType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.collections.ScopedMap;
import pt.up.fe.specs.util.utilities.StringLines;

public class ShapeTest {

    @Test
    public void test() {
        SpecsSystem.programStandardInit();
        MatlabAspectsUtils typesParser = new MatlabAspectsUtils(DataStore.newInstance("shape_test"));

        ScopedMap<VariableType> types = typesParser
                .getVariableTypes(SpecsIo.getResource(TypesParserTestsResource.MATRIX_SHAPES));

        List<String> result = types.getSymbols().stream()
                .map(type -> type.toString())
                // .collect(Collectors.joining("\n"));
                .collect(Collectors.toList());

        List<String> expectedLines = StringLines
                .getLines(SpecsIo.getResource(TypesParserTestsResource.MATRIX_SHAPES_RESULT));
        assertEquals(expectedLines, result);
        // fail("Not yet implemented");
    }

}
