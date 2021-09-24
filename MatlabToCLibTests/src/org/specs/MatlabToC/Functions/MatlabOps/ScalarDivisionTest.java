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

package org.specs.MatlabToC.Functions.MatlabOps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCBuilder;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.Utils.TestUtils;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ScalarDivisionTest {

    @Test
    public void testDefaultReal() {

        // 'c' should be of type 'default real'
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = 1 / 6;");

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB);
        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        VariableNode cVar = cInsts.get().get(0).getFirstDescendantsAndSelf(VariableNode.class).get();
        // CNode cVar = TreeNodeIndexUtils.getFirstToken(cInsts.get().get(0), CNodeType.Variable);

        assertEquals(data.getSettings().get(CirKeys.DEFAULT_REAL), cVar.getVariableType());
    }

    @Test
    public void testFirstLevelDivision() {

        // When setting 'c' to int, should use integer division, since the function is at level 1 (last function to be
        // executed)
        DataStore setup = MatlabToCOptionUtils.newDefaultSettings();

        VariableType integer = new NumericFactory(setup).newInt();

        TypesMap map = new TypesMap();
        map.addSymbol("c", integer);

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB, setup, map);
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = 1 / 6;");

        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        // Get first division
        CNode division = TestUtils.getFirstFunction(cInsts.get().get(0), ScalarDivisionInstance.NAME_PREFIX);

        FunctionInstance instance = ((FunctionCallNode) division).getFunctionInstance();

        assertEquals(integer, instance.getFunctionType().getCReturnType());
    }

    @Test
    public void testOthertLevelDivision() {

        // When setting 'c' to int, should use real division, since the function is at level different than one (is not
        // last function to be executed), to preserve intermediate results
        DataStore setup = MatlabToCOptionUtils.newDefaultSettings();

        VariableType integer = new NumericFactory(setup).newInt();

        TypesMap map = new TypesMap();
        map.addSymbol("c", integer);

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB, setup, map);
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = 2 * (1 / 6);");

        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        // Get first division
        CNode division = TestUtils.getFirstFunction(cInsts.get().get(0), ScalarDivisionInstance.NAME_PREFIX);

        FunctionInstance instance = ((FunctionCallNode) division).getFunctionInstance();

        assertEquals(data.getSettings().get(CirKeys.DEFAULT_REAL), instance.getFunctionType().getCReturnType());
    }

    @Test
    public void testFirstLevelDivision2() {

        // When setting 'c' to int, should use integer division
        DataStore setup = MatlabToCOptionUtils.newDefaultSettings();

        VariableType integer = new NumericFactory(setup).newInt();

        TypesMap map = new TypesMap();
        map.addSymbol("c", integer);

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB, setup, map);
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = 14 / (1 * 6);");

        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        // Get first division
        CNode division = TestUtils.getFirstFunction(cInsts.get().get(0), ScalarDivisionInstance.NAME_PREFIX);

        FunctionInstance instance = ((FunctionCallNode) division).getFunctionInstance();

        assertEquals(integer, instance.getFunctionType().getCReturnType());
    }

    @Test
    public void testOthertLevelDivision2() {

        // When setting 'c' to int, should use real division, since the function is at level different than one (is not
        // last function to be executed), to preserve intermediate results
        DataStore setup = MatlabToCOptionUtils.newDefaultSettings();

        VariableType integer = new NumericFactory(setup).newInt();

        TypesMap map = new TypesMap();
        map.addSymbol("c", integer);

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB, setup, map);
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = floor(14 / (1 * 6));");

        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        // Get first division
        CNode division = TestUtils.getFirstFunction(cInsts.get().get(0), ScalarDivisionInstance.NAME_PREFIX);

        FunctionInstance instance = ((FunctionCallNode) division).getFunctionInstance();

        assertEquals(data.getSettings().get(CirKeys.DEFAULT_REAL), instance.getFunctionType().getCReturnType());
    }

    @Test
    public void testWeakDivision() {

        // When setting 'c' to weak int, should use real division, even if the function is at level 1 (last function to
        // be executed)
        DataStore setup = MatlabToCOptionUtils.newDefaultSettings();

        VariableType weakInteger = new NumericFactory(setup).newInt().setWeakType(true);
        // VariableType weakInteger = setup.getNumerics().newInt();

        TypesMap map = new TypesMap();
        map.addSymbol("c", weakInteger);

        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(LanguageMode.MATLAB, setup, map);
        MatlabUnitNode token = MatlabProcessorUtils.newToken("c = 1 / 6;");

        CInstructionList cInsts = MatlabToCBuilder.build(token, data);

        // Get first division
        CNode division = TestUtils.getFirstFunction(cInsts.get().get(0), ScalarDivisionInstance.NAME_PREFIX);

        FunctionInstance instance = ((FunctionCallNode) division).getFunctionInstance();
        // System.out.println("CODE:" + division.getCode());

        assertEquals(data.getSettings().get(CirKeys.DEFAULT_REAL), instance.getFunctionType().getCReturnType());

    }
}
