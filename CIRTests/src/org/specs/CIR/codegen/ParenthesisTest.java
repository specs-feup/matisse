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

package org.specs.CIR.codegen;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ParenthesisTest {

    @Test
    public void testNumber() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        Assert.assertEquals("1", numberNode.getCode());
    }

    @Test
    public void testOperation() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        VariableType intType = numberNode.getVariableType();
        VariableNode variableNode = CNodeFactory.newVariable("foo", intType);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType), DataStore.newInstance("test"));
        FunctionInstance instance = COperator.Addition.newCInstance(data);

        String code = instance.getCallCode(Arrays.asList(numberNode, variableNode));
        Assert.assertEquals("1 + foo", code);
    }

    @Test
    public void testProperPrecedence() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        VariableType intType = numberNode.getVariableType();
        VariableNode variableNode = CNodeFactory.newVariable("foo", intType);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType), DataStore.newInstance("test"));
        FunctionInstance addInstance = COperator.Addition.newCInstance(data);
        FunctionInstance prodInstance = COperator.Multiplication.newCInstance(data);
        CNode rightNode = CNodeFactory.newFunctionCall(prodInstance, variableNode, variableNode);
        CNode functionNode = CNodeFactory.newFunctionCall(addInstance, numberNode, rightNode);

        String code = functionNode.getCode();
        Assert.assertEquals("1 + foo * foo", code);
    }

    @Test
    public void testParenthesisAssociativity() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        VariableType intType = numberNode.getVariableType();
        VariableNode variableNode = CNodeFactory.newVariable("foo", intType);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType), DataStore.newInstance("test"));
        FunctionInstance divInstance = COperator.Division.newCInstance(data);
        FunctionInstance prodInstance = COperator.Multiplication.newCInstance(data);
        CNode rightNode = CNodeFactory.newFunctionCall(prodInstance, variableNode, variableNode);
        CNode functionNode = CNodeFactory.newFunctionCall(divInstance, numberNode, rightNode);

        String code = functionNode.getCode();
        Assert.assertEquals("1 / (foo * foo)", code);
    }

    @Test
    public void testProperAssociativity() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        VariableType intType = numberNode.getVariableType();
        VariableNode variableNode = CNodeFactory.newVariable("foo", intType);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType), DataStore.newInstance("test"));
        FunctionInstance divInstance = COperator.Division.newCInstance(data);
        FunctionInstance prodInstance = COperator.Multiplication.newCInstance(data);
        CNode leftNode = CNodeFactory.newFunctionCall(divInstance, numberNode, variableNode);
        CNode functionNode = CNodeFactory.newFunctionCall(prodInstance, leftNode, variableNode);

        String code = functionNode.getCode();
        Assert.assertEquals("1 / foo * foo", code);
    }

    @Test
    public void testRequiredParenthesis() {
        CNumberNode numberNode = CNodeFactory.newCNumber(1);
        VariableType intType = numberNode.getVariableType();
        VariableNode variableNode = CNodeFactory.newVariable("foo", intType);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType), DataStore.newInstance("test"));
        FunctionInstance addInstance = COperator.Addition.newCInstance(data);
        FunctionInstance prodInstance = COperator.Multiplication.newCInstance(data);
        CNode rightNode = CNodeFactory.newFunctionCall(addInstance, variableNode, variableNode);
        CNode functionNode = CNodeFactory.newFunctionCall(prodInstance, numberNode, rightNode);

        String code = functionNode.getCode();
        Assert.assertEquals("1 * (foo + foo)", code);
    }
}
