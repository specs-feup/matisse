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

package org.specs.matisselib.tests;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DefaultDataProviderService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.services.log.NullLogService;
import org.specs.matisselib.services.scalarbuilderinfo.Z3ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.treenode.NodeInsertUtils;

public class TestUtils {
    private TestUtils() {
    }

    private static String getStringWithoutLines(MatlabNode token) {
        return token.toString().replaceAll("\\(line -?[0-9]+;?\\)", "(<removed>)");
    }

    public static void assertTreesEqual(MatlabNode expected, MatlabNode obtained, boolean ignoreExpressionNodes) {
        if (ignoreExpressionNodes) {
            expected = getTokenWithoutExpressionNodes(expected);
            obtained = getTokenWithoutExpressionNodes(obtained);
        }

        String expectedString = getStringWithoutLines(expected);
        String obtainedString = getStringWithoutLines(obtained);

        assertEquals(expectedString, obtainedString);
    }

    private static MatlabNode getTokenWithoutExpressionNodes(MatlabNode node) {
        // Copy the node so we won't change the original.
        // There's probably never going to be anyone using the node after
        // this method is called, so this is just an extra precaution "just in case".
        // node = MatlabTokenUtils.normalizeExpr(node).copy();
        node = node.normalizeExpr().copy();

        removeExpressionNodesRecursively(node);

        return node;
    }

    private static void removeExpressionNodesRecursively(MatlabNode node) {
        // This function is never called with an Expression node
        // (getTokenWithoutExpressionNodes uses MatlabTokenUtils.normalizeExpr)
        // So we don't have to deal with the case node.getType() == Expression

        // However, there *IS* an important case we need to handle:
        // for statements have an Expression child with more than one child.
        // Those expressions must not be removed

        for (int childIndex = 0; childIndex < node.getNumChildren(); ++childIndex) {
            MatlabNode child = node.getChildren().get(childIndex);
            // if (child.getType() == MType.Expression &&
            // !isForcedExpressionParentNode(node)) {
            if (child instanceof ExpressionNode &&
                    !isForcedExpressionParentNode(node)) {

                MatlabNode nonExpressionDescendant = child.normalizeExpr();
                removeExpressionNodesRecursively(nonExpressionDescendant);
                NodeInsertUtils.replace(child, nonExpressionDescendant);

            } else {
                removeExpressionNodesRecursively(child);
            }
        }
    }

    private static boolean isForcedExpressionParentNode(MatlabNode node) {
        // Certain nodes, such as for and parfor, have a child Expression node
        // that must not be removed.

        if (!(node instanceof StatementNode)) {
            return false;
        }

        // ForSt includes SimpleFor and ParFor
        return node instanceof ForSt;
    }

    public static String normalize(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (String line : str.replace("\r\n", "\n").split("\n")) {
            if (!line.matches("^[ ]+$")) {
                builder.append(line);
            }
            builder.append("\n");
        }

        return builder.toString().trim();
    }

    public static void testTypeTransparentPass(PostTypeInferencePass pass,
            FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        Map<String, VariableType> modifiedTypes = types == null ? new HashMap<>() : types;
        Map<String, InstanceProvider> modifiedFunctions = functions == null ? new HashMap<>() : functions;

        ProviderData providerData = ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings());
        TypedInstance instance = new TypedInstance(new FunctionIdentification("test.m"), Collections.emptyList(), body,
                () -> "test", providerData);
        for (String variableName : modifiedTypes.keySet()) {
            VariableType variableType = modifiedTypes.get(variableName);
            instance.addVariable(variableName, variableType);
        }
        DataStore passData = buildPassData(modifiedFunctions, instance);

        pass.apply(instance, passData);

        modifiedTypes.clear();
        modifiedTypes.putAll(instance.getVariableTypes());
    }

    public static DataStore buildPassData(Map<String, InstanceProvider> functions, TypedInstance instance) {

        CommonPassData passData = new CommonPassData("foo");
        SystemFunctionProviderService functionProvider = new TestFunctionProviderService(functions);
        passData.add(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER, functionProvider);
        passData.add(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER,
                new Z3ScalarValueInformationBuilderService());
        passData.add(ProjectPassServices.DATA_PROVIDER, new DefaultDataProviderService(instance, passData));
        passData.add(PreTypeInferenceServices.LOG, new NullLogService());

        return passData;
    }

    public static void testTypeNeutralPass(TypeNeutralSsaPass pass, FunctionBody body) {
        CommonPassData passData = new CommonPassData("Test " + pass.getName());
        passData.add(PreTypeInferenceServices.LOG, new NullLogService());

        pass.apply(body, passData);
    }

    public static String generateCCode(CInstructionList instructions) {
        StringBuilder code = new StringBuilder();

        instructions.getLocalVars().forEach((name, type) -> {
            code.append(type.code().getDeclaration(name));
            code.append(";\n");
        });

        code.append("\n");

        instructions.forEach(node -> {
            code.append(node.getCode());
        });

        return code.toString();
    }

    public static void assertStrictEquals(VariableType expected, VariableType obtained) {
        if (expected == null) {
            Assert.assertNull(obtained);
        } else {
            Assert.assertTrue("Expected " + expected + ", got " + obtained, expected.strictEquals(obtained));
        }
    }
}
